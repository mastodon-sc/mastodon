package net.trackmate.revised.ui;

import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;
import net.trackmate.graph.Edge;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.AbstractBehaviours;
import net.trackmate.revised.ui.selection.HighlightModel;
import net.trackmate.undo.UndoPointMarker;

/**
 * User-interface actions that are related to a model highlight.
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class HighlightBehaviours< V extends Vertex< E >, E extends Edge< V > >
		extends AbstractBehaviours
{

	private static final String REMOVE_HIGHLIGHTED_VERTEX = "remove highlight vertex";
	private static final String REMOVE_HIGHLIGHTED_EDGE = "remove highlight edge";

	private static final String[] REMOVE_HIGHLIGHTED_VERTEX_KEYS = new String[] { "D" };
	private static final String[] REMOVE_HIGHLIGHTED_EDGE_KEYS = new String[] { "D" };

	public static < V extends Vertex< E >, E extends Edge< V > > void installActionBindings(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final ListenableGraph< V, E > graph,
			final HighlightModel< V, E > highlight,
			final UndoPointMarker undo )
	{
		new HighlightBehaviours<>( triggerBehaviourBindings, config, graph, highlight, undo );
	}

	private final UndoPointMarker undo;

	private final HighlightModel< V, E > highlight;

	private final ListenableGraph< V, E > graph;

	private HighlightBehaviours(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final ListenableGraph< V, E > graph,
			final HighlightModel< V, E > highlight,
			final UndoPointMarker undo )
	{
		super( triggerBehaviourBindings, "highlight", config, new String[] {} );
		this.graph = graph;
		this.highlight = highlight;
		this.undo = undo;

		// Behaviours
		behaviour( new RemoveHighlightedVertex(), REMOVE_HIGHLIGHTED_VERTEX, REMOVE_HIGHLIGHTED_VERTEX_KEYS );
		behaviour( new RemoveHighlightedEdge(), REMOVE_HIGHLIGHTED_EDGE, REMOVE_HIGHLIGHTED_EDGE_KEYS );
	}

	private class RemoveHighlightedVertex implements ClickBehaviour
	{
		private final V ref;

		public RemoveHighlightedVertex()
		{
			ref = graph.vertexRef();
		}

		@Override
		public void click( final int x, final int y )
		{
			final V v = highlight.getHighlightedVertex( ref );
			if ( v != null )
			{
				graph.remove( v );
				undo.setUndoPoint();
				graph.notifyGraphChanged();
			}
		}
	}

	private class RemoveHighlightedEdge implements ClickBehaviour
	{
		private final E ref;

		public RemoveHighlightedEdge()
		{
			ref = graph.edgeRef();
		}

		@Override
		public void click( final int x, final int y )
		{
			final E e = highlight.getHighlightedEdge( ref );
			if ( e != null )
			{
				graph.remove( e );
				undo.setUndoPoint();
				graph.notifyGraphChanged();
			}
		}
	}

}
