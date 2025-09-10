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
package org.mastodon.app.views.table;

import static org.mastodon.app.MastodonIcons.TABLE_VIEW_ICON;
import static org.mastodon.app.ui.ViewMenuBuilder2.item;
import static org.mastodon.app.ui.ViewMenuBuilder2.menu;
import static org.mastodon.app.ui.ViewMenuBuilder2.separator;
import static org.mastodon.mamut.MamutMenuBuilder2.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.viewMenu;

import java.awt.Component;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mastodon.app.UIModel;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.UIUtils;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenu2;
import org.mastodon.app.ui.ViewMenuBuilder2.JMenuHandle;
import org.mastodon.app.views.AbstractMastodonFrameView2;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.mamut.MamutMenuBuilder2;
import org.mastodon.mamut.UndoActions;
import org.mastodon.model.HasBranchModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.branch.BranchGraphEdgeBimap;
import org.mastodon.model.branch.BranchGraphNavigationHandlerAdapter;
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

	private final ColoringModel coloringModel;

	private final ColoringModel branchColoringModel;

	public MastodonViewTable2(
			final M dataModel,
			final UIModel< ? > uiModel,
			final TableModelGraphProperties< V > properties )
	{
		this( dataModel, uiModel, properties, false );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	protected MastodonViewTable2(
			final M dataModel,
			final UIModel< ? > uiModel,
			final TableModelGraphProperties< V > properties,
			final boolean selectionTable )
	{
		super( dataModel, uiModel, createViewGraph( dataModel ), CONTEXTS );

		// Create tables.
		final TableViewFrameBuilder builder = new TableViewFrameBuilder().groupHandle( groupHandle );
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
				.selectionTable( selectionTable )
				.vertexLabelSetter( ( s, label ) -> properties.setLabel( s, label ) )
				.vertexLabelGetter( s -> properties.getLabel( s ) )
				.done();

		// Tables for branch graph.
		final GraphColorGeneratorAdapter branchColoringAdapter;
		if ( dataModel instanceof HasBranchModel )
		{
			final HasBranchModel bm = ( HasBranchModel ) dataModel;
			final MastodonModel branchModel = bm.branchModel();
			final BranchGraph branchGraph = ( BranchGraph ) branchModel.getGraph();
			final ViewGraph viewBranchGraph = UIUtils.wrap( branchGraph, branchGraph.getGraphIdBimap() );
			final GraphTableBuilder gtbBranch = builder.addGraph( branchGraph );
			branchColoringAdapter = new GraphColorGeneratorAdapter<>( viewBranchGraph.getVertexMap(), viewBranchGraph.getEdgeMap() );

			// Do we have a label property?
			final Object bv = branchGraph.vertices().iterator().next();
			if ( bv instanceof HasLabel )
			{
				gtbBranch
						.vertexLabelGetter( s -> ( ( HasLabel ) s ).getLabel() )
						.vertexLabelSetter( ( s, label ) -> ( ( HasLabel ) s ).setLabel( ( String ) label ) );
			}
			gtbBranch
					.featureModel( branchModel.getFeatureModel() )
					.tagSetModel( branchModel.getTagSetModel() )
					.selectionModel( branchModel.getSelectionModel() )
					.highlightModel( branchModel.getHighlightModel() )
					// TODO timepoint model for the branch highlight model.
					.focusModel( branchModel.getFocusModel() )
					.navigationHandler( branchGraphNavigation( dataModel, navigationHandler ) )
					.coloring( branchColoringAdapter )
					.selectionTable( selectionTable )
					.done();
		}
		else
		{
			branchColoringAdapter = null;
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

		/*
		 * Register a listener to vertex label property changes, will update the
		 * table-view when the label change.
		 */
		final PropertyChangeListener< V > labelChangedRefresher = e -> SwingUtilities.invokeLater( () -> frame.repaint() );
		properties.addVertexLabelListener( labelChangedRefresher );
		onClose( () -> properties.removeVertexLabelListener( labelChangedRefresher ) );

		// Menus
		final ViewMenu2 menu = new ViewMenu2( frame.getJMenuBar(), uiModel.getKeymap(), CONTEXTS );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();
		final JMenuHandle colorMenuHandle = new JMenuHandle();
		final JMenuHandle colorBranchMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		uiModel.getViewFactories().addWindowMenuTo( menu, actionMap );
		MamutMenuBuilder2.build( menu, actionMap,
				fileMenu(
						menu( "Export",
								item( TableViewActions.EXPORT_TO_CSV ) ) ),
				viewMenu(
						MamutMenuBuilder2.colorMenu( colorMenuHandle ),
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
		uiModel.getPlugins().addMenus( menu );

		coloringModel = registerColoring( coloringAdapter, colorMenuHandle, () -> frame.repaint() );
		if ( dataModel instanceof HasBranchModel )
		{
			branchColoringModel = registerBranchColoring( branchColoringAdapter, colorBranchMenuHandle, () -> frame.repaint() );
		}
		else
		{
			branchColoringModel = null;
		}

		// Set the tag-set menu and listen to user selecting a tag in it.
		registerTagSetMenu( tagSetMenuHandle, () -> frame.repaint() );

		// Search panels.
		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler,
				selectionModel, focusModel, frame.getCurrentlyDisplayedTable(), ( vertex ) -> properties.getLabel( vertex ) );
		searchPanel.setAlignmentY( Component.CENTER_ALIGNMENT );
		frame.getSettingsPanel().add( searchPanel );

		// Table actions.
		MastodonFrameViewActions.install( viewActions, this );
		TableViewActions.install( viewActions, frame );
		final CommandFinder cf = CommandFinder.build()
				.context( uiModel.getContext() )
				.inputTriggerConfig( uiModel.getKeymap().getConfig() )
				.keyConfigContexts( keyConfigContexts )
				.descriptionProvider( uiModel.getViewFactories().getCommandDescriptions() )
				.register( viewActions )
				.register( uiModel.getModelActions() )
				.register( uiModel.getProjectActions() )
				.register( uiModel.getPlugins().getPluginActions() )
				.modificationListeners( uiModel.getKeymap().updateListeners() )
				.parent( frame )
				.installOn( viewActions );
		cf.getDialog().setTitle( cf.getDialog().getTitle() + " - " + frame.getTitle() );
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
	public ContextChooser< V > getContextChooser()
	{
		/*
		 * We configured the table creator so that only the first table pair,
		 * for the core graph, has a context.
		 */
		return ( ContextChooser< V > ) getFrame().getContextChoosers().get( 0 );
	}

	@Override
	public ColoringModel getColoringModel()
	{
		return coloringModel;
	}

	/**
	 * Returns the branch-graph coloring model, or <code>null</code> if the data
	 * model has no branch graph.
	 *
	 * @return the branch-graph coloring model, or <code>null</code>.
	 */
	public ColoringModel getBranchColoringModel()
	{
		return branchColoringModel;
	}

	/*
	 * Functions.
	 */

	private static < V extends Vertex< E >, E extends Edge< V > > ViewGraph< V, E, V, E > createViewGraph( final MastodonModel< ?, V, E > model )
	{
		return UIUtils.wrap( model.getGraph(), model.getGraphIdBimap() );
	}

	/**
	 * Creates a branch-graph navigation handler adapter, that maps the
	 * navigation on the branch graph to navigation on the view graph.
	 *
	 * @param <BV>
	 *            the vertex type of the branch graph.
	 * @param <BE>
	 *            the edge type of the branch graph.
	 * @param dataModel
	 *            the mastodon model, that must implement
	 *            {@link HasBranchModel}.
	 * @param navigationHandler
	 *            the navigation handler for the view graph.
	 * @return the navigation handler for the branch graph.
	 * @throws IllegalArgumentException
	 *             if the data model has no branch graph, i.e., does not
	 *             implement {@link HasBranchModel}.
	 */
	@SuppressWarnings( "unchecked" )
	private final < BV extends Vertex< BE >, BE extends Edge< BV > > NavigationHandler< BV, BE > branchGraphNavigation(
			final M dataModel,
			final NavigationHandler< V, E > navigationHandler )
	{
		if ( !( dataModel instanceof HasBranchModel ) )
			throw new IllegalArgumentException( "Data model has no branch graph." );

		final ReadOnlyGraph< V, E > graph = dataModel.getGraph();
		final BranchGraph< BV, BE, V, E > branchGraph = ( BranchGraph< BV, BE, V, E > ) ( ( HasBranchModel< ?, BV, BE > ) dataModel ).branchModel().getGraph();
		final GraphIdBimap< V, E > idMap = dataModel.getGraphIdBimap();
		final NavigationHandler< BV, BE > branchGraphNavigation =
				new BranchGraphNavigationHandlerAdapter< V, E, BV, BE >(
						branchGraph,
						graph,
						idMap,
						navigationHandler );
		return branchGraphNavigation;
	}

	private final < BV extends Vertex< BE >, BE extends Edge< BV > > ColoringModel registerBranchColoring(
			final GraphColorGeneratorAdapter< BV, BE, BV, BE > colorGeneratorAdapter,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		if ( !( dataModel instanceof HasBranchModel ) )
			throw new IllegalArgumentException( "Data model has no branch graph." );

		final ReadOnlyGraph< V, E > graph = dataModel.getGraph();
		@SuppressWarnings( "unchecked" )
		final MastodonModel< ?, BV, BE > branchModel = ( ( HasBranchModel< ?, BV, BE > ) dataModel ).branchModel();
		@SuppressWarnings( "unchecked" )
		final BranchGraph< BV, BE, V, E > branchGraph = ( BranchGraph< BV, BE, V, E > ) branchModel.getGraph();
		final TagSetModel< BV, BE > branchTagSetModel = branchModel.getTagSetModel();


		final TagSetModel< V, E > tagSetModel = dataModel.getTagSetModel();
		final FeatureModel featureModel = dataModel.getFeatureModel();
		final FeatureColorModeManager featureColorModeManager = uiModel.getInstance( FeatureColorModeManager.class );
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
		final TrackGraphColorGenerator< V, E > tgcg = uiModel.getInstance( TrackGraphColorGenerator.class );
		// Adapt it so that is a color generator for the branch graph.
		final GraphColorGeneratorAdapter< V, E, BV, BE > branchTgcg = new GraphColorGeneratorAdapter<>(
				new BranchGraphVertexBimap<>( branchGraph, graph ),
				new BranchGraphEdgeBimap<>( branchGraph,graph ) );
		branchTgcg.setColorGenerator( tgcg );

		@SuppressWarnings( "unchecked" )
		final ColoringModelMain.ColoringChangedListener coloringChangedListener = () -> {
			final GraphColorGenerator< BV, BE > colorGenerator;
			switch ( coloringModel.getColoringStyle() )
			{
			case BY_FEATURE:
				colorGenerator = ( GraphColorGenerator< BV, BE > ) coloringModel.getFeatureGraphColorGenerator();
				break;
			case BY_TAGSET:
				colorGenerator = new TagSetGraphColorGenerator<>( branchTagSetModel, coloringModel.getTagSet() );
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
}
