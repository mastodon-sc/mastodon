package net.trackmate.revised.bdv.overlay.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.revised.bdv.overlay.OverlayGraph;
import net.trackmate.spatial.HasTimepoint;
import net.trackmate.spatial.SpatioTemporalIndex;

/**
 * TODO: implement remaining ReadOnlyGraph methods
 * TODO: implement CollectionCreator
 *
 * @param <V>
 * @param <E>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class OverlayGraphWrapper< V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
	implements OverlayGraph< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	final ReadOnlyGraph< V, E > wrappedGraph;

	final GraphIdBimap< V, E > idmap;

	final OverlayProperties< V > overlayProperties;

	private final Map< Integer, RefSet< OverlayVertexWrapper< V, E > > > timepointToSpots;

	private final ConcurrentLinkedQueue< OverlayVertexWrapper< V, E > > tmpVertexRefs =
			new ConcurrentLinkedQueue< OverlayVertexWrapper< V, E > >();

	private final ConcurrentLinkedQueue< OverlayEdgeWrapper< V, E > > tmpEdgeRefs =
			new ConcurrentLinkedQueue< OverlayEdgeWrapper< V, E > >();

	public OverlayGraphWrapper(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final OverlayProperties< V > overlayProperties )
	{
		this.wrappedGraph = graph;
		this.idmap = idmap;
		this.overlayProperties = overlayProperties;
		timepointToSpots = new HashMap< Integer, RefSet< OverlayVertexWrapper< V, E > > >();
	}

	@Override
	public RefSet< OverlayVertexWrapper< V, E > > getSpots( final int timepoint )
	{
		RefSet< OverlayVertexWrapper< V, E > > spots = timepointToSpots.get( timepoint );
		if ( null == spots )
		{
			spots = new PoolObjectSet< OverlayVertexWrapper< V, E > >( vertexPool );
			timepointToSpots.put( timepoint, spots );
		}
		return spots;
	}



	// TODO: REMOVE
	public void HACK_updateTimepointSets()
	{
		timepointToSpots.clear();
		final OverlayVertexWrapper< V, E > v = vertexRef();
		for ( final V mv : wrappedGraph.vertices() )
		{
			vertexPool.getByInternalPoolIndex( idmap.getVertexId( mv ), v );
			getSpots( v.getTimepoint() ).add( v );
		}
		releaseRef( v );
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
	public void releaseRef( final OverlayVertexWrapper< V, E >... refs )
	{
		for ( final OverlayVertexWrapper< V, E > ref : refs )
			tmpVertexRefs.add( ref );
	}

	@Override
	public void releaseRef( final OverlayEdgeWrapper< V, E >... refs )
	{
		for ( final OverlayEdgeWrapper< V, E > ref : refs )
			tmpEdgeRefs.add( ref );
	}

	private final RefPool< OverlayVertexWrapper< V, E > > vertexPool = new RefPool< OverlayVertexWrapper< V, E > >()
	{
		@Override
		public OverlayVertexWrapper< V, E > createRef()
		{
			return vertexRef();
		}

		@Override
		public void releaseRef( final OverlayVertexWrapper< V, E > obj )
		{
			OverlayGraphWrapper.this.releaseRef( obj );
		}

		@Override
		public void getByInternalPoolIndex( final int index, final OverlayVertexWrapper< V, E > obj )
		{
			obj.wv = idmap.getVertex( index, obj.wv );
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
		public void releaseRef( final OverlayEdgeWrapper< V, E > obj )
		{
			OverlayGraphWrapper.this.releaseRef( obj );
		}

		@Override
		public void getByInternalPoolIndex( final int index, final OverlayEdgeWrapper< V, E > obj )
		{
			obj.we = idmap.getEdge( index, obj.we );
		}
	};

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
	public Iterator< OverlayVertexWrapper< V, E > > vertexIterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator< OverlayEdgeWrapper< V, E > > edgeIterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection< OverlayVertexWrapper< V, E > > vertices()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection< OverlayEdgeWrapper< V, E > > edges()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpatioTemporalIndex< OverlayVertexWrapper< V, E > > getIndex()
	{
		// TODO Auto-generated method stub
		return null;
	}



	V wrappedVertexRef()
	{
		return wrappedGraph.vertexRef();
	}

	E wrappedEdgeRef()
	{
		return wrappedGraph.edgeRef();
	}
}
