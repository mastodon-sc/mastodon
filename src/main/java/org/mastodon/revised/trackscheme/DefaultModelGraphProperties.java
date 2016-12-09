package org.mastodon.revised.trackscheme;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.HasLabel;
import org.mastodon.revised.ui.selection.Selection;
import org.mastodon.spatial.HasTimepoint;

/**
 * A default implementation of {@link ModelGraphProperties} for {@link Vertex}
 * that implements the {@link HasLabel} and {@link HasTimepoint} interfaces.
 * <p>
 * This class relies on a {@link Selection} object to determine the selected
 * state of vertices and edges, and on the {@link HasLabel#getLabel()} method
 * for the vertex string labels. Since we access properties via edge and vertex
 * ids, we need to have the bidirectional id map for the graph.
 *
 * @param <V>
 *            the type of the graph vertices.
 * @param <E>
 *            the type of the graph edges.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class DefaultModelGraphProperties<
		V extends Vertex< E > & HasTimepoint & HasLabel,
		E extends Edge< V > >
	implements ModelGraphProperties
{
	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	/**
	 * Creates a new graph properties object.
	 *
	 * @param graph
	 *            the graph.
	 * @param idmap
	 *            the bidirectional id map between vertices and their id and
	 *            between edges and their id.
	 * @param selection
	 *            a selection object that will be used to determine the selected
	 *            state of vertices and edges.
	 */
	public DefaultModelGraphProperties(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap )
	{
		this.graph = graph;
		this.idmap = idmap;
	}

	@Override
	public ModelVertexProperties createVertexProperties()
	{
		return new VertexProps<>( graph, idmap );
	}

	@Override
	public ModelEdgeProperties createEdgeProperties()
	{
		return new EdgeProps<>( graph, idmap );
	}

	private static class VertexProps<
			V extends Vertex< ? > & HasTimepoint & HasLabel >
		implements ModelVertexProperties
	{
		private final GraphIdBimap< V, ? > idmap;

		private final V v;

		private VertexProps(
				final ReadOnlyGraph< V, ? > graph,
				final GraphIdBimap< V, ? > idmap )
		{
			this.idmap = idmap;
			v = graph.vertexRef();
		}

		@Override
		public String getLabel( final int id )
		{
			return idmap.getVertex( id, v ).getLabel();
		}

		@Override
		public void setLabel( final int id, final String label )
		{
			idmap.getVertex( id, v ).setLabel( label );
		}
	}

	// Does nothing currently, but maybe we'll add to it later
	private static class EdgeProps<
			E extends Edge< ? > >
		implements ModelEdgeProperties
	{
		private final GraphIdBimap< ?, E > idmap;

		private final E e;

		private EdgeProps( final ReadOnlyGraph< ?, E > graph, final GraphIdBimap< ?, E > idmap )
		{
			this.idmap = idmap;
			e = graph.edgeRef();
		}
	}
}
