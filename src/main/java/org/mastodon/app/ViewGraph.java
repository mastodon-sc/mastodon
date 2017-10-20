package org.mastodon.app;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

/**
 * A view {@code Graph<V, E>} is an adapter of the model {@code Graph<MV, ME>},
 * providing {@link RefBimap} mappings between the two.
 *
 * @param <MV>
 *            model vertex type
 * @param <ME>
 *            model edge type
 * @param <V>
 *            view vertex type
 * @param <E>
 *            view edge type
 */
public interface ViewGraph< MV extends Vertex< ME >, ME extends Edge< MV >, V extends Vertex< E >, E extends Edge< V > >
		extends ReadOnlyGraph< V, E >
{
	/**
	 * Get bidirectional mapping between model vertices and view vertices.
	 *
	 * @return bidirectional mapping between model vertices and view vertices.
	 */
	public RefBimap< MV, V > getVertexMap();

	/**
	 * Get bidirectional mapping between model edges and view edges.
	 *
	 * @return bidirectional mapping between model edges and view edges.
	 */
	public RefBimap< ME, E > getEdgeMap();
}
