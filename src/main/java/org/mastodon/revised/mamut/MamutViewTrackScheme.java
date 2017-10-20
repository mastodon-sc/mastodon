package org.mastodon.revised.mamut;

import bdv.tools.ToggleDialogAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;
import org.mastodon.revised.context.ContextChooser;
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
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

class MamutViewTrackScheme extends MamutView< TrackSchemeGraph< Spot, Link >, TrackSchemeVertex, TrackSchemeEdge >
{
	private final WindowManager.TsWindow tsWindow;

	public MamutViewTrackScheme( final MamutAppModel appModel )
	{
		super( appModel,
				new TrackSchemeGraph<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						new DefaultModelGraphProperties<>() ) );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener<>( viewGraph );
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
				viewGraph,
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

		tsWindow = new WindowManager.TsWindow( frame, contextChooser );
		frame.getTrackschemePanel().repaint();
	}

	public WindowManager.TsWindow getTsWindow()
	{
		return tsWindow;
	}
}
