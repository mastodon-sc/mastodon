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
package org.mastodon.views.bdv.overlay;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

public class BdvSelectionBehaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
{
	public static final String FOCUS_VERTEX = "bdv click focus vertex";

	public static final String NAVIGATE_TO_VERTEX = "bdv click navigate to vertex";

	public static final String SELECT = "bdv click select";

	public static final String ADD_SELECT = "bdv click add to selection";

	private static final String[] FOCUS_VERTEX_KEYS = new String[] { "button1", "shift button1" };

	private static final String[] NAVIGATE_TO_VERTEX_KEYS =
			new String[] { "double-click button1", "shift double-click button1" };

	private static final String[] SELECT_KEYS = new String[] { "button1" };

	private static final String[] ADD_SELECT_KEYS = new String[] { "shift button1" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( FOCUS_VERTEX, FOCUS_VERTEX_KEYS, "Focus spot (spot gets keyboard focus)." );
			descriptions.add( NAVIGATE_TO_VERTEX, NAVIGATE_TO_VERTEX_KEYS, "Navigate to spot (in all linked views)." );
			descriptions.add( SELECT, SELECT_KEYS, "Select spot." );
			descriptions.add( ADD_SELECT, ADD_SELECT_KEYS, "Add spot to selection." );
		}
	}

	public static final double EDGE_SELECT_DISTANCE_TOLERANCE = 5.0;

	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 8.0;

	private final ClickFocusBehaviour focusVertexBehaviour;

	private final ClickNavigateBehaviour navigateBehaviour;

	private final ClickSelectionBehaviour selectBehaviour;

	private final ClickSelectionBehaviour addSelectBehaviour;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void install(
			final Behaviours behaviours,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final SelectionModel< V, E > selection,
			final FocusModel< V > focus,
			final NavigationHandler< V, E > navigation )
	{
		final BdvSelectionBehaviours< V, E > sb =
				new BdvSelectionBehaviours<>( overlayGraph, renderer, selection, focus, navigation );

		behaviours.namedBehaviour( sb.focusVertexBehaviour, FOCUS_VERTEX_KEYS );
		behaviours.namedBehaviour( sb.navigateBehaviour, NAVIGATE_TO_VERTEX_KEYS );
		behaviours.namedBehaviour( sb.selectBehaviour, SELECT_KEYS );
		behaviours.namedBehaviour( sb.addSelectBehaviour, ADD_SELECT_KEYS );
	}

	private final OverlayGraph< V, E > overlayGraph;

	private final ReentrantReadWriteLock lock;

	private final OverlayGraphRenderer< V, E > renderer;

	private final SelectionModel< V, E > selection;

	private final FocusModel< V > focus;

	private final NavigationHandler< V, E > navigation;

	private BdvSelectionBehaviours(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final SelectionModel< V, E > selection,
			final FocusModel< V > focus,
			final NavigationHandler< V, E > navigation )
	{
		this.overlayGraph = overlayGraph;
		this.lock = overlayGraph.getLock();
		this.renderer = renderer;
		this.selection = selection;
		this.focus = focus;
		this.navigation = navigation;

		focusVertexBehaviour = new ClickFocusBehaviour( FOCUS_VERTEX );
		navigateBehaviour = new ClickNavigateBehaviour( NAVIGATE_TO_VERTEX );
		selectBehaviour = new ClickSelectionBehaviour( SELECT, false );
		addSelectBehaviour = new ClickSelectionBehaviour( ADD_SELECT, true );
	}

	private void select( final int x, final int y, final boolean addToSelection )
	{
		selection.pauseListeners();
		final V vertex = overlayGraph.vertexRef();
		final E edge = overlayGraph.edgeRef();
		lock.readLock().lock();
		try
		{

			// See if we can select an edge.
			if ( renderer.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
			{
				final boolean selected = selection.isSelected( edge );
				if ( !addToSelection )
					selection.clearSelection();
				selection.setSelected( edge, !selected );
			}
			// See if we can select a vertex.
			else if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
			{
				final boolean selected = selection.isSelected( vertex );
				if ( !addToSelection )
					selection.clearSelection();
				selection.setSelected( vertex, !selected );
			}
			// Nothing found. clear selection if addToSelection == false
			else if ( !addToSelection )
				selection.clearSelection();

		}
		finally
		{
			overlayGraph.releaseRef( vertex );
			overlayGraph.releaseRef( edge );
			lock.readLock().unlock();
			selection.resumeListeners();
		}
	}

	private void navigate( final int x, final int y )
	{
		final V vertex = overlayGraph.vertexRef();
		final E edge = overlayGraph.edgeRef();
		lock.readLock().lock();
		try
		{

			// See if we can find a vertex.
			if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
			{
				navigation.notifyNavigateToVertex( vertex );
			}
			// See if we can find an edge.
			else if ( renderer.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
			{
				navigation.notifyNavigateToEdge( edge );
			}

		}
		finally
		{
			lock.readLock().unlock();
			overlayGraph.releaseRef( edge );
			overlayGraph.releaseRef( vertex );
		}
	}

	private void focus( final int x, final int y )
	{
		final V vertex = overlayGraph.vertexRef();
		lock.readLock().lock();
		try
		{
			focus.focusVertex( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex ) ); // if clicked outside, getVertexAt == null, clears the focus.
		}
		finally
		{
			lock.readLock().unlock();
			overlayGraph.releaseRef( vertex );
		}
	}

	/*
	 * BEHAVIOURS
	 */

	/**
	 * Behaviour to focus a vertex with a mouse click. If the click happens
	 * outside of a vertex, the focus is cleared.
	 */
	private class ClickFocusBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickFocusBehaviour( final String name )
		{
			super( name );
		}

		@Override
		public void click( final int x, final int y )
		{
			focus( x, y );
		}
	}

	/**
	 * Behaviour to select a vertex or edge with a mouse click.
	 */
	private class ClickSelectionBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		private final boolean addToSelection;

		public ClickSelectionBehaviour( final String name, final boolean addToSelection )
		{
			super( name );
			this.addToSelection = addToSelection;
		}

		@Override
		public void click( final int x, final int y )
		{
			select( x, y, addToSelection );
		}
	}

	/**
	 * Behaviour to navigate to a vertex with a mouse click.
	 */
	private class ClickNavigateBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickNavigateBehaviour( final String name )
		{
			super( name );
		}

		@Override
		public void click( final int x, final int y )
		{
			navigate( x, y );
		}
	}

}
