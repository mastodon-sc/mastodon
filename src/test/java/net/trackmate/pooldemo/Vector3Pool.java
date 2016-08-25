package net.trackmate.pooldemo;

import net.trackmate.collection.util.PoolObjectCollectionCreator;
import net.trackmate.graph.features.Features;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.ByteMappedElementArray;
import net.trackmate.pool.MemPool;
import net.trackmate.pool.Pool;
import net.trackmate.pool.PoolObject;
import net.trackmate.pool.SingleArrayMemPool;

public class Vector3Pool extends Pool< Vector3, ByteMappedElement >
{
	public Vector3Pool( final int initialCapacity )
	{
		this( initialCapacity, new Vector3Factory() );
	}

	@Override
	public Vector3 create( final Vector3 obj )
	{
		return super.create( obj );
	}

	public Vector3 create()
	{
		return super.create( createRef() );
	}

	public void delete( final Vector3 obj )
	{
		deleteByInternalPoolIndex( obj.getInternalPoolIndex() );
	}

	private Vector3Pool( final int initialCapacity, final Vector3Pool.Vector3Factory f )
	{
		super( initialCapacity, f );
		f.pool = this;
		f.features = new Features<>( new PoolObjectCollectionCreator<>( this ) );
	}

	private static class Vector3Factory implements PoolObject.Factory< Vector3, ByteMappedElement >
	{
		private Vector3Pool pool;

		private Features< Vector3 > features;

		@Override
		public int getSizeInBytes()
		{
			return Vector3.SIZE_IN_BYTES;
		}

		@Override
		public Vector3 createEmptyRef()
		{
//			return new Vector3( pool );
			return new Vector3( pool, features );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public Class< Vector3 > getRefClass()
		{
			return Vector3.class;
		}
	};
}
