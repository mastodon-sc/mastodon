package org.mastodon.revised.bdv.overlay.wrap;

import java.util.Iterator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;

public class OverlayVertexIteratorWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements Iterator< OverlayVertexWrapper< V, E > >
{
	private final OverlayVertexWrapper< V, E > vertex;

	private Iterator< V > wrappedIterator;

	private final GraphIdBimap< V, E > idmap;

	public OverlayVertexIteratorWrapper(
			final OverlayGraphWrapper< V, E > graph,
			final OverlayVertexWrapper< V, E > vertex,
			final Iterator< V > wrappedIterator )
	{
		this.idmap = graph.idmap;
		this.vertex = vertex;
		this.wrappedIterator = wrappedIterator;
	}

	void wrap( final Iterator< V > iterator )
	{
		wrappedIterator = iterator;
	}

	@Override
	public boolean hasNext()
	{
		return wrappedIterator.hasNext();
	}

	@Override
	public OverlayVertexWrapper< V, E > next()
	{
		vertex.wv = idmap.getVertex( idmap.getVertexId( wrappedIterator.next() ), vertex.ref );
		return vertex;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
