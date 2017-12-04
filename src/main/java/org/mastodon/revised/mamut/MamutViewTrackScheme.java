package org.mastodon.revised.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.viewMenu;

import javax.swing.ActionMap;

import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.ModelGraphTrackSchemeProperties;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.trackscheme.TrackSchemeContextListener;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.display.AbstractTrackSchemeOverlay.TrackSchemeOverlayFactory;
import org.mastodon.revised.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.revised.trackscheme.display.ToggleLinkBehaviour;
import org.mastodon.revised.trackscheme.display.TrackSchemeFrame;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.style.DefaultTrackSchemeOverlay;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle.UpdateListener;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.views.context.ContextChooser;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

class MamutViewTrackScheme extends MamutView< TrackSchemeGraph< Spot, Link >, TrackSchemeVertex, TrackSchemeEdge >
{
	private final ContextChooser< Spot > contextChooser;

	public MamutViewTrackScheme( final MamutAppModel appModel )
	{
		super( appModel,
				new TrackSchemeGraph<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						new ModelGraphTrackSchemeProperties( appModel.getModel().getGraph() ),
						appModel.getModel().getGraph().getLock() ),
				new String[] { "ts" } );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener<>( viewGraph );
		contextChooser = new ContextChooser<>( contextListener );

		final InputTriggerConfig keyconf = appModel.getKeyConfig();
		final KeyPressedManager keyPressedManager = appModel.getKeyPressedManager();
		final Model model = appModel.getModel();
		final ModelGraph modelGraph = model.getGraph();

		/*
		 * show TrackSchemeFrame
		 */
		final TrackSchemeStyle forwardDefaultStyle = appModel.getTrackSchemeStyleManager().getForwardDefaultStyle();
		final TrackSchemeOverlayFactory overlayFactory = ( graph, highlight, focus, options ) -> new DefaultTrackSchemeOverlay(
				graph, highlight, focus, options,
				forwardDefaultStyle );

		final TrackSchemeOptions options = TrackSchemeOptions.options()
				.inputTriggerConfig( keyconf )
				.shareKeyPressedEvents( keyPressedManager )
				.trackSchemeOverlayFactory( overlayFactory );
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				viewGraph,
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

		final UpdateListener updateListener = () -> frame.getTrackschemePanel().repaint();
		forwardDefaultStyle.addUpdateListener( updateListener );
		onClose( () -> forwardDefaultStyle.removeUpdateListener( updateListener ) );

		setFrame( frame );
		frame.setVisible( true );

		MastodonFrameViewActions.installActionBindings( viewActions, this );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				viewMenu(
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL )
				),
				editMenu(
						item( UndoActions.UNDO ),
						item( UndoActions.REDO ),
						separator(),
						item( SelectionActions.DELETE_SELECTION ),
						item( SelectionActions.SELECT_WHOLE_TRACK ),
						item( SelectionActions.SELECT_TRACK_DOWNWARD ),
						item( SelectionActions.SELECT_TRACK_UPWARD )
				)
		);

		HighlightBehaviours.installActionBindings( viewBehaviours, viewGraph, viewGraph.getLock(), viewGraph, highlightModel, model );
		ToggleLinkBehaviour.installActionBindings( viewBehaviours, frame.getTrackschemePanel(),	viewGraph, viewGraph.getLock(),	viewGraph, model );
		EditFocusVertexLabelAction.installActionBindings( viewActions, frame.getTrackschemePanel(), focusModel,	model );

		frame.getTrackschemePanel().repaint();
	}

	public ContextChooser< Spot > getContextChooser()
	{
		return contextChooser;
	}
}
