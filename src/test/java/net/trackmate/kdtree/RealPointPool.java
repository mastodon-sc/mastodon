package net.trackmate.kdtree;

import net.imglib2.EuclideanSpace;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.ByteMappedElementArray;
import net.trackmate.pool.MemPool;
import net.trackmate.pool.Pool;
import net.trackmate.pool.PoolObject;
import net.trackmate.pool.SingleArrayMemPool;

class RealPointPool extends Pool< RealPoint, ByteMappedElement > implements EuclideanSpace
{
	private final int n;

	public RealPointPool( final int numDimensions, final int initialCapacity )
	{
		this( numDimensions, initialCapacity, new RealPointFactory( numDimensions ) );
	}

	@Override
	public RealPoint create( final RealPoint obj )
	{
		return super.create( obj );
	}

	public RealPoint create()
	{
		return super.create( createRef() );
	}

	public void delete( final RealPoint obj )
	{
		deleteByInternalPoolIndex( obj.getInternalPoolIndex() );
	}

	@Override
	public int numDimensions()
	{
		return n;
	};

	private RealPointPool( final int numDimensions, final int initialCapacity, final RealPointFactory f )
	{
		super( initialCapacity, f );
		n = numDimensions;
		f.pool = this;
	}

	private static class RealPointFactory implements PoolObject.Factory< RealPoint, ByteMappedElement >
	{
		private RealPointPool pool;

		private final int n;

		private RealPointFactory( final int numDimensions )
		{
			this.n = numDimensions;
		}

		@Override
		public int getSizeInBytes()
		{
			return RealPoint.SIZE_IN_BYTES( n );
		}

		@Override
		public RealPoint createEmptyRef()
		{
			return new RealPoint( pool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public Class< RealPoint > getRefClass()
		{
			return RealPoint.class;
		}
	}
}
