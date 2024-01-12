/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.views;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.adapter.TimepointModelAdapter;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.branch.BranchGraphEdgeBimap;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphNavigationHandlerAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.model.branch.BranchGraphTagSetAdapter;
import org.mastodon.model.branch.BranchGraphVertexBimap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.ui.TagSetMenu;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColorBarOverlayMenu;
import org.mastodon.ui.coloring.ColoringMenu;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.ColoringModelBranchGraph;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.TagSetGraphColorGenerator;
import org.mastodon.ui.coloring.TrackGraphColorGenerator;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.Keymap.UpdateListener;

/**
 * Mother class for views that display the branch graph of the model.
 * 
 * @param <VG>
 *            the type of the branch <b>view</b> graph.
 * @param <V>
 *            the type of vertices in the view graph.
 * @param <E>
 *            the type of edges in the view graph.
 */
public class MamutBranchView<
		VG extends ViewGraph< BranchSpot, BranchLink, V, E >,
		V extends Vertex< E >,
		E extends Edge< V > >
		implements MamutViewI
{

	/**
	 * Key that specifies settings specific to the branch-graph view in a common
	 * view. Values are <code>Map&lt;String, Object&gt;</code>.
	 */
	public static final String BRANCH_GRAPH = "BranchGraph";

	protected final ProjectModel appModel;

	protected ViewFrame frame;

	protected final String[] keyConfigContexts;

	protected Actions viewActions;

	protected Behaviours viewBehaviours;

	protected final VG viewGraph;

	protected final GroupHandle groupHandle;

	protected final TimepointModelAdapter timepointModel;

	protected final HighlightModelAdapter< BranchSpot, BranchLink, V, E > highlightModel;

	protected final FocusModelAdapter< BranchSpot, BranchLink, V, E > focusModel;

	protected final SelectionModelAdapter< BranchSpot, BranchLink, V, E > selectionModel;

	protected final NavigationHandlerAdapter< BranchSpot, BranchLink, V, E > navigationHandler;

	protected final ArrayList< Runnable > runOnClose;

	protected final RefBimap< BranchSpot, V > vertexMap;

	protected final RefBimap< BranchLink, E > edgeMap;

	protected final TagSetModel< BranchSpot, BranchLink > tagSetModel;

	public MamutBranchView( final ProjectModel appModel, final VG viewGraph, final String[] keyConfigContexts )
	{
		this.appModel = appModel;
		this.viewGraph = viewGraph;
		this.keyConfigContexts = keyConfigContexts;

		// Maps.
		this.vertexMap = viewGraph.getVertexMap();
		this.edgeMap = viewGraph.getEdgeMap();

		// Group handle.
		this.groupHandle = appModel.getGroupManager().createGroupHandle();

		// Graphs.
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final ModelGraph graph = appModel.getModel().getGraph();

		// Highlight.
		final HighlightModel< Spot, Link > graphHighlightModel = appModel.getHighlightModel();
		final HighlightModel< BranchSpot, BranchLink > branchHighlightModel =
				new BranchGraphHighlightAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphHighlightModel );
		this.highlightModel = new HighlightModelAdapter<>( branchHighlightModel, vertexMap, edgeMap );

		// Focus
		final FocusModel< Spot > graphFocusModel = appModel.getFocusModel();
		final FocusModel< BranchSpot > branchFocusfocusModel =
				new BranchGraphFocusAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphFocusModel );
		this.focusModel = new FocusModelAdapter<>( branchFocusfocusModel, vertexMap, edgeMap );

		// Selection
		final SelectionModel< Spot, Link > graphSelectionModel = appModel.getSelectionModel();
		final SelectionModel< BranchSpot, BranchLink > branchSelectionModel =
				new BranchGraphSelectionAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), graphSelectionModel );
		selectionModel = new SelectionModelAdapter<>( branchSelectionModel, vertexMap, edgeMap );

		// Navigation.
		final NavigationHandler< Spot, Link > graphNavigationHandler = groupHandle.getModel( appModel.NAVIGATION );
		final NavigationHandler< BranchSpot, BranchLink > branchGraphNavigation =
				new BranchGraphNavigationHandlerAdapter<>( branchGraph, graph, graph.getGraphIdBimap(),
						graphNavigationHandler );
		this.navigationHandler = new NavigationHandlerAdapter<>( branchGraphNavigation, vertexMap, edgeMap );

		// Time-point.
		this.timepointModel = new TimepointModelAdapter( groupHandle.getModel( appModel.TIMEPOINT ) );

		// Tag-set.
		this.tagSetModel = branchTagSetModel( appModel );

		// Closing runnables.
		this.runOnClose = new ArrayList<>();
		runOnClose.add( () -> {
			timepointModel.listeners().removeAll();
			highlightModel.listeners().removeAll();
			focusModel.listeners().removeAll();
			selectionModel.listeners().removeAll();
			navigationHandler.listeners().removeAll();
		} );
	}

	protected void setFrame( final ViewFrame frame )
	{
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );
		this.frame = frame;

		final Actions projectActions = appModel.getProjectActions();
		if ( projectActions != null )
		{
			frame.getKeybindings().addActionMap( "project", new WrappedActionMap( projectActions.getActionMap() ) );
			frame.getKeybindings().addInputMap( "project", new WrappedInputMap( projectActions.getInputMap() ) );
		}

		final Actions pluginActions = appModel.getPlugins().getPluginActions();
		if ( pluginActions != null )
		{
			frame.getKeybindings().addActionMap( "plugin", new WrappedActionMap( pluginActions.getActionMap() ) );
			frame.getKeybindings().addInputMap( "plugin", new WrappedInputMap( pluginActions.getInputMap() ) );
		}

		final Actions modelActions = appModel.getModelActions();
		frame.getKeybindings().addActionMap( "model", new WrappedActionMap( modelActions.getActionMap() ) );
		frame.getKeybindings().addInputMap( "model", new WrappedInputMap( modelActions.getInputMap() ) );

		final Keymap keymap = appModel.getKeymap();

		viewActions = new Actions( keymap.getConfig(), keyConfigContexts );
		viewActions.install( frame.getKeybindings(), "view" );

		viewBehaviours = new Behaviours( keymap.getConfig(), keyConfigContexts );
		viewBehaviours.install( frame.getTriggerbindings(), "view" );

		final UpdateListener updateListener = () -> {
			viewBehaviours.updateKeyConfig( keymap.getConfig() );
			viewActions.updateKeyConfig( keymap.getConfig() );
		};
		keymap.updateListeners().add( updateListener );
		onClose( () -> keymap.updateListeners().remove( updateListener ) );
	}

	protected final ColoringModel registerBranchColoring(
			final GraphColorGeneratorAdapter< BranchSpot, BranchLink, V, E > colorGeneratorAdapter,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final FeatureModel featureModel = appModel.getModel().getFeatureModel();
		final FeatureColorModeManager featureColorModeManager = appModel.getWindowManager().getManager( FeatureColorModeManager.class );
		final ColoringModelBranchGraph< ?, ? > coloringModel = new ColoringModelBranchGraph<>( tagSetModel, featureColorModeManager, featureModel );
		final ColoringMenu coloringMenu = new ColoringMenu( menuHandle.getMenu(), coloringModel );

		tagSetModel.listeners().add( coloringModel );
		onClose( () -> tagSetModel.listeners().remove( coloringModel ) );
		tagSetModel.listeners().add( coloringMenu );
		onClose( () -> tagSetModel.listeners().remove( coloringMenu ) );

		featureColorModeManager.listeners().add( coloringModel );
		onClose( () -> featureColorModeManager.listeners().remove( coloringModel ) );
		featureColorModeManager.listeners().add( coloringMenu );
		onClose( () -> featureColorModeManager.listeners().remove( coloringMenu ) );

		featureModel.listeners().add( coloringMenu );
		onClose( () -> featureModel.listeners().remove( coloringMenu ) );

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
			switch(coloringModel.getColoringStyle())
			{
			case BY_FEATURE:
				colorGenerator = ( GraphColorGenerator< BranchSpot, BranchLink > ) coloringModel.getFeatureGraphColorGenerator();
				break;
			case BY_TAGSET:
				colorGenerator = new TagSetGraphColorGenerator<>( tagSetModel, coloringModel.getTagSet() ) ;
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
			colorGeneratorAdapter.setColorGenerator(colorGenerator);
			refresh.run();
		};
		coloringModel.listeners().add( coloringChangedListener );

		return coloringModel;
	}

	protected void registerColorbarOverlay(
			final ColorBarOverlay colorBarOverlay,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final ColorBarOverlayMenu menu = new ColorBarOverlayMenu( menuHandle.getMenu(), colorBarOverlay, refresh );
		colorBarOverlay.listeners().add( menu );
	}

	protected void registerTagSetMenu(
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final Model model = appModel.getModel();
		final TagSetMenu< Spot, Link > tagSetMenu = new TagSetMenu<>(
				menuHandle.getMenu(),
				model.getTagSetModel(),
				appModel.getSelectionModel(),
				model.getGraph().getLock(), model );
		tagSetModel.listeners().add( tagSetMenu );
		onClose( () -> tagSetModel.listeners().remove( tagSetMenu ) );
	}

	/**
	 * Adds the specified {@link Runnable} to the list of runnables to execute
	 * when this view is closed.
	 *
	 * @param runnable
	 *            the {@link Runnable} to add.
	 */
	@Override
	public synchronized void onClose( final Runnable runnable )
	{
		runOnClose.add( runnable );
	}

	protected synchronized void close()
	{
		runOnClose.forEach( Runnable::run );
		runOnClose.clear();
	}

	@Override
	public ViewFrame getFrame()
	{
		return frame;
	}

	@Override
	public GroupHandle getGroupHandle()
	{
		return groupHandle;
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
}
