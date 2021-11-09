/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.display;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.graph.algorithm.traversal.UndirectedDepthFirstIterator;
import org.mastodon.model.FocusModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions.NavigatorEtiquette;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;

/**
 * Keyboard navigation actions in TrackScheme. Supports two flavours:
 * {@link NavigatorEtiquette#FINDER_LIKE} and
 * {@link NavigatorEtiquette#MIDNIGHT_COMMANDER_LIKE}.
 *
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 */
public class DataDisplayNavigationActions
{
	public static final String NAVIGATE_CHILD = "grapher navigate to child";

	public static final String NAVIGATE_PARENT = "grapher navigate to parent";

	public static final String NAVIGATE_LEFT = "grapher navigate left";

	public static final String NAVIGATE_RIGHT = "grapher navigate right";

	public static final String SELECT_NAVIGATE_CHILD = "grapher select navigate to child";

	public static final String SELECT_NAVIGATE_PARENT = "grapher select navigate to parent";

	public static final String SELECT_NAVIGATE_LEFT = "grapher select navigate left";

	public static final String SELECT_NAVIGATE_RIGHT = "grapher select navigate right";

	public static final String TOGGLE_FOCUS_SELECTION = "grapher toggle focus selection";

	private static final String[] NAVIGATE_CHILD_KEYS = new String[] { "DOWN" };

	private static final String[] NAVIGATE_PARENT_KEYS = new String[] { "UP" };

	private static final String[] NAVIGATE_LEFT_KEYS = new String[] { "LEFT" };

	private static final String[] NAVIGATE_RIGHT_KEYS = new String[] { "RIGHT" };

	private static final String[] SELECT_NAVIGATE_CHILD_KEYS = new String[] { "shift DOWN" };

	private static final String[] SELECT_NAVIGATE_PARENT_KEYS = new String[] { "shift UP" };

	private static final String[] SELECT_NAVIGATE_LEFT_KEYS = new String[] { "shift LEFT" };

	private static final String[] SELECT_NAVIGATE_RIGHT_KEYS = new String[] { "shift RIGHT" };

	private static final String[] TOGGLE_FOCUS_SELECTION_KEYS = new String[] { "SPACE" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.GRAPHER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( NAVIGATE_CHILD, NAVIGATE_CHILD_KEYS, "Go to the first child of the current spot." );
			descriptions.add( NAVIGATE_PARENT, NAVIGATE_PARENT_KEYS, "Go to the parent of the current spot." );
			descriptions.add( NAVIGATE_LEFT, NAVIGATE_LEFT_KEYS, "Go to the spot on the left." );
			descriptions.add( NAVIGATE_RIGHT, NAVIGATE_RIGHT_KEYS, "Go to the spot on the right." );
			descriptions.add( SELECT_NAVIGATE_CHILD, SELECT_NAVIGATE_CHILD_KEYS, "Go to the first child of the current spot, and select it." );
			descriptions.add( SELECT_NAVIGATE_PARENT, SELECT_NAVIGATE_PARENT_KEYS, "Go to the parent of the current spot, and select it." );
			descriptions.add( SELECT_NAVIGATE_LEFT, SELECT_NAVIGATE_LEFT_KEYS, "Go to the spot on the left, and select it." );
			descriptions.add( SELECT_NAVIGATE_RIGHT, SELECT_NAVIGATE_RIGHT_KEYS, "Go to the spot on the right, and select it." );
			descriptions.add( TOGGLE_FOCUS_SELECTION, TOGGLE_FOCUS_SELECTION_KEYS, "Toggle selection of the current spot." );
		}
	}

	private enum Direction
	{
		CHILD,
		PARENT,
		LEFT_SIBLING,
		RIGHT_SIBLING
	}

	private final DataGraph< ?, ? > graph;

	private final ReentrantReadWriteLock lock;

	private final SelectionModel< DataVertex, DataEdge > selection;

	private final FocusModel< DataVertex, DataEdge > focus;

	private final RefArrayList< DataVertex > siblings;

	public DataDisplayNavigationActions(
			final DataGraph< ?, ? > graph,
			final FocusModel< DataVertex, DataEdge > focus,
			final SelectionModel< DataVertex, DataEdge > selection )
	{
		this.graph = graph;
		this.lock = graph.getLock();
		this.focus = focus;
		this.selection = selection;
		this.siblings = new RefArrayList<>( graph.getVertexPool() );
	}

	public void install(
			final Actions actions,
			final NavigatorEtiquette etiquette )
	{
		switch ( etiquette )
		{
		case MIDNIGHT_COMMANDER_LIKE:
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.CHILD, false ), NAVIGATE_CHILD, NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.PARENT, false ), NAVIGATE_PARENT, NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.LEFT_SIBLING, false ), NAVIGATE_LEFT, NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.RIGHT_SIBLING, false ), NAVIGATE_RIGHT, NAVIGATE_RIGHT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.CHILD, true ), SELECT_NAVIGATE_CHILD, SELECT_NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.PARENT, true ), SELECT_NAVIGATE_PARENT, SELECT_NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.LEFT_SIBLING, true ), SELECT_NAVIGATE_LEFT, SELECT_NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighbor( Direction.RIGHT_SIBLING, true ), SELECT_NAVIGATE_RIGHT, SELECT_NAVIGATE_RIGHT_KEYS );
			actions.runnableAction( () -> toggleSelectionOfFocusedVertex(), TOGGLE_FOCUS_SELECTION, TOGGLE_FOCUS_SELECTION_KEYS );
			break;
		case FINDER_LIKE:
		default:
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.CHILD, true ), NAVIGATE_CHILD, NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.PARENT, true ), NAVIGATE_PARENT, NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.LEFT_SIBLING, true ), NAVIGATE_LEFT, NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.RIGHT_SIBLING, true ), NAVIGATE_RIGHT, NAVIGATE_RIGHT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.CHILD, false ), SELECT_NAVIGATE_CHILD, SELECT_NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.PARENT, false ), SELECT_NAVIGATE_PARENT, SELECT_NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.LEFT_SIBLING, false ), SELECT_NAVIGATE_LEFT, SELECT_NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> selectAndFocusNeighborFL( Direction.RIGHT_SIBLING, false ), SELECT_NAVIGATE_RIGHT, SELECT_NAVIGATE_RIGHT_KEYS );
			break;
		}
	}

	/*
	 * COMMANDER-LIKE ETIQUETTE METHODS.
	 */

	/**
	 * Focus a neighbor (parent, child, left sibling, right sibling) of the
	 * currently focused vertex. Possibly, the currently focused vertex is added
	 * to the selection.
	 *
	 * @param direction
	 *            which neighbor to focus.
	 * @param select
	 *            if {@code true}, the currently focused vertex is added to the
	 *            selection (before moving the focus).
	 */
	private void selectAndFocusNeighbor( final Direction direction, final boolean select )
	{
		final DataVertex ref = graph.vertexRef();
		selectAndFocusNeighbor( direction, select, ref );
		graph.releaseRef( ref );
	}

	private DataVertex selectAndFocusNeighbor( final Direction direction, final boolean select, final DataVertex ref )
	{
		lock.readLock().lock();
		try
		{
			final DataVertex vertex = focus.getFocusedVertex( ref );
			if ( vertex == null )
				return null;

			if ( select )
				selection.setSelected( vertex, true );

			final DataVertex current;
			switch ( direction )
			{
			case CHILD:
				current = getFirstChild( vertex, ref );
				break;
			case PARENT:
				current = getFirstParent( vertex, ref );
				break;
			case LEFT_SIBLING:
				current = getLeftSibling( vertex, ref );
				break;
			case RIGHT_SIBLING:
			default:
				current = getRightSibling( vertex, ref );
				break;
			}

			if ( current != null )
				focus.focusVertex( current );

			return current;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	private DataVertex getRightSibling( final DataVertex vertex, final DataVertex ref )
	{
		final int s = getSiblings( vertex );
		if ( s == siblings.size() - 1 )
			return siblings.get( 0, ref );

		return siblings.get( s + 1, ref );
	}

	private DataVertex getLeftSibling( final DataVertex vertex, final DataVertex ref )
	{
		final int s = getSiblings( vertex );
		if ( s == 0 )
			return siblings.get( siblings.size() - 1, ref );

		return siblings.get( s - 1, ref );
	}

	private synchronized int getSiblings( final DataVertex vertex )
	{
		final int index = siblings.indexOf( vertex );
		if ( index < 0 )
		{
			// regen siblings
			siblings.clear();
			final UndirectedDepthFirstIterator< DataVertex, DataEdge > it = new UndirectedDepthFirstIterator<>( vertex, graph );
			final int timepoint = vertex.getTimepoint();
			while ( it.hasNext() )
			{
				final DataVertex v = it.next();
				if ( v.getTimepoint() == timepoint )
					siblings.add( v );
			}
			return 0;
		}
		return index;
	}

	private DataVertex getFirstParent( final DataVertex vertex, final DataVertex ref )
	{
		for ( final DataEdge edge : vertex.incomingEdges() )
			return edge.getSource( ref );
		return null;
	}

	private DataVertex getFirstChild( final DataVertex vertex, final DataVertex ref )
	{
		for ( final DataEdge edge : vertex.outgoingEdges() )
			return edge.getTarget( ref );
		return null;
	}

	/**
	 * Toggle the selected state of the currently focused vertex.
	 */
	private void toggleSelectionOfFocusedVertex()
	{
		final DataVertex ref = graph.vertexRef();
		lock.readLock().lock();
		try
		{
			final DataVertex v = focus.getFocusedVertex( ref );
			if ( v != null )
				selection.toggle( v );
		}
		finally
		{
			lock.readLock().unlock();
			graph.releaseRef( ref );
		}
	}

	/*
	 * FINDER-LIKE ETIQUETTE METHODS.
	 */

	/**
	 * Focus and select a neighbor (parent, child, left sibling, right sibling)
	 * of the currently focused vertex. The selection can be cleared before
	 * moving focus.
	 *
	 * @param direction
	 *            which neighbor to focus.
	 * @param clearSelection
	 *            if {@code true}, the selection is cleared before moving the
	 *            focus.
	 */
	private void selectAndFocusNeighborFL( final Direction direction, final boolean clearSelection )
	{
		final DataVertex ref = graph.vertexRef();
		selectAndFocusNeighborFL( direction, clearSelection, ref );
		graph.releaseRef( ref );
	}

	private DataVertex selectAndFocusNeighborFL( final Direction direction, final boolean clearSelection, final DataVertex ref )
	{
		lock.readLock().lock();
		try
		{
			final DataVertex vertex = focus.getFocusedVertex( ref );
			if ( vertex == null )
				return null;

			selection.pauseListeners();

			final DataVertex current;
			switch ( direction )
			{
			case CHILD:
				current = getFirstChild( vertex, ref );
				break;
			case PARENT:
				current = getFirstParent( vertex, ref );
				break;
			case LEFT_SIBLING:
				current = getLeftSibling( vertex, ref );
				break;
			case RIGHT_SIBLING:
			default:
				current = getRightSibling( vertex, ref );
				break;
			}

			if ( current != null )
			{
				focus.focusVertex( current );
				if ( clearSelection )
					selection.clearSelection();
				selection.setSelected( current, true );
			}

			selection.resumeListeners();
			return current;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}
}
