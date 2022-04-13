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

import java.util.HashMap;
import java.util.Map;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.BranchGraphModelOverlayProperties;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.ModelOverlayProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchEdge;
import org.mastodon.mamut.model.branch.BranchVertex;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphNavigationHandlerAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.views.bdv.BigDataViewerActionsMamut;
import org.mastodon.views.bdv.BigDataViewerMamut;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.ViewerFrameMamut;
import org.mastodon.views.bdv.overlay.BdvHighlightHandler;
import org.mastodon.views.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.views.bdv.overlay.OverlayActions;
import org.mastodon.views.bdv.overlay.OverlayBranchGraphRenderer;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.bdv.overlay.OverlayNavigation;
import org.mastodon.views.bdv.overlay.wrap.OverlayBranchGraphWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayEdgeWrapperBimap;
import org.mastodon.views.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayProperties;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapperBimap;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.tools.InitializeViewerState;
import bdv.viewer.NavigationActions;
import bdv.viewer.ViewerPanel;

public class MamutViewBranchBdv
{

	private static int bdvName = 1;

	public MamutViewBranchBdv( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewBranchBdv( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		// Model.
		final Model model = appModel.getModel();

		// Branch graph.
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		final GraphIdBimap< BranchVertex, BranchEdge > branchGraphIdBimap = model.getBranchGraphIdBimap();

		// Graph.
		final ModelGraph graph = model.getGraph();
		final GraphIdBimap< Spot, Link > graphIdBimap = model.getGraphIdBimap();
		final BoundingSphereRadiusStatistics radiusStats = appModel.getRadiusStats();

		// Overlay graph.
		final OverlayGraphWrapper< Spot, Link > overlayGraph = new OverlayGraphWrapper<>(
				graph,
				graphIdBimap,
				appModel.getModel().getSpatioTemporalIndex(),
				appModel.getModel().getGraph().getLock(),
				new ModelOverlayProperties( graph, radiusStats ) );

		final OverlayProperties< BranchVertex, BranchEdge > properties =
				new BranchGraphModelOverlayProperties( branchGraph, graph, radiusStats );
		final OverlayBranchGraphWrapper< BranchVertex, BranchEdge, Spot, Link > overlayBranchGraph = new OverlayBranchGraphWrapper<>(
				branchGraph,
				branchGraphIdBimap,
				model.getBranchGraphSpatioTemporalIndex(),
				graph.getLock(),
				properties,
				overlayGraph );

		// Image data.
		final SharedBigDataViewerData sharedBdvData = appModel.getSharedBdvData();

		// Bimaps.
		final RefBimap< BranchVertex, OverlayVertexWrapper< BranchVertex, BranchEdge > > vertexMap = new OverlayVertexWrapperBimap<>( overlayBranchGraph );
		final RefBimap< BranchEdge, OverlayEdgeWrapper< BranchVertex, BranchEdge > > edgeMap = new OverlayEdgeWrapperBimap<>( overlayBranchGraph );

		// Group handle.
		final GroupHandle groupHandle = appModel.getGroupManager().createGroupHandle();

		// Highlight.
		final HighlightModel< Spot, Link > highlightModel = appModel.getHighlightModel();
		final HighlightModel< BranchVertex, BranchEdge > branchHighlightModel =
				new BranchGraphHighlightAdapter<>( branchGraph, graph, highlightModel );
		final HighlightModel< OverlayVertexWrapper< BranchVertex, BranchEdge >, OverlayEdgeWrapper< BranchVertex, BranchEdge > > overlayHighlight =
				new HighlightModelAdapter<>( branchHighlightModel, vertexMap, edgeMap );

		// Focus
		final FocusModel< Spot, Link > focusModel = appModel.getFocusModel();
		final FocusModel< BranchVertex, BranchEdge > branchFocusfocusModel =
				new BranchGraphFocusAdapter<>( branchGraph, graph, focusModel );
		final FocusModel< OverlayVertexWrapper< BranchVertex, BranchEdge >, OverlayEdgeWrapper< BranchVertex, BranchEdge > > overlayFocus =
				new FocusModelAdapter<>( branchFocusfocusModel, vertexMap, edgeMap );

		// Selection
		final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();
		final SelectionModel< BranchVertex, BranchEdge > branchSelectionModel =
				new BranchGraphSelectionAdapter<>( branchGraph, graph, selectionModel );
		final SelectionModel< OverlayVertexWrapper< BranchVertex, BranchEdge >, OverlayEdgeWrapper< BranchVertex, BranchEdge > > overlaySelection =
				new SelectionModelAdapter<>( branchSelectionModel, vertexMap, edgeMap );

		// Navigation.
		final NavigationHandler< Spot, Link > navigationHandler = groupHandle.getModel( appModel.NAVIGATION );
		final NavigationHandler< BranchVertex, BranchEdge > branchGraphNavigation =
				new BranchGraphNavigationHandlerAdapter<>( branchGraph, graph, navigationHandler );
		final NavigationHandler< OverlayVertexWrapper< BranchVertex, BranchEdge >, OverlayEdgeWrapper< BranchVertex, BranchEdge > > overlayNavigationHandler =
				new NavigationHandlerAdapter<>( branchGraphNavigation, vertexMap, edgeMap );

		// BDV.
		final String windowTitle = "BigDataViewer Branch " + ( bdvName++ );
		final BigDataViewerMamut bdv = new BigDataViewerMamut( sharedBdvData, windowTitle, groupHandle );
		final ViewerFrameMamut viewerFrame = bdv.getViewerFrame();
		final ViewerPanel viewer = bdv.getViewer();

		// Coloring.
		final GraphColorGeneratorAdapter< BranchVertex, BranchEdge, OverlayVertexWrapper< BranchVertex, BranchEdge >, OverlayEdgeWrapper< BranchVertex, BranchEdge > > coloring =
				new GraphColorGeneratorAdapter<>( vertexMap, edgeMap );

		// Renderer.
		final OverlayGraphRenderer< OverlayVertexWrapper< BranchVertex, BranchEdge >, OverlayEdgeWrapper< BranchVertex, BranchEdge > > tracksOverlay =
				new OverlayBranchGraphRenderer<>(
						overlayBranchGraph,
						overlayGraph,
						overlayHighlight,
						overlayFocus,
						overlaySelection,
						coloring );
		viewer.getDisplay().overlays().add( tracksOverlay );
		viewer.renderTransformListeners().add( tracksOverlay );
		viewer.addTimePointListener( tracksOverlay );

		overlayHighlight.listeners().add( () -> viewer.getDisplay().repaint() );
		overlayFocus.listeners().add( () -> viewer.getDisplay().repaint() );
		model.getGraph().addGraphChangeListener( () -> viewer.getDisplay().repaint() );
		model.getGraph().addVertexPositionListener( ( v ) -> viewer.getDisplay().repaint() );
		overlaySelection.listeners().add( () -> viewer.getDisplay().repaint() );

		final OverlayNavigation< OverlayVertexWrapper< BranchVertex, BranchEdge >, OverlayEdgeWrapper< BranchVertex, BranchEdge > > overlayNavigation = new OverlayNavigation<>( viewer, overlayBranchGraph );
		overlayNavigationHandler.listeners().add( overlayNavigation );

		final BdvHighlightHandler< ?, ? > highlightHandler = new BdvHighlightHandler<>( overlayBranchGraph, tracksOverlay, overlayHighlight );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.renderTransformListeners().add( highlightHandler );

		// Menus.
		final Keymap keymap = appModel.getKeymap();
		final String[] keyConfigContexts = new String[] { KeyConfigContexts.BIGDATAVIEWER };

		// Actions.
		final Actions viewActions = new Actions( keymap.getConfig(), keyConfigContexts );
		viewActions.install( viewerFrame.getKeybindings(), "view" );

		// Behaviors.
		final Behaviours viewBehaviours = new Behaviours( keymap.getConfig(), keyConfigContexts );
		viewBehaviours.install( viewerFrame.getTriggerbindings(), "view" );

		BigDataViewerActionsMamut.install( viewActions, bdv );
		NavigationActions.install( viewActions, viewer, sharedBdvData.is2D() );
		viewer.getTransformEventHandler().install( viewBehaviours );

		HighlightBehaviours.install( viewBehaviours, branchGraph, graph.getLock(), graph, branchHighlightModel, model );
		FocusActions.install( viewActions, branchGraph, graph.getLock(), branchFocusfocusModel, branchSelectionModel );
		BdvSelectionBehaviours.install( viewBehaviours, overlayBranchGraph, tracksOverlay, overlaySelection, overlayFocus, overlayNavigationHandler );
		OverlayActions.install( viewActions, viewer, tracksOverlay );

		InitializeViewerState.initTransform( viewer );
		viewerFrame.setVisible( true );
	}
}
