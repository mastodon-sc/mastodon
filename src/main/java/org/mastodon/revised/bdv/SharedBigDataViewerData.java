package org.mastodon.revised.bdv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.BehaviourTransformEventHandler3D.BehaviourTransformEventHandler3DFactory;
import bdv.BigDataViewer;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.spimdata.WrapBasicImgLoader;
import bdv.tools.InitializeViewerState;
import bdv.tools.bookmarks.Bookmarks;
import bdv.tools.brightness.BrightnessDialog;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.RealARGBColorConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformation;
import bdv.viewer.RequestRepaint;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.state.ViewerState;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;

public class SharedBigDataViewerData
{
	private final ArrayList< ConverterSetup > converterSetups;

	private final ArrayList< SourceAndConverter< ? > > sources;

	private final SetupAssignments setupAssignments;

	private final BrightnessDialog brightnessDialog;

	private final ManualTransformation manualTransformation;

	private final Bookmarks bookmarks;

	private final ViewerOptions options;

	private final InputTriggerConfig inputTriggerConfig;

	private final AbstractSpimData< ? > spimData;

	private final int numTimepoints;

	private final CacheControl cache;

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
		this.options = options;

		inputTriggerConfig = ( options.values.getInputTriggerConfig() != null )
				? options.values.getInputTriggerConfig()
				: new InputTriggerConfig();

		if ( options.values.getTransformEventHandlerFactory() instanceof BehaviourTransformEventHandler3DFactory )
			( ( BehaviourTransformEventHandler3DFactory ) options.values.getTransformEventHandlerFactory() ).setConfig( inputTriggerConfig );


		final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		numTimepoints = seq.getTimePoints().size();
		cache = ( ( ViewerImgLoader ) seq.getImgLoader() ).getCacheControl();

		converterSetups = new ArrayList<>();
		sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, converterSetups, sources );

		for ( final ConverterSetup cs : converterSetups )
			if ( RealARGBColorConverterSetup.class.isInstance( cs ) )
				( ( RealARGBColorConverterSetup ) cs ).setViewer( requestRepaint );

		setupAssignments = new SetupAssignments( converterSetups, 0, 65535 );
		if ( setupAssignments.getMinMaxGroups().size() > 0 )
		{
			final MinMaxGroup group = setupAssignments.getMinMaxGroups().get( 0 );
			for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
				setupAssignments.moveSetupToGroup( setup, group );
		}

		manualTransformation = new ManualTransformation( sources );

		bookmarks = new Bookmarks();

		// TODO: dialog parent?
		brightnessDialog = new BrightnessDialog( null, setupAssignments );

		if ( !tryLoadSettings( spimDataXmlFilename ) )
		{
			final ViewerState state = new ViewerState( sources, new ArrayList<>(), 1 );
			InitializeViewerState.initBrightness( 0.001, 0.999, state, setupAssignments );
		}

		WrapBasicImgLoader.removeWrapperIfPresent( spimData );
	}

	private boolean tryLoadSettings( final String xmlFilename )
	{
		if( xmlFilename.startsWith( "http://" ) )
		{
			// load settings.xml from the BigDataServer
			final String settings = xmlFilename + "settings";
			{
				try
				{
					loadSettings( settings );
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
			final File proposedSettingsFile = new File( settings );
			if ( proposedSettingsFile.isFile() )
			{
				try
				{
					loadSettings( settings );
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

	private void loadSettings( final String xmlFilename ) throws IOException, JDOMException
	{
		final SAXBuilder sax = new SAXBuilder();
		final Document doc = sax.build( xmlFilename );
		final Element root = doc.getRootElement();
		setupAssignments.restoreFromXml( root );
		manualTransformation.restoreFromXml( root );
		bookmarks.restoreFromXml( root );
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

	public ArrayList< ConverterSetup > getConverterSetups()
	{
		return converterSetups;
	}

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

	public BrightnessDialog getBrightnessDialog()
	{
		return brightnessDialog;
	}
}
