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

import bdv.TransformEventHandler2D;
import bdv.TransformEventHandler3D;
import bdv.viewer.BasicViewerState;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.BigDataViewer;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.spimdata.WrapBasicImgLoader;
import bdv.tools.InitializeViewerState;
import bdv.tools.bookmarks.Bookmarks;
import bdv.tools.brightness.BrightnessDialog;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformation;
import bdv.viewer.RequestRepaint;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;

public class SharedBigDataViewerData
{
	private final ArrayList< SourceAndConverter< ? > > sources;

	private final ConverterSetups setups;

	// TODO: Remove
	private final SetupAssignments setupAssignments;

	// TODO: Remove
	private BrightnessDialog brightnessDialog;

	private final ManualTransformation manualTransformation;

	private final Bookmarks bookmarks;

	private final ViewerOptions options;

	private final InputTriggerConfig inputTriggerConfig;

	private final AbstractSpimData< ? > spimData;

	private final int numTimepoints;

	private final CacheControl cache;

	private final boolean is2D;

	private File proposedSettingsFile;

	public SharedBigDataViewerData(
			final String spimDataXmlFilename,
			final AbstractSpimData< ? > spimData,
			final ViewerOptions options,
			final RequestRepaint requestRepaint )
	{
		if ( WrapBasicImgLoader.wrapImgLoaderIfNecessary( spimData ) )
		{
			System.err.println( "WARNING:\nOpening <SpimData> dataset that is not suited for interactive browsing.\nConsider resaving as HDF5 for better performance." );
		}

		this.spimData = spimData;

		inputTriggerConfig = ( options.values.getInputTriggerConfig() != null )
				? options.values.getInputTriggerConfig()
				: new InputTriggerConfig();

		final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		numTimepoints = seq.getTimePoints().size();
		cache = ( ( ViewerImgLoader ) seq.getImgLoader() ).getCacheControl();

		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, converterSetups, sources );

		setups = new ConverterSetups( new BasicViewerState() );
		for ( int i = 0; i < sources.size(); ++i )
			setups.put( sources.get( i ), converterSetups.get( i ) );

		setupAssignments = new SetupAssignments( converterSetups, 0, 65535 );
		if ( setupAssignments.getMinMaxGroups().size() > 0 )
		{
			final MinMaxGroup group = setupAssignments.getMinMaxGroups().get( 0 );
			for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
				setupAssignments.moveSetupToGroup( setup, group );
		}

		manualTransformation = new ManualTransformation( sources );

		bookmarks = new Bookmarks();

		if ( !tryLoadSettings( spimDataXmlFilename ) )
		{
			final BasicViewerState state = new BasicViewerState();
			state.addSource( sources.get( 0 ) );
			state.setCurrentSource( sources.get( 0 ) );
			InitializeViewerState.initBrightness( 0.001, 0.999, state, setups );
		}

		is2D = computeIs2D();
		this.options = options
				.inputTriggerConfig( inputTriggerConfig )
				.transformEventHandlerFactory( is2D
						? TransformEventHandler2D::new
						: TransformEventHandler3D::new );

		WrapBasicImgLoader.removeWrapperIfPresent( spimData );
	}

	public boolean tryLoadSettings( final String xmlFilename )
	{
		proposedSettingsFile = null;
		if( xmlFilename.startsWith( "http://" ) )
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
			final String settings = xmlFilename.substring( 0, xmlFilename.length() - ".xml".length() ) + ".settings" + ".xml";
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
		setupAssignments.restoreFromXml( root );
		manualTransformation.restoreFromXml( root );
		bookmarks.restoreFromXml( root );
	}

	public void saveSettings( final String xmlFilename, final ViewerPanel viewer ) throws IOException
	{
		final Element root = new Element( "Settings" );
		root.addContent( viewer.stateToXml() );
		root.addContent( setupAssignments.toXml() );
		root.addContent( manualTransformation.toXml() );
		root.addContent( bookmarks.toXml() );
		final Document doc = new Document( root );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		xout.output( doc, new FileWriter( xmlFilename ) );
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

	@Deprecated
	public SetupAssignments getSetupAssignments()
	{
		return setupAssignments;
	}

	public int getNumTimepoints()
	{
		return numTimepoints;
	}

	public CacheControl getCache()
	{
		return cache;
	}

	public Bookmarks getBookmarks()
	{
		return bookmarks;
	}

	@Deprecated
	public synchronized BrightnessDialog getBrightnessDialog()
	{
		if ( brightnessDialog == null )
			brightnessDialog = new BrightnessDialog( null, setupAssignments );

		return brightnessDialog;
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
	 * Utility that returns <code>true</code> if all the sources specified are 2D.
	 *
	 * @return <code>true</code> if all the sources specified are 2D.
	 */
	private boolean computeIs2D()
	{
		for ( final SourceAndConverter< ? > sac : sources )
		{
			final Source< ? > source = sac.getSpimSource();
			for ( int t = 0; t < numTimepoints; t++ )
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
}
