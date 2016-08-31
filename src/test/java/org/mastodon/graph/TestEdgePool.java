package org.mastodon.graph;

import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.SingleArrayMemPool;

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

		@Override
		public Class< TestEdge > getRefClass()
		{
			return TestEdge.class;
		}
	};
}
