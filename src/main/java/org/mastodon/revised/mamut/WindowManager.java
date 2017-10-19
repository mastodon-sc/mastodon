package org.mastodon.revised.mamut;

import bdv.spimdata.SpimDataMinimal;
import bdv.tools.ToggleDialogAction;
import bdv.viewer.RequestRepaint;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import mpicbg.spim.data.generic.AbstractSpimData;
import org.mastodon.adapter.RefBimap;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.revised.bdv.BigDataViewerMaMuT;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.BdvHighlightHandler;
import org.mastodon.revised.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.revised.bdv.overlay.EditBehaviours;
import org.mastodon.revised.bdv.overlay.EditSpecialBehaviours;
import org.mastodon.revised.bdv.overlay.OverlayContext;
import org.mastodon.revised.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.revised.bdv.overlay.OverlayNavigation;
import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.RenderSettings.UpdateListener;
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsDialog;
import org.mastodon.revised.bdv.overlay.wrap.OverlayContextWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayEdgeWrapperBimap;
import org.mastodon.revised.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayVertexWrapperBimap;
import org.mastodon.revised.context.Context;
import org.mastodon.revised.context.ContextChooser;
import org.mastodon.revised.context.ContextListener;
import org.mastodon.revised.context.ContextProvider;
import org.mastodon.revised.model.mamut.BoundingSphereRadiusStatistics;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.ModelOverlayProperties;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.trackscheme.TrackSchemeContextListener;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeEdgeBimap;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.TrackSchemeVertexBimap;
import org.mastodon.revised.trackscheme.display.TrackSchemeEditBehaviours;
import org.mastodon.revised.trackscheme.display.TrackSchemeFrame;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.ui.TrackSchemeStyleChooser;
import org.mastodon.revised.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

public class WindowManager
{
	/**
	 * Information for one BigDataViewer window.
	 */
	public static class BdvWindow
	{
		private final ViewerFrame viewerFrame;

		private final OverlayGraphRenderer< ?, ? > tracksOverlay;

		private final GroupHandle groupHandle;

		private final ContextProvider< Spot > contextProvider;

		public BdvWindow(
				final ViewerFrame viewerFrame,
				final OverlayGraphRenderer< ?, ? > tracksOverlay,
				final GroupHandle groupHandle,
				final ContextProvider< Spot > contextProvider )
		{
			this.viewerFrame = viewerFrame;
			this.tracksOverlay = tracksOverlay;
			this.groupHandle = groupHandle;
			this.contextProvider = contextProvider;
		}

		public ViewerFrame getViewerFrame()
		{
			return viewerFrame;
		}

		public OverlayGraphRenderer< ?, ? > getTracksOverlay()
		{
			return tracksOverlay;
		}

		public GroupHandle getGroupHandle()
		{
			return groupHandle;
		}

		public ContextProvider< Spot > getContextProvider()
		{
			return contextProvider;
		}
	}

	/**
	 * Information for one TrackScheme window.
	 */
	public static class TsWindow
	{
		private final TrackSchemeFrame trackSchemeFrame;

		private final GroupHandle groupHandle;

		private final ContextChooser< Spot > contextChooser;

		public TsWindow(
				final TrackSchemeFrame trackSchemeFrame,
				final GroupHandle groupHandle,
				final ContextChooser< Spot > contextChooser )
		{
			this.trackSchemeFrame = trackSchemeFrame;
			this.groupHandle = groupHandle;
			this.contextChooser = contextChooser;
		}

		public TrackSchemeFrame getTrackSchemeFrame()
		{
			return trackSchemeFrame;
		}

		public GroupHandle getGroupHandle()
		{
			return groupHandle;
		}

		public ContextChooser< Spot > getContextChooser()
		{
			return contextChooser;
		}
	}

	/**
	 * TODO!!! related to {@link OverlayContextWrapper}
	 *
	 * @param <V>
	 * 		the type of vertices in the model.
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public static class BdvContextAdapter< V > implements ContextListener< V >, ContextProvider< V >
	{
		private final String contextProviderName;

		private final ArrayList< ContextListener< V > > listeners;

		private Context< V > context;

		public BdvContextAdapter( final String contextProviderName )
		{
			this.contextProviderName = contextProviderName;
			listeners = new ArrayList<>();
		}

		@Override
		public String getContextProviderName()
		{
			return contextProviderName;
		}

		@Override
		public synchronized boolean addContextListener( final ContextListener< V > l )
		{
			if ( !listeners.contains( l ) )
			{
				listeners.add( l );
				l.contextChanged( context );
				return true;
			}
			return false;
		}

		@Override
		public synchronized boolean removeContextListener( final ContextListener< V > l )
		{
			return listeners.remove( l );
		}

		@Override
		public synchronized void contextChanged( final Context< V > context )
		{
			this.context = context;
			for ( final ContextListener< V > l : listeners )
				l.contextChanged( context );
		}
	}

	private final InputTriggerConfig keyconf;

	private final KeyPressedManager keyPressedManager;

	private final MamutAppModel appModel;

	/**
	 * All currently open BigDataViewer windows.
	 */
	private final List< BdvWindow > bdvWindows = new ArrayList<>();

	/**
	 * The {@link ContextProvider}s of all currently open BigDataViewer windows.
	 */
	private final List< ContextProvider< Spot > > contextProviders = new ArrayList<>();

	/**
	 * All currently open TrackScheme windows.
	 */
	private final List< TsWindow > tsWindows = new ArrayList<>();

	public WindowManager(
			final String spimDataXmlFilename,
			final SpimDataMinimal spimData,
			final Model model,
			final InputTriggerConfig keyconf )
	{
		this.keyconf = keyconf;

		keyPressedManager = new KeyPressedManager();
		final RequestRepaint requestRepaint = () -> {
			for ( final BdvWindow w : bdvWindows )
				w.getViewerFrame().getViewerPanel().requestRepaint();
		};

		final ViewerOptions options = ViewerOptions.options()
				.inputTriggerConfig( keyconf )
				.shareKeyPressedEvents( keyPressedManager );
		final SharedBigDataViewerData sharedBdvData = new SharedBigDataViewerData( spimDataXmlFilename, spimData, options, requestRepaint );

		appModel = new MamutAppModel( model, sharedBdvData, keyconf );
	}

	private synchronized void addBdvWindow( final BdvWindow w )
	{
		w.getViewerFrame().addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				removeBdvWindow( w );
			}
		} );
		bdvWindows.add( w );
		contextProviders.add( w.getContextProvider() );
		for ( final TsWindow tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void removeBdvWindow( final BdvWindow w )
	{
		bdvWindows.remove( w );
		contextProviders.remove( w.getContextProvider() );
		for ( final TsWindow tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void addTsWindow( final TsWindow w )
	{
		w.getTrackSchemeFrame().addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				removeTsWindow( w );
			}
		} );
		tsWindows.add( w );
		w.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void removeTsWindow( final TsWindow w )
	{
		tsWindows.remove( w );
		w.getContextChooser().updateContextProviders( new ArrayList<>() );
	}

	static class MamutViewBdv extends MamutView< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > >
	{
		// TODO
		private int bdvName = 1;

		private final BoundingSphereRadiusStatistics radiusStats;

		private final SharedBigDataViewerData sharedBdvData;

		private final OverlayGraphWrapper< Spot, Link > overlayGraph;

		private BdvWindow bdvWindow;

		public MamutViewBdv( final MamutAppModel appModel )
		{
			this( appModel,
					new OverlayGraphWrapper<>(
							appModel.getModel().getGraph(),
							appModel.getModel().getGraphIdBimap(),
							appModel.getModel().getSpatioTemporalIndex(),
							new ModelOverlayProperties( appModel.getModel().getGraph(), appModel.getRadiusStats() ) ) );
		}

		private MamutViewBdv(
				final MamutAppModel appModel,
				final OverlayGraphWrapper< Spot, Link > overlayGraph )
		{
			super( appModel, overlayGraph.getVertexMap(), overlayGraph.getEdgeMap() );
			radiusStats = appModel.getRadiusStats();
			sharedBdvData = appModel.getSharedBdvData();
			this.overlayGraph = overlayGraph;

			final String windowTitle = "BigDataViewer " + ( bdvName++ ); // TODO: use JY naming scheme
			final BigDataViewerMaMuT bdv = BigDataViewerMaMuT.open( sharedBdvData, windowTitle, groupHandle );
			final ViewerFrame viewerFrame = bdv.getViewerFrame();
			final ViewerPanel viewer = bdv.getViewer();

			viewer.setTimepoint( timepointModel.getTimepoint() );
			final OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > tracksOverlay = new OverlayGraphRenderer<>(
					overlayGraph,
					highlightModel,
					focusModel,
					selectionModel );
			viewer.getDisplay().addOverlayRenderer( tracksOverlay );
			viewer.addRenderTransformListener( tracksOverlay );
			viewer.addTimePointListener( tracksOverlay );

			final Model model = appModel.getModel();
			final ModelGraph modelGraph = model.getGraph();

			highlightModel.listeners().add( () -> viewer.getDisplay().repaint() );
			focusModel.listeners().add( () -> viewer.getDisplay().repaint() );
			modelGraph.addGraphChangeListener( () -> viewer.getDisplay().repaint() );
			modelGraph.addVertexPositionListener( ( v ) -> viewer.getDisplay().repaint() );
			selectionModel.listeners().add( () -> viewer.getDisplay().repaint() );
			// TODO: remember those listeners and remove them when the BDV window is closed!!!

			final OverlayNavigation< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > overlayNavigation = new OverlayNavigation<>( viewer, overlayGraph );
			navigationHandler.listeners().add( overlayNavigation );

			final BdvHighlightHandler< ?, ? > highlightHandler = new BdvHighlightHandler<>( overlayGraph, tracksOverlay, highlightModel );
			viewer.getDisplay().addHandler( highlightHandler );
			viewer.addRenderTransformListener( highlightHandler );

			final InputTriggerConfig keyconf = appModel.getKeyconf();

			final BdvSelectionBehaviours< ?, ? > selectionBehaviours = new BdvSelectionBehaviours<>( overlayGraph, tracksOverlay, selectionModel, navigationHandler );
			selectionBehaviours.installBehaviourBindings( viewerFrame.getTriggerbindings(), keyconf );

			final OverlayContext< OverlayVertexWrapper< Spot, Link > > overlayContext = new OverlayContext<>( overlayGraph, tracksOverlay );
			viewer.addRenderTransformListener( overlayContext );
			final BdvContextAdapter< Spot > contextProvider = new BdvContextAdapter<>( windowTitle );
			final OverlayContextWrapper< Spot, Link > overlayContextWrapper = new OverlayContextWrapper<>(
					overlayContext,
					contextProvider );

			UndoActions.installActionBindings( viewerFrame.getKeybindings(), model, keyconf );
			EditBehaviours.installActionBindings( viewerFrame.getTriggerbindings(), keyconf, overlayGraph, tracksOverlay, model );
			EditSpecialBehaviours.installActionBindings( viewerFrame.getTriggerbindings(), keyconf, viewerFrame.getViewerPanel(), overlayGraph, tracksOverlay, model );
			HighlightBehaviours.installActionBindings(
					viewerFrame.getTriggerbindings(),
					keyconf,
					new String[] { "bdv" },
					model.getGraph(),
					model.getGraph(),
					appModel.getHighlightModel(),
					model );
			SelectionActions.installActionBindings(
					viewerFrame.getKeybindings(),
					keyconf,
					new String[] { "bdv" },
					model.getGraph(),
					model.getGraph(),
					appModel.getSelectionModel(),
					model );

			viewer.addTimePointListener( timePointIndex -> timepointModel.setTimepoint( timePointIndex ) );
			timepointModel.listeners().add( () -> viewer.setTimepoint( timepointModel.getTimepoint() ) );

			// TODO revise
			// RenderSettingsDialog triggered by "R"
			final RenderSettings renderSettings = new RenderSettings(); // TODO should be in overlay eventually
			final String RENDER_SETTINGS = "render settings";
			final RenderSettingsDialog renderSettingsDialog = new RenderSettingsDialog( viewerFrame, renderSettings );
			final ActionMap actionMap = new ActionMap();
			new ToggleDialogAction( RENDER_SETTINGS, renderSettingsDialog ).put( actionMap );
			final InputMap inputMap = new InputMap();
			final KeyStrokeAdder a = keyconf.keyStrokeAdder( inputMap, "mamut" );
			a.put( RENDER_SETTINGS, "R" );
			viewerFrame.getKeybindings().addActionMap( "mamut", actionMap );
			viewerFrame.getKeybindings().addInputMap( "mamut", inputMap );
			renderSettings.addUpdateListener( new UpdateListener()
			{
				@Override
				public void renderSettingsChanged()
				{
					tracksOverlay.setRenderSettings( renderSettings );
					// TODO: less hacky way of triggering repaint and context update
					viewer.repaint();
					overlayContextWrapper.contextChanged( overlayContext );
				}
			} );

//		if ( !bdv.tryLoadSettings( bdvFile ) ) // TODO
//			InitializeViewerState.initBrightness( 0.001, 0.999, bdv.getViewer(), bdv.getSetupAssignments() );

			bdvWindow = new BdvWindow( viewerFrame, tracksOverlay, groupHandle, contextProvider );
		}
	}

	static class MamutViewTrackScheme extends MamutView< TrackSchemeVertex, TrackSchemeEdge >
	{
		private final TrackSchemeGraph< Spot, Link > trackSchemeGraph;

		private TsWindow tsWindow;

		public MamutViewTrackScheme( final MamutAppModel appModel )
		{
			this( appModel,
					new TrackSchemeGraph<>(
							appModel.getModel().getGraph(),
							appModel.getModel().getGraphIdBimap(),
							new DefaultModelGraphProperties<>() ) );
		}

		private MamutViewTrackScheme(
				final MamutAppModel appModel,
				final TrackSchemeGraph< Spot, Link > trackSchemeGraph )
		{
			super( appModel, trackSchemeGraph.getVertexMap(), trackSchemeGraph.getEdgeMap() );
			this.trackSchemeGraph = trackSchemeGraph;

			/*
			 * TrackScheme ContextChooser
			 */
			final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener<>( trackSchemeGraph );
			final ContextChooser< Spot > contextChooser = new ContextChooser<>( contextListener );


			final InputTriggerConfig keyconf = appModel.getKeyconf();
			KeyPressedManager keyPressedManager = appModel.getSharedBdvData().getOptions().values.getKeyPressedManager();
			final Model model = appModel.getModel();
			final ModelGraph modelGraph = model.getGraph();

			/*
			 * show TrackSchemeFrame
			 */
			final TrackSchemeOptions options = TrackSchemeOptions.options()
					.inputTriggerConfig( keyconf )
					.shareKeyPressedEvents( keyPressedManager );
			final TrackSchemeFrame frame = new TrackSchemeFrame(
					trackSchemeGraph,
					highlightModel,
					focusModel,
					timepointModel,
					selectionModel,
					navigationHandler,
					model,
					groupHandle,
					contextChooser,
					options );
			frame.getTrackschemePanel().setTimepointRange( appModel.getMinTimepoint(), appModel.getMaxTimepoint() );
			frame.getTrackschemePanel().graphChanged();
			contextListener.setContextListener( frame.getTrackschemePanel() );
			frame.setVisible( true );


			UndoActions.installActionBindings( frame.getKeybindings(), model, keyconf );
			HighlightBehaviours.installActionBindings(
					frame.getTriggerbindings(),
					keyconf,
					new String[] { "ts" },
					modelGraph,
					modelGraph,
					appModel.getHighlightModel(),
					model );
			SelectionActions.installActionBindings(
					frame.getKeybindings(),
					keyconf,
					new String[] { "ts" },
					modelGraph,
					modelGraph,
					appModel.getSelectionModel(),
					model );
			TrackSchemeEditBehaviours.installActionBindings(
					frame.getTriggerbindings(),
					keyconf,
					frame.getTrackschemePanel(),
					trackSchemeGraph,
					frame.getTrackschemePanel().getGraphOverlay(),
					modelGraph,
					modelGraph.getGraphIdBimap(),
					model );

			// TrackSchemeStyleDialog triggered by "R"
			final String TRACK_SCHEME_STYLE_SETTINGS = "render settings";
			final TrackSchemeStyleChooser styleChooser = new TrackSchemeStyleChooser( frame, frame.getTrackschemePanel() );
			final JDialog styleDialog = styleChooser.getDialog();
			final ActionMap actionMap = new ActionMap();
			new ToggleDialogAction( TRACK_SCHEME_STYLE_SETTINGS, styleDialog ).put( actionMap );
			final InputMap inputMap = new InputMap();
			final KeyStrokeAdder a = keyconf.keyStrokeAdder( inputMap, "mamut" );
			a.put( TRACK_SCHEME_STYLE_SETTINGS, "R" );
			frame.getKeybindings().addActionMap( "mamut", actionMap );
			frame.getKeybindings().addInputMap( "mamut", inputMap );

			tsWindow = new TsWindow( frame, groupHandle, contextChooser );
			frame.getTrackschemePanel().repaint();
		}
	}

	public void createBigDataViewer()
	{
		final MamutViewBdv view = new MamutViewBdv( appModel );
		addBdvWindow( view.bdvWindow );
	}

	public void createTrackScheme()
	{
		final MamutViewTrackScheme view = new MamutViewTrackScheme( appModel );
		addTsWindow( view.tsWindow );
	}

	public void closeAllWindows()
	{
		final ArrayList< JFrame > frames = new ArrayList<>();
		for ( final BdvWindow w : bdvWindows )
			frames.add( w.getViewerFrame() );
		for ( final TsWindow w : tsWindows )
			frames.add( w.getTrackSchemeFrame() );
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				for ( final JFrame f : frames )
					f.dispatchEvent( new WindowEvent( f, WindowEvent.WINDOW_CLOSING ) );
			}
		} );
	}

	public Model getModel()
	{
		return appModel.getModel();
	}

	public AbstractSpimData< ? > getSpimData()
	{
		return appModel.getSharedBdvData().getSpimData();
	}

	// TODO: move somewhere else. make bdvWindows, tsWindows accessible.
	public static class DumpInputConfig
	{
		private static List< InputTriggerDescription > buildDescriptions( final WindowManager wm ) throws IOException
		{
			final InputTriggerDescriptionsBuilder builder = new InputTriggerDescriptionsBuilder();

			final ViewerFrame viewerFrame = wm.bdvWindows.get( 0 ).viewerFrame;
			builder.addMap( viewerFrame.getKeybindings().getConcatenatedInputMap(), "bdv" );
			builder.addMap( viewerFrame.getTriggerbindings().getConcatenatedInputTriggerMap(), "bdv" );

			final TrackSchemeFrame trackschemeFrame = wm.tsWindows.get( 0 ).trackSchemeFrame;
			builder.addMap( trackschemeFrame.getKeybindings().getConcatenatedInputMap(), "ts" );
			builder.addMap( trackschemeFrame.getTriggerbindings().getConcatenatedInputTriggerMap(), "ts" );

			return builder.getDescriptions();
		}

		public static boolean mkdirs( final String fileName )
		{
			final File dir = new File( fileName ).getParentFile();
			return dir == null ? false : dir.mkdirs();
		}

		public static void writeToYaml( final String fileName, final WindowManager wm ) throws IOException
		{
			mkdirs( fileName );
			YamlConfigIO.write( buildDescriptions( wm ), fileName );
		}
	}
}
