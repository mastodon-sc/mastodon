/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.views.table;

import static org.mastodon.app.MastodonIcons.TABLE_VIEW_ICON;
import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;

import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.AbstractMastodonFrameView2;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.UIModel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.UndoActions;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.SpotPool;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasBranchModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.model.branch.BranchGraphEdgeBimap;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.model.branch.BranchGraphTagSetAdapter;
import org.mastodon.model.branch.BranchGraphVertexBimap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringMenu;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.ColoringModelBranchGraph;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.HasColoringModel;
import org.mastodon.ui.coloring.TagSetGraphColorGenerator;
import org.mastodon.ui.coloring.TrackGraphColorGenerator;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.commandfinder.CommandFinder;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.table.TableViewActions;
import org.mastodon.views.table.TableViewFrameBuilder;
import org.mastodon.views.table.TableViewFrameBuilder.GraphTableBuilder;
import org.mastodon.views.table.TableViewFrameBuilder.MyTableViewFrame;

import bdv.BigDataViewerActions;

public class MastodonViewTable2<
			M extends MastodonModel< G, V, E >,
			G extends ListenableReadOnlyGraph< V, E >,
			V extends Vertex< E >,
			E extends Edge< V > >
		extends AbstractMastodonFrameView2< M, ViewGraph< V, E, V, E >, V, E, V, E >
		implements HasContextChooser< V >, HasColoringModel
{

	private static final int DEFAULT_WIDTH = 500;

	private static final int DEFAULT_HEIGHT = 300;

	public static String csvExportPath = null;

	private static final String[] CONTEXTS = new String[] { KeyConfigContexts.TABLE };

	public MastodonViewTable2( final M dataModel, final UIModel< ? > uiModel )
	{
		this( dataModel, uiModel, false );
	}

	protected MastodonViewTable2(
			final M dataModel,
			final UIModel< ? > uiModel,
			final boolean selectionTable )
	{
		super( dataModel, uiModel, createViewGraph( dataModel ), CONTEXTS );

		// Create tables.
		final TableViewFrameBuilder builder = new TableViewFrameBuilder()
				.groupHandle( groupHandle );
		if ( dataModel instanceof UndoPointMarker )
		{
			builder.undo( ( UndoPointMarker ) dataModel );
		}
		// Tables for core graph.
		final GraphTableBuilder< V, E > gtbCore = builder.addGraph( dataModel.getGraph() );
		gtbCore.selectionModel( selectionModel )
				.highlightModel( highlightModel )
				.focusModel( focusModel )
				.featureModel( dataModel.getFeatureModel() )
				.tagSetModel( dataModel.getTagSetModel() )
				.navigationHandler( navigationHandler )
				.coloring( coloringAdapter )
				.listenToContext( true )
				.selectionTable( selectionTable );
		// Check if the core vertices have labels.
		final V v = dataModel.getGraph().vertices().iterator().next();
		if ( v instanceof HasLabel )
		{
			gtbCore
					.vertexLabelSetter( ( s, label ) -> ( ( HasLabel ) s ).setLabel( label ) )
					.vertexLabelGetter( s -> ( ( HasLabel ) s ).getLabel() );

		}
		gtbCore.done();

		// Tables for branch graph.
		if ( dataModel instanceof HasBranchModel )
		{
			final HasBranchModel bm = ( HasBranchModel ) dataModel;
			final MastodonModel branchModel = bm.branchModel();
			final BranchGraph branchGraph = ( BranchGraph ) branchModel.getGraph();
			final GraphTableBuilder gtbBranch = builder.addGraph( branchGraph );
			// Do we have a label property?
			final Object bv = branchGraph.vertices().iterator().next();
			if ( bv instanceof HasLabel )
			{
				gtbBranch
						.vertexLabelGetter( s -> ( ( HasLabel ) s ).getLabel() )
						.vertexLabelSetter( ( s, label ) -> ( ( HasLabel ) s ).setLabel( label ) );
			}
			gtbBranch
					.featureModel( branchModel.getFeatureModel() )
					.tagSetModel( branchModel.getTagSetModel() )
					.selectionModel( branchModel.getSelectionModel() )
					.highlightModel( branchModel.getHighlightModel() ) // TODO: timepoint model
					.focusModel( branchModel.getFocusModel() )
					.navigationHandler( branchGraphNavigation( dataModel, navigationHandler ) )
					.coloring( branchColoringAdapter() )
					.selectionTable( selectionTable )
					.done();
		}
		builder.title(
				selectionTable ? "Selection table" : "Data table" )
				.x( -1 )
				.y( -1 )
				.width( DEFAULT_WIDTH )
				.height( DEFAULT_HEIGHT )
				.get();
		final MyTableViewFrame frame = builder.get();
		setFrame( frame );
		frame.setIconImages( TABLE_VIEW_ICON );

		// Search panels.
		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel,
				focusModel, frame.getCurrentlyDisplayedTable() );
		frame.getSettingsPanel().add( searchPanel );

		// Table actions.
		MastodonFrameViewActions.install( viewActions, this );
		TableViewActions.install( viewActions, frame );
		final CommandFinder cf = CommandFinder.build()
				.context( appModel.getContext() )
				.inputTriggerConfig( appModel.getKeymap().getConfig() )
				.keyConfigContexts( keyConfigContexts )
				.descriptionProvider( appModel.getWindowManager().getViewFactories().getCommandDescriptions() )
				.register( viewActions )
				.register( appModel.getModelActions() )
				.register( appModel.getProjectActions() )
				.register( appModel.getPlugins().getPluginActions() )
				.modificationListeners( appModel.getKeymap().updateListeners() )
				.parent( frame )
				.installOn( viewActions );
		cf.getDialog().setTitle( cf.getDialog().getTitle() + " - " + frame.getTitle() );

		// Menus
		final ViewMenu menu = new ViewMenu( frame.getJMenuBar(), projectModel.getKeymap(), CONTEXTS );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();
		final JMenuHandle colorMenuHandle = new JMenuHandle();
		final JMenuHandle colorBranchMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		MainWindow.addMenus( menu, actionMap );
		appModel.getWindowManager().addWindowMenu( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						menu( "Export",
								item( TableViewActions.EXPORT_TO_CSV ) ) ),
				viewMenu(
						MamutMenuBuilder.colorMenu( colorMenuHandle ),
						menu( "Branch coloring", colorBranchMenuHandle ),
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
						tagSetMenu( tagSetMenuHandle ),
						separator(),
						item( TableViewActions.EDIT_LABEL ),
						item( TableViewActions.TOGGLE_TAG ) ),
				menu( "Settings",
						item( BigDataViewerActions.BRIGHTNESS_SETTINGS ),
						item( BigDataViewerActions.VISIBILITY_AND_GROUPING ) ) );
		projectModel.getPlugins().addMenus( menu );

		coloringModel = registerColoring( coloringAdapter, colorMenuHandle, () -> frame.repaint() );
		branchColoringModel = registerBranchColoring( projectModel, branchColoringAdapter, colorBranchMenuHandle,
				() -> frame.repaint(), runOnClose );

		// Set the tag-set menu and listen to user selecting a tag in it.
		registerTagSetMenu( tagSetMenuHandle, () -> frame.repaint() );

		/*
		 * Register a listener to vertex label property changes, will update the
		 * table-view when the label change.
		 */
		final SpotPool spotPool = ( SpotPool ) projectModel.getModel().getGraph().vertices().getRefPool();
		final PropertyChangeListener< Spot > labelChangedRefresher = v -> frame.repaint();
		spotPool.labelProperty().propertyChangeListeners().add( labelChangedRefresher );
		onClose( () -> spotPool.labelProperty().propertyChangeListeners().remove( labelChangedRefresher ) );
	}

	private static final ColoringModel registerBranchColoring(
			final ProjectModel appModel,
			final GraphColorGeneratorAdapter< BranchSpot, BranchLink, BranchSpot, BranchLink > colorGeneratorAdapter,
			final JMenuHandle menuHandle,
			final Runnable refresh,
			final List< Runnable > runOnClose )
	{
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final FeatureModel featureModel = appModel.getModel().getFeatureModel();
		final FeatureColorModeManager featureColorModeManager = appModel.getWindowManager().getManager( FeatureColorModeManager.class );
		final ColoringModelBranchGraph< ?, ? > coloringModel =
				new ColoringModelBranchGraph<>( tagSetModel, featureColorModeManager, featureModel );
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

		// Handle track color generator.
		@SuppressWarnings( "unchecked" )
		final TrackGraphColorGenerator< Spot, Link > tgcg = appModel.getWindowManager().getManager( TrackGraphColorGenerator.class );
		// Adapt it so that is a color generator for the branch graph.
		final Model m = appModel.getModel();
		final GraphColorGeneratorAdapter< Spot, Link, BranchSpot, BranchLink > branchTgcg = new GraphColorGeneratorAdapter<>(
				new BranchGraphVertexBimap<>( m.getBranchGraph(), m.getGraph() ),
				new BranchGraphEdgeBimap<>( m.getBranchGraph(), m.getGraph() ) );
		branchTgcg.setColorGenerator( tgcg );

		@SuppressWarnings( "unchecked" )
		final ColoringModelMain.ColoringChangedListener coloringChangedListener = () -> {
			final GraphColorGenerator< BranchSpot, BranchLink > colorGenerator;
			switch ( coloringModel.getColoringStyle() )
			{
			case BY_FEATURE:
				colorGenerator = ( GraphColorGenerator< BranchSpot, BranchLink > ) coloringModel.getFeatureGraphColorGenerator();
				break;
			case BY_TAGSET:
				colorGenerator = new TagSetGraphColorGenerator<>( branchTagSetModel( appModel ), coloringModel.getTagSet() );
				break;
			case BY_TRACK:
				colorGenerator = branchTgcg;
				break;
			case NONE:
				colorGenerator = null;
				break;
			default:
				throw new IllegalArgumentException( "Unknown coloring style: " + coloringModel.getColoringStyle() );
			}
			colorGeneratorAdapter.setColorGenerator( colorGenerator );
			refresh.run();
		};
		coloringModel.listeners().add( coloringChangedListener );

		return coloringModel;
	}

	private static TagSetModel< BranchSpot, BranchLink > branchTagSetModel( final ProjectModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final BranchGraphTagSetAdapter< Spot, Link, BranchSpot, BranchLink > branchGraphTagSetModel =
				new BranchGraphTagSetAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), tagSetModel );
		return branchGraphTagSetModel;
	}



	private static HighlightModel< BranchSpot, BranchLink > branchHighlightModel( final ProjectModel appModel,
			final TimepointModel timepointModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final HighlightModel< Spot, Link > graphHighlightModel = appModel.getHighlightModel();
		final HighlightModel< BranchSpot, BranchLink > branchHighlightModel =
				new BranchGraphHighlightAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphHighlightModel, timepointModel );
		return branchHighlightModel;
	}

	private static FocusModel< BranchSpot > branchFocusfocusModel( final ProjectModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final FocusModel< Spot > graphFocusModel = appModel.getFocusModel();
		final FocusModel< BranchSpot > branchFocusfocusModel =
				new BranchGraphFocusAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphFocusModel );
		return branchFocusfocusModel;
	}

	private static SelectionModel< BranchSpot, BranchLink > branchSelectionModel( final ProjectModel appModel )
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

	@Override
	@SuppressWarnings( "unchecked" )
	public ContextChooser< Spot > getContextChooser()
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

	@Override
	public ColoringModelMain< Spot, Link, BranchSpot, BranchLink > getColoringModel()
	{
		return coloringModel;
	}

	public ColoringModel getBranchColoringModel()
	{
		return branchColoringModel;
	}

	/*
	 * Functions.
	 */

	private static < V extends Vertex< E >, E extends Edge< V > > ViewGraph< V, E, V, E > createViewGraph( final MastodonModel< ?, V, E > model )
	{
		return IdentityViewGraph.wrap( model.getGraph(), model.getGraphIdBimap() );
	}
}
