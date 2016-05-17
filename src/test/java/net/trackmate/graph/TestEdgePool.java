package net.trackmate.graph;

import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.ByteMappedElementArray;
import net.trackmate.pool.MemPool;
import net.trackmate.pool.PoolObject;
import net.trackmate.pool.SingleArrayMemPool;

public class TestEdgePool extends AbstractEdgePool< TestEdge, TestVertex, ByteMappedElement >
{
	public TestEdgePool( final int initialCapacity, final TestVertexPool vertexPool )
	{
		this( initialCapacity, new TestEdgeFactory(), vertexPool );
	}

	private TestEdgePool( final int initialCapacity, final TestEdgePool.TestEdgeFactory f, final TestVertexPool vertexPool )
	{
		super( initialCapacity, f, vertexPool );
		f.edgePool = this;
	}

	private static class TestEdgeFactory implements PoolObject.Factory< TestEdge, ByteMappedElement >
	{
		private TestEdgePool edgePool;

		@Override
		public int getSizeInBytes()
		{
			return TestEdge.SIZE_IN_BYTES;
		}

		@Override
		public TestEdge createEmptyRef()
		{
			return new TestEdge( edgePool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}
	};
}
