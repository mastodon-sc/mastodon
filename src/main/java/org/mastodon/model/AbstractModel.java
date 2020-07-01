package org.mastodon.model;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ref.AbstractListenableEdge;

/**
 * Manages the model graph.
 *
 * @author Tobias Pietzsch
 * @param <MG>
 *            the model-graph used in this model.
 * @param <V>
 *            the type of vertices in the model-graph.
 * @param <E>
 *            the type of edges in the model-graph.
 */
public class AbstractModel<
		MG extends AbstractModelGraph< MG, ?, ?, V, E, ? >,
		V extends AbstractSpot< V, E, ?, ?, MG >,
		E extends AbstractListenableEdge< E, V, ?, ? > >
{
	/**
	 * Exposes the graph managed by this model.
	 *
	 * @return the graph.
	 */
	public MG getGraph()
	{
		return modelGraph;
	}

	/**
	 * Exposes the bidirectional map between vertices and their id, and between
	 * edges and their id.
	 *
	 * @return the bidirectional id map.
	 */
	public GraphIdBimap< V, E > getGraphIdBimap()
	{
		return modelGraph.idmap;
	}

	protected final MG modelGraph;

	protected AbstractModel( final MG modelGraph )
	{
		this.modelGraph = modelGraph;
	}
}
