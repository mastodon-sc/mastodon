package org.mastodon.revised.bdv.overlay.wrap;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.RefPool;
import org.mastodon.adapter.RefBimap;
import org.mastodon.app.ViewGraph;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.util.AbstractRefPoolCollectionWrapper;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.bdv.overlay.OverlayGraph;
import org.mastodon.spatial.SpatioTemporalIndex;

/**
 * TODO: implement remaining ReadOnlyGraph methods
 * TODO: implement CollectionCreator
 *
 * @param <V>
 *            the type of the model vertex wrapped.
 * @param <E>
 *            the type of the model edge wrapped.
 *
 * @author Tobias Pietzsch
 */
public class OverlayGraphWrapper< V extends Vertex< E >, E extends Edge< V > > implements
		OverlayGraph< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >,
		ViewGraph< V, E, OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	final ReadOnlyGraph< V, E > wrappedGraph;

	final GraphIdBimap< V, E > idmap;

	final OverlayProperties< V, E > overlayProperties;

	private final ReentrantReadWriteLock lock;

	private final ConcurrentLinkedQueue< OverlayVertexWrapper< V, E > > tmpVertexRefs;

	private final ConcurrentLinkedQueue< OverlayEdgeWrapper< V, E > > tmpEdgeRefs;

	private final SpatioTemporalIndexWrapper< V, E > wrappedIndex;

	private final RefBimap< V, OverlayVertexWrapper< V, E > > vertexMap;

	private final RefBimap< E, OverlayEdgeWrapper< V, E > > edgeMap;

	public OverlayGraphWrapper(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final SpatioTemporalIndex< V > graphIndex,
			final ReentrantReadWriteLock lock,
			final OverlayProperties< V, E > overlayProperties )
	{
		this.wrappedGraph = graph;
		this.idmap = idmap;
		this.lock = lock;
		this.overlayProperties = overlayProperties;
		tmpVertexRefs =	new ConcurrentLinkedQueue<>();
		tmpEdgeRefs = new ConcurrentLinkedQueue<>();
		wrappedIndex = new SpatioTemporalIndexWrapper<>( this, graphIndex );
		vertexMap = new OverlayVertexWrapperBimap<>( this );
		edgeMap = new OverlayEdgeWrapperBimap<>( this );
	}

	@Override
	public OverlayVertexWrapper< V, E > vertexRef()
	{
		final OverlayVertexWrapper< V, E > ref = tmpVertexRefs.poll();
		return ref == null ? new OverlayVertexWrapper<>( this ) : ref;
	}

	@Override
	public OverlayEdgeWrapper< V, E > edgeRef()
	{
		final OverlayEdgeWrapper< V, E > ref = tmpEdgeRefs.poll();
		return ref == null ? new OverlayEdgeWrapper<>( this ) : ref;
	}

	@Override
	public void releaseRef( final OverlayVertexWrapper< V, E > ref )
	{
		tmpVertexRefs.add( ref );
	}

	@Override
	public void releaseRef( final OverlayEdgeWrapper< V, E > ref )
	{
		tmpEdgeRefs.add( ref );
	}

	@Override
	public OverlayEdgeWrapper< V, E > getEdge( final OverlayVertexWrapper< V, E > source, final OverlayVertexWrapper< V, E > target )
	{
		return getEdge( source, target, edgeRef() );
	}

	@Override
	public OverlayEdgeWrapper< V, E > getEdge( final OverlayVertexWrapper< V, E > source, final OverlayVertexWrapper< V, E > target, final OverlayEdgeWrapper< V, E > edge )
	{
		edge.we = wrappedGraph.getEdge( source.wv, target.wv, edge.ref );
		return edge.orNull();
	}

	@Override public Edges< OverlayEdgeWrapper< V, E > > getEdges( final OverlayVertexWrapper< V, E > source, final OverlayVertexWrapper< V, E > target )
	{
		return getEdges( source, target, vertexRef() );
	}

	@Override public Edges< OverlayEdgeWrapper< V, E > > getEdges( final OverlayVertexWrapper< V, E > source, final OverlayVertexWrapper< V, E > target, final OverlayVertexWrapper< V, E > ref )
	{
		final Edges< E > wes = wrappedGraph.getEdges( source.wv, target.wv, ref.wv );
		ref.edges.wrap( wes );
		return ref.edges;
	}

	@Override
	public RefCollection< OverlayVertexWrapper< V, E > > vertices()
	{
		return vertices;
	}

	@Override
	public RefCollection< OverlayEdgeWrapper< V, E > > edges()
	{
		return edges;
	}

	@Override
	public SpatioTemporalIndex< OverlayVertexWrapper< V, E > > getIndex()
	{
		return wrappedIndex;
	}

	@Override
	public double getMaxBoundingSphereRadiusSquared( final int timepoint )
	{
		return overlayProperties.getMaxBoundingSphereRadiusSquared( timepoint );
	}

	@Override
	public OverlayVertexWrapper< V, E > addVertex()
	{
		return addVertex( vertexRef() );
	}

	@Override
	public OverlayVertexWrapper< V, E > addVertex( final OverlayVertexWrapper< V, E > ref )
	{
		ref.wv = overlayProperties.addVertex( ref.ref );
		return ref;
	}

	@Override
	public OverlayEdgeWrapper< V, E > addEdge( final OverlayVertexWrapper< V, E > source, final OverlayVertexWrapper< V, E > target )
	{
		return addEdge( source, target, edgeRef() );
	}

	@Override
	public OverlayEdgeWrapper< V, E > addEdge( final OverlayVertexWrapper< V, E > source, final OverlayVertexWrapper< V, E > target, final OverlayEdgeWrapper< V, E > ref )
	{
		ref.we = overlayProperties.addEdge( source.wv, target.wv, ref.ref );
		return ref;
	}

	@Override
	public OverlayEdgeWrapper< V, E > insertEdge( final OverlayVertexWrapper< V, E > source, final int sourceOutIndex, final OverlayVertexWrapper< V, E > target, final int targetInIndex )
	{
		return insertEdge( source, sourceOutIndex, target, targetInIndex, edgeRef() );
	}

	@Override
	public OverlayEdgeWrapper< V, E > insertEdge( final OverlayVertexWrapper< V, E > source, final int sourceOutIndex, final OverlayVertexWrapper< V, E > target, final int targetInIndex, final OverlayEdgeWrapper< V, E > ref )
	{
		ref.we = overlayProperties.insertEdge( source.wv, sourceOutIndex, target.wv, targetInIndex, ref.ref );
		return ref;
	}

	@Override
	public void remove( final OverlayEdgeWrapper< V, E > edge )
	{
		overlayProperties.removeEdge( edge.we );
	}

	@Override
	public void removeAllLinkedEdges( final OverlayVertexWrapper< V, E > vertex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove( final OverlayVertexWrapper< V, E > vertex )
	{
		overlayProperties.removeVertex( vertex.wv );
	}

	@Override
	public ReentrantReadWriteLock getLock()
	{
		return lock;
	}

	@Override
	public void notifyGraphChanged()
	{
		overlayProperties.notifyGraphChanged();
	}

	/**
	 * Get bidirectional mapping between model vertices and view vertices.
	 *
	 * @return bidirectional mapping between model vertices and view vertices.
	 */
	@Override
	public RefBimap< V, OverlayVertexWrapper< V, E > > getVertexMap()
	{
		return vertexMap;
	}

	/**
	 * Get bidirectional mapping between model edges and view edges.
	 *
	 * @return bidirectional mapping between model edges and view edges.
	 */
	@Override
	public RefBimap< E, OverlayEdgeWrapper< V, E > > getEdgeMap()
	{
		return edgeMap;
	}

	private final RefPool< OverlayVertexWrapper< V, E > > vertexPool = new RefPool< OverlayVertexWrapper< V, E > >()
	{
		@Override
		public OverlayVertexWrapper< V, E > createRef()
		{
			return vertexRef();
		}

		@Override
		public void releaseRef( final OverlayVertexWrapper< V, E > v )
		{
			OverlayGraphWrapper.this.releaseRef( v );
		}

		@Override
		public OverlayVertexWrapper< V, E > getObject( final int index, final OverlayVertexWrapper< V, E > v )
		{
			v.wv = idmap.getVertex( index, v.ref );
			return v;
		}

		@Override
		public OverlayVertexWrapper< V, E > getObjectIfExists( final int index, final OverlayVertexWrapper< V, E > v )
		{
			v.wv = idmap.getVertexIfExists( index, v.ref );
			return v.wv == null ? null : v;
		}

		@Override
		public int getId( final OverlayVertexWrapper< V, E > v )
		{
			return idmap.getVertexId( v.wv );
		}

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		@Override
		public Class< OverlayVertexWrapper< V, E > > getRefClass()
		{
			return ( Class ) OverlayVertexWrapper.class;
		}
	};

	private final AbstractRefPoolCollectionWrapper< OverlayVertexWrapper< V, E >, RefPool< OverlayVertexWrapper< V, E > > > vertices = new AbstractRefPoolCollectionWrapper< OverlayVertexWrapper< V, E >, RefPool< OverlayVertexWrapper< V, E > > >( vertexPool )
	{
		@Override
		public int size()
		{
			return wrappedGraph.vertices().size();
		}

		@Override
		public Iterator< OverlayVertexWrapper< V, E > > iterator()
		{
			return new OverlayVertexIteratorWrapper<>( OverlayGraphWrapper.this, OverlayGraphWrapper.this.vertexRef(), wrappedGraph.vertices().iterator() );
		}
	};

	private final RefPool< OverlayEdgeWrapper< V, E > > edgePool = new RefPool< OverlayEdgeWrapper< V, E > >()
	{
		@Override
		public OverlayEdgeWrapper< V, E > createRef()
		{
			return edgeRef();
		}

		@Override
		public void releaseRef( final OverlayEdgeWrapper< V, E > e )
		{
			OverlayGraphWrapper.this.releaseRef( e );
		}

		@Override
		public OverlayEdgeWrapper< V, E > getObject( final int index, final OverlayEdgeWrapper< V, E > e )
		{
			e.we = idmap.getEdge( index, e.ref );
			return e;
		}

		@Override
		public OverlayEdgeWrapper< V, E > getObjectIfExists( final int index, final OverlayEdgeWrapper< V, E > e )
		{
			e.we = idmap.getEdgeIfExists( index, e.ref );
			return e.we == null ? null : e;
		}

		@Override
		public int getId( final OverlayEdgeWrapper< V, E > e )
		{
			return idmap.getEdgeId( e.we );
		}

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		@Override
		public Class< OverlayEdgeWrapper< V, E > > getRefClass()
		{
			return ( Class ) OverlayEdgeWrapper.class;
		}
	};

	private final AbstractRefPoolCollectionWrapper< OverlayEdgeWrapper< V, E >, RefPool< OverlayEdgeWrapper< V, E > > > edges = new AbstractRefPoolCollectionWrapper< OverlayEdgeWrapper< V, E >, RefPool< OverlayEdgeWrapper< V, E > > >( edgePool )
	{
		@Override
		public int size()
		{
			return wrappedGraph.edges().size();
		}

		@Override
		public Iterator< OverlayEdgeWrapper< V, E > > iterator()
		{
			return new OverlayEdgeIteratorWrapper<>( OverlayGraphWrapper.this, OverlayGraphWrapper.this.edgeRef(), wrappedGraph.edges().iterator() );
		}
	};

}
