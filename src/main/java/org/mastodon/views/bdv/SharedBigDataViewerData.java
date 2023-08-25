/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.bdv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mastodon.util.DummySpimData;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.BigDataViewer;
import bdv.TransformEventHandler2D;
import bdv.TransformEventHandler3D;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.img.imagestack.ImageStackImageLoader;
import bdv.img.virtualstack.VirtualStackImageLoader;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.WrapBasicImgLoader;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.InitializeViewerState;
import bdv.tools.bookmarks.Bookmarks;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.ManualTransformation;
import bdv.util.Bounds;
import bdv.viewer.BasicViewerState;
import bdv.viewer.ConverterSetups;
import bdv.viewer.DisplayMode;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import bdv.viewer.ViewerState;
import ch.systemsx.cisd.hdf5.exceptions.HDF5FileNotFoundException;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.process.LUT;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.SpimDataIOException;
import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class SharedBigDataViewerData
{
	private final ArrayList< SourceAndConverter< ? > > sources;

	private final ConverterSetups setups;

	private final ManualTransformation manualTransformation;

	private final Bookmarks bookmarks;

	private final ViewerOptions options;

	private final InputTriggerConfig inputTriggerConfig;

	private final AbstractSpimData< ? > spimData;

	private final CacheControl cache;

	private final boolean is2D;

	private File proposedSettingsFile;

	private SharedBigDataViewerData(
			final AbstractSpimData< ? > spimData,
			final ArrayList< SourceAndConverter< ? > > sources,
			final ConverterSetups setups,
			final CacheControl cache )
	{
		this.spimData = spimData;
		this.sources = sources;
		this.setups = setups;
		this.cache = cache;

		final ViewerOptions lvo = new ViewerOptions();
		this.inputTriggerConfig = ( lvo.values.getInputTriggerConfig() != null )
				? lvo.values.getInputTriggerConfig()
				: new InputTriggerConfig();

		this.manualTransformation = new ManualTransformation( sources );

		this.bookmarks = new Bookmarks();

		this.is2D = computeIs2D();
		this.options = lvo
				.inputTriggerConfig( inputTriggerConfig )
				.transformEventHandlerFactory( is2D
						? TransformEventHandler2D::new
						: TransformEventHandler3D::new );

		if ( WrapBasicImgLoader.wrapImgLoaderIfNecessary( spimData ) )
			System.err.println(
					"WARNING:\nOpening <SpimData> dataset that is not suited for interactive browsing.\nConsider resaving as HDF5 for better performance." );

		WrapBasicImgLoader.removeWrapperIfPresent( spimData );
	}

	public boolean tryLoadSettings( final String xmlFilename )
	{
		proposedSettingsFile = null;
		if ( xmlFilename.startsWith( "http://" ) )
		{
			// load settings.xml from the BigDataServer
			final String settings = xmlFilename + "settings";
			{
				try
				{
					loadSettings( settings, null );
					return true;
				}
				catch ( final FileNotFoundException e )
				{}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		else if ( xmlFilename.endsWith( ".xml" ) )
		{
			final String settings =
					xmlFilename.substring( 0, xmlFilename.length() - ".xml".length() ) + ".settings" + ".xml";
			proposedSettingsFile = new File( settings );
			if ( proposedSettingsFile.isFile() )
			{
				try
				{
					loadSettings( settings, null );
					return true;
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public void loadSettings( final String xmlFilename, final ViewerPanel viewer ) throws IOException, JDOMException
	{
		final SAXBuilder sax = new SAXBuilder();
		final Document doc = sax.build( xmlFilename );
		final Element root = doc.getRootElement();
		if ( viewer != null )
			viewer.stateFromXml( root );
		restoreFromXmlSetupAssignments( root );
		manualTransformation.restoreFromXml( root );
		bookmarks.restoreFromXml( root );
	}

	public void saveSettings( final String xmlFilename, final ViewerPanel viewer ) throws IOException
	{
		final Element root = new Element( "Settings" );
		if ( viewer != null )
			root.addContent( viewer.stateToXml() );
		root.addContent( toXmlSetupAssignments() );
		root.addContent( manualTransformation.toXml() );
		root.addContent( bookmarks.toXml() );
		final Document doc = new Document( root );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		xout.output( doc, new FileWriter( xmlFilename ) );
	}

	/**
	 * Creates an {@link Element} containing suitable information for the
	 * <code>SetupAssignments</code> part of the serialization of the BDV
	 * settings.
	 * 
	 * @return a new {@link Element}.
	 */
	public Element toXmlSetupAssignments()
	{
		final Element elem = new Element( "SetupAssignments" );

		final Element elemConverterSetups = new Element( "ConverterSetups" );
		for ( int i = 0; i < sources.size(); ++i )
		{
			final SourceAndConverter< ? > source = sources.get( i );
			final ConverterSetup setup = setups.getConverterSetup( source );
			final Element elemConverterSetup = new Element( "ConverterSetup" );
			elemConverterSetup.addContent( XmlHelpers.intElement( "id", setup.getSetupId() ) );
			elemConverterSetup.addContent( XmlHelpers.doubleElement( "min", setup.getDisplayRangeMin() ) );
			elemConverterSetup.addContent( XmlHelpers.doubleElement( "max", setup.getDisplayRangeMax() ) );
			elemConverterSetup.addContent( XmlHelpers.intElement( "color", setup.getColor().get() ) );
			elemConverterSetup.addContent( XmlHelpers.intElement( "groupId", i ) );
			elemConverterSetups.addContent( elemConverterSetup );
		}
		elem.addContent( elemConverterSetups );

		// Can't save MinMaxGroups without calling to deprecated methods.
		// Put a dummy list instead copying single source for now.
		final Element elemMinMaxGroups = new Element( "MinMaxGroups" );
		for ( int i = 0; i < sources.size(); ++i )
		{
			final SourceAndConverter< ? > source = sources.get( i );
			final ConverterSetup setup = setups.getConverterSetup( source );
			final Bounds bounds = setups.getBounds().getBounds( setup );
			final Element elemMinMaxGroup = new Element( "MinMaxGroup" );
			elemMinMaxGroup.addContent( XmlHelpers.intElement( "id", i ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "fullRangeMin", bounds.getMinBound() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "fullRangeMax", bounds.getMaxBound() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "rangeMin", bounds.getMinBound() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "rangeMax", bounds.getMaxBound() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "currentMin", setup.getDisplayRangeMin() ) );
			elemMinMaxGroup.addContent( XmlHelpers.doubleElement( "currentMax", setup.getDisplayRangeMax() ) );
			elemMinMaxGroups.addContent( elemMinMaxGroup );
		}
		elem.addContent( elemMinMaxGroups );

		return elem;
	}

	private void restoreFromXmlSetupAssignments( final Element parent )
	{
		final Element elemSetupAssignments = parent.getChild( "SetupAssignments" );
		if ( elemSetupAssignments == null )
			return;
		final Element elemConverterSetups = elemSetupAssignments.getChild( "ConverterSetups" );
		final List< Element > converterSetupNodes = elemConverterSetups.getChildren( "ConverterSetup" );
		if ( converterSetupNodes.size() != sources.size() )
			throw new IllegalArgumentException();

		final Element elemMinMaxGroups = elemSetupAssignments.getChild( "MinMaxGroups" );
		final List< Element > minMaxGroupNodes = elemMinMaxGroups.getChildren( "MinMaxGroup" );
		for ( final Element elem : minMaxGroupNodes )
		{
			final int id = Integer.parseInt( elem.getChildText( "id" ) );
//			final double fullRangeMin = Double.parseDouble( elem.getChildText( "fullRangeMin" ) );
//			final double fullRangeMax = Double.parseDouble( elem.getChildText( "fullRangeMax" ) );
			final double rangeMin = Double.parseDouble( elem.getChildText( "rangeMin" ) );
			final double rangeMax = Double.parseDouble( elem.getChildText( "rangeMax" ) );
//			final double currentMin = Double.parseDouble( elem.getChildText( "currentMin" ) );
//			final double currentMax = Double.parseDouble( elem.getChildText( "currentMax" ) );

			final SourceAndConverter< ? > source = sources.get( id );
			final ConverterSetup setup = setups.getConverterSetup( source );
			setups.getBounds().setBounds( setup, new Bounds( rangeMin, rangeMax ) );
		}

		for ( final Element elem : converterSetupNodes )
		{
			final int id = Integer.parseInt( elem.getChildText( "id" ) );
			final double min = Double.parseDouble( elem.getChildText( "min" ) );
			final double max = Double.parseDouble( elem.getChildText( "max" ) );
			final int color = Integer.parseInt( elem.getChildText( "color" ) );
//			final int groupId = Integer.parseInt( elem.getChildText( "groupId" ) );
			final ConverterSetup setup = getSetupById( id );
			setup.setDisplayRange( min, max );
			setup.setColor( new ARGBType( color ) );
		}
	}

	private ConverterSetup getSetupById( final int id )
	{
		for ( final SourceAndConverter< ? > source : sources )
		{
			final ConverterSetup setup = setups.getConverterSetup( source );
			if ( setup.getSetupId() == id )
				return setup;
		}
		return null;
	}

	public AbstractSpimData< ? > getSpimData()
	{
		return spimData;
	}

	public ViewerOptions getOptions()
	{
		return options;
	}

	public InputTriggerConfig getInputTriggerConfig()
	{
		return inputTriggerConfig;
	}

	public ArrayList< SourceAndConverter< ? > > getSources()
	{
		return sources;
	}

	public ConverterSetups getConverterSetups()
	{
		return setups;
	}

	public int getNumTimepoints()
	{
		return spimData.getSequenceDescription().getTimePoints().size();
	}

	public CacheControl getCache()
	{
		return cache;
	}

	public Bookmarks getBookmarks()
	{
		return bookmarks;
	}

	public ManualTransformation getManualTransformation()
	{
		return manualTransformation;
	}

	public File getProposedSettingsFile()
	{
		return proposedSettingsFile;
	}

	public void setProposedSettingsFile( final File file )
	{
		this.proposedSettingsFile = file;
	}

	public boolean is2D()
	{
		return is2D;
	}

	/**
	 * Utility that returns <code>true</code> if all the sources specified are
	 * 2D.
	 *
	 * @return <code>true</code> if all the sources specified are 2D.
	 */
	private boolean computeIs2D()
	{
		for ( final SourceAndConverter< ? > sac : sources )
		{
			final Source< ? > source = sac.getSpimSource();
			for ( int t = 0; t < getNumTimepoints(); t++ )
			{
				if ( source.isPresent( t ) )
				{
					final RandomAccessibleInterval< ? > level = source.getSource( t, 0 );
					if ( level.dimension( 2 ) > 1 )
						return false;
					break;
				}
			}
		}
		return true;
	}

	public static SharedBigDataViewerData fromSpimDataXmlFile( final String spimDataXmlFilename ) throws SpimDataException
	{
		try
		{
			final AbstractSpimData< ? > spimData = new XmlIoSpimDataMinimal().load( spimDataXmlFilename );
			return fromSpimData( spimDataXmlFilename, spimData );
		}
		catch ( final RuntimeException e )
		{
			if ( FileNotFoundException.class.isInstance( e.getCause() ) )
				throw new SpimDataIOException( "Could not find the image data file:\n" + e.getCause().getMessage() );
			else if ( HDF5FileNotFoundException.class.isInstance( e ) )
				throw new SpimDataIOException( "Error in the BDV XML file:" + spimDataXmlFilename
						+ "\nCould not find the HDF5 image data file:\n"
						+ e.getMessage() );
			
			throw ( e );
		}
	}

	public static SharedBigDataViewerData fromDummyFilename( final String spimDataXmlFilename )
	{
		final AbstractSpimData< ? > spimData = DummySpimData.tryCreate( spimDataXmlFilename );
		return fromSpimData( spimDataXmlFilename, spimData );
	}

	/**
	 * Returns a "dummy" {@link SharedBigDataViewerData} object. Pixel sizes,
	 * image sizes, and image transformations are read from the given
	 * BigDataViewer XML. The actual image data is not loaded, all pixels are
	 * black.
	 * 
	 * @param spimDataXmlFilename
	 *            the pseudo XML file, containing the dummy information.
	 * @return a "dummy" {@link SharedBigDataViewerData} object.
	 * @throws SpimDataException
	 *             if something wrong happens when build the spim data object.
	 */
	public static SharedBigDataViewerData createDummyDataFromSpimDataXml( final String spimDataXmlFilename ) throws SpimDataException
	{
		final AbstractSpimData< ? > spimData = DummySpimData.fromSpimDataXml( spimDataXmlFilename );
		return fromSpimData( spimDataXmlFilename, spimData );
	}

	private static SharedBigDataViewerData fromSpimData(
			final String spimDataXmlFilename,
			final AbstractSpimData< ? > spimData )
	{
		final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		final CacheControl cache = ( ( ViewerImgLoader ) seq.getImgLoader() ).getCacheControl();

		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		final ArrayList< SourceAndConverter< ? > > sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, converterSetups, sources );

		final ConverterSetups setups = new ConverterSetups( new BasicViewerState() );
		for ( int i = 0; i < sources.size(); ++i )
			setups.put( sources.get( i ), converterSetups.get( i ) );

		WrapBasicImgLoader.removeWrapperIfPresent( spimData );

		final SharedBigDataViewerData sbdv = new SharedBigDataViewerData(
				spimData,
				sources,
				setups,
				cache );

		if ( !sbdv.tryLoadSettings( spimDataXmlFilename ) )
		{
			final BasicViewerState state = new BasicViewerState();
			state.addSource( sources.get( 0 ) );
			state.setCurrentSource( sources.get( 0 ) );
			InitializeViewerState.initBrightness( 0.001, 0.999, state, setups );
		}

		return sbdv;
	}

	/*
	 * FROM IMAGEPLUS.
	 */

	public static SharedBigDataViewerData fromImagePlus( final ImagePlus imp )
	{
		// check the image type
		switch ( imp.getType() )
		{
		case ImagePlus.GRAY8:
		case ImagePlus.GRAY16:
		case ImagePlus.GRAY32:
		case ImagePlus.COLOR_RGB:
			break;
		default:
			IJ.showMessage(
					imp.getShortTitle() + ": Only 8, 16, 32-bit images and RGB images are supported currently!" );
			return null;
		}

		// get calibration and image size
		final double pw = imp.getCalibration().pixelWidth;
		final double ph = imp.getCalibration().pixelHeight;
		final double pd = imp.getCalibration().pixelDepth;
		String punit = imp.getCalibration().getUnit();
		if ( punit == null || punit.isEmpty() )
			punit = "pixel";
		final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions( punit, pw, ph, pd );
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getNSlices();
		final FinalDimensions size = new FinalDimensions( w, h, d );

		// create ImgLoader wrapping the image
		final BasicImgLoader imgLoader;
		int setupIdOffset = 0;
		if ( imp.getStack().isVirtual() )
		{
			switch ( imp.getType() )
			{
			case ImagePlus.GRAY8:
				imgLoader = VirtualStackImageLoader.createUnsignedByteInstance( imp, setupIdOffset );
				break;
			case ImagePlus.GRAY16:
				imgLoader = VirtualStackImageLoader.createUnsignedShortInstance( imp, setupIdOffset );
				break;
			case ImagePlus.GRAY32:
				imgLoader = VirtualStackImageLoader.createFloatInstance( imp, setupIdOffset );
				break;
			case ImagePlus.COLOR_RGB:
			default:
				imgLoader = VirtualStackImageLoader.createARGBInstance( imp, setupIdOffset );
				break;
			}
		}
		else
		{
			switch ( imp.getType() )
			{
			case ImagePlus.GRAY8:
				imgLoader = ImageStackImageLoader.createUnsignedByteInstance( imp, setupIdOffset );
				break;
			case ImagePlus.GRAY16:
				imgLoader = ImageStackImageLoader.createUnsignedShortInstance( imp, setupIdOffset );
				break;
			case ImagePlus.GRAY32:
				imgLoader = ImageStackImageLoader.createFloatInstance( imp, setupIdOffset );
				break;
			case ImagePlus.COLOR_RGB:
			default:
				imgLoader = ImageStackImageLoader.createARGBInstance( imp, setupIdOffset );
				break;
			}
		}

		final int numTimepoints = imp.getNFrames();
		final int numSetups = imp.getNChannels();

		// create setups from channels
		final HashMap< Integer, BasicViewSetup > setups = new HashMap<>( numSetups );
		for ( int s = 0; s < numSetups; ++s )
		{
			final BasicViewSetup setup = new BasicViewSetup( setupIdOffset + s,
					String.format( imp.getTitle() + " channel %d", s + 1 ), size, voxelSize );
			setup.setAttribute( new Channel( s + 1 ) );
			setups.put( setupIdOffset + s, setup );
		}

		// create timepoints
		final ArrayList< TimePoint > timepoints = new ArrayList<>( numTimepoints );
		for ( int t = 0; t < numTimepoints; ++t )
			timepoints.add( new TimePoint( t ) );
		final SequenceDescriptionMinimal seq =
				new SequenceDescriptionMinimal( new TimePoints( timepoints ), setups, imgLoader, null );

		// create ViewRegistrations from the images calibration
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		sourceTransform.set( pw, 0, 0, 0, 0, ph, 0, 0, 0, 0, pd, 0 );
		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
		for ( int t = 0; t < numTimepoints; ++t )
			for ( int s = 0; s < numSetups; ++s )
				registrations.add( new ViewRegistration( t, setupIdOffset + s, sourceTransform ) );

		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		final ArrayList< SourceAndConverter< ? > > sources = new ArrayList<>();

		final File basePath = new File( "." );
		final AbstractSpimData< ? > spimData =
				new SpimDataMinimal( basePath, seq, new ViewRegistrations( registrations ) );
		WrapBasicImgLoader.wrapImgLoaderIfNecessary( spimData );
		BigDataViewer.initSetups( spimData, converterSetups, sources );

		final CacheControl.CacheControls cache = new CacheControl.CacheControls();
		cache.addCacheControl(
				( ( ViewerImgLoader ) spimData.getSequenceDescription().getImgLoader() ).getCacheControl() );
		setupIdOffset += imp.getNChannels();

		final BasicViewerState state = new BasicViewerState();
		for ( final SourceAndConverter< ? > sourceAndConverter : sources )
			state.addSource( sourceAndConverter );

		final ConverterSetups css = new ConverterSetups( state );
		for ( int i = 0; i < sources.size(); i++ )
			css.put( sources.get( i ), converterSetups.get( i ) );

		final SharedBigDataViewerData sbdv = new SharedBigDataViewerData(
				spimData,
				sources,
				css,
				cache );

		// File info
		final FileInfo fileInfo = imp.getOriginalFileInfo();
		String imageFileName;
		String imageFolder;
		if ( null != fileInfo )
		{
			imageFileName = fileInfo.fileName;
			imageFolder = fileInfo.directory;
		}
		else
		{
			imageFileName = imp.getShortTitle();
			imageFolder = "";

		}
		final String imageSourceFilename = new File( imageFolder, imageFileName ).getAbsolutePath();

		if ( !sbdv.tryLoadSettings( imageSourceFilename ) )
		{
			int channelOffset = 0;
			final int numActiveChannels = transferChannelVisibility( imp, state );
			transferChannelSettings( channelOffset, imp, state, css );
			channelOffset += imp.getNChannels();
			state.setDisplayMode( numActiveChannels > 1 ? DisplayMode.FUSED : DisplayMode.SINGLE );
		}

		return sbdv;
	}

	/**
	 * @return number of setups that were set active.
	 */
	private static int transferChannelVisibility(
			final ImagePlus imp,
			final ViewerState state )
	{
		final int nChannels = imp.getNChannels();
		final CompositeImage ci = imp.isComposite() ? ( CompositeImage ) imp : null;
		final List< SourceAndConverter< ? > > sources = state.getSources();
		if ( ci != null && ci.getCompositeMode() == IJ.COMPOSITE )
		{
			final boolean[] activeChannels = ci.getActiveChannels();
			int numActiveChannels = 0;
			for ( int i = 0; i < Math.min( activeChannels.length, nChannels ); ++i )
			{
				final SourceAndConverter< ? > source = sources.get( i );
				state.setSourceActive( source, activeChannels[ i ] );
				state.setCurrentSource( source );
				numActiveChannels += activeChannels[ i ] ? 1 : 0;
			}
			return numActiveChannels;
		}
		else
		{
			final int activeChannel = imp.getChannel() - 1;
			for ( int i = 0; i < nChannels; ++i )
				state.setSourceActive( sources.get( i ), i == activeChannel );
			state.setCurrentSource( sources.get( activeChannel ) );
			return 1;
		}
	}

	private static void transferChannelSettings( final int channelOffset, final ImagePlus imp, final ViewerState state,
			final ConverterSetups converterSetups )
	{
		final int nChannels = imp.getNChannels();
		final CompositeImage ci = imp.isComposite() ? ( CompositeImage ) imp : null;
		final List< SourceAndConverter< ? > > sources = state.getSources();
		if ( ci != null )
		{
			final int mode = ci.getCompositeMode();
			final boolean transferColor = mode == IJ.COMPOSITE || mode == IJ.COLOR;
			for ( int c = 0; c < nChannels; ++c )
			{
				final LUT lut = ci.getChannelLut( c + 1 );
				final ConverterSetup setup = converterSetups.getConverterSetup( sources.get( channelOffset + c ) );
				if ( transferColor )
					setup.setColor( new ARGBType( lut.getRGB( 255 ) ) );
				setup.setDisplayRange( lut.min, lut.max );
			}
		}
		else
		{
			final double displayRangeMin = imp.getDisplayRangeMin();
			final double displayRangeMax = imp.getDisplayRangeMax();
			for ( int i = 0; i < nChannels; ++i )
			{
				final ConverterSetup setup = converterSetups.getConverterSetup( sources.get( channelOffset + i ) );
				final LUT[] luts = imp.getLuts();
				if ( luts.length != 0 )
					setup.setColor( new ARGBType( luts[ 0 ].getRGB( 255 ) ) );
				setup.setDisplayRange( displayRangeMin, displayRangeMax );
			}
		}
	}
}
