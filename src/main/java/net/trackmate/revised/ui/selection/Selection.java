package net.trackmate.revised.ui.selection;

import java.util.BitSet;
import java.util.Collection;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;


// TODO: less severe synchronization
public class Selection< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
{
	private final ListenableGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	private final RefSet< V > selectedVertices;

	private final RefSet< E > selectedEdges;

	private final BitSet vertexBits;

	private final BitSet edgeBits;

	public Selection( final ListenableGraph< V, E > graph, final GraphIdBimap< V, E > idmap )
	{
		this.graph = graph;
		this.idmap = idmap;
		selectedVertices = CollectionUtils.createVertexSet( graph );
		selectedEdges = CollectionUtils.createEdgeSet( graph );
		vertexBits = new BitSet();
		edgeBits = new BitSet();
	}

	/**
	 * Get selected state of a vertex.
	 *
	 * @param v
	 *            a vertex.
	 * @return {@code true} if specified vertex is selected.
	 */
	public synchronized boolean isSelected( final V v )
	{
		return vertexBits.get( idmap.getVertexId( v ) );
	}

	/**
	 * Get selected state of an edge.
	 *
	 * @param e
	 *            an edge.
	 * @return {@code true} if specified edge is selected.
	 */
	public synchronized boolean isSelected( final E e )
	{
		return edgeBits.get( idmap.getEdgeId( e ) );
	}

	/**
	 * Set selected state of a vertex.
	 *
	 * @param v
	 *            a vertex.
	 * @param selected
	 *            selected state to set for specified vertex.
	 */
	public synchronized void setSelected( final V v, final boolean selected )
	{
		if ( isSelected( v ) != selected )
		{
			vertexBits.set( idmap.getVertexId( v ), selected );
			if ( selected )
				selectedVertices.add( v );
			else
				selectedVertices.remove( v );
		}
	}

	/**
	 * Set selected state of an edge.
	 *
	 * @param e
	 *            an edge.
	 * @param selected
	 *            selected state to set for specified edge.
	 */
	public synchronized void setSelected( final E e, final boolean selected )
	{
		if ( isSelected( e ) != selected )
		{
			edgeBits.set( idmap.getEdgeId( e ), selected );
			if ( selected )
				selectedEdges.add( e );
			else
				selectedEdges.remove( e );
		}
	}

	/**
	 * TODO
	 * @param v
	 */
	public synchronized void toggle( final V v )
	{
		setSelected( v, !isSelected( v ) );
	}

	/**
	 * TODO
	 * @param e
	 */
	public synchronized void toggle( final E e )
	{
		setSelected( e, !isSelected( e ) );
	}

	/**
	 * TODO
	 * @param edges
	 * @param selected
	 * @return
	 */
	public synchronized boolean setEdgesSelected( final Collection< E > edges, final boolean selected )
	{
		for ( final E e : edges )
			edgeBits.set( idmap.getEdgeId( e ), selected );
		if ( selected )
			return selectedEdges.addAll( edges );
		else
			return selectedEdges.removeAll( edges );
	}

	/**
	 * TODO
	 * @param edges
	 * @param selected
	 * @return
	 */
	public synchronized boolean setVerticesSelected( final Collection< V > vertices, final boolean selected )
	{
		for ( final V v : vertices )
			vertexBits.set( idmap.getVertexId( v ), selected );
		if ( selected )
			return selectedVertices.addAll( vertices );
		else
			return selectedVertices.removeAll( vertices );
	}

	public synchronized boolean clearSelection()
	{
		vertexBits.clear();
		edgeBits.clear();
		if ( selectedEdges.isEmpty() && selectedVertices.isEmpty() )
			return false;
		selectedEdges.clear();
		selectedVertices.clear();
		return true;
	}

	public synchronized RefSet< E > getSelectedEdges()
	{
		final RefSet< E > set = CollectionUtils.createEdgeSet( graph );
		set.addAll( selectedEdges );
		return set;
	}

	public synchronized RefSet< V > getSelectedVertices()
	{
		final RefSet< V > set = CollectionUtils.createVertexSet( graph );
		set.addAll( selectedVertices );
		return set;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( super.toString() );
		sb.append( "\nVertices: " + selectedVertices );
		sb.append( "\nEdges:    " + selectedEdges );
		return sb.toString();
	}

	/*
	 * GraphListener
	 */

	@Override
	public void vertexAdded( final V v )
	{}

	@Override
	public void vertexRemoved( final V v )
	{
		setSelected( v, false );
	}

	@Override
	public void edgeAdded( final E e )
	{}

	@Override
	public void edgeRemoved( final E e )
	{
		setSelected( e, false );
	}

	@Override
	public void graphRebuilt()
	{
		clearSelection();
	}
}
