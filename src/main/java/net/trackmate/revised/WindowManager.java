package net.trackmate.revised;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.BehaviourTransformEventHandler3D;
import bdv.BigDataViewer;
import bdv.export.ProgressWriterConsole;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.InitializeViewerState;
import bdv.tools.ToggleDialogAction;
import bdv.util.AbstractNamedAction;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.TimePoint;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.listenable.GraphChangeListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.revised.bdv.overlay.MouseNavigationHandler;
import net.trackmate.revised.bdv.overlay.MouseOverListener;
import net.trackmate.revised.bdv.overlay.MouseSelectionHandler;
import net.trackmate.revised.bdv.overlay.OverlayContext;
import net.trackmate.revised.bdv.overlay.OverlayGraphRenderer;
import net.trackmate.revised.bdv.overlay.RenderSettings;
import net.trackmate.revised.bdv.overlay.RenderSettings.UpdateListener;
import net.trackmate.revised.bdv.overlay.ui.RenderSettingsDialog;
import net.trackmate.revised.bdv.overlay.wrap.OverlayContextWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayGraphWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayHighlightWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayNavigationWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlaySelectionWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import net.trackmate.revised.model.mamut.BoundingSphereRadiusStatistics;
import net.trackmate.revised.model.mamut.Link;
import net.trackmate.revised.model.mamut.Model;
import net.trackmate.revised.model.mamut.ModelOverlayProperties;
import net.trackmate.revised.model.mamut.Spot;
import net.trackmate.revised.trackscheme.DefaultModelFocusProperties;
import net.trackmate.revised.trackscheme.DefaultModelGraphProperties;
import net.trackmate.revised.trackscheme.DefaultModelHighlightProperties;
import net.trackmate.revised.trackscheme.DefaultModelNavigationProperties;
import net.trackmate.revised.trackscheme.DefaultModelSelectionProperties;
import net.trackmate.revised.trackscheme.ModelFocusProperties;
import net.trackmate.revised.trackscheme.ModelHighlightProperties;
import net.trackmate.revised.trackscheme.ModelNavigationProperties;
import net.trackmate.revised.trackscheme.ModelSelectionProperties;
import net.trackmate.revised.trackscheme.TrackSchemeContext;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.context.Context;
import net.trackmate.revised.trackscheme.context.ContextListener;
import net.trackmate.revised.trackscheme.display.TrackSchemeFrame;
import net.trackmate.revised.trackscheme.display.TrackSchemePanel;
import net.trackmate.revised.ui.grouping.GroupHandle;
import net.trackmate.revised.ui.grouping.GroupLocksPanel;
import net.trackmate.revised.ui.grouping.GroupManager;
import net.trackmate.revised.ui.selection.FocusModel;
import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.HighlightModel;
import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.revised.ui.selection.SelectionListener;

public class WindowManager
{
	private final GroupManager groupManager;

	private final SpimDataMinimal spimData;

	private final InputTriggerConfig keyconf;

	final int minTimepoint;

	final int maxTimepoint;

	private final Model model;

	private final Selection< Spot, Link > selection;

	private final HighlightModel< Spot, Link > highlightModel;

	final BoundingSphereRadiusStatistics radiusStats;

	public WindowManager(
			final SpimDataMinimal spimData,
			final Model model,
			final InputTriggerConfig keyconf )
	{
		this.spimData = spimData;
		this.model = model;
		this.keyconf = keyconf;
		groupManager = new GroupManager();

		final ListenableGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();
		selection = new Selection<>( graph, idmap );
		highlightModel = new HighlightModel< Spot, Link  >( idmap );
		radiusStats = new BoundingSphereRadiusStatistics( model );

		final List< TimePoint > timePointsOrdered = spimData.getSequenceDescription().getTimePoints().getTimePointsOrdered();
		minTimepoint = 0;
		maxTimepoint = timePointsOrdered.size() - 1;
		/*
		 * TODO: (?) For now, we use timepoint indices in MaMuT model, instead
		 * of IDs/names. This is because BDV also displays timepoint index, and
		 * it would be confusing to have different labels in TrackScheme. If
		 * this is changed in the future, then probably only in the model files.
		 */
	}

	private final List< BdvWindow > bdvWindows = new ArrayList<>();

	public static class BdvWindow
	{
		private final ViewerFrame viewerFrame;

		private final OverlayGraphRenderer< ?, ? > tracksOverlay;

		private final GroupHandle groupHandle;

		public BdvWindow(
				final ViewerFrame viewerFrame,
				final OverlayGraphRenderer< ?, ? > tracksOverlay,
				final GroupHandle groupHandle )
		{
			this.viewerFrame = viewerFrame;
			this.tracksOverlay = tracksOverlay;
			this.groupHandle = groupHandle;
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
	}

	private synchronized void addBdvWindow( final BdvWindow w )
	{
		System.out.println( "add bdv" );
		bdvWindows.add( w );
	}

	private synchronized void removeBdvWindow( final BdvWindow w )
	{
		System.out.println( "remove bdv" );
		bdvWindows.remove( w );
	}

	public void createBigDataViewer()
	{
		final GroupHandle bdvGroupHandle = groupManager.createGroupHandle();

		final OverlayGraphWrapper< Spot, Link > overlayGraph = new OverlayGraphWrapper<>(
				model.getGraph(),
				model.getGraphIdBimap(),
				model.getSpatioTemporalIndex(),
				new ModelOverlayProperties( radiusStats, selection ) );

		final OverlayHighlightWrapper< Spot, Link > overlayHighlight = new OverlayHighlightWrapper<>(
				model.getGraphIdBimap(),
				highlightModel );

		final OverlaySelectionWrapper< Spot, Link > overlaySelection = new OverlaySelectionWrapper<>(
				selection );

		final String windowTitle = "BigDataViewer";
		final BigDataViewer bdv = BigDataViewer.open( spimData, windowTitle, new ProgressWriterConsole(),
				ViewerOptions.options().
				transformEventHandlerFactory( BehaviourTransformEventHandler3D.factory( keyconf ) ).
				inputTriggerConfig( keyconf ) );

//		if ( !bdv.tryLoadSettings( bdvFile ) ) // TODO
			InitializeViewerState.initBrightness( 0.001, 0.999, bdv.getViewer(), bdv.getSetupAssignments() );

		final ViewerPanel viewer = bdv.getViewer();
		viewer.setTimepoint( currentTimepoint );
		final OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > tracksOverlay = new OverlayGraphRenderer<>( overlayGraph, overlayHighlight );
		viewer.getDisplay().addOverlayRenderer( tracksOverlay );
		viewer.addRenderTransformListener( tracksOverlay );
		viewer.addTimePointListener( tracksOverlay );
		overlayHighlight.addHighlightListener( new HighlightListener()
		{
			@Override
			public void highlightChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		model.getGraph().addGraphChangeListener( new GraphChangeListener()
		{
			@Override
			public void graphChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		overlaySelection.addSelectionListener( new SelectionListener()
		{
			@Override
			public void selectionChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );

		final MouseOverListener< ?, ? > mouseOver = new MouseOverListener<>( overlayGraph, tracksOverlay, overlayHighlight );
		viewer.getDisplay().addHandler( mouseOver );

		final MouseSelectionHandler< ?, ? > mouseSelectionListener = new MouseSelectionHandler<>( overlayGraph, tracksOverlay, overlaySelection );
		viewer.getDisplay().addHandler( mouseSelectionListener );

		final NavigationHandler< Spot > navigationHandler = new NavigationHandler<>( bdvGroupHandle );
		final OverlayNavigationWrapper< Spot, Link > navigation =
				new OverlayNavigationWrapper< Spot, Link >( viewer, overlayGraph, navigationHandler );

		final MouseNavigationHandler< ?, ? > mouseNavigationHandler = new MouseNavigationHandler<>( overlayGraph, tracksOverlay, navigation );
		viewer.getDisplay().addHandler( mouseNavigationHandler );

		final OverlayContext< OverlayVertexWrapper< Spot, Link > > overlayContext = new OverlayContext<>( overlayGraph, tracksOverlay );
		viewer.addRenderTransformListener( overlayContext );
		viewer.addTimePointListener( overlayContext );
		final OverlayContextWrapper< Spot, Link > overlayContextWrapper = new OverlayContextWrapper<>( overlayContext, new BdvContextAdapter() );

		final ViewerFrame viewerFrame = bdv.getViewerFrame();
		final GroupLocksPanel lockPanel = new GroupLocksPanel( bdvGroupHandle );
		viewerFrame.add( lockPanel, BorderLayout.NORTH );
		viewerFrame.pack();

		/*
		 * TODO: this is still wrong. There should be one central entity syncing
		 * time for several BDV frames and TrackSchemePanel should listen to
		 * that. Ideally windows should be configurable to "share" timepoints or
		 * not.
		 */
		viewer.addTimePointListener( tpl );
//		viewer.repaint(); // TODO remove?


		// TODO revise
		// RenderSettingsDialog triggered by "R"
		final RenderSettings renderSettings = new RenderSettings(); // TODO should be in overlay eventually
		final String RENDER_SETTINGS = "render settings";
		final RenderSettingsDialog renderSettingsDialog = new RenderSettingsDialog( viewerFrame, renderSettings );
		final ActionMap actionMap = new ActionMap();
		AbstractNamedAction.put( actionMap, new ToggleDialogAction( RENDER_SETTINGS, renderSettingsDialog ) );
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

		final BdvWindow bdvWindow = new BdvWindow( viewerFrame, tracksOverlay, bdvGroupHandle );
		viewerFrame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				removeBdvWindow( bdvWindow );
			}
		} );
		addBdvWindow( bdvWindow );
	}


	private final List< TsWindow > tsWindows = new ArrayList<>();

	public static class TsWindow
	{
		private final TrackSchemeFrame trackSchemeFrame;

		private final GroupHandle groupHandle;

		public TsWindow(
				final TrackSchemeFrame trackSchemeFrame,
				final GroupHandle groupHandle )
		{
			this.trackSchemeFrame = trackSchemeFrame;
			this.groupHandle = groupHandle;
		}

		public TrackSchemeFrame getTrackSchemeFrame()
		{
			return trackSchemeFrame;
		}

		public GroupHandle getGroupHandle()
		{
			return groupHandle;
		}
	}

	// TODO testing only
	private int currentTimepoint = 0;

	// TODO testing only
	private final TimePointListener tpl = new TimePointListener()
	{
		@Override
		public void timePointChanged( final int timePointIndex )
		{
			if ( currentTimepoint != timePointIndex )
			{
				currentTimepoint = timePointIndex;
				for ( final TsWindow w : tsWindows )
					w.getTrackSchemeFrame().getTrackschemePanel().timePointChanged( timePointIndex );
			}
		}
	};

	public void createTrackScheme()
	{
		final ListenableGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();

		/*
		 * TrackSchemeGraph listening to model
		 */
		final DefaultModelGraphProperties< Spot, Link > properties = new DefaultModelGraphProperties<>( graph, idmap, selection );
		final TrackSchemeGraph< Spot, Link > trackSchemeGraph = new TrackSchemeGraph<>( graph, idmap, properties );

		/*
		 * TrackSchemeHighlight wrapping HighlightModel
		 */
		final ModelHighlightProperties highlightProperties = new DefaultModelHighlightProperties< Spot, Link >( graph, idmap, highlightModel );
		final TrackSchemeHighlight trackSchemeHighlight = new TrackSchemeHighlight( highlightProperties, trackSchemeGraph );

		/*
		 * TrackScheme selection
		 */
		final ModelSelectionProperties selectionProperties = new DefaultModelSelectionProperties< Spot, Link >( graph, idmap, selection );
		final TrackSchemeSelection trackSchemeSelection = new TrackSchemeSelection( selectionProperties );

		/*
		 * TrackScheme GroupHandle
		 */
		final GroupHandle groupHandle = groupManager.createGroupHandle();

		/*
		 * TrackScheme navigation
		 */
		final NavigationHandler< Spot > navigationHandler = new NavigationHandler< Spot >( groupHandle );
		final ModelNavigationProperties navigationProperties = new DefaultModelNavigationProperties< Spot, Link >( graph, idmap, navigationHandler );
		final TrackSchemeNavigation trackSchemeNavigation = new TrackSchemeNavigation( navigationProperties, trackSchemeGraph );

		/*
		 * TrackScheme focus
		 */
		final FocusModel< Spot, Link > focusModel = new FocusModel<>( idmap );
		final ModelFocusProperties focusProperties = new DefaultModelFocusProperties<>( graph, idmap, focusModel );
		final TrackSchemeFocus trackSchemeFocus = new TrackSchemeFocus( focusProperties, trackSchemeGraph );

		/*
		 * show TrackSchemeFrame
		 */
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				trackSchemeGraph,
				trackSchemeHighlight,
				trackSchemeFocus,
				trackSchemeSelection,
				trackSchemeNavigation,
				groupHandle );
		frame.getTrackschemePanel().setTimepointRange( minTimepoint, maxTimepoint );
		frame.getTrackschemePanel().graphChanged();
		frame.setVisible( true );

		tsWindows.add( new TsWindow( frame, groupHandle ) );
	}

	public void buildContext( final Context< Spot > context, final int id )
	{
		for ( final TsWindow tsWindow : tsWindows )
		{
			final TrackSchemePanel panel = tsWindow.getTrackSchemeFrame().getTrackschemePanel();
			panel.contextChanged( new TrackSchemeContext< Spot >( model.getGraphIdBimap(), panel.getGraph(), context ) );
		}
	}

	private class BdvContextAdapter implements ContextListener< Spot >
	{
		private final int id = 0; // TODO

		@Override
		public void contextChanged( final Context< Spot > context )
		{
			buildContext( context, id );
		}
	}

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		final String bdvFile = "samples/datasethdf5.xml";
		final String modelFile = "samples/model_revised.raw";

		/*
		 * Load Model
		 */
		final Model model = new Model();
		model.loadRaw( new File( modelFile ) );

		/*
		 * Load SpimData
		 */
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( bdvFile );

		/*
		 * Load keyconfig
		 */
		InputTriggerConfig keyconf;
		try
		{
			keyconf = new InputTriggerConfig( YamlConfigIO.read( "samples/keyconf.yaml" ) );
		}
		catch ( final IOException e )
		{
			keyconf = new InputTriggerConfig();
		}

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final WindowManager wm = new WindowManager( spimData, model, keyconf );
		wm.createBigDataViewer();
		wm.createTrackScheme();
//		wm.createBigDataViewer();
		wm.bdvWindows.get( 0 ).getViewerFrame().getViewerPanel().setTimepoint( 15 );
	}
}
