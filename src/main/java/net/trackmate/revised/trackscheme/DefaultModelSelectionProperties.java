package net.trackmate.revised.trackscheme;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.revised.ui.selection.SelectionListener;

public class DefaultModelSelectionProperties< V extends Vertex< E >, E extends Edge< V > > implements ModelSelectionProperties
{
	private final Selection< V, E > selection;

	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	public DefaultModelSelectionProperties(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final Selection< V, E > selection )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.selection = selection;
	}

	@Override
	public boolean addSelectionListener( final SelectionListener l )
	{
		return selection.addSelectionListener( l );
	}

	@Override
	public boolean removeSelectionListener( final SelectionListener l )
	{
		return selection.removeSelectionListener( l );
	}

	@Override
	public void resumeListeners()
	{
		selection.resumeListeners();
	}

	@Override
	public void pauseListeners()
	{
		selection.pauseListeners();
	}

	@Override
	public void setVertexSelected( final int vertexId, final boolean selected )
	{
		final V ref = graph.vertexRef();
		final V v = idmap.getVertex( vertexId, ref );
		selection.setSelected( v, selected );
		graph.releaseRef( ref );
	}

	@Override
	public void setEdgeSelected( final int edgeId, final boolean selected )
	{
		final E ref = graph.edgeRef();
		final E e = idmap.getEdge( edgeId, ref );
		selection.setSelected( e, selected );
		graph.releaseRef( ref );
	}

	@Override
	public boolean isVertexSelected( final int vertexId )
	{
		final V ref = graph.vertexRef();
		final V v = idmap.getVertex( vertexId, ref );
		final boolean selected = selection.isSelected( v );
		graph.releaseRef( ref );
		return selected;
	}

	@Override
	public boolean isEdgeSelected( final int edgeId )
	{
		final E ref = graph.edgeRef();
		final E e = idmap.getEdge( edgeId, ref );
		final boolean selected = selection.isSelected( e );
		graph.releaseRef( ref );
		return selected;
	}

	@Override
	public void toggleVertexSelected( final int vertexId )
	{
		final V ref = graph.vertexRef();
		final V v = idmap.getVertex( vertexId, ref );
		selection.toggle( v );
		graph.releaseRef( ref );
	}

	@Override
	public void toggleEdgeSelected( final int edgeId )
	{
		final E ref = graph.edgeRef();
		final E e = idmap.getEdge( edgeId, ref );
		selection.toggle( e );
		graph.releaseRef( ref );
	}

	@Override
	public void clearSelection()
	{
		selection.clearSelection();
	}

	@Override
	public String toString()
	{
		return super.toString() + "\n:" + selection;
	}
}
