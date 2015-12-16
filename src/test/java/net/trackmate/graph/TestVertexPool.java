package net.trackmate.graph;

import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

public class TestVertexPool extends AbstractVertexPool< TestVertex, TestEdge, ByteMappedElement >
{
	public TestVertexPool( final int initialCapacity )
	{
		this( initialCapacity, new TestVertexFactory() );
	}

	private TestVertexPool( final int initialCapacity, final TestVertexPool.TestVertexFactory f )
	{
		super( initialCapacity, f );
		f.vertexPool = this;
	}

	private static class TestVertexFactory implements PoolObject.Factory< TestVertex, ByteMappedElement >
	{
		private TestVertexPool vertexPool;

		@Override
		public int getSizeInBytes()
		{
			return TestVertex.SIZE_IN_BYTES;
		}

		@Override
		public TestVertex createEmptyRef()
		{
			return new TestVertex( vertexPool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}
	};
}
