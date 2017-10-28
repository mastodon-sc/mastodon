package org.mastodon.revised.mamut;

import javax.swing.ActionMap;

import org.mastodon.app.ui.ViewMenu;
import org.mastodon.revised.bdv.BdvContextProvider;
import org.mastodon.revised.bdv.BigDataViewerActionsMamut;
import org.mastodon.revised.bdv.BigDataViewerMaMuT;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.ViewerFrameMamut;
import org.mastodon.revised.bdv.overlay.BdvHighlightHandler;
import org.mastodon.revised.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.revised.bdv.overlay.EditBehaviours;
import org.mastodon.revised.bdv.overlay.EditSpecialBehaviours;
import org.mastodon.revised.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.revised.bdv.overlay.OverlayNavigation;
import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsDialog;
import org.mastodon.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.ModelOverlayProperties;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.util.ToggleDialogAction;
import org.mastodon.views.context.ContextProvider;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.InitializeViewerState;
import bdv.viewer.ViewerPanel;

class MamutViewBdv extends MamutView< OverlayGraphWrapper< Spot, Link >, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > >
{
	// TODO
	private static int bdvName = 1;

	private final SharedBigDataViewerData sharedBdvData;

	private final BdvContextProvider< Spot, Link > contextProvider;

	private final ViewerPanel viewer;

	public MamutViewBdv( final MamutAppModel appModel )
	{
		super( appModel,
				new OverlayGraphWrapper<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getSpatioTemporalIndex(),
						appModel.getModel().getGraph().getLock(),
						new ModelOverlayProperties( appModel.getModel().getGraph(), appModel.getRadiusStats() ) ),
				new String[] { "bdv" } );

		sharedBdvData = appModel.getSharedBdvData();

		final String windowTitle = "BigDataViewer " + ( bdvName++ ); // TODO: use JY naming scheme
		final BigDataViewerMaMuT bdv = new BigDataViewerMaMuT( sharedBdvData, windowTitle, groupHandle );
		final ViewerFrameMamut frame = bdv.getViewerFrame();
		setFrame( frame );

		final Actions viewActions = new Actions( appModel.getKeyConfig(), appModel.getKeyConfigContexts() );
		viewActions.install( frame.getKeybindings(), "view" );
		BigDataViewerActionsMamut.installActionBindings( viewActions, bdv );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();
		menu.addItem( "File", "Load settings", actionMap.get( BigDataViewerActionsMamut.LOAD_SETTINGS ) );
		menu.addItem( "File", "Save settings", actionMap.get( BigDataViewerActionsMamut.SAVE_SETTINGS ) );

		menu.addItem( "Edit", "Undo", actionMap.get( UndoActions.UNDO ) );
		menu.addItem( "Edit", "Redo", actionMap.get( UndoActions.REDO ) );
		menu.addSeparator( "Edit" );
		menu.addItem( "Edit", "Delete Selection", actionMap.get( SelectionActions.DELETE_SELECTION ) );
		menu.addItem( "Edit", "Select Whole Track", actionMap.get( SelectionActions.SELECT_WHOLE_TRACK ) );
		menu.addItem( "Edit", "Select Track Downward", actionMap.get( SelectionActions.SELECT_TRACK_DOWNWARD ) );
		menu.addItem( "Edit", "Select Track Upward", actionMap.get( SelectionActions.SELECT_TRACK_UPWARD ) );

		menu.addItem( "Settings", "Brightness & Color", actionMap.get( BigDataViewerActionsMamut.BRIGHTNESS_SETTINGS ) );
		menu.addItem( "Settings", "Visibility & Grouping", actionMap.get( BigDataViewerActionsMamut.VISIBILITY_AND_GROUPING ) );

		frame.setVisible( true );

		viewer = bdv.getViewer();
		InitializeViewerState.initTransform( viewer );

		viewer.setTimepoint( timepointModel.getTimepoint() );
		final OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > tracksOverlay = new OverlayGraphRenderer<>(
				viewGraph,
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

		final OverlayNavigation< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > overlayNavigation = new OverlayNavigation<>( viewer, viewGraph );
		navigationHandler.listeners().add( overlayNavigation );

		final BdvHighlightHandler< ?, ? > highlightHandler = new BdvHighlightHandler<>( viewGraph, tracksOverlay, highlightModel );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.addRenderTransformListener( highlightHandler );

		final InputTriggerConfig keyconf = appModel.getKeyConfig();

		final BdvSelectionBehaviours< ?, ? > selectionBehaviours = new BdvSelectionBehaviours<>( viewGraph, tracksOverlay, selectionModel, navigationHandler );
		selectionBehaviours.installBehaviourBindings( frame.getTriggerbindings(), keyconf );

		contextProvider = new BdvContextProvider<>( windowTitle, viewGraph, tracksOverlay );
		viewer.addRenderTransformListener( contextProvider );

		EditBehaviours.installActionBindings( frame.getTriggerbindings(), keyconf, viewGraph, tracksOverlay, model );
		EditSpecialBehaviours.installActionBindings( frame.getTriggerbindings(), keyconf, frame.getViewerPanel(), viewGraph, tracksOverlay, model );
		HighlightBehaviours.installActionBindings(
				frame.getTriggerbindings(),
				keyconf,
				new String[] { "bdv" },
				model.getGraph(),
				model.getGraph().getLock(),
				model.getGraph(),
				appModel.getHighlightModel(),
				model );

		viewer.addTimePointListener( timePointIndex -> timepointModel.setTimepoint( timePointIndex ) );
		timepointModel.listeners().add( () -> viewer.setTimepoint( timepointModel.getTimepoint() ) );

		// TODO revise
		// RenderSettingsDialog triggered by "R"
		final RenderSettings renderSettings = new RenderSettings(); // TODO should be in overlay eventually
		final String RENDER_SETTINGS = "render settings";
		final RenderSettingsDialog renderSettingsDialog = new RenderSettingsDialog( frame, renderSettings );
		final Actions actions = new Actions( keyconf, "mamut" );
		actions.install( frame.getKeybindings(), "mamut" );
		actions.namedAction( new ToggleDialogAction( RENDER_SETTINGS, renderSettingsDialog ), "R" );
		renderSettings.addUpdateListener( () -> {
			tracksOverlay.setRenderSettings( renderSettings );
			// TODO: less hacky way of triggering repaint and context update
			viewer.repaint();
			contextProvider.notifyContextChanged();
		} );

//		if ( !bdv.tryLoadSettings( bdvFile ) ) // TODO
//			InitializeViewerState.initBrightness( 0.001, 0.999, bdv.getViewer(), bdv.getSetupAssignments() );
	}

	public ContextProvider< Spot > getContextProvider()
	{
		return contextProvider;
	}

	public void requestRepaint()
	{
		viewer.requestRepaint();
	}
}
