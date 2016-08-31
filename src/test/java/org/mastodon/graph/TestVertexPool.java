package org.mastodon.graph;

import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.SingleArrayMemPool;

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

		@Override
		public Class< TestVertex > getRefClass()
		{
			return TestVertex.class;
		}
	};
}
