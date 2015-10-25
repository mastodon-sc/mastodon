package net.trackmate.revised.trackscheme;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.model.HasLabel;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.spatial.HasTimepoint;


public class DefaultModelGraphProperties<
		V extends Vertex< E > & HasTimepoint & HasLabel,
		E extends Edge< V > >
	implements ModelGraphProperties
{
	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	private final Selection< V, E > selection;

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
