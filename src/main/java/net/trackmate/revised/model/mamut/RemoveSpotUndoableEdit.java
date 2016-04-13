package net.trackmate.revised.model.mamut;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.revised.model.AbstractRemoveAbstractSpot3DUndoableEdit;
import net.trackmate.revised.undo.UndoIdBimap;

public class RemoveSpotUndoableEdit
	extends AbstractRemoveAbstractSpot3DUndoableEdit< RemoveSpotUndoableEdit, Spot, Link, ByteMappedElement >
{
	protected static final int COVARIANCE_OFFSET = AbstractRemoveAbstractSpot3DUndoableEdit.SIZE_IN_BYTES;
	protected static final int BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET = COVARIANCE_OFFSET + 6 * DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET + DOUBLE_SIZE;

	RemoveSpotUndoableEdit(
			final UndoableEditPool< RemoveSpotUndoableEdit > pool,
			final ModelGraph modelGraph,
			final UndoIdBimap< Spot > vertexUndoIdBimap
			)
	{
		super( pool, modelGraph, vertexUndoIdBimap );
	}

	@Override
	public RemoveSpotUndoableEdit init( final Spot vertex )
	{
		super.init( vertex );
		for ( int i = 0; i < 6; ++i )
			setFlattenedCovarianceEntry( vertex.getFlattenedCovarianceEntry( i ), i );
		setBoundingSphereRadiusSquared( vertex.getBoundingSphereRadiusSquared() );
		return this;
	}

	@Override
	protected void setVertexFields( final Spot vertex )
	{
		super.setVertexFields( vertex );
		for ( int i = 0; i < 6; ++i )
			vertex.setFlattenedCovarianceEntry( getFlattenedCovarianceEntry( i ), i );
		vertex.setBoundingSphereRadiusSquared( getBoundingSphereRadiusSquared() );
	}

	private double getFlattenedCovarianceEntry( final int index )
	{
		return access.getDouble( COVARIANCE_OFFSET + index * DOUBLE_SIZE );
	}

	private void setFlattenedCovarianceEntry( final double entry, final int index )
	{
		access.putDouble( entry, COVARIANCE_OFFSET + index * DOUBLE_SIZE );
	}

	private double getBoundingSphereRadiusSquared()
	{
		return access.getDouble( BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
	}

	private void setBoundingSphereRadiusSquared( final double r2 )
	{
		access.putDouble( r2, BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
	}

	public static class RemoveSpotUndoableEditPool extends UndoableEditPool< RemoveSpotUndoableEdit >
	{
		public RemoveSpotUndoableEditPool(
				final int initialCapacity,
				final ModelGraph modelGraph,
				final UndoIdBimap< Spot > vertexUndoIdBimap )
		{
			this( initialCapacity, new Factory( modelGraph, vertexUndoIdBimap ) );
		}

		private RemoveSpotUndoableEditPool( final int initialCapacity, final Factory f )
		{
			super( initialCapacity, f );
			f.pool = this;
		}

		@Override
		public RemoveSpotUndoableEdit create( final RemoveSpotUndoableEdit vertex )
		{
			return super.create( vertex );
		}

		private static class Factory implements PoolObject.Factory< RemoveSpotUndoableEdit, ByteMappedElement >
		{
			private RemoveSpotUndoableEditPool pool;

			private final ModelGraph modelGraph;

			private final UndoIdBimap< Spot > vertexUndoIdBimap;

			public Factory(
					final ModelGraph modelGraph,
					final UndoIdBimap< Spot > vertexUndoIdBimap )
			{
				this.modelGraph = modelGraph;
				this.vertexUndoIdBimap = vertexUndoIdBimap;
			}

			@Override
			public int getSizeInBytes()
			{
				return RemoveSpotUndoableEdit.SIZE_IN_BYTES;
			}

			@Override
			public RemoveSpotUndoableEdit createEmptyRef()
			{
				return new RemoveSpotUndoableEdit( pool, modelGraph, vertexUndoIdBimap );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}
}
