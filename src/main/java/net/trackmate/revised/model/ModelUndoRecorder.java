package net.trackmate.revised.model;

import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.ref.AbstractEdge;
import net.trackmate.undo.UndoIdBimap;
import net.trackmate.undo.UndoRecorder;
import net.trackmate.undo.UndoSerializer;

public class ModelUndoRecorder<
		V extends AbstractSpot3D< V, E, ? >,
		E extends AbstractEdge< E, V, ? > >
	extends UndoRecorder< V, E, ModelUndoableEditList<V,E> >
{
	private static final int defaultCapacity = 1000;

	public ModelUndoRecorder(
			final ListenableGraph< V, E > graph,
			final GraphFeatures< V, E > graphFeatures,
			final GraphIdBimap< V, E > idmap,
			final UndoSerializer< V, E > serializer )
	{
		super( graph,
				graphFeatures,
				new ModelUndoableEditList< >(
						defaultCapacity, graph, graphFeatures, serializer,
						new UndoIdBimap< >( idmap.vertexIdBimap() ),
						new UndoIdBimap< >( idmap.edgeIdBimap() ) ) );
	}
}
