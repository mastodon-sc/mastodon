package org.mastodon.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.branchColorMenu;
import static org.mastodon.mamut.MamutMenuBuilder.colorbarMenu;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;
import static org.mastodon.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.TRACKSCHEME_TRANSFORM_KEY;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.mastodon.app.ui.BranchGraphSyncButton;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.BranchTrackSchemeRootsModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.RootsModel;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.trackscheme.LineageTreeLayout;
import org.mastodon.views.trackscheme.LongEdgesLineageTreeLayout;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.display.BranchTrackSchemeEditLabelAction;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;
import org.mastodon.views.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.views.trackscheme.display.PaintBranchGraph;
import org.mastodon.views.trackscheme.display.PaintDecorations;
import org.mastodon.views.trackscheme.display.ShowSelectedTracksActions;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;
import org.mastodon.views.trackscheme.display.TrackSchemeOptions;
import org.mastodon.views.trackscheme.display.TrackSchemeOverlay;
import org.mastodon.views.trackscheme.display.TrackSchemeOverlay.TrackSchemeOverlayFactory;
import org.mastodon.views.trackscheme.display.TrackSchemeZoom;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyle;
import org.mastodon.views.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;

public class MamutBranchViewTrackScheme extends MamutBranchView< TrackSchemeGraph< BranchSpot, BranchLink >, TrackSchemeVertex, TrackSchemeEdge >
{

	private final ColorBarOverlay colorBarOverlay;

	private final ColoringModel coloringModel;

	public MamutBranchViewTrackScheme( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutBranchViewTrackScheme( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		this( appModel, guiState, new BranchTimeTrackSchemeFactory(), new BranchTrackSchemeOverlayFactory(), LongEdgesLineageTreeLayout::new );
	}

	protected MamutBranchViewTrackScheme(
			final MamutAppModel appModel,
			final Map< String, Object > guiState,
			final BranchTrackSchemeFactory trackSchemeGraphFactory,
			final TrackSchemeOverlayFactory overlayFactory,
			final LineageTreeLayout.LineageTreeLayoutFactory layoutFactory )
	{
		super( appModel, trackSchemeGraphFactory.createViewGraph( appModel ), new String[] { KeyConfigContexts.TRACKSCHEME } );

		// TrackScheme options.
		final GraphColorGeneratorAdapter< BranchSpot, BranchLink, TrackSchemeVertex, TrackSchemeEdge > coloringAdapter =
				new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );
		final TrackSchemeStyle forwardDefaultStyle = appModel.getTrackSchemeStyleManager().getForwardDefaultStyle();
		final TrackSchemeOptions options = TrackSchemeOptions.options()
				.trackSchemeOverlayFactory( overlayFactory )
				.lineageTreeLayoutFactory( layoutFactory )
				.style( forwardDefaultStyle )
				.graphColorGenerator( coloringAdapter );

		// Restore position
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos && pos.length == 4 )
			options
					.x( pos[ 0 ] )
					.y( pos[ 1 ] )
					.width( pos[ 2 ] )
					.height( pos[ 3 ] );

		// Restore group handle.
		MamutView.restoreGroupHandle( groupHandle, guiState );

		// Show TrackSchemeFrame.
		final Model model = appModel.getModel();
		final AutoNavigateFocusModel< TrackSchemeVertex, TrackSchemeEdge > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );
		final RootsModel< TrackSchemeVertex > rootsModel = new BranchTrackSchemeRootsModel( model.getGraph(), model.getBranchGraph(), viewGraph);
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				viewGraph,
				highlightModel,
				navigateFocusModel,
				timepointModel,
				selectionModel,
				rootsModel,
				navigationHandler,
				model,
				groupHandle,
				null,
				options );
		frame.setTitle( "TrackScheme Branch" );
		setFrame( frame );

		// Restore settings panel visibility.
		MamutView.restoreSettingsPanelVisibility( frame, guiState );

		// Default location.
		if ( null == pos || pos.length != 4 )
			frame.setLocationRelativeTo( null );

		final TrackSchemeStyle.UpdateListener updateListener = () -> frame.getTrackschemePanel().repaint();
		forwardDefaultStyle.updateListeners().add( updateListener );

		// Transform.
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( TRACKSCHEME_TRANSFORM_KEY );
		if ( null != tLoaded )
			frame.getTrackschemePanel().getScreenTransform().set( tLoaded );

		// Regen branch graph.
		frame.getSettingsPanel().add( new BranchGraphSyncButton( appModel.getBranchGraphSync() ) );

		// Search vertex label.
		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, frame.getTrackschemePanel() );
		frame.getSettingsPanel().add( searchPanel );

		// Actions.
		BranchGraphUndoActions.install( viewActions, model, appModel.getBranchGraphSync() );
		MastodonFrameViewActions.install( viewActions, this );
		final ReentrantReadWriteLock lock = model.getGraph().getLock();
		BranchTrackSchemeEditLabelAction.install( viewActions, frame.getTrackschemePanel(), focusModel, model, model.getBranchGraph() );
		FocusActions.install( viewActions, viewGraph, lock, focusModel, selectionModel );
		TrackSchemeZoom.install( viewBehaviours, frame.getTrackschemePanel() );
		EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), model.getTagSetModel(), appModel.getSelectionModel(), lock, frame.getTrackschemePanel(), frame.getTrackschemePanel().getDisplay(), model );
		ShowSelectedTracksActions.install( viewActions, viewGraph, selectionModel, rootsModel, frame.getTrackschemePanel() );

		frame.getTrackschemePanel().getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		frame.getTrackschemePanel().getNavigationBehaviours().install( viewBehaviours );
		frame.getTrackschemePanel().getTransformEventHandler().install( viewBehaviours );

		// Forward graph change event to a graphRebuilt event for branch graph.
		viewGraph.graphChangeListeners().remove( frame.getTrackschemePanel() );
		viewGraph.graphChangeListeners().add( () -> {
			viewGraph.getLock().writeLock().lock();
			try
			{
				viewGraph.graphRebuilt();
			}
			finally
			{
				viewGraph.getLock().writeLock().unlock();
			}
			frame.getTrackschemePanel().graphChanged();
		} );

		// Menus.
		final ViewMenu menu = new ViewMenu( frame.getJMenuBar(), appModel.getKeymap(), keyConfigContexts );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final JMenuHandle coloringMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		final JMenuHandle colorbarMenuHandle = new JMenuHandle();

		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				viewMenu(
						branchColorMenu( coloringMenuHandle ),
						colorbarMenu( colorbarMenuHandle ),
						separator(),
						item( ShowSelectedTracksActions.SHOW_TRACK_DOWNWARD ),
						item( ShowSelectedTracksActions.SHOW_SELECTED_TRACKS ),
						item( ShowSelectedTracksActions.SHOW_ALL_TRACKS ),
						separator(),
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL ) ),
				editMenu(
						item( BranchGraphUndoActions.UNDO ),
						item( BranchGraphUndoActions.REDO ),
						separator(),
						item( SelectionActions.SELECT_WHOLE_TRACK ),
						item( SelectionActions.SELECT_TRACK_DOWNWARD ),
						item( SelectionActions.SELECT_TRACK_UPWARD ),
						separator(),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_CHILD ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_PARENT ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_LEFT ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_RIGHT ),
						separator(),
						item( TrackSchemeNavigationActions.NAVIGATE_CHILD ),
						item( TrackSchemeNavigationActions.NAVIGATE_PARENT ),
						item( TrackSchemeNavigationActions.NAVIGATE_LEFT ),
						item( TrackSchemeNavigationActions.NAVIGATE_RIGHT ),
						separator(),
						item( EditFocusVertexLabelAction.EDIT_FOCUS_LABEL ),
						tagSetMenu( tagSetMenuHandle ) ) );
		appModel.getPlugins().addMenus( menu );

		coloringModel = registerBranchColoring( coloringAdapter, coloringMenuHandle,
				() -> frame.getTrackschemePanel().entitiesAttributesChanged() );
		colorBarOverlay = new ColorBarOverlay( coloringModel, () -> frame.getTrackschemePanel().getBackground() );
		frame.getTrackschemePanel().getOffsetHeaders().listeners().add( ( w, h ) -> colorBarOverlay.setInsets( h + 15, w + 15, 15, 15 ) );
		registerColorbarOverlay( colorBarOverlay, colorbarMenuHandle, () -> frame.getTrackschemePanel().repaint() );

		// Listen to user changing the tag-set menu.
		registerTagSetMenu( tagSetMenuHandle, () -> frame.getTrackschemePanel().entitiesAttributesChanged() );

		// Listen to vertex labels being changed.
		model.getGraph().addVertexLabelListener( v -> frame.getTrackschemePanel().entitiesAttributesChanged() );

		// Restore colorbar state.
		MamutView.restoreColorbarState( colorBarOverlay, guiState );
		frame.getTrackschemePanel().getDisplay().overlays().add( colorBarOverlay );

		// Restore coloring.
		MamutView.restoreColoring( coloringModel, guiState );

		// Time range and display refresh.
		frame.getTrackschemePanel().setTimepointRange( appModel.getMinTimepoint(), appModel.getMaxTimepoint() );
		frame.getTrackschemePanel().graphChanged();

		frame.setVisible( true );
		frame.getTrackschemePanel().repaint();

		/*
		 * Give focus to the display so that it can receive key presses
		 * immediately.
		 */
		frame.getTrackschemePanel().getDisplay().requestFocusInWindow();

		/*
		 * Automatically regenerate branch graph, if it never has been generated
		 * before
		 */
		if ( !appModel.getBranchGraphSync().isUptodate() )
		{
			new Thread( () -> {
				appModel.getBranchGraphSync().sync();
				frame.getTrackschemePanel().getTransformEventHandler().zoomOutFully();
			} ).start();
		}
	}

	@Override
	public TrackSchemeFrame getFrame()
	{
		return ( TrackSchemeFrame ) super.getFrame();
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

	/**
	 * Interface for classes that will generate a TrackScheme graph, based on
	 * the branch graph, to be displayed in this view. This offers flexibility
	 * as to how TrackScheme interprets the Y coordinates.
	 */
	public static interface BranchTrackSchemeFactory
	{
		public TrackSchemeGraph< BranchSpot, BranchLink > createViewGraph( final MamutAppModel appModel );
	}

	/**
	 * A {@link BranchTrackSchemeFactory} that returns a TrackScheme graph where
	 * the Y coordinates of nodes are taken from the time-point they belong to.
	 */
	public static class BranchTimeTrackSchemeFactory implements BranchTrackSchemeFactory
	{

		@Override
		public TrackSchemeGraph< BranchSpot, BranchLink > createViewGraph( final MamutAppModel appModel )
		{
			final Model model = appModel.getModel();
			final ModelBranchGraph graph = model.getBranchGraph();
			final GraphIdBimap< BranchSpot, BranchLink > idmap = graph.getGraphIdBimap();
			final ModelGraphProperties< BranchSpot, BranchLink > properties = new DefaultModelGraphProperties<>();
			final TrackSchemeGraph< BranchSpot, BranchLink > trackSchemeGraph =
					new TrackSchemeGraph<>( graph, idmap, properties );
			return trackSchemeGraph;
		}
	}

	ColoringModel getColoringModel()
	{
		return coloringModel;
	}

	ColorBarOverlay getColorBarOverlay()
	{
		return colorBarOverlay;
	}
}
