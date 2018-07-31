package org.mastodon.revised.bvv.wrap;

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
import org.mastodon.revised.bdv.overlay.wrap.OverlayProperties;
import org.mastodon.revised.bvv.BvvGraph;
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
public class BvvGraphWrapper< V extends Vertex< E >, E extends Edge< V > > implements
		BvvGraph< BvvVertexWrapper< V, E >, BvvEdgeWrapper< V, E > >,
		ViewGraph< V, E, BvvVertexWrapper< V, E >, BvvEdgeWrapper< V, E > >
{
	final ReadOnlyGraph< V, E > wrappedGraph;

	final GraphIdBimap< V, E > idmap;

	final OverlayProperties< V, E > overlayProperties;

	private final ReentrantReadWriteLock lock;

	private final ConcurrentLinkedQueue< BvvVertexWrapper< V, E > > tmpVertexRefs;

	private final ConcurrentLinkedQueue< BvvEdgeWrapper< V, E > > tmpEdgeRefs;

	private final BvvSpatioTemporalIndexWrapper< V, E > wrappedIndex;

	private final RefBimap< V, BvvVertexWrapper< V, E > > vertexMap;

	private final RefBimap< E, BvvEdgeWrapper< V, E > > edgeMap;

	public BvvGraphWrapper(
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
		wrappedIndex = new BvvSpatioTemporalIndexWrapper<>( this, graphIndex );
		vertexMap = new BvvVertexWrapperBimap<>( this );
		edgeMap = new BvvEdgeWrapperBimap<>( this );
	}

	@Override
	public BvvVertexWrapper< V, E > vertexRef()
	{
		final BvvVertexWrapper< V, E > ref = tmpVertexRefs.poll();
		return ref == null ? new BvvVertexWrapper<>( this ) : ref;
	}

	@Override
	public BvvEdgeWrapper< V, E > edgeRef()
	{
		final BvvEdgeWrapper< V, E > ref = tmpEdgeRefs.poll();
		return ref == null ? new BvvEdgeWrapper<>( this ) : ref;
	}

	@Override
	public void releaseRef( final BvvVertexWrapper< V, E > ref )
	{
		tmpVertexRefs.add( ref );
	}

	@Override
	public void releaseRef( final BvvEdgeWrapper< V, E > ref )
	{
		tmpEdgeRefs.add( ref );
	}

	@Override
	public BvvEdgeWrapper< V, E > getEdge( final BvvVertexWrapper< V, E > source, final BvvVertexWrapper< V, E > target )
	{
		return getEdge( source, target, edgeRef() );
	}

	@Override
	public BvvEdgeWrapper< V, E > getEdge( final BvvVertexWrapper< V, E > source, final BvvVertexWrapper< V, E > target, final BvvEdgeWrapper< V, E > edge )
	{
		edge.we = wrappedGraph.getEdge( source.wv, target.wv, edge.ref );
		return edge.orNull();
	}

	@Override
	public Edges< BvvEdgeWrapper< V, E > > getEdges( final BvvVertexWrapper< V, E > source, final BvvVertexWrapper< V, E > target )
	{
		return getEdges( source, target, vertexRef() );
	}

	@Override
	public Edges< BvvEdgeWrapper< V, E > > getEdges( final BvvVertexWrapper< V, E > source, final BvvVertexWrapper< V, E > target, final BvvVertexWrapper< V, E > ref )
	{
		final Edges< E > wes = wrappedGraph.getEdges( source.wv, target.wv, ref.wv );
		ref.edges.wrap( wes );
		return ref.edges;
	}

	@Override
	public RefCollection< BvvVertexWrapper< V, E > > vertices()
	{
		return vertices;
	}

	@Override
	public RefCollection< BvvEdgeWrapper< V, E > > edges()
	{
		return edges;
	}

	@Override
	public SpatioTemporalIndex< BvvVertexWrapper< V, E > > getIndex()
	{
		return wrappedIndex;
	}

	@Override
	public double getMaxBoundingSphereRadiusSquared( final int timepoint )
	{
		return overlayProperties.getMaxBoundingSphereRadiusSquared( timepoint );
	}

	@Override
	public BvvVertexWrapper< V, E > addVertex()
	{
		return addVertex( vertexRef() );
	}

	@Override
	public BvvVertexWrapper< V, E > addVertex( final BvvVertexWrapper< V, E > ref )
	{
		throw new UnsupportedOperationException( "NOT IMPLEMENTED" );
//		ref.wv = overlayProperties.addVertex( ref.ref );
//		return ref;
	}

	@Override
	public BvvEdgeWrapper< V, E > addEdge( final BvvVertexWrapper< V, E > source, final BvvVertexWrapper< V, E > target )
	{
		return addEdge( source, target, edgeRef() );
	}

	@Override
	public BvvEdgeWrapper< V, E > addEdge( final BvvVertexWrapper< V, E > source, final BvvVertexWrapper< V, E > target, final BvvEdgeWrapper< V, E > ref )
	{
		throw new UnsupportedOperationException( "NOT IMPLEMENTED" );
//		ref.we = overlayProperties.addEdge( source.wv, target.wv, ref.ref );
//		return ref;
	}

	@Override
	public BvvEdgeWrapper< V, E > insertEdge( final BvvVertexWrapper< V, E > source, final int sourceOutIndex, final BvvVertexWrapper< V, E > target, final int targetInIndex )
	{
		return insertEdge( source, sourceOutIndex, target, targetInIndex, edgeRef() );
	}

	@Override
	public BvvEdgeWrapper< V, E > insertEdge( final BvvVertexWrapper< V, E > source, final int sourceOutIndex, final BvvVertexWrapper< V, E > target, final int targetInIndex, final BvvEdgeWrapper< V, E > ref )
	{
		throw new UnsupportedOperationException( "NOT IMPLEMENTED" );
//		ref.we = overlayProperties.insertEdge( source.wv, sourceOutIndex, target.wv, targetInIndex, ref.ref );
//		return ref;
	}

	@Override
	public void remove( final BvvEdgeWrapper< V, E > edge )
	{
		overlayProperties.removeEdge( edge.we );
	}

	@Override
	public void remove( final BvvVertexWrapper< V, E > vertex )
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
	public RefBimap< V, BvvVertexWrapper< V, E > > getVertexMap()
	{
		return vertexMap;
	}

	/**
	 * Get bidirectional mapping between model edges and view edges.
	 *
	 * @return bidirectional mapping between model edges and view edges.
	 */
	@Override
	public RefBimap< E, BvvEdgeWrapper< V, E > > getEdgeMap()
	{
		return edgeMap;
	}

	private final RefPool< BvvVertexWrapper< V, E > > vertexPool = new RefPool< BvvVertexWrapper< V, E > >()
	{
		@Override
		public BvvVertexWrapper< V, E > createRef()
		{
			return vertexRef();
		}

		@Override
		public void releaseRef( final BvvVertexWrapper< V, E > v )
		{
			BvvGraphWrapper.this.releaseRef( v );
		}

		@Override
		public BvvVertexWrapper< V, E > getObject( final int index, final BvvVertexWrapper< V, E > v )
		{
			v.wv = idmap.getVertex( index, v.ref );
			return v;
		}

		@Override
		public BvvVertexWrapper< V, E > getObjectIfExists( final int index, final BvvVertexWrapper< V, E > v )
		{
			v.wv = idmap.getVertexIfExists( index, v.ref );
			return v.wv == null ? null : v;
		}

		@Override
		public int getId( final BvvVertexWrapper< V, E > v )
		{
			return idmap.getVertexId( v.wv );
		}

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		@Override
		public Class< BvvVertexWrapper< V, E > > getRefClass()
		{
			return ( Class ) BvvVertexWrapper.class;
		}
	};

	private final AbstractRefPoolCollectionWrapper< BvvVertexWrapper< V, E >, RefPool< BvvVertexWrapper< V, E > > > vertices = new AbstractRefPoolCollectionWrapper< BvvVertexWrapper< V, E >, RefPool< BvvVertexWrapper< V, E > > >( vertexPool )
	{
		@Override
		public int size()
		{
			return wrappedGraph.vertices().size();
		}

		@Override
		public Iterator< BvvVertexWrapper< V, E > > iterator()
		{
			return new BvvVertexIteratorWrapper<>( BvvGraphWrapper.this, BvvGraphWrapper.this.vertexRef(), wrappedGraph.vertices().iterator() );
		}
	};

	private final RefPool< BvvEdgeWrapper< V, E > > edgePool = new RefPool< BvvEdgeWrapper< V, E > >()
	{
		@Override
		public BvvEdgeWrapper< V, E > createRef()
		{
			return edgeRef();
		}

		@Override
		public void releaseRef( final BvvEdgeWrapper< V, E > e )
		{
			BvvGraphWrapper.this.releaseRef( e );
		}

		@Override
		public BvvEdgeWrapper< V, E > getObject( final int index, final BvvEdgeWrapper< V, E > e )
		{
			e.we = idmap.getEdge( index, e.ref );
			return e;
		}

		@Override
		public BvvEdgeWrapper< V, E > getObjectIfExists( final int index, final BvvEdgeWrapper< V, E > e )
		{
			e.we = idmap.getEdgeIfExists( index, e.ref );
			return e.we == null ? null : e;
		}

		@Override
		public int getId( final BvvEdgeWrapper< V, E > e )
		{
			return idmap.getEdgeId( e.we );
		}

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		@Override
		public Class< BvvEdgeWrapper< V, E > > getRefClass()
		{
			return ( Class ) BvvEdgeWrapper.class;
		}
	};

	private final AbstractRefPoolCollectionWrapper< BvvEdgeWrapper< V, E >, RefPool< BvvEdgeWrapper< V, E > > > edges = new AbstractRefPoolCollectionWrapper< BvvEdgeWrapper< V, E >, RefPool< BvvEdgeWrapper< V, E > > >( edgePool )
	{
		@Override
		public int size()
		{
			return wrappedGraph.edges().size();
		}

		@Override
		public Iterator< BvvEdgeWrapper< V, E > > iterator()
		{
			return new BvvEdgeIteratorWrapper<>( BvvGraphWrapper.this, BvvGraphWrapper.this.edgeRef(), wrappedGraph.edges().iterator() );
		}
	};

}
