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

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.BranchGraphModelOverlayProperties;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.ModelOverlayProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.keymap.KeyConfigContexts;
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
import org.mastodon.views.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayProperties;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapper;

import bdv.tools.InitializeViewerState;
import bdv.viewer.NavigationActions;
import bdv.viewer.ViewerPanel;

public class MamutBranchViewBdv extends MamutBranchView< 
	OverlayBranchGraphWrapper< BranchSpot, BranchLink, Spot, Link >, 
	OverlayVertexWrapper< BranchSpot, BranchLink >, 
	OverlayEdgeWrapper< BranchSpot, BranchLink > >
{

	private static int bdvName = 1;

	public MamutBranchViewBdv( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutBranchViewBdv( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, createViewBranchGraph( appModel ), new String[] { KeyConfigContexts.BIGDATAVIEWER } );
		
		// Image data.
		final SharedBigDataViewerData sharedBdvData = appModel.getSharedBdvData();

		// BDV.
		final String windowTitle = "BigDataViewer Branch " + ( bdvName++ );
		final BigDataViewerMamut bdv = new BigDataViewerMamut( sharedBdvData, windowTitle, groupHandle );
		final ViewerFrameMamut viewerFrame = bdv.getViewerFrame();
		setFrame( viewerFrame );
		final ViewerPanel viewer = bdv.getViewer();

		// Coloring.
		final GraphColorGeneratorAdapter< BranchSpot, BranchLink, OverlayVertexWrapper< BranchSpot, BranchLink >, OverlayEdgeWrapper< BranchSpot, BranchLink > > coloring =
				new GraphColorGeneratorAdapter<>( vertexMap, edgeMap );

		// Renderer.
		final OverlayGraphWrapper< Spot, Link > overlayGraph = viewGraph.getGraphWrapper();
		final OverlayGraphRenderer< OverlayVertexWrapper< BranchSpot, BranchLink >, OverlayEdgeWrapper< BranchSpot, BranchLink > > tracksOverlay =
				new OverlayBranchGraphRenderer<>(
						viewGraph,
						overlayGraph,
						highlightModel,
						focusModel,
						selectionModel,
						coloring );
		viewer.getDisplay().overlays().add( tracksOverlay );
		viewer.renderTransformListeners().add( tracksOverlay );
		viewer.addTimePointListener( tracksOverlay );

		// Core graph.
		final Model model = appModel.getModel();
		final ModelGraph graph = model.getGraph();

		// Listen to changes in models -> repaint.
		highlightModel.listeners().add( () -> viewer.getDisplay().repaint() );
		focusModel.listeners().add( () -> viewer.getDisplay().repaint() );
		selectionModel.listeners().add( () -> viewer.getDisplay().repaint() );
		graph.addGraphChangeListener( () -> viewer.getDisplay().repaint() );
		graph.addVertexPositionListener( ( v ) -> viewer.getDisplay().repaint() );

		// Forward navigation to view.
		final OverlayNavigation< OverlayVertexWrapper< BranchSpot, BranchLink >, OverlayEdgeWrapper< BranchSpot, BranchLink > > overlayNavigation =
				new OverlayNavigation<>( viewer, viewGraph );
		navigationHandler.listeners().add( overlayNavigation );

		final BdvHighlightHandler< ?, ? > highlightHandler = new BdvHighlightHandler<>( viewGraph, tracksOverlay, highlightModel );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.renderTransformListeners().add( highlightHandler );

		// Actions & Behaviors.
		BigDataViewerActionsMamut.install( viewActions, bdv );
		NavigationActions.install( viewActions, viewer, sharedBdvData.is2D() );
		viewer.getTransformEventHandler().install( viewBehaviours );
		HighlightBehaviours.install( viewBehaviours, viewGraph, graph.getLock(), graph, highlightModel, model );
		FocusActions.install( viewActions, viewGraph, graph.getLock(), focusModel, selectionModel );
		BdvSelectionBehaviours.install( viewBehaviours, viewGraph, tracksOverlay, selectionModel, focusModel, navigationHandler );
		OverlayActions.install( viewActions, viewer, tracksOverlay );

		InitializeViewerState.initTransform( viewer );
		viewerFrame.setVisible( true );
	}

	private static OverlayBranchGraphWrapper< BranchSpot, BranchLink, Spot, Link > createViewBranchGraph( final MamutAppModel appModel )
	{
		// Model.
		final Model model = appModel.getModel();

		// Branch graph.
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		final GraphIdBimap< BranchSpot, BranchLink > branchGraphIdBimap = model.getBranchGraphIdBimap();

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

		final OverlayProperties< BranchSpot, BranchLink > properties =
				new BranchGraphModelOverlayProperties( branchGraph, graph, radiusStats );
		final OverlayBranchGraphWrapper< BranchSpot, BranchLink, Spot, Link > overlayBranchGraph = new OverlayBranchGraphWrapper<>(
				branchGraph,
				branchGraphIdBimap,
				model.getBranchGraphSpatioTemporalIndex(),
				graph.getLock(),
				properties,
				overlayGraph );
		return overlayBranchGraph;
	}
}
