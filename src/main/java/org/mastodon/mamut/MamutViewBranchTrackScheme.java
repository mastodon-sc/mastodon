package org.mastodon.mamut;

import static org.mastodon.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.TRACKSCHEME_TRANSFORM_KEY;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.adapter.TimepointModelAdapter;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.grouping.GroupManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchEdge;
import org.mastodon.mamut.model.branch.BranchVertex;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphNavigationHandlerAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeEdgeBimap;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.TrackSchemeVertexBimap;
import org.mastodon.views.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.views.trackscheme.display.PaintBranchGraph;
import org.mastodon.views.trackscheme.display.PaintDecorations;
import org.mastodon.views.trackscheme.display.ToggleLinkBehaviour;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;
import org.mastodon.views.trackscheme.display.TrackSchemeOptions;
import org.mastodon.views.trackscheme.display.TrackSchemeOverlay;
import org.mastodon.views.trackscheme.display.TrackSchemeOverlay.TrackSchemeOverlayFactory;
import org.mastodon.views.trackscheme.display.TrackSchemeZoom;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyle;
import org.mastodon.views.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

public class MamutViewBranchTrackScheme
{

	public MamutViewBranchTrackScheme( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewBranchTrackScheme( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		final Model model = appModel.getModel();
		
		final ModelBranchGraph graph = model.getBranchGraph();
		final GraphIdBimap< BranchVertex, BranchEdge > idmap = graph.getGraphIdBimap();
		final ReentrantReadWriteLock lock = model.getGraph().getLock();

		/*
		 * TrackSchemeGraph listening to branch graph.
		 */
		final ModelGraphProperties< BranchVertex, BranchEdge > properties = new DefaultModelGraphProperties<>();
		final TrackSchemeGraph< BranchVertex, BranchEdge > trackSchemeGraph =
				new TrackSchemeGraph< BranchVertex, BranchEdge >( graph, idmap, properties );
		final RefBimap< BranchVertex, TrackSchemeVertex > vertexMap = new TrackSchemeVertexBimap<>( trackSchemeGraph );
		final RefBimap< BranchEdge, TrackSchemeEdge > edgeMap = new TrackSchemeEdgeBimap<>( trackSchemeGraph );

		/*
		 * Highlight model for branch graph.
		 */
		final HighlightModel< Spot, Link > highlightModel = appModel.getHighlightModel();
		final HighlightModel< BranchVertex, BranchEdge > branchGraphHighlight =
				new BranchGraphHighlightAdapter<>( graph, model.getGraph(), highlightModel );
		final HighlightModel< TrackSchemeVertex, TrackSchemeEdge > trackSchemeHighlight =
				new HighlightModelAdapter<>( branchGraphHighlight, vertexMap, edgeMap );

		/*
		 * Selection model for branch graph.
		 */
		final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();
		final SelectionModel< BranchVertex, BranchEdge > branchGraphSelection =
				new BranchGraphSelectionAdapter<>( graph, model.getGraph(), selectionModel );
		final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > trackSchemeSelection =
				new SelectionModelAdapter<>( branchGraphSelection, vertexMap, edgeMap );

		/*
		 * TrackScheme GroupHandle.
		 */
		final GroupManager groupManager = appModel.getGroupManager();
		final GroupHandle groupHandle = groupManager.createGroupHandle();

		/*
		 * Navigation model for branch graph.
		 */
		final NavigationHandler< Spot, Link > navigationHandler = groupHandle.getModel( appModel.NAVIGATION );
		final NavigationHandler< BranchVertex, BranchEdge > branchGraphNavigation =
				new BranchGraphNavigationHandlerAdapter<>( graph, model.getGraph(), navigationHandler );
		final NavigationHandler< TrackSchemeVertex, TrackSchemeEdge > trackSchemeNavigation =
				new NavigationHandlerAdapter<>( branchGraphNavigation, vertexMap, edgeMap );

		/*
		 * Timepoint navigation.
		 */

		final TimepointModelAdapter timepointModelAdapter = new TimepointModelAdapter( groupHandle.getModel( appModel.TIMEPOINT ) );

		/*
		 * Focus model for branch graph.
		 */
		final FocusModel< Spot, Link > focusModel = appModel.getFocusModel();
		final FocusModel< BranchVertex, BranchEdge > branchGraphFocus =
				new BranchGraphFocusAdapter<>( graph, model.getGraph(), focusModel );
		final FocusModel< TrackSchemeVertex, TrackSchemeEdge > trackSchemeFocus =
				new FocusModelAdapter<>( branchGraphFocus, vertexMap, edgeMap );

		// Navigate to focus.
		final AutoNavigateFocusModel< TrackSchemeVertex, TrackSchemeEdge > navigateFocusModel =
				new AutoNavigateFocusModel<>( trackSchemeFocus, trackSchemeNavigation );

		/*
		 * TrackScheme options.
		 */
		final TrackSchemeStyle forwardDefaultStyle = appModel.getTrackSchemeStyleManager().getForwardDefaultStyle();
		final TrackSchemeOptions options = TrackSchemeOptions.options()
				.trackSchemeOverlayFactory( new BranchTrackSchemeOverlayFactory() );
				

		// Restore position
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos && pos.length == 4 )
			options
					.x( pos[ 0 ] )
					.y( pos[ 1 ] )
					.width( pos[ 2 ] )
					.height( pos[ 3 ] );

		// Restore group handle.
		final Integer groupID = ( Integer ) guiState.get( GROUP_HANDLE_ID_KEY );
		if ( null != groupID )
			groupHandle.setGroupId( groupID.intValue() );

		/*
		 * Show TrackSchemeFrame.
		 */
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				trackSchemeGraph,
				trackSchemeHighlight,
				trackSchemeFocus,
				timepointModelAdapter,
				trackSchemeSelection,
				trackSchemeNavigation,
				model,
				groupHandle,
				null,
				options );

		// Restore settings panel visibility.
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );

		// Default location.
		if ( null == pos || pos.length != 4 )
			frame.setLocationRelativeTo( null );

		frame.getTrackschemePanel().setTimepointRange( appModel.getMinTimepoint(), appModel.getMaxTimepoint() );
		frame.getTrackschemePanel().graphChanged();

		final TrackSchemeStyle.UpdateListener updateListener = () -> frame.getTrackschemePanel().repaint();
		forwardDefaultStyle.updateListeners().add( updateListener );

		// Transform.
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( TRACKSCHEME_TRANSFORM_KEY );
		if ( null != tLoaded )
			frame.getTrackschemePanel().getScreenTransform().set( tLoaded );

		// Keymap.
		final Keymap keymap = appModel.getKeymap();
		final String[] keyConfigContexts = new String[] { KeyConfigContexts.TRACKSCHEME };

		// Actions.
		final Actions viewActions = new Actions( keymap.getConfig(), keyConfigContexts );
		viewActions.install( frame.getKeybindings(), "view" );

		// Behaviors.
		final Behaviours viewBehaviours = new Behaviours( keymap.getConfig(), keyConfigContexts );
		viewBehaviours.install( frame.getTriggerbindings(), "view" );

		HighlightBehaviours.install( viewBehaviours, graph, lock, trackSchemeGraph, branchGraphHighlight, model );
		ToggleLinkBehaviour.install( viewBehaviours, frame.getTrackschemePanel(), trackSchemeGraph, lock, trackSchemeGraph, model );

		EditFocusVertexLabelAction.install( viewActions, frame.getTrackschemePanel(), trackSchemeFocus, model );
		FocusActions.install( viewActions, trackSchemeGraph, lock, navigateFocusModel, trackSchemeSelection );
		TrackSchemeZoom.install( viewBehaviours, frame.getTrackschemePanel() );
		EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), model.getTagSetModel(), appModel.getSelectionModel(), lock, frame.getTrackschemePanel(), frame.getTrackschemePanel().getDisplay(), model );

		frame.getTrackschemePanel().getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		frame.getTrackschemePanel().getNavigationBehaviours().install( viewBehaviours );
		frame.getTrackschemePanel().getTransformEventHandler().install( viewBehaviours );

		frame.setVisible( true );
		frame.getTrackschemePanel().repaint();

		// Give focus to the display so that it can receive key presses immediately.
		frame.getTrackschemePanel().getDisplay().requestFocusInWindow();
	}


	public static class BranchTrackSchemeOverlayFactory extends TrackSchemeOverlayFactory
	{
		@Override
		public TrackSchemeOverlay create(
				final TrackSchemeGraph< ?, ? > graph,
				final HighlightModel< TrackSchemeVertex, TrackSchemeEdge > highlight,
				final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
				final TrackSchemeOptions options )
		{
			return new TrackSchemeOverlay( graph, highlight, focus, new PaintDecorations(), new PaintBranchGraph(), options );
		}
	}
}
