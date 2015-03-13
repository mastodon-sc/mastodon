package net.trackmate.graph;

import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

class TestObjectPool extends Pool< TestObject, ByteMappedElement >
{
	public TestObjectPool( final int initialCapacity )
	{
		this( initialCapacity, new TestObjectFactory() );
	}

	@Override
	public TestObject create( final TestObject obj )
	{
		return super.create( obj );
	}

	public TestObject create()
	{
		return super.create( createRef() );
	}

	public void release( final TestObject obj )
	{
		releaseByInternalPoolIndex( obj.getInternalPoolIndex() );
	}

	private TestObjectPool( final int initialCapacity, final TestObjectPool.TestObjectFactory f )
	{
		super( initialCapacity, f );
		f.pool = this;
	}

	private static class TestObjectFactory implements PoolObject.Factory< TestObject, ByteMappedElement >
	{
		private TestObjectPool pool;

		@Override
		public int getSizeInBytes()
		{
			return TestObject.SIZE_IN_BYTES;
		}

		@Override
		public TestObject createEmptyRef()
		{
			return new TestObject( pool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}
	};
}
