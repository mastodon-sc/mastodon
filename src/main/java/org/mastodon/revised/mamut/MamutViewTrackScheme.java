package org.mastodon.revised.mamut;

import javax.swing.ActionMap;
import javax.swing.JDialog;

import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.trackscheme.TrackSchemeContextListener;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.display.TrackSchemeEditBehaviours;
import org.mastodon.revised.trackscheme.display.TrackSchemeFrame;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.ui.TrackSchemeStyleChooser;
import org.mastodon.revised.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.util.ToggleDialogAction;
import org.mastodon.views.context.ContextChooser;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

class MamutViewTrackScheme extends MamutView< TrackSchemeGraph< Spot, Link >, TrackSchemeVertex, TrackSchemeEdge >
{
	private final ContextChooser< Spot > contextChooser;

	public MamutViewTrackScheme( final MamutAppModel appModel )
	{
		super( appModel,
				new TrackSchemeGraph<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						new DefaultModelGraphProperties<>(),
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
		final TrackSchemeOptions options = TrackSchemeOptions.options()
				.inputTriggerConfig( keyconf )
				.shareKeyPressedEvents( keyPressedManager );
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

		setFrame( frame );
		frame.setVisible( true );

		MastodonFrameViewActions.installActionBindings( viewActions, this );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		menu.addItem( "View", "Settings Toolbar", actionMap.get( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL ) );

		menu.addItem( "Edit", "Undo", actionMap.get( UndoActions.UNDO ) );
		menu.addItem( "Edit", "Redo", actionMap.get( UndoActions.REDO ) );
		menu.addSeparator( "Edit" );
		menu.addItem( "Edit", "Delete Selection", actionMap.get( SelectionActions.DELETE_SELECTION ) );
		menu.addItem( "Edit", "Select Whole Track", actionMap.get( SelectionActions.SELECT_WHOLE_TRACK ) );
		menu.addItem( "Edit", "Select Track Downward", actionMap.get( SelectionActions.SELECT_TRACK_DOWNWARD ) );
		menu.addItem( "Edit", "Select Track Upward", actionMap.get( SelectionActions.SELECT_TRACK_UPWARD ) );

		HighlightBehaviours.installActionBindings(
				viewBehaviours,
				modelGraph,
				modelGraph.getLock(),
				modelGraph,
				appModel.getHighlightModel(),
				model );
		TrackSchemeEditBehaviours.installActionBindings(
				frame.getTriggerbindings(),
				keyconf,
				frame.getTrackschemePanel(),
				viewGraph,
				frame.getTrackschemePanel().getGraphOverlay(),
				modelGraph,
				modelGraph.getGraphIdBimap(),
				model );

		// TODO revise
		// TrackSchemeStyleDialog triggered by "R"
		final String TRACK_SCHEME_STYLE_SETTINGS = "render settings";
		final TrackSchemeStyleChooser styleChooser = new TrackSchemeStyleChooser( frame, frame.getTrackschemePanel() );
		final JDialog styleDialog = styleChooser.getDialog();
		final Actions actions = new Actions( keyconf, "mamut" );
		actions.install( frame.getKeybindings(), "mamut" );
		actions.namedAction( new ToggleDialogAction( TRACK_SCHEME_STYLE_SETTINGS, styleDialog ), "R" );

		frame.getTrackschemePanel().repaint();
	}

	public ContextChooser< Spot > getContextChooser()
	{
		return contextChooser;
	}
}
