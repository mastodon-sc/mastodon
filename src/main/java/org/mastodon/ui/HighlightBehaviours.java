/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HighlightModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

/**
 * User-interface actions that are related to a model highlight.
 *
 * @param <V>
 *            the type of the model vertices.
 * @param <E>
 *            the type of the model edges.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class HighlightBehaviours< V extends Vertex< E >, E extends Edge< V > >
{
	private static final String REMOVE_HIGHLIGHTED_VERTEX = "remove highlighted vertex";

	private static final String REMOVE_HIGHLIGHTED_EDGE = "remove highlighted edge";

	private static final String[] REMOVE_HIGHLIGHTED_VERTEX_KEYS = new String[] { "D" };

	private static final String[] REMOVE_HIGHLIGHTED_EDGE_KEYS = new String[] { "D" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			// TODO Grapher as well?
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.BIGDATAVIEWER, KeyConfigContexts.TRACKSCHEME );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( REMOVE_HIGHLIGHTED_VERTEX, REMOVE_HIGHLIGHTED_VERTEX_KEYS,
					"Remove highlighted spot (spot under the mouse)." );
			descriptions.add( REMOVE_HIGHLIGHTED_EDGE, REMOVE_HIGHLIGHTED_EDGE_KEYS,
					"Remove highlighted edge (edge under the mouse)." );
		}
	}

	private final RemoveHighlightedVertex removeHighlightedVertexBehaviour;

	private final RemoveHighlightedEdge removeHighlightedEdgeBehaviour;

	public static < V extends Vertex< E >, E extends Edge< V > > void install(
			final Behaviours behaviours,
			final Graph< V, E > graph,
			final ReentrantReadWriteLock lock,
			final GraphChangeNotifier notify,
			final HighlightModel< V, E > highlight,
			final UndoPointMarker undo )
	{
		final HighlightBehaviours< V, E > hb = new HighlightBehaviours<>( graph, lock, notify, highlight, undo );

		behaviours.namedBehaviour( hb.removeHighlightedVertexBehaviour, REMOVE_HIGHLIGHTED_VERTEX_KEYS );
		behaviours.namedBehaviour( hb.removeHighlightedEdgeBehaviour, REMOVE_HIGHLIGHTED_EDGE_KEYS );
	}

	private final Graph< V, E > graph;

	private final ReentrantReadWriteLock lock;

	private final GraphChangeNotifier notify;

	private final HighlightModel< V, E > highlight;

	private final UndoPointMarker undo;

	private HighlightBehaviours(
			final Graph< V, E > graph,
			final ReentrantReadWriteLock lock,
			final GraphChangeNotifier notify,
			final HighlightModel< V, E > highlight,
			final UndoPointMarker undo )
	{
		this.graph = graph;
		this.lock = lock;
		this.notify = notify;
		this.highlight = highlight;
		this.undo = undo;

		removeHighlightedVertexBehaviour = new RemoveHighlightedVertex( REMOVE_HIGHLIGHTED_VERTEX );
		removeHighlightedEdgeBehaviour = new RemoveHighlightedEdge( REMOVE_HIGHLIGHTED_EDGE );
	}

	private class RemoveHighlightedVertex extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public RemoveHighlightedVertex( final String name )
		{
			super( name );
		}

		@Override
		public void click( final int x, final int y )
		{
			final V ref = graph.vertexRef();
			final V v = highlight.getHighlightedVertex( ref );
			if ( v != null )
			{
				lock.writeLock().lock();
				try
				{
					graph.remove( v );
					undo.setUndoPoint();
					notify.notifyGraphChanged();
				}
				finally
				{
					lock.writeLock().unlock();
				}
			}
			graph.releaseRef( ref );
		}
	}

	private class RemoveHighlightedEdge extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public RemoveHighlightedEdge( final String name )
		{
			super( name );
		}

		@Override
		public void click( final int x, final int y )
		{
			final E ref = graph.edgeRef();
			final E e = highlight.getHighlightedEdge( ref );
			if ( e != null )
			{
				lock.writeLock().lock();
				try
				{
					graph.remove( e );
					undo.setUndoPoint();
					notify.notifyGraphChanged();
				}
				finally
				{
					lock.writeLock().unlock();
				}
			}
			graph.releaseRef( ref );
		}
	}
}
