package net.trackmate.revised.trackscheme;

import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.GraphImp;
import net.trackmate.graph.IntPoolObjectArrayMap;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.IntRefMap;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.revised.trackscheme.ModelGraphProperties.ModelVertexProperties;
import net.trackmate.spatial.HasTimepoint;

public class TrackSchemeGraph<
		V extends Vertex< E > & HasTimepoint,
		E extends Edge< V > >
	extends GraphImp<
				TrackSchemeGraph.TrackSchemeVertexPool,
				TrackSchemeGraph.TrackSchemeEdgePool,
				TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
	implements GraphListener< V, E >
{
	private final ListenableGraph< V, E > modelGraph;

	private final GraphIdBimap< V, E > idmap;

	private final IntRefMap< TrackSchemeVertex > idToTrackSchemeVertex;

	private final IntRefMap< TrackSchemeEdge > idToTrackSchemeEdge;

	private final ModelVertexProperties modelVertexProperties;

	private final RefSet< TrackSchemeVertex > roots;

	private V mv;

	private final TrackSchemeVertex tsv;

	private final TrackSchemeVertex tsv2;

	private final TrackSchemeEdge tse;

	public TrackSchemeGraph(
			final ListenableGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final ModelGraphProperties modelGraphProperties )
	{
		this( modelGraph, idmap, modelGraphProperties, 10000 );
	}

	public TrackSchemeGraph(
			final ListenableGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final ModelGraphProperties modelGraphProperties,
			final int initialCapacity )
	{
		super( new TrackSchemeEdgePool(
				modelGraphProperties, initialCapacity,
				new TrackSchemeVertexPool( modelGraphProperties, initialCapacity ) ) );
		this.modelGraph = modelGraph;
		this.idmap = idmap;
		idToTrackSchemeVertex =	new IntPoolObjectArrayMap< TrackSchemeVertex >( vertexPool );
		idToTrackSchemeEdge = new IntPoolObjectArrayMap< TrackSchemeEdge >( edgePool );
		roots = new PoolObjectSet< TrackSchemeVertex >( vertexPool );
		mv = modelGraph.vertexRef();
		tsv = vertexRef();
		tsv2 = vertexRef();
		tse = edgeRef();
		modelVertexProperties = modelGraphProperties.createVertexProperties();
		modelGraph.addGraphListener( this );
		graphRebuilt();
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
		sb.append( "},\n" );
		sb.append( "  roots = {\n" );
		for ( final TrackSchemeVertex vertex : roots )
			sb.append( "    " + vertex + "\n" );
		sb.append( "  },\n" );
		sb.append( "}" );
		return sb.toString();
	}

	/*
	 * GraphListener
	 */

	@Override
	public void graphRebuilt()
	{
		idToTrackSchemeVertex.clear();
		idToTrackSchemeEdge.clear();

		for ( final V v : modelGraph.vertices() )
		{
			final int id = idmap.getVertexId( v );
			final int timepoint = v.getTimepoint();
			addVertex( tsv ).init( id, timepoint );
			idToTrackSchemeVertex.put( id, tsv );
			if ( v.incomingEdges().isEmpty() )
				roots.add( tsv );
		}
		for ( final E e : modelGraph.edges() )
		{
			final int id = idmap.getEdgeId( e );
			idToTrackSchemeVertex.get( idmap.getVertexId( e.getSource( mv ) ), tsv );
			idToTrackSchemeVertex.get( idmap.getVertexId( e.getTarget( mv ) ), tsv2 );
			addEdge( tsv, tsv2, tse );
			idToTrackSchemeEdge.put( id, tse );
		}
	}

	@Override
	public void vertexAdded( final V vertex )
	{
		final int id = idmap.getVertexId( vertex );
		addVertex( tsv ).init( id, vertex.getTimepoint() );
		idToTrackSchemeVertex.put( id, tsv );
		roots.add( tsv );
	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		final int id = idmap.getVertexId( vertex );
		if ( idToTrackSchemeVertex.remove( id, tsv ) != null )
		{
			if ( tsv.incomingEdges().isEmpty() )
				roots.remove( tsv );
			this.remove( tsv );
		}
	}

	@Override
	public void edgeAdded( final E edge )
	{
		final int id = idmap.getEdgeId( edge );
		idToTrackSchemeVertex.get( idmap.getVertexId( edge.getSource( mv ) ), tsv );
		idToTrackSchemeVertex.get( idmap.getVertexId( edge.getTarget( mv ) ), tsv2 );
		if ( tsv2.incomingEdges().isEmpty() )
			roots.remove( tsv2 );
		addEdge( tsv, tsv2, tse );
		idToTrackSchemeEdge.put( id, tse );
	}

	@Override
	public void edgeRemoved( final E edge )
	{
		final int id = idmap.getEdgeId( edge );
		if ( idToTrackSchemeEdge.remove( id, tse ) != null )
		{
			if ( tse.getTarget( tsv ).incomingEdges().size() == 1 )
				roots.add( tsv );
			this.remove( tse );
		}
	}

//	@Override // TODO: should be implemented for some listener interface
	public void vertexTimepointChanged( final V vertex )
	{
		idToTrackSchemeVertex.get( idmap.getVertexId( vertex ), tsv );
		tsv.setTimepoint( vertex.getTimepoint() );
	}

	/*
	 * vertex and edge pools
	 */

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
}
