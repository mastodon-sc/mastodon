package net.trackmate.revised.model;

import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.features.Features;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.undo.GraphUndoableEditList;
import net.trackmate.undo.UndoIdBimap;
import net.trackmate.undo.GraphUndoSerializer;
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

	protected final SetVertexPositionType setVertexPosition = new SetVertexPositionType();

	protected class SetVertexPositionType extends UndoableEditTypeImp< SetVertexPosition >
	{
		@Override
		public SetVertexPosition createInstance( final UndoableEditRef ref )
		{
			return new SetVertexPosition( ref, typeIndex() );
		}
	}

	private class SetVertexPosition extends AbstractClearableUndoableEdit
	{
		private final double[] pos;

		private final double[] tmp;

		SetVertexPosition( final UndoableEditRef ref, final int typeIndex )
		{
			super( ref, typeIndex );
			pos = new double[ numDimensions ];
			tmp = new double[ numDimensions ];
		}

		public void init( final V vertex )
		{
			super.init();
			final int vi = vertexUndoIdBimap.getId( vertex );
			vertex.localize( pos );

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( vi );
			for ( int d = 0; d < numDimensions; ++d )
				dataStack.out.writeDouble( pos[ d ] );

			ref.setDataIndex( dataIndex );
		}

		@Override
		public void redo()
		{
			final long d0 = ref.getDataIndex();
			final long d1 = swap( d0 );
			dataStack.setWriteDataIndex( d1 );
		}

		@Override
		public void undo()
		{
			final long d0 = ref.getDataIndex();
			swap( d0 );
			dataStack.setWriteDataIndex( d0 );
		}

		private long swap( final long dataIndex )
		{
			dataStack.setReadDataIndex( dataIndex );
			final int vi = dataStack.in.readInt();
			for ( int d = 0; d < numDimensions; ++d )
				tmp[ d ] = dataStack.in.readDouble();

			final V ref = graph.vertexRef();
			final V vertex = vertexUndoIdBimap.getObject( vi, ref );
			vertex.localize( pos );
			vertex.setPosition( tmp );
			graph.releaseRef( ref );

			dataStack.setWriteDataIndex( dataIndex );
//			dataStack.out.writeInt( vi );
			dataStack.out.skip( 4 );
			for ( int d = 0; d < numDimensions; ++d )
				dataStack.out.writeDouble( pos[ d ] );

			return dataStack.getWriteDataIndex();
		}
	}
}
