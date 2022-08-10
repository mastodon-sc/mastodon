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
package org.mastodon.views.trackscheme.display;

import javax.swing.SwingUtilities;

import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.RootsModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.util.DepthFirstIteration;
import org.mastodon.views.trackscheme.LexicographicalVertexOrder;
import org.mastodon.views.trackscheme.LineageTreeLayout;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * These actions can be used to limit the track scheme views
 * to only show selected tracks or downward tracks.
 */
public class ShowSelectedTracksActions<V extends Vertex<E>, E extends Edge<V>>
{

	public static final String SHOW_TRACK_DOWNWARD = "ts show track downward";
	public static final String SHOW_SELECTED_TRACKS = "ts show selected tracks";
	public static final String SHOW_ALL_TRACKS = "ts show all tracks";

	public static final String[] SHOW_TRACK_DOWNWARD_KEYS = {"ctrl PAGE_DOWN"};
	public static final String[] SHOW_SELECTED_TRACKS_KEYS = {"ctrl SPACE"};
	public static final String[] SHOW_ALL_TRACKS_KEYS = {"ctrl DELETE"};

	/**
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( SHOW_TRACK_DOWNWARD, SHOW_TRACK_DOWNWARD_KEYS, "Show only the downward tracks of selected spots and in the TrackScheme." );
			descriptions.add( SHOW_SELECTED_TRACKS, SHOW_SELECTED_TRACKS_KEYS, "Show only the tracks with selected spots in the TrackScheme." );
			descriptions.add( SHOW_ALL_TRACKS, SHOW_ALL_TRACKS_KEYS, "Show all tracks in the TrackScheme." );
		}
	}

	private final TrackSchemeGraph<V, E> viewGraph;

	private final LineageTreeLayout layout;

	private final SelectionModel<TrackSchemeVertex, TrackSchemeEdge> selectionModel;

	private final RootsModel<TrackSchemeVertex> rootsModel;

	private final TrackSchemePanel panel;

	private final RunnableAction showTrackDownwardAction = new RunnableAction( SHOW_TRACK_DOWNWARD, this::showTrackDownward );

	private final RunnableAction showSelectedTracksAction = new RunnableAction( SHOW_SELECTED_TRACKS, this::showSelectedTracks );

	private final RunnableAction showAllTracksAction = new RunnableAction( SHOW_ALL_TRACKS, this::showAllTracks );

	public static <V extends Vertex<E>, E extends Edge<V>> void install(
			Actions actions,
			TrackSchemeGraph<V, E> viewGraph,
			SelectionModel<TrackSchemeVertex, TrackSchemeEdge> selectionModel,
			RootsModel<TrackSchemeVertex> rootsModel,
			TrackSchemePanel trackschemePanel )
	{
		new ShowSelectedTracksActions<>( viewGraph, selectionModel, rootsModel, trackschemePanel).install(actions);
	}

	private ShowSelectedTracksActions( TrackSchemeGraph<V, E> viewGraph,
			SelectionModel<TrackSchemeVertex, TrackSchemeEdge> selectionModel,
			RootsModel<TrackSchemeVertex> rootsModel,
			TrackSchemePanel panel )
	{
		this.viewGraph = viewGraph;
		this.layout = panel.getLineageTreeLayout();
		this.selectionModel = selectionModel;
		this.rootsModel = rootsModel;
		this.panel = panel;
		selectionModelChanged();
		selectionModel.listeners().add( this::selectionModelChanged );
	}

	private void selectionModelChanged()
	{
		SwingUtilities.invokeLater( () -> {
			showTrackDownwardAction.setEnabled( ! this.selectionModel.isEmpty() );
			showSelectedTracksAction.setEnabled( ! this.selectionModel.isEmpty() );
		} );
	}

	private void install( Actions actions )
	{
		actions.namedAction( showTrackDownwardAction, SHOW_TRACK_DOWNWARD_KEYS );
		actions.namedAction( showSelectedTracksAction, SHOW_SELECTED_TRACKS_KEYS );
		actions.namedAction( showAllTracksAction, SHOW_ALL_TRACKS_KEYS );
	}

	public void showTrackDownward()
	{
		setLayoutRoots( getSelectedSubtreeRoots() );
	}

	private void showSelectedTracks()
	{
		setLayoutRoots( getSelectedWholeTrackRoots() );
	}

	public void showAllTracks()
	{
		setLayoutRoots( new RefArrayList<>( viewGraph.getVertexPool() ) );
	}

	private void setLayoutRoots( RefList<TrackSchemeVertex> roots )
	{
		rootsModel.setRoots( roots );
		panel.graphChanged();
		panel.getTransformEventHandler().zoomOutFully();
	}

	private RefList<TrackSchemeVertex> getSelectedSubtreeRoots()
	{
		RefSet<TrackSchemeVertex> selectedNodes = new RefSetImp<>( viewGraph.getVertexPool() );
		selectedNodes.addAll( selectionModel.getSelectedVertices() );
		addEdgeTargets( selectedNodes, selectionModel.getSelectedEdges() );
		return filterRootNodes( selectedNodes );
	}

	private RefList<TrackSchemeVertex> getSelectedWholeTrackRoots()
	{
		RefSet<TrackSchemeVertex> selectedNodes = new RefSetImp<>( viewGraph.getVertexPool() );
		selectedNodes.addAll( selectionModel.getSelectedVertices() );
		addEdgeTargets( selectedNodes, selectionModel.getSelectedEdges() );
		return getRealRoots( selectedNodes );
	}

	private void addEdgeTargets( RefSet<TrackSchemeVertex> selected, RefSet<TrackSchemeEdge> selectedEdges )
	{
		TrackSchemeVertex targetRef = viewGraph.vertexRef();
		for(TrackSchemeEdge edge : selectedEdges )
			selected.add(edge.getTarget(targetRef));
		viewGraph.releaseRef( targetRef );
	}

	private RefList<TrackSchemeVertex> filterRootNodes( RefSet<TrackSchemeVertex> selectedVertices )
	{
		RefList<TrackSchemeVertex> roots = new RefArrayList<>( viewGraph.getVertexPool() );
		for( TrackSchemeVertex realRoot : LexicographicalVertexOrder.sort( viewGraph, viewGraph.getRoots() ) )
			for( DepthFirstIteration.Step<TrackSchemeVertex> step : DepthFirstIteration.forRoot(viewGraph, realRoot) ) {
				TrackSchemeVertex node = step.node();
				if(selectedVertices.contains( node )) {
					roots.add( node );
					step.truncate();
				}
			}
		return roots;
	}

	private RefList<TrackSchemeVertex> getRealRoots( RefSet<TrackSchemeVertex> selectedNodes )
	{
		TrackSchemeVertex parent = viewGraph.vertexRef();
		RefSet<TrackSchemeVertex> roots = new RefSetImp<>( viewGraph.getVertexPool() );
		A: for(TrackSchemeVertex vertex : selectedNodes ) {
			parent.refTo( vertex );
			while ( ! parent.incomingEdges().isEmpty() )
			{
				parent.incomingEdges().iterator().next().getSource( parent );
				if ( selectedNodes.contains( parent ) )
					continue A;
			}
			roots.add( parent );
		}
		viewGraph.releaseRef( parent );
		return LexicographicalVertexOrder.sort( viewGraph, roots );
	}

}
