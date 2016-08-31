package org.mastodon.revised.trackscheme;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.ui.selection.Selection;
import org.mastodon.revised.ui.selection.SelectionListener;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

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
