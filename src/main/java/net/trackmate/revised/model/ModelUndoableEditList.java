package net.trackmate.revised.model;

import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.features.Features;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.pool.PoolObjectAttributeSerializer;
import net.trackmate.undo.GraphUndoSerializer;
import net.trackmate.undo.GraphUndoableEditList;
import net.trackmate.undo.UndoIdBimap;
import net.trackmate.undo.UndoableEditRef;

public class ModelUndoableEditList<
			V extends AbstractSpot< V, E, ?, ? >,
			E extends AbstractListenableEdge< E, V, ? > >
		extends GraphUndoableEditList< V, E >
{
	protected final int numDimensions = 3; // TODO

	public ModelUndoableEditList(
			final int initialCapacity,
			final ListenableGraph< V, E > graph,
			final Features< V > vertexFeatures,
			final Features< E > edgeFeatures,
			final GraphUndoSerializer< V, E > serializer,
			final UndoIdBimap< V > vertexUndoIdBimap,
			final UndoIdBimap< E > edgeUndoIdBimap )
	{
		super( initialCapacity, graph, vertexFeatures, edgeFeatures, serializer, vertexUndoIdBimap, edgeUndoIdBimap );

		setVertexPosition = new SetAttributeType< >(
				vertexUndoIdBimap,
				new PoolObjectAttributeSerializer< V >( AbstractSpot.X_OFFSET, 24)
				{
					@Override
					public void notifySet( V vertex )
					{
						vertex.modelGraph.notifyVertexPositionChanged( vertex );
					}
				} );
	}

	public void recordSetPosition( final V vertex )
	{
		final UndoableEditRef ref = createRef();
		boolean createNewEdit = true;
		if ( nextEditIndex > 0 )
		{
			final UndoableEditRef edit = get( nextEditIndex - 1, ref );
			createNewEdit = !setVertexPosition.isInstance( edit ) || edit.isUndoPoint();
		}
		if ( createNewEdit )
			create( ref ).getEdit( setVertexPosition ).init( vertex );
		releaseRef( ref );
	}

	private final SetAttributeType< V > setVertexPosition;
}
