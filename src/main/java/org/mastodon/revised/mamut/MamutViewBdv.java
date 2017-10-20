package org.mastodon.revised.mamut;

import bdv.tools.ToggleDialogAction;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerPanel;
import javax.swing.ActionMap;
import javax.swing.InputMap;
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
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsDialog;
import org.mastodon.revised.bdv.overlay.wrap.OverlayContextWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.revised.model.mamut.BoundingSphereRadiusStatistics;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.ModelOverlayProperties;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

class MamutViewBdv extends MamutView< OverlayGraphWrapper< Spot, Link >, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > >
{
	// TODO
	private int bdvName = 1;

	private final BoundingSphereRadiusStatistics radiusStats;

	private final SharedBigDataViewerData sharedBdvData;

	private final WindowManager.BdvWindow bdvWindow;

	public MamutViewBdv( final MamutAppModel appModel )
	{
		super( appModel,
				new OverlayGraphWrapper<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getSpatioTemporalIndex(),
						new ModelOverlayProperties( appModel.getModel().getGraph(), appModel.getRadiusStats() ) ) );

		radiusStats = appModel.getRadiusStats();
		sharedBdvData = appModel.getSharedBdvData();

		final String windowTitle = "BigDataViewer " + ( bdvName++ ); // TODO: use JY naming scheme
		final BigDataViewerMaMuT bdv = BigDataViewerMaMuT.open( sharedBdvData, windowTitle, groupHandle );
		final ViewerFrame viewerFrame = bdv.getViewerFrame();
		final ViewerPanel viewer = bdv.getViewer();

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

		final InputTriggerConfig keyconf = appModel.getKeyconf();

		final BdvSelectionBehaviours< ?, ? > selectionBehaviours = new BdvSelectionBehaviours<>( viewGraph, tracksOverlay, selectionModel, navigationHandler );
		selectionBehaviours.installBehaviourBindings( viewerFrame.getTriggerbindings(), keyconf );

		final OverlayContext< OverlayVertexWrapper< Spot, Link > > overlayContext = new OverlayContext<>( viewGraph, tracksOverlay );
		viewer.addRenderTransformListener( overlayContext );
		final WindowManager.BdvContextAdapter< Spot > contextProvider = new WindowManager.BdvContextAdapter<>( windowTitle );
		final OverlayContextWrapper< Spot, Link > overlayContextWrapper = new OverlayContextWrapper<>(
				overlayContext,
				contextProvider );

		UndoActions.installActionBindings( viewerFrame.getKeybindings(), model, keyconf );
		EditBehaviours.installActionBindings( viewerFrame.getTriggerbindings(), keyconf, viewGraph, tracksOverlay, model );
		EditSpecialBehaviours.installActionBindings( viewerFrame.getTriggerbindings(), keyconf, viewerFrame.getViewerPanel(), viewGraph, tracksOverlay, model );
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
		renderSettings.addUpdateListener( new RenderSettings.UpdateListener()
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

		bdvWindow = new WindowManager.BdvWindow( viewerFrame, contextProvider );
	}

	public WindowManager.BdvWindow getBdvWindow()
	{
		return bdvWindow;
	}
}
