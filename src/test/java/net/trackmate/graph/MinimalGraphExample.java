package net.trackmate.graph;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.GraphImp;
import net.trackmate.graph.zzgraphinterfaces.Graph;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.ByteMappedElementArray;
import net.trackmate.pool.MemPool;
import net.trackmate.pool.PoolObject;
import net.trackmate.pool.SingleArrayMemPool;

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
		};
	}

	public static void main( final String[] args )
	{
		final int initialCapacity = 1000;
		final MyVertexPool vertexPool = new MyVertexPool( initialCapacity );
		final MyEdgePool edgePool = new MyEdgePool( initialCapacity, vertexPool );
		final Graph< MyVertex, MyEdge > graph = GraphImp.create( edgePool );
	}
}
