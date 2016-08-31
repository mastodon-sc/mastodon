package org.mastodon.graph;

import org.mastodon.graph.Graph;
import org.mastodon.graph.ref.AbstractEdge;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractVertex;
import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.graph.ref.GraphImp;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.SingleArrayMemPool;

public class MinimalGraphExample
{
	static class MyVertex extends AbstractVertex< MyVertex, MyEdge, ByteMappedElement >
	{
		protected static final int SIZE_IN_BYTES = AbstractVertex.SIZE_IN_BYTES;

		protected MyVertex( final AbstractVertexPool< MyVertex, ?, ByteMappedElement > pool )
		{
			super( pool );
		}
	}

	static class MyEdge extends AbstractEdge< MyEdge, MyVertex, ByteMappedElement >
	{
		protected static final int SIZE_IN_BYTES = AbstractEdge.SIZE_IN_BYTES;

		protected MyEdge( final AbstractEdgePool< MyEdge, MyVertex, ByteMappedElement > pool )
		{
			super( pool );
		}
	}

	static class MyVertexPool extends AbstractVertexPool< MyVertex, MyEdge, ByteMappedElement >
	{
		public MyVertexPool( final int initialCapacity )
		{
			this( initialCapacity, new MyVertexFactory() );
		}

		private MyVertexPool( final int initialCapacity, final MyVertexFactory f )
		{
			super( initialCapacity, f );
			f.vertexPool = this;
		}

		private static class MyVertexFactory implements PoolObject.Factory< MyVertex, ByteMappedElement >
		{
			private MyVertexPool vertexPool;

			@Override
			public int getSizeInBytes()
			{
				return MyVertex.SIZE_IN_BYTES;
			}

			@Override
			public MyVertex createEmptyRef()
			{
				return new MyVertex( vertexPool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}

			@Override
			public Class< MyVertex > getRefClass()
			{
				return MyVertex.class;
			}
		};
	}

	static class MyEdgePool extends AbstractEdgePool< MyEdge, MyVertex, ByteMappedElement >
	{
		public MyEdgePool( final int initialCapacity, final MyVertexPool vertexPool )
		{
			this( initialCapacity, new MyEdgeFactory(), vertexPool );
		}

		private MyEdgePool( final int initialCapacity, final MyEdgeFactory f, final MyVertexPool vertexPool )
		{
			super( initialCapacity, f, vertexPool );
			f.edgePool = this;
		}

		private static class MyEdgeFactory implements PoolObject.Factory< MyEdge, ByteMappedElement >
		{
			private MyEdgePool edgePool;

			@Override
			public int getSizeInBytes()
			{
				return MyEdge.SIZE_IN_BYTES;
			}

			@Override
			public MyEdge createEmptyRef()
			{
				return new MyEdge( edgePool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}

			@Override
			public Class< MyEdge > getRefClass()
			{
				return MyEdge.class;
			}
		};
	}

	public static void main( final String[] args )
	{
		final int initialCapacity = 1000;
		final MyVertexPool vertexPool = new MyVertexPool( initialCapacity );
		final MyEdgePool edgePool = new MyEdgePool( initialCapacity, vertexPool );
		final Graph< MyVertex, MyEdge > graph = new GraphImp<>( edgePool );
	}
}
