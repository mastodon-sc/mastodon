package org.mastodon.mamut;

import static org.mastodon.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.TRACKSCHEME_TRANSFORM_KEY;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchEdge;
import org.mastodon.mamut.model.branch.BranchVertex;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
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

public class MamutViewBranchTrackScheme extends MamutBranchView< TrackSchemeGraph< BranchVertex, BranchEdge >, TrackSchemeVertex, TrackSchemeEdge >

{

	public MamutViewBranchTrackScheme( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewBranchTrackScheme( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, createViewGraph( appModel ), new String[] { KeyConfigContexts.TRACKSCHEME } );

		// TrackScheme options.
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

		// Show TrackSchemeFrame.
		final Model model = appModel.getModel();
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				viewGraph,
				highlightModel,
				focusModel,
				timepointModel,
				selectionModel,
				navigationHandler,
				model,
				groupHandle,
				null,
				options );
		setFrame( frame );

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

		// Behaviors.
		final ReentrantReadWriteLock lock = model.getGraph().getLock();
		HighlightBehaviours.install( viewBehaviours, viewGraph, lock, viewGraph, highlightModel, model );
		ToggleLinkBehaviour.install( viewBehaviours, frame.getTrackschemePanel(), viewGraph, lock, viewGraph, model );

		// Actions.
		EditFocusVertexLabelAction.install( viewActions, frame.getTrackschemePanel(), focusModel, model );
		FocusActions.install( viewActions, viewGraph, lock, focusModel, selectionModel );
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


	private static TrackSchemeGraph< BranchVertex, BranchEdge > createViewGraph( final MamutAppModel appModel )
	{
		final Model model = appModel.getModel();
		final ModelBranchGraph graph = model.getBranchGraph();
		final GraphIdBimap< BranchVertex, BranchEdge > idmap = graph.getGraphIdBimap();
		final ModelGraphProperties< BranchVertex, BranchEdge > properties = new DefaultModelGraphProperties<>();
		final TrackSchemeGraph< BranchVertex, BranchEdge > trackSchemeGraph =
				new TrackSchemeGraph< BranchVertex, BranchEdge >( graph, idmap, properties );
		return trackSchemeGraph;
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
