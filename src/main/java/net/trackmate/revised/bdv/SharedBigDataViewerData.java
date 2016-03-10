package net.trackmate.revised.bdv;

import java.util.ArrayList;

import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.BigDataViewer;
import bdv.ViewerImgLoader;
import bdv.BehaviourTransformEventHandler3D.BehaviourTransformEventHandler3DFactory;
import bdv.img.cache.Cache;
import bdv.spimdata.WrapBasicImgLoader;
import bdv.tools.InitializeViewerState;
import bdv.tools.bookmarks.Bookmarks;
import bdv.tools.brightness.BrightnessDialog;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.state.SourceGroup;
import bdv.viewer.state.ViewerState;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;

public class SharedBigDataViewerData
	{
		private final ArrayList< ConverterSetup > converterSetups;

		private final ArrayList< SourceAndConverter< ? > > sources;

		private final SetupAssignments setupAssignments;

		private final BrightnessDialog brightnessDialog;

		private final Bookmarks bookmarks;

		private final ViewerOptions options;

		private final InputTriggerConfig inputTriggerConfig;

		private final AbstractSpimData< ? > spimData;

		private final int numTimepoints;

		private final Cache cache;

		public SharedBigDataViewerData(
				final AbstractSpimData< ? > spimData,
				final ViewerOptions options )
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
			cache = ( ( ViewerImgLoader ) seq.getImgLoader() ).getCache();

			converterSetups = new ArrayList<>();
			sources = new ArrayList<>();
			BigDataViewer.initSetups( spimData, converterSetups, sources );

			setupAssignments = new SetupAssignments( converterSetups, 0, 65535 );
			if ( setupAssignments.getMinMaxGroups().size() > 0 )
			{
				final MinMaxGroup group = setupAssignments.getMinMaxGroups().get( 0 );
				for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
					setupAssignments.moveSetupToGroup( setup, group );
			}

			bookmarks = new Bookmarks();

			// TODO: dialog parent?
			brightnessDialog = new BrightnessDialog( null, setupAssignments );

//			if ( !bdv.tryLoadSettings( bdvFile ) ) // TODO
			{
				final ViewerState state = new ViewerState( sources, new ArrayList<>(), 1 );
				InitializeViewerState.initBrightness( 0.001, 0.999, state, setupAssignments );
			}

			WrapBasicImgLoader.removeWrapperIfPresent( spimData );
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

		public Cache getCache()
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