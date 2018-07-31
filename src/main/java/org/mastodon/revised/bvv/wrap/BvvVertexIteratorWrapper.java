package org.mastodon.revised.bvv.wrap;

import java.util.Iterator;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;

public class BvvVertexIteratorWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements Iterator< BvvVertexWrapper< V, E > >
{
	private final BvvVertexWrapper< V, E > vertex;

	private Iterator< V > wrappedIterator;

	private final GraphIdBimap< V, E > idmap;

	public BvvVertexIteratorWrapper(
			final BvvGraphWrapper< V, E > graph,
			final BvvVertexWrapper< V, E > vertex,
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
	public BvvVertexWrapper< V, E > next()
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
