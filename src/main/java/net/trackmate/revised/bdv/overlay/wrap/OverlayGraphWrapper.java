package net.trackmate.revised.bdv.overlay.wrap;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.trackmate.graph.collection.RefCollection;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.pool.RefArrayList;
import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.GraphIdBimap;
import net.trackmate.graph.zzgraphinterfaces.ReadOnlyGraph;
import net.trackmate.graph.zzgraphinterfaces.Vertex;
import net.trackmate.graph.zzgraphinterfaces.CollectionUtils.ListCreator;
import net.trackmate.graph.zzrefcollections.RefPool;
import net.trackmate.revised.bdv.overlay.OverlayGraph;
import net.trackmate.spatial.SpatioTemporalIndex;

/**
 * TODO: implement remaining ReadOnlyGraph methods
 * TODO: implement CollectionCreator
 *
 * @param <V>
 * @param <E>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class OverlayGraphWrapper< V extends Vertex< E >, E extends Edge< V > > implements
		OverlayGraph< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >,
		ListCreator< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	final ReadOnlyGraph< V, E > wrappedGraph;

	final GraphIdBimap< V, E > idmap;

	final OverlayProperties< V, E > overlayProperties;

	private final ConcurrentLinkedQueue< OverlayVertexWrapper< V, E > > tmpVertexRefs;

	private final ConcurrentLinkedQueue< OverlayEdgeWrapper< V, E > > tmpEdgeRefs;

	private final SpatioTemporalIndexWrapper< V, E > wrappedIndex;

	public OverlayGraphWrapper(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final SpatioTemporalIndex< V > graphIndex,
			final OverlayProperties< V, E > overlayProperties )
	{
		this.wrappedGraph = graph;
		this.idmap = idmap;
		this.overlayProperties = overlayProperties;
		tmpVertexRefs =	new ConcurrentLinkedQueue< OverlayVertexWrapper< V, E > >();
		tmpEdgeRefs = new ConcurrentLinkedQueue< OverlayEdgeWrapper< V, E > >();
		this.wrappedIndex = new SpatioTemporalIndexWrapper< V, E >( this, graphIndex );
	}

	@Override
	public OverlayVertexWrapper< V, E > vertexRef()
	{
		final OverlayVertexWrapper< V, E > ref = tmpVertexRefs.poll();
		return ref == null ? new OverlayVertexWrapper< V, E >( this ) : ref;
	}

	@Override
	public OverlayEdgeWrapper< V, E > edgeRef()
	{
		final OverlayEdgeWrapper< V, E > ref = tmpEdgeRefs.poll();
		return ref == null ? new OverlayEdgeWrapper< V, E >( this ) : ref;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OverlayEdgeWrapper< V, E > getEdge( final OverlayVertexWrapper< V, E > source, final OverlayVertexWrapper< V, E > target, final OverlayEdgeWrapper< V, E > edge )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefCollection< OverlayVertexWrapper< V, E > > vertices()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefCollection< OverlayEdgeWrapper< V, E > > edges()
	{
		// TODO Auto-generated method stub
		return null;
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
			releaseRef( v );
		}

		@Override
		public void getByInternalPoolIndex( final int index, final OverlayVertexWrapper< V, E > v )
		{
			v.wv = idmap.getVertex( index, v.wv );
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
			releaseRef( e );
		}

		@Override
		public void getByInternalPoolIndex( final int index, final OverlayEdgeWrapper< V, E > e )
		{
			e.we = idmap.getEdge( index, e.we );
		}
	};

	@Override
	public RefList< OverlayVertexWrapper< V, E > > createVertexList()
	{
		return new RefArrayList< OverlayVertexWrapper< V, E > >( vertexPool );
	}

	@Override
	public RefList< OverlayVertexWrapper< V, E > > createVertexList( final int initialCapacity )
	{
		return new RefArrayList< OverlayVertexWrapper< V, E > >( vertexPool, initialCapacity );
	}

	@Override
	public RefList< OverlayEdgeWrapper< V, E > > createEdgeList()
	{
		return new RefArrayList< OverlayEdgeWrapper< V, E > >( edgePool );
	}

	@Override
	public RefList< OverlayEdgeWrapper< V, E > > createEdgeList( final int initialCapacity )
	{
		return new RefArrayList< OverlayEdgeWrapper< V, E > >( edgePool, initialCapacity );
	}
}
