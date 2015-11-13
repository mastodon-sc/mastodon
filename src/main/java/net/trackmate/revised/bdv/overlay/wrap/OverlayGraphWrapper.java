package net.trackmate.revised.bdv.overlay.wrap;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayGraph;
import net.trackmate.revised.ui.selection.Selection;
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
public class OverlayGraphWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayGraph< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	final ReadOnlyGraph< V, E > wrappedGraph;

	final GraphIdBimap< V, E > idmap;

	final OverlayProperties< V > overlayProperties;

	private final ConcurrentLinkedQueue< OverlayVertexWrapper< V, E > > tmpVertexRefs;

	private final ConcurrentLinkedQueue< OverlayEdgeWrapper< V, E > > tmpEdgeRefs;

	private final SpatioTemporalIndexWrapper< V, E > wrappedIndex;

	private final Selection< V, E > selection;

	public OverlayGraphWrapper(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final SpatioTemporalIndex< V > graphIndex,
			final Selection< V, E > selection,
			final OverlayProperties< V > overlayProperties )
	{
		this.wrappedGraph = graph;
		this.idmap = idmap;
		this.selection = selection;
		this.overlayProperties = overlayProperties;
		tmpVertexRefs =	new ConcurrentLinkedQueue< OverlayVertexWrapper< V, E > >();
		tmpEdgeRefs = new ConcurrentLinkedQueue< OverlayEdgeWrapper< V, E > >();
		this.wrappedIndex = new SpatioTemporalIndexWrapper< V, E >( this, graphIndex );
	}

	@Override
	public OverlayVertexWrapper< V, E > vertexRef()
	{
		final OverlayVertexWrapper< V, E > ref = tmpVertexRefs.poll();
		return ref == null ? new OverlayVertexWrapper< V, E >( this, selection ) : ref;
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

	@SuppressWarnings( "unchecked" )
	@Override
	public void releaseRef( final OverlayVertexWrapper< V, E >... refs )
	{
		for ( final OverlayVertexWrapper< V, E > ref : refs )
			tmpVertexRefs.add( ref );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void releaseRef( final OverlayEdgeWrapper< V, E >... refs )
	{
		for ( final OverlayEdgeWrapper< V, E > ref : refs )
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
	public Iterator< OverlayVertexWrapper< V, E > > vertexIterator()
	{
		return vertices().iterator();
	}

	@Override
	public Iterator< OverlayEdgeWrapper< V, E > > edgeIterator()
	{
		return edges().iterator();
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
		return wrappedIndex;
	}

	@Override
	public double getMaxBoundingSphereRadiusSquared( final int timepoint )
	{
		return overlayProperties.getMaxBoundingSphereRadiusSquared( timepoint );
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
