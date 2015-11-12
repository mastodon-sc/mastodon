package net.trackmate.revised.trackscheme;

import gnu.trove.impl.unmodifiable.TUnmodifiableIntSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.revised.ui.selection.SelectionListener;

public class DefaultModelSelectionProperties< V extends Vertex< E >, E extends Edge< V > > implements ModelSelectionProperties
{
	private final Selection< V, E > selection;

	private final ArrayList< SelectionListener > listeners;

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
		this.listeners = new ArrayList< SelectionListener >();
	}

	@Override
	public boolean addSelectionListener( final SelectionListener l )
	{
		return listeners.add( l );
	}

	@Override
	public boolean removeSelectionListener( final SelectionListener l )
	{
		return listeners.remove( l );
	}

	@Override
	public TIntSet getSelectedVerticesIds()
	{
		final RefSet< V > selectedVertices = selection.getSelectedVertices();
		final TIntSet set = new TUnmodifiableIntSet( new TIntHashSet( selectedVertices.size() ) );
		for ( final V v : selectedVertices )
		{
			set.add( idmap.getVertexId( v ) );
		}
		return set;
	}

	@Override
	public TIntSet getSelectedEdgesIds()
	{
		final RefSet< E > selectedEdges = selection.getSelectedEdges();
		final TIntSet set = new TUnmodifiableIntSet( new TIntHashSet( selectedEdges.size() ) );
		for ( final E e : selectedEdges )
		{
			set.add( idmap.getEdgeId( e ) );
		}
		return set;
	}

	@Override
	public void setVertexSelected( final int vertexId, final boolean selected )
	{
		final V ref = graph.vertexRef();
		final V v = idmap.getVertex( vertexId, ref );
		selection.setSelected( v, selected );
		graph.releaseRef( v );
	}

	@Override
	public void setEdgeSelected( final int edgeId, final boolean selected )
	{
		final E ref = graph.edgeRef();
		final E e = idmap.getEdge( edgeId, ref );
		selection.setSelected( e, selected );
		graph.releaseRef( e );
	}

	@Override
	public boolean isVertexSelected( final int vertexId )
	{
		final V ref = graph.vertexRef();
		final V v = idmap.getVertex( vertexId, ref );
		final boolean selected = selection.isSelected( v );
		graph.releaseRef( v );
		return selected;
	}

	@Override
	public boolean isEdgeSelected( final int edgeId )
	{
		final E ref = graph.edgeRef();
		final E e = idmap.getEdge( edgeId, ref );
		final boolean selected = selection.isSelected( e );
		graph.releaseRef( e );
		return selected;
	}
}
