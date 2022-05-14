/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;
import static org.mastodon.mamut.MamutViewStateSerialization.BRANCH_GRAPH;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_DISPLAYED;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_ELEMENT;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_SELECTION_ONLY;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_VISIBLE_POS;

import java.awt.Point;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.SpotPool;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphNavigationHandlerAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.model.branch.BranchGraphTagSetAdapter;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringMenu;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.ColoringModelBranchGraph;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.TagSetGraphColorGenerator;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.table.FeatureTagTablePanel;
import org.mastodon.views.table.TableViewActions;
import org.mastodon.views.table.TableViewFrameBuilder;
import org.mastodon.views.table.TableViewFrameBuilder.MyTableViewFrame;

import bdv.BigDataViewerActions;

public class MamutViewTable extends MamutView< ViewGraph< Spot, Link, Spot, Link >, Spot, Link >
{

	public static String csvExportPath = null;

	private static final String[] CONTEXTS = new String[] { KeyConfigContexts.TABLE };

	private final ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel;

	private final ColoringModel branchColoringModel;

	private final boolean selectionTable;

	public MamutViewTable( final MamutAppModel appModel, final boolean selectionOnly )
	{
		this( appModel, Collections.singletonMap(
				TABLE_SELECTION_ONLY, Boolean.valueOf( selectionOnly ) ) );
	}

	public MamutViewTable( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, createViewGraph( appModel ), CONTEXTS );

		// Data model.
		final Model model = appModel.getModel();
		final FeatureModel featureModel = model.getFeatureModel();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();

		// Core graph coloring.
		final GraphColorGeneratorAdapter< Spot, Link, Spot, Link > coloringAdapter = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );

		// Branch-graph coloring.
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		final ViewGraph< BranchSpot, BranchLink, BranchSpot, BranchLink > viewBranchGraph = IdentityViewGraph.wrap( branchGraph, branchGraph.getGraphIdBimap() );
		final GraphColorGeneratorAdapter< BranchSpot, BranchLink, BranchSpot, BranchLink > branchColoringAdapter = new GraphColorGeneratorAdapter<>( viewBranchGraph.getVertexMap(), viewBranchGraph.getEdgeMap() );

		// Selection table?
		this.selectionTable = ( boolean ) guiState.getOrDefault( TABLE_SELECTION_ONLY, false );

		// Create tables.
		final TableViewFrameBuilder builder = new TableViewFrameBuilder();
		final MyTableViewFrame frame = builder
				.groupHandle( groupHandle )
				.undo( model )
				.addGraph( model.getGraph() )
					.selectionModel( selectionModel )
					.highlightModel( highlightModel )
					.focusModel( focusModel )
					.featureModel( featureModel )
					.tagSetModel( tagSetModel )
					.navigationHandler( navigationHandler )
					.coloring( coloringAdapter )
					.vertexLabelGetter( s -> s.getLabel() )
					.vertexLabelSetter( ( s, label ) -> s.setLabel( label ) )
					.listenToContext( true )
					.selectionTable( selectionTable )
					.done()
				.addGraph( model.getBranchGraph() )
					.vertexLabelGetter( s -> s.getLabel() )
					.vertexLabelSetter( ( s, label ) -> s.setLabel( label ) )
					.featureModel( featureModel )
					.tagSetModel( branchTagSetModel( appModel ) )
					.selectionModel( branchSelectionModel( appModel ) )
					.highlightModel( branchHighlightModel( appModel ) )
					.coloring( branchColoringAdapter )
					.focusModel( branchFocusfocusModel( appModel ) )
					.navigationHandler( branchGraphNavigation( appModel, navigationHandler ) )
					.done()
				.get();
		setFrame( frame );

		// Restore position.
		restoreFramePosition( frame, guiState );

		// Restore group handle.
		restoreGroupHandle( groupHandle, guiState );

		// Restore settings panel visibility.
		restoreSettingsPanelVisibility( frame, guiState );

		// Table actions.
		MastodonFrameViewActions.install( viewActions, this );
		TableViewActions.install( viewActions, frame );

		// Search panels.
		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, frame.getCurrentlyDisplayedTable() );
		frame.getSettingsPanel().add( searchPanel );

		// Menus.
		final ViewMenu menu = new ViewMenu( frame.getJMenuBar(), appModel.getKeymap(), CONTEXTS );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();
		final JMenuHandle colorMenuHandle = new JMenuHandle();
		final JMenuHandle colorBranchMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				viewMenu(
						MamutMenuBuilder.colorMenu( colorMenuHandle ),
						ViewMenuBuilder.menu( "Branch coloring", colorBranchMenuHandle ),
						separator(),
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL ) ),
				editMenu(
						item( UndoActions.UNDO ),
						item( UndoActions.REDO ),
						separator(),
						item( SelectionActions.DELETE_SELECTION ),
						item( SelectionActions.SELECT_WHOLE_TRACK ),
						item( SelectionActions.SELECT_TRACK_DOWNWARD ),
						item( SelectionActions.SELECT_TRACK_UPWARD ),
						separator(),
						tagSetMenu( tagSetMenuHandle ) ),
				ViewMenuBuilder.menu( "Settings",
						item( BigDataViewerActions.BRIGHTNESS_SETTINGS ),
						item( BigDataViewerActions.VISIBILITY_AND_GROUPING ) ) );
		appModel.getPlugins().addMenus( menu );

		coloringModel = registerColoring( coloringAdapter, colorMenuHandle, () -> frame.repaint() );
		branchColoringModel = registerBranchColoring( appModel, branchColoringAdapter, colorBranchMenuHandle, () -> frame.repaint(), runOnClose );

		// Restore coloring.
		restoreColoring( coloringModel, guiState );

		// Restore branch-graph coloring.
		@SuppressWarnings( "unchecked" )
		final Map< String, Object > branchGraphGuiState = ( Map< String, Object > ) guiState.get( BRANCH_GRAPH );
		restoreColoring( branchColoringModel, branchGraphGuiState );

		/*
		 * Register a listener to vertex label property changes, will update the
		 * table-view when the label change.
		 */
		final SpotPool spotPool = ( SpotPool ) appModel.getModel().getGraph().vertices().getRefPool();
		final PropertyChangeListener< Spot > labelChangedRefresher = v -> frame.repaint();
		spotPool.labelProperty().propertyChangeListeners().add( labelChangedRefresher );
		onClose( () -> spotPool.labelProperty().propertyChangeListeners().remove( labelChangedRefresher ) );

		// Restore table visible rectangle and displayed table.
		final String displayedTableName = ( String ) guiState.getOrDefault( TABLE_DISPLAYED, "TableSpot" );
		final List< FeatureTagTablePanel< ? > > tables = frame.getTables();
		final List< String > names = frame.getTableNames();
		@SuppressWarnings( "unchecked" )
		final List< Map< String, Object > > list = ( List< Map< String, Object > > ) guiState.getOrDefault( TABLE_ELEMENT, Collections.emptyList() );
		for ( int i = 0; i < list.size(); i++ )
		{
			final String name = names.get( i );
			if ( name.equals( displayedTableName ) )
				frame.displayTable( name );

			final Map< String, Object > tableGuiState = list.get( i );
			final int[] viewPos = ( int[] ) tableGuiState.get( TABLE_VISIBLE_POS );
			if ( viewPos != null )
			{
				final FeatureTagTablePanel< ? > table = tables.get( i );
				table.getScrollPane().getViewport().setViewPosition( new Point(
						viewPos[ 0 ],
						viewPos[ 1 ] ) );
			}
		}

		/*
		 * Show table.
		 */

		frame.setVisible( true );
	}

	private static final ColoringModel registerBranchColoring(
			final MamutAppModel appModel,
			final GraphColorGeneratorAdapter< BranchSpot, BranchLink, BranchSpot, BranchLink > colorGeneratorAdapter,
			final JMenuHandle menuHandle,
			final Runnable refresh,
			final List< Runnable > runOnClose )
	{
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final FeatureModel featureModel = appModel.getModel().getFeatureModel();
		final FeatureColorModeManager featureColorModeManager = appModel.getFeatureColorModeManager();
		final ColoringModelBranchGraph< ?, ? > coloringModel = new ColoringModelBranchGraph<>( tagSetModel, featureColorModeManager, featureModel );
		final ColoringMenu coloringMenu = new ColoringMenu( menuHandle.getMenu(), coloringModel );

		tagSetModel.listeners().add( coloringModel );
		runOnClose.add( () -> tagSetModel.listeners().remove( coloringModel ) );
		tagSetModel.listeners().add( coloringMenu );
		runOnClose.add( () -> tagSetModel.listeners().remove( coloringMenu ) );

		featureColorModeManager.listeners().add( coloringModel );
		runOnClose.add( () -> featureColorModeManager.listeners().remove( coloringModel ) );
		featureColorModeManager.listeners().add( coloringMenu );
		runOnClose.add( () -> featureColorModeManager.listeners().remove( coloringMenu ) );

		featureModel.listeners().add( coloringMenu );
		runOnClose.add( () -> featureModel.listeners().remove( coloringMenu ) );

		@SuppressWarnings( "unchecked" )
		final ColoringModelMain.ColoringChangedListener coloringChangedListener = () -> {
			if ( coloringModel.noColoring() )
				colorGeneratorAdapter.setColorGenerator( null );
			else if ( coloringModel.getTagSet() != null )
				colorGeneratorAdapter.setColorGenerator( new TagSetGraphColorGenerator<>( branchTagSetModel( appModel ), coloringModel.getTagSet() ) );
			else if ( coloringModel.getFeatureColorMode() != null )
				colorGeneratorAdapter.setColorGenerator( ( GraphColorGenerator< BranchSpot, BranchLink > ) coloringModel.getFeatureGraphColorGenerator() );
			refresh.run();
		};
		coloringModel.listeners().add( coloringChangedListener );

		return coloringModel;
	}

	private static TagSetModel< BranchSpot, BranchLink > branchTagSetModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final BranchGraphTagSetAdapter< Spot, Link, BranchSpot, BranchLink > branchGraphTagSetModel =
				new BranchGraphTagSetAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), tagSetModel );
		return branchGraphTagSetModel;
	}

	private static NavigationHandler< BranchSpot, BranchLink > branchGraphNavigation( final MamutAppModel appModel, final NavigationHandler< Spot, Link > navigationHandler )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final NavigationHandler< BranchSpot, BranchLink > branchGraphNavigation =
				new BranchGraphNavigationHandlerAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), navigationHandler );
		return branchGraphNavigation;
	}

	private static HighlightModel< BranchSpot, BranchLink > branchHighlightModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final HighlightModel< Spot, Link > graphHighlightModel = appModel.getHighlightModel();
		final HighlightModel< BranchSpot, BranchLink > branchHighlightModel =
				new BranchGraphHighlightAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphHighlightModel );
		return branchHighlightModel;
	}

	private static FocusModel< BranchSpot, BranchLink > branchFocusfocusModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final FocusModel< Spot, Link > graphFocusModel = appModel.getFocusModel();
		final FocusModel< BranchSpot, BranchLink > branchFocusfocusModel =
				new BranchGraphFocusAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphFocusModel );
		return branchFocusfocusModel;
	}

	private static SelectionModel< BranchSpot, BranchLink > branchSelectionModel( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final SelectionModel< Spot, Link > graphSelectionModel = appModel.getSelectionModel();
		final SelectionModel< BranchSpot, BranchLink > branchSelectionModel =
				new BranchGraphSelectionAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphSelectionModel );
		return branchSelectionModel;
	}

	@Override
	public MyTableViewFrame getFrame()
	{
		final ViewFrame f = super.getFrame();
		final MyTableViewFrame vf = ( MyTableViewFrame ) f;
		return vf;
	}

	@SuppressWarnings( "unchecked" )
	ContextChooser< Spot > getContextChooser()
	{
		/*
		 * We configured the table creator so that only the first table pair,
		 * for the core graph, has a context.
		 */
		return ( ContextChooser< Spot > ) getFrame().getContextChoosers().get( 0 );
	}

	/*
	 * De/serialization related methods.
	 */

	ColoringModelMain< Spot, Link, BranchSpot, BranchLink > getColoringModel()
	{
		return coloringModel;
	}

	ColoringModel getBranchColoringModel()
	{
		return branchColoringModel;
	}

	boolean isSelectionTable()
	{
		return selectionTable;
	}

	/*
	 * Functions.
	 */

	private static ViewGraph< Spot, Link, Spot, Link > createViewGraph( final MamutAppModel appModel )
	{
		return IdentityViewGraph.wrap( appModel.getModel().getGraph(), appModel.getModel().getGraphIdBimap() );
	}
}
