package net.trackmate.revised.trackscheme;

import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.GraphIdBimap;
import net.trackmate.graph.zzgraphinterfaces.ReadOnlyGraph;
import net.trackmate.graph.zzgraphinterfaces.Vertex;
import net.trackmate.revised.model.HasLabel;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.spatial.HasTimepoint;

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

	private final Selection< V, E > selection;

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
			final GraphIdBimap< V, E > idmap,
			final Selection< V, E > selection )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.selection = selection;
	}

	@Override
	public ModelVertexProperties createVertexProperties()
	{
		return new VertexProps< V >( graph, idmap, selection );
	}

	@Override
	public ModelEdgeProperties createEdgeProperties()
	{
		return new EdgeProps< E >( graph, idmap, selection );
	}

	private static class VertexProps<
			V extends Vertex< ? > & HasTimepoint & HasLabel >
		implements ModelVertexProperties
	{
		private final GraphIdBimap< V, ? > idmap;

		private final Selection< V, ? > selection;

		private final V v;

		private VertexProps(
				final ReadOnlyGraph< V, ? > graph,
				final GraphIdBimap< V, ? > idmap,
				final Selection< V, ? > selection )
		{
			this.idmap = idmap;
			this.selection = selection;
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

		@Override
		public boolean isSelected( final int id )
		{
			return selection.isSelected( idmap.getVertex( id, v ) );
		}
	}

	private static class EdgeProps<
			E extends Edge< ? > >
		implements ModelEdgeProperties
	{
		private final GraphIdBimap< ?, E > idmap;

		private final Selection< ?, E > selection;

		private final E e;

		private EdgeProps( final ReadOnlyGraph< ?, E > graph, final GraphIdBimap< ?, E > idmap, final Selection< ?, E > selection )
		{
			this.idmap = idmap;
			this.selection = selection;
			e = graph.edgeRef();
		}

		@Override
		public boolean isSelected( final int id )
		{
			return selection.isSelected( idmap.getEdge( id, e ) );
		}
	}
}
