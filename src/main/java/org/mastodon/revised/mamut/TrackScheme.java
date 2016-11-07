package org.mastodon.revised.mamut;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.revised.context.ContextChooser;
import org.mastodon.revised.mamut.WindowManager.TsWindow;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.trackscheme.DefaultModelFocusProperties;
import org.mastodon.revised.trackscheme.DefaultModelGraphProperties;
import org.mastodon.revised.trackscheme.DefaultModelHighlightProperties;
import org.mastodon.revised.trackscheme.DefaultModelNavigationProperties;
import org.mastodon.revised.trackscheme.DefaultModelScalarFeaturesProperties;
import org.mastodon.revised.trackscheme.DefaultModelSelectionProperties;
import org.mastodon.revised.trackscheme.ModelFocusProperties;
import org.mastodon.revised.trackscheme.ModelHighlightProperties;
import org.mastodon.revised.trackscheme.ModelNavigationProperties;
import org.mastodon.revised.trackscheme.ModelScalarFeaturesProperties;
import org.mastodon.revised.trackscheme.ModelSelectionProperties;
import org.mastodon.revised.trackscheme.TrackSchemeContextListener;
import org.mastodon.revised.trackscheme.TrackSchemeFeatures;
import org.mastodon.revised.trackscheme.TrackSchemeFocus;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeHighlight;
import org.mastodon.revised.trackscheme.TrackSchemeNavigation;
import org.mastodon.revised.trackscheme.TrackSchemeSelection;
import org.mastodon.revised.trackscheme.display.TrackSchemeEditBehaviours;
import org.mastodon.revised.trackscheme.display.TrackSchemeFrame;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleChooser;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.ui.grouping.GroupHandle;
import org.mastodon.revised.ui.grouping.GroupManager;
import org.mastodon.revised.ui.selection.FocusModel;
import org.mastodon.revised.ui.selection.HighlightModel;
import org.mastodon.revised.ui.selection.NavigationHandler;
import org.mastodon.revised.ui.selection.Selection;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.tools.ToggleDialogAction;

public class TrackScheme
{

	public static final TsWindow launch( final Model model )
	{

		int minTimepoint = Integer.MAX_VALUE;
		int maxTimepoint = Integer.MIN_VALUE;

		for ( final Spot spot : model.getGraph().vertices() )
		{
			final int timepoint = spot.getTimepoint();
			if ( timepoint > maxTimepoint )
				maxTimepoint = timepoint;
			if ( timepoint < minTimepoint )
				minTimepoint = timepoint;
		}

		return launch( model, minTimepoint, maxTimepoint );
	}

	public static final TsWindow launch( final Model model, final int minTimepoint, final int maxTimepoint )
	{

		final ListenableReadOnlyGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();
		final Selection< Spot, Link > selection = new Selection<>( graph, idmap );
		final HighlightModel< Spot, Link > highlightModel = new HighlightModel<>( idmap );
		final FocusModel< Spot, Link > focusModel = new FocusModel<>( idmap );
		final GroupHandle groupHandle = new GroupManager().createGroupHandle();
		final InputTriggerConfig keyconf = MainWindow.getInputTriggerConfig();
		return launch( model, selection, highlightModel, focusModel, groupHandle, keyconf, minTimepoint, maxTimepoint );
	}

	public static final TsWindow launch(
			final Model model,
			final Selection< Spot, Link > selection,
			final HighlightModel< Spot, Link > highlightModel,
			final FocusModel< Spot, Link > focusModel,
			final GroupHandle groupHandle,
			final InputTriggerConfig keyconf,
			final int minTimepoint,
			final int maxTimepoint )
	{

		final ListenableReadOnlyGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();

		/*
		 * TrackSchemeGraph listening to model
		 */
		final DefaultModelGraphProperties< Spot, Link > properties = new DefaultModelGraphProperties<>( graph, idmap, selection );
		final TrackSchemeGraph< Spot, Link > trackSchemeGraph = new TrackSchemeGraph<>( graph, idmap, properties );

		/*
		 * TrackSchemeHighlight wrapping HighlightModel
		 */
		final ModelHighlightProperties highlightProperties = new DefaultModelHighlightProperties<>( graph, idmap, highlightModel );
		final TrackSchemeHighlight trackSchemeHighlight = new TrackSchemeHighlight( highlightProperties, trackSchemeGraph );

		/*
		 * TrackScheme selection
		 */
		final ModelSelectionProperties selectionProperties = new DefaultModelSelectionProperties<>( graph, idmap, selection );
		final TrackSchemeSelection trackSchemeSelection = new TrackSchemeSelection( selectionProperties );

		/*
		 * TrackScheme navigation
		 */
		final NavigationHandler< Spot, Link > navigationHandler = new NavigationHandler<>( groupHandle );
		final ModelNavigationProperties navigationProperties = new DefaultModelNavigationProperties<>( graph, idmap, navigationHandler );
		final TrackSchemeNavigation trackSchemeNavigation = new TrackSchemeNavigation( navigationProperties, trackSchemeGraph );

		/*
		 * TrackScheme focus
		 */
		final ModelFocusProperties focusProperties = new DefaultModelFocusProperties<>( graph, idmap, focusModel );
		final TrackSchemeFocus trackSchemeFocus = new TrackSchemeFocus( focusProperties, trackSchemeGraph );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener<>(
				idmap,
				trackSchemeGraph );
		final ContextChooser< Spot > contextChooser = new ContextChooser<>( contextListener );

		/*
		 * TrackScheme features.
		 */
		final ModelScalarFeaturesProperties featureProps = new DefaultModelScalarFeaturesProperties<>( graph, idmap );
		final TrackSchemeFeatures trackSchemeFeatures = new TrackSchemeFeatures( featureProps, trackSchemeGraph );

		/*
		 * show TrackSchemeFrame
		 */
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				trackSchemeGraph,
				trackSchemeHighlight,
				trackSchemeFocus,
				trackSchemeSelection,
				trackSchemeNavigation,
				trackSchemeFeatures,
				model,
				groupHandle,
				contextChooser,
				TrackSchemeOptions.options().inputTriggerConfig( keyconf ) );
		frame.getTrackschemePanel().setTimepointRange( minTimepoint, maxTimepoint );
		frame.getTrackschemePanel().graphChanged();
		contextListener.setContextListener( frame.getTrackschemePanel() );
		frame.setVisible( true );

		UndoActions.installActionBindings( frame.getKeybindings(), model, keyconf );
		HighlightBehaviours.installActionBindings(
				frame.getTriggerbindings(),
				keyconf,
				new String[] { "ts" },
				model.getGraph(),
				model.getGraph(),
				highlightModel,
				model );
		SelectionActions.installActionBindings(
				frame.getKeybindings(),
				keyconf,
				new String[] { "ts" },
				model.getGraph(),
				model.getGraph(),
				selection,
				model );
		TrackSchemeEditBehaviours.installActionBindings(
				frame.getTriggerbindings(),
				keyconf,
				frame.getTrackschemePanel(),
				trackSchemeGraph,
				frame.getTrackschemePanel().getGraphOverlay(),
				model.getGraph(),
				model.getGraph().getGraphIdBimap(),
				model );

		// TrackSchemeStyleDialog triggered by "R"
		final String TRACK_SCHEME_STYLE_SETTINGS = "render settings";
		final TrackSchemeStyleChooser styleChooser = new TrackSchemeStyleChooser(
				frame,
				frame.getTrackschemePanel() );
		final JDialog styleDialog = styleChooser.getDialog();
		final ActionMap actionMap = new ActionMap();
		new ToggleDialogAction( TRACK_SCHEME_STYLE_SETTINGS, styleDialog ).put( actionMap );
		final InputMap inputMap = new InputMap();
		final KeyStrokeAdder a = keyconf.keyStrokeAdder( inputMap, "mamut" );
		a.put( TRACK_SCHEME_STYLE_SETTINGS, "R" );
		frame.getKeybindings().addActionMap( "mamut", actionMap );
		frame.getKeybindings().addInputMap( "mamut", inputMap );

		final TsWindow tsWindow = new TsWindow( frame, groupHandle, contextChooser );
		frame.getTrackschemePanel().repaint();
		return tsWindow;
	}

	private TrackScheme()
	{}
}