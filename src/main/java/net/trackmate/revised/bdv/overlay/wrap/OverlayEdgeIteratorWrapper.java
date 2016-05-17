package net.trackmate.revised.bdv.overlay.wrap;

import java.util.Iterator;

import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.GraphIdBimap;
import net.trackmate.graph.zzgraphinterfaces.Vertex;

public class OverlayEdgeIteratorWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements Iterator< OverlayEdgeWrapper< V, E > >
{
	private final OverlayEdgeWrapper< V, E > edge;

	private Iterator< E > wrappedIterator;

	private final GraphIdBimap< V, E > idmap;

	public OverlayEdgeIteratorWrapper(
			final OverlayGraphWrapper< V, E > graph,
			final OverlayEdgeWrapper< V, E > edge,
			final Iterator< E > wrappedIterator )
	{
		this.idmap = graph.idmap;
		this.edge = edge;
		this.wrappedIterator = wrappedIterator;
	}

	void wrap( final Iterator< E > iterator )
	{
		wrappedIterator = iterator;
	}

	@Override
	public boolean hasNext()
	{
		return wrappedIterator.hasNext();
	}

	@Override
	public OverlayEdgeWrapper< V, E > next()
	{
		edge.we = idmap.getEdge( idmap.getEdgeId( wrappedIterator.next() ), edge.we );
		return edge;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
