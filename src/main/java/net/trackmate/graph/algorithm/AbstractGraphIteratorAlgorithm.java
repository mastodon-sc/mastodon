package net.trackmate.graph.algorithm;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.MaybeRefIterator;

public abstract class AbstractGraphIteratorAlgorithm< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E > implements MaybeRefIterator
{
	public AbstractGraphIteratorAlgorithm( final Graph< V, E > graph )
	{
		super( graph );
	}

	@Override
	public boolean isRefIterator()
	{
		final V v = graph.vertexRef();
		final boolean isRefIterator = v != null && v instanceof PoolObject;
		graph.releaseRef( v );
		return isRefIterator;
	}
}
