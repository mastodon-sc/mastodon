package net.trackmate.revised.model;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.ListenableReadOnlyGraph;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.graph.ref.AbstractListenableVertex;

/**
 * Manages the model graph.
 * <p>
 * The model graph is only exposed as a {@link ReadOnlyGraph}. All updates to
 * the model graph are done through {@link AbstractModel}. This includes vertex
 * and edge attribute changes (although this currently cannot be enforced
 * through {@link ReadOnlyGraph}).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AbstractModel<
		MG extends AbstractModelGraph< ?, ?, V, E, ? >,
		V extends AbstractListenableVertex< V, E, ? >,
		E extends AbstractListenableEdge< E, V, ? > >
{

	/**
	 * Exposes the graph managed by this model.
	 * <p>
	 * The graph is only exposed as a {@link ListenableReadOnlyGraph} which is a
	 * {@link ReadOnlyGraph}. All updates to the model graph must be done
	 * through this model instance directly.
	 *
	 * @return the graph.
	 */
	public ListenableGraph< V, E > getGraph()
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
