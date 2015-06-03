package net.trackmate.trackscheme;

import java.util.Arrays;

import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.GraphImp;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

public class TrackSchemeGraph extends GraphImp< TrackSchemeGraph.TrackSchemeVertexPool, TrackSchemeGraph.TrackSchemeEdgePool, TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
{
	public TrackSchemeGraph()
	{
		this( 10000 );
	}

	public TrackSchemeGraph( final int initialCapacity )
	{
		super( new TrackSchemeEdgePool( initialCapacity, new TrackSchemeVertexPool( initialCapacity ) ) );
	}

	public static class TrackSchemeVertexPool extends AbstractVertexPool< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
	{
		public TrackSchemeVertexPool( final int initialCapacity )
		{
			this( initialCapacity, new VertexFactory( initialCapacity ) );
		}

		private TrackSchemeVertexPool( final int initialCapacity, final VertexFactory f )
		{
			super( initialCapacity, f );
			f.vertexPool = this;
		}

		private static class VertexFactory implements PoolObject.Factory< TrackSchemeVertex, ByteMappedElement >
		{
			private TrackSchemeVertexPool vertexPool;

			private final Labels labels;

			public VertexFactory( final int initialCapacity )
			{
				labels = new Labels( initialCapacity );
			}

			@Override
			public int getSizeInBytes()
			{
				return TrackSchemeVertex.SIZE_IN_BYTES;
			}

			@Override
			public TrackSchemeVertex createEmptyRef()
			{
				return new TrackSchemeVertex( vertexPool, labels );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}

	public static class TrackSchemeEdgePool extends AbstractEdgePool< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement >
	{
		public TrackSchemeEdgePool( final int initialCapacity, final TrackSchemeVertexPool vertexPool )
		{
			this( initialCapacity, new EdgeFactory(), vertexPool );
			vertexPool.linkEdgePool( this );
		}

		private TrackSchemeEdgePool( final int initialCapacity, final EdgeFactory f, final TrackSchemeVertexPool vertexPool )
		{
			super( initialCapacity, f, vertexPool );
			f.edgePool = this;
		}

		private static class EdgeFactory implements PoolObject.Factory< TrackSchemeEdge, ByteMappedElement >
		{
			private TrackSchemeEdgePool edgePool;

			@Override
			public int getSizeInBytes()
			{
				return TrackSchemeEdge.SIZE_IN_BYTES;
			}

			@Override
			public TrackSchemeEdge createEmptyRef()
			{
				return new TrackSchemeEdge( edgePool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}

	public int numVertices()
	{
		return vertexPool.size();
	}

	public int numEdges()
	{
		return edgePool.size();
	}

	public void clear()
	{
		vertexPool.clear();
		edgePool.clear();
	}

	public Iterable< TrackSchemeVertex > vertices()
	{
		return vertexPool;
	}

	public Iterable< TrackSchemeEdge > edges()
	{
		return edgePool;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "TrackSchemeGraph {\n" );
		sb.append( "  vertices = {\n" );
		for ( final TrackSchemeVertex vertex : vertices() )
			sb.append( "    " + vertex + "\n" );
		sb.append( "  },\n" );
		sb.append( "  edges = {\n" );
		for ( final TrackSchemeEdge edge : edges() )
			sb.append( "    " + edge + "\n" );
		sb.append( "  }\n" );
		sb.append( "}" );
		return sb.toString();
	}

	public TrackSchemeVertexPool getVertexPool()
	{
		return vertexPool;
	}

	public TrackSchemeEdgePool getEdgePool()
	{
		return edgePool;
	}

	public static void main( final String[] args )
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		final TrackSchemeVertex v1 = graph.addVertex().init( "1", 0, false );
		final TrackSchemeVertex v2 = graph.addVertex().init( "2", 1, false );
		final TrackSchemeVertex v3 = graph.addVertex().init( "3", 1, false );;
		final TrackSchemeVertex v4 = graph.addVertex().init( "4", 2, false );;
		graph.addEdge( v1, v2 );
		graph.addEdge( v1, v3 );
		graph.addEdge( v3, v4 );
		System.out.println( graph );
		System.out.println();

		final LineageTreeLayout layout = new LineageTreeLayout( graph );
		layout.layoutX( Arrays.asList( v1 ) );
		System.out.println( graph );

		System.out.println( "done" );
	}
}
