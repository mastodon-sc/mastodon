/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.model.branch;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.SelectionListener;
import org.mastodon.model.SelectionModel;
import org.scijava.listeners.Listeners;

public class BranchGraphSelectionAdapter< 
	V extends Vertex< E >, 
	E extends Edge< V >, 
	BV extends Vertex< BE >, 
	BE extends Edge< BV > >
		extends AbstractBranchGraphAdapter< V, E, BV, BE >
		implements SelectionModel< BV, BE >
{

	private final SelectionModel< V, E > selection;

	public BranchGraphSelectionAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final SelectionModel< V, E > selection )
	{
		super( branchGraph, graph, idmap );
		this.selection = selection;
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
	public boolean isSelected( final BV vertex )
	{
		Iterator<V> vIter = branchGraph.vertexBranchIterator( vertex );
		Iterator<E> eIter = branchGraph.edgeBranchIterator( vertex );
		try
		{
			while ( vIter.hasNext() )
			{
				V v = vIter.next();
				if ( !selection.isSelected( v ) )
					return false;
			}

			while ( eIter.hasNext() )
			{
				E e = eIter.next();
				if ( !selection.isSelected( e ) )
					return false;
			}

			return true;
		}
		finally
		{
			branchGraph.releaseIterator( vIter );
			branchGraph.releaseIterator( eIter );
		}
	}

	@Override
	public boolean isSelected( final BE edge )
	{
		final E eRef = graph.edgeRef();
		try
		{
			final E e = branchGraph.getLinkedEdge( edge, eRef );
			return e != null && selection.isSelected( e );
		}
		finally
		{
			graph.releaseRef( eRef );
		}
	}

	@Override
	public void setSelected( final BV vertex, final boolean selected )
	{
		selection.pauseListeners();
		setVertexSelected( vertex, selected );
		selection.resumeListeners();
	}

	private boolean setVertexSelected( final BV branchVertex, final boolean selected )
	{
		Iterator<V> vertices = branchGraph.vertexBranchIterator( branchVertex );
		Iterator<E> edges = branchGraph.edgeBranchIterator( branchVertex );
		try
		{
			boolean changed = false;
			while ( vertices.hasNext() )
			{
				V v = vertices.next();
				changed = changed || selected != selection.isSelected( v );
				selection.setSelected( v, selected );
			}

			while ( edges.hasNext() )
			{
				E e = edges.next();
				changed = changed || selected != selection.isSelected( e );
				selection.setSelected( e, selected );
			}

			return changed;
		}
		finally
		{
			branchGraph.releaseIterator( vertices );
			branchGraph.releaseIterator( edges );
		}
	}

	@Override
	public void setSelected( final BE edge, final boolean selected )
	{
		final E eRef = graph.edgeRef();
		try
		{
			E e = branchGraph.getLinkedEdge( edge, eRef );
			if( e != null )
				selection.setSelected( e, selected );
		}
		finally
		{
			graph.releaseRef( eRef );
		}
	}

	/**
	 * Determines whether this call changed the underlying selection model.
	 *
	 * @param branchEdge
	 *            the branch edge to select.
	 * @param selected
	 *            whether to select the specified edge.
	 * @return <code>true</code> if the selection model has been changed by this
	 *         call.
	 */
	private boolean setEdgeSelected( final BE branchEdge, final boolean selected )
	{
		final E eRef = graph.edgeRef();

		try
		{
			E e = branchGraph.getLinkedEdge( branchEdge, eRef );
			if ( !isValid( e ) )
				return false;
			boolean changed = selection.isSelected( e ) == selected;
			selection.setSelected( e, selected );
			return changed;
		}
		finally
		{
			graph.releaseRef( eRef );
		}
	}

	@Override
	public void toggle( final BV vertex )
	{
		setSelected( vertex, !isSelected( vertex ) );
	}

	@Override
	public void toggle( final BE edge )
	{
		setSelected( edge, !isSelected( edge ) );
	}

	@Override
	public boolean setEdgesSelected( final Collection< BE > edges, final boolean selected )
	{
		boolean changed = false;
		selection.pauseListeners();

		for ( final BE edge : edges )
			changed = setEdgeSelected( edge, selected ) || changed;

		selection.resumeListeners();
		return changed;
	}

	@Override
	public boolean setVerticesSelected( final Collection< BV > vertices, final boolean selected )
	{
		boolean changed = false;
		selection.pauseListeners();
		for ( final BV vertex : vertices )
			changed = setVertexSelected( vertex, selected ) || changed;
		selection.resumeListeners();
		return changed;
	}

	@Override
	public RefSet< BE > getSelectedEdges()
	{
		final BE beRef = branchGraph.edgeRef();
		try {
			final RefSet< BE > branchEdges =
					RefCollections.createRefSet( branchGraph.edges() );

			for ( final E edges : selection.getSelectedEdges() )
			{
				final BE branchEdge = branchGraph.getBranchEdge( edges, beRef );
				if ( branchEdge != null )
					branchEdges.add( branchEdge );
			}

			return branchEdges;
		}
		finally {
			branchGraph.releaseRef( beRef );
		}
	}

	@Override
	public RefSet< BV > getSelectedVertices()
	{
		final BV bvRef = branchGraph.vertexRef();
		try
		{
			final RefSet<BV> branchVertices = RefCollections.createRefSet( branchGraph.vertices() );

			for ( final V v : selection.getSelectedVertices() )
			{
				final BV branchVertex = branchGraph.getBranchVertex( v, bvRef );
				if ( branchVertex != null && isSelected( branchVertex ) )
					branchVertices.add( branchVertex );
			}

			return branchVertices;
		}
		finally {
			branchGraph.releaseRef( bvRef );
		}
	}

	@Override
	public boolean clearSelection()
	{
		return selection.clearSelection();
	}

	@Override
	public boolean isEmpty()
	{
		if( selection.isEmpty() )
			return true;
		return getSelectedEdges().isEmpty() && getSelectedVertices().isEmpty();
	}

	@Override
	public Listeners< SelectionListener > listeners()
	{
		return selection.listeners();
	}
}
