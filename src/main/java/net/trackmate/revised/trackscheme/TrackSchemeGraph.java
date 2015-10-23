package net.trackmate.revised.trackscheme;

import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphImp;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

public class TrackSchemeGraph<
		V extends Vertex< E >,
		E extends Edge< V > >
	extends GraphImp<
				TrackSchemeGraph.TrackSchemeVertexPool,
				TrackSchemeGraph.TrackSchemeEdgePool,
				TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
	implements GraphListener< V, E >
{
	public TrackSchemeGraph(
			final ListenableGraph< V, E > modelGraph,
			final ModelGraphProperties modelGraphProperties )
	{
		this( modelGraph, modelGraphProperties, 10000 );
	}

	public TrackSchemeGraph(
			final ListenableGraph< V, E > modelGraph,
			final ModelGraphProperties modelGraphProperties,
			final int initialCapacity )
	{
		super( new TrackSchemeEdgePool(
				modelGraphProperties, initialCapacity,
				new TrackSchemeVertexPool( modelGraphProperties, initialCapacity ) ) );
		modelGraph.addGraphListener( this );
	}

	static class TrackSchemeVertexPool extends AbstractVertexPool< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
	{
		private TrackSchemeVertexPool( final ModelGraphProperties props, final int initialCapacity )
		{
			this( initialCapacity, new VertexFactory( props ) );
		}

		private TrackSchemeVertexPool( final int initialCapacity, final VertexFactory f )
		{
			super( initialCapacity, f );
			f.vertexPool = this;
		}

		private static class VertexFactory implements PoolObject.Factory< TrackSchemeVertex, ByteMappedElement >
		{
			private TrackSchemeVertexPool vertexPool;

			private final ModelGraphProperties props;

			private VertexFactory( final ModelGraphProperties modelGraphProperties )
			{
				this.props = modelGraphProperties;
			}

			@Override
			public int getSizeInBytes()
			{
				return TrackSchemeVertex.SIZE_IN_BYTES;
			}

			@Override
			public TrackSchemeVertex createEmptyRef()
			{
				return new TrackSchemeVertex( vertexPool, props.createVertexProperties() );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		}
	}

	static class TrackSchemeEdgePool extends AbstractEdgePool< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement >
	{
		private TrackSchemeEdgePool( final ModelGraphProperties props, final int initialCapacity, final TrackSchemeVertexPool vertexPool )
		{
			this( props, initialCapacity, new EdgeFactory( props ), vertexPool );
			vertexPool.linkEdgePool( this );
		}

		private TrackSchemeEdgePool( final ModelGraphProperties props, final int initialCapacity, final EdgeFactory f, final TrackSchemeVertexPool vertexPool )
		{
			super( initialCapacity, f, vertexPool );
			f.edgePool = this;
		}

		private static class EdgeFactory implements PoolObject.Factory< TrackSchemeEdge, ByteMappedElement >
		{
			private TrackSchemeEdgePool edgePool;

			private final ModelGraphProperties props;

			private EdgeFactory( final ModelGraphProperties modelGraphProperties )
			{
				this.props = modelGraphProperties;
			}

			@Override
			public int getSizeInBytes()
			{
				return TrackSchemeEdge.SIZE_IN_BYTES;
			}

			@Override
			public TrackSchemeEdge createEmptyRef()
			{
				return new TrackSchemeEdge( edgePool, props.createEdgeProperties() );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}

	public RefPool< TrackSchemeVertex > getVertexPool()
	{
		return vertexPool;
	}

	public RefPool< TrackSchemeEdge > getEdgePool()
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

	/*
	 * GraphListener
	 */

	@Override
	public void graphRebuilt()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void vertexAdded( final V vertex )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void edgeAdded( final E edge )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void edgeRemoved( final E edge )
	{
		// TODO Auto-generated method stub

	}

//	@Override // TODO: should be implemented for some listener interface
	public void vertexTimepointChanged( final V vertex )
	{
		// TODO
	}
}
