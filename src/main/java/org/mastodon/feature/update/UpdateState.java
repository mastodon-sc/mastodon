package org.mastodon.feature.update;

import java.util.Collection;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

/**
 * Simple data class used to store graph updates in a stack along with the
 * feature keys they are up-to-date for.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
class UpdateState< V extends Vertex< E >, E extends Edge< V > >
{

	private final Collection< String > featureKeys;

	private final GraphUpdate< V, E > changes;

	public UpdateState( final Collection< String > featureKeys, final GraphUpdate< V, E > changes )
	{
		this.featureKeys = featureKeys;
		this.changes = changes;
	}

	public boolean contains( final String featureKey )
	{
		return featureKeys.contains( featureKey );
	}

	@Override
	public String toString()
	{
		return super.toString() + " -> " + featureKeys.toString();
	}

	public GraphUpdate< V, E > getChanges()
	{
		return changes;
	}
}
