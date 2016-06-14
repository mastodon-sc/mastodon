package net.trackmate.revised.model.mamut;

import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.features.Features;
import net.trackmate.revised.model.ModelUndoableEditList;
import net.trackmate.undo.UndoIdBimap;
import net.trackmate.undo.GraphUndoSerializer;
import net.trackmate.undo.UndoableEditRef;

public class ModelSpotUndoableEditList extends ModelUndoableEditList< Spot, Link >
{

	public ModelSpotUndoableEditList(
			final int initialCapacity,
			final ListenableGraph< Spot, Link > graph,
			final Features< Spot > vertexFeatures,
			final Features< Link > edgeFeatures,
			final GraphUndoSerializer< Spot, Link > serializer,
			final UndoIdBimap< Spot > vertexUndoIdBimap,
			final UndoIdBimap< Link > edgeUndoIdBimap )
	{
		super( initialCapacity, graph, vertexFeatures, edgeFeatures, serializer, vertexUndoIdBimap, edgeUndoIdBimap );
	}

	public void recordSetCovariance( final Spot vertex )
	{
		final UndoableEditRef ref = createRef();
		boolean createNewEdit = true;
		if ( nextEditIndex > 0 )
		{
			final UndoableEditRef edit = get( nextEditIndex - 1, ref );
			createNewEdit = !setVertexPosition.isInstance( edit ) || edit.isUndoPoint();
		}
		if ( createNewEdit )
			create( ref ).getEdit( setVertexCovariance ).init( vertex );
		releaseRef( ref );
	}

	protected final SetVertexCovarianceType setVertexCovariance = new SetVertexCovarianceType();

	protected class SetVertexCovarianceType extends UndoableEditTypeImp< SetVertexCovariance >
	{
		@Override
		public SetVertexCovariance createInstance( final UndoableEditRef ref )
		{
			return new SetVertexCovariance( ref, typeIndex() );
		}
	}

	private class SetVertexCovariance extends AbstractClearableUndoableEdit
	{
		private final double[][] mat;

		private final double[][] tmp;

		SetVertexCovariance( final UndoableEditRef ref, final int typeIndex )
		{
			super( ref, typeIndex );
			mat = new double[ numDimensions ][ numDimensions ];
			tmp = new double[ numDimensions ][ numDimensions ];
		}

		public void init( final Spot vertex )
		{
			super.init();
			final int vi = vertexUndoIdBimap.getId( vertex );
			vertex.getCovariance( mat );

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( vi );
			for ( int d1 = 0; d1 < numDimensions; ++d1 )
				for ( int d2 = 0; d2 < numDimensions; ++d2 )
					dataStack.out.writeDouble( mat[ d1 ][ d2 ] );

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
			for ( int d1 = 0; d1 < numDimensions; ++d1 )
				for ( int d2 = 0; d2 < numDimensions; ++d2 )
					tmp[ d1 ][ d2 ] = dataStack.in.readDouble();

			final Spot ref = graph.vertexRef();
			final Spot vertex = vertexUndoIdBimap.getObject( vi, ref );
			vertex.getCovariance( mat );
			vertex.setCovariance( tmp );
			graph.releaseRef( ref );

			dataStack.setWriteDataIndex( dataIndex );
			dataStack.out.skip( 4 );
			for ( int d1 = 0; d1 < numDimensions; ++d1 )
				for ( int d2 = 0; d2 < numDimensions; ++d2 )
					dataStack.out.writeDouble( mat[ d1 ][ d2 ] );

			return dataStack.getWriteDataIndex();
		}
	}
}
