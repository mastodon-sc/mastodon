package org.mastodon.revised.ui;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HighlightModel;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.ui.behaviour.ClickBehaviour;
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

	private final RemoveHighlightedVertex removeHighlightedVertexBehaviour;

	private final RemoveHighlightedEdge removeHighlightedEdgeBehaviour;

	// TODO: rename to "install" (in all similar classes)
	public static < V extends Vertex< E >, E extends Edge< V > > void installActionBindings(
			final Behaviours behaviours,
			final ListenableGraph< V, E > graph,
			final ReentrantReadWriteLock lock,
			final GraphChangeNotifier notify,
			final HighlightModel< V, E > highlight,
			final UndoPointMarker undo )
	{
		final HighlightBehaviours< V, E > hb = new HighlightBehaviours<>( graph, lock, notify, highlight, undo );

		behaviours.namedBehaviour( hb.removeHighlightedVertexBehaviour, REMOVE_HIGHLIGHTED_VERTEX_KEYS );
		behaviours.namedBehaviour( hb.removeHighlightedEdgeBehaviour, REMOVE_HIGHLIGHTED_EDGE_KEYS );
	}

	private final ListenableGraph< V, E > graph;

	private final ReentrantReadWriteLock lock;

	private final GraphChangeNotifier notify;

	private final HighlightModel< V, E > highlight;

	private final UndoPointMarker undo;

	private HighlightBehaviours(
			final ListenableGraph< V, E > graph,
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
