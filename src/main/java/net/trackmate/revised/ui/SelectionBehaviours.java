/**
 *
 */
package net.trackmate.revised.ui;

import net.trackmate.collection.RefSet;
import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphChangeNotifier;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.AbstractBehaviours;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.undo.UndoPointMarker;

import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;

/**
 * User-interface actions that are related to a model selection.
 *
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class SelectionBehaviours< V extends Vertex< E >, E extends Edge< V > >
		extends AbstractBehaviours
{

	private static final String DELETE_SELECTION = "delete selection";

	private static final String[] DELETE_SELECTION_KEYS = new String[] { "shift DELETE" };

	public static < V extends Vertex< E >, E extends Edge< V > > void installActionBindings(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final String[] keyConfigContexts,
			final ListenableGraph< V, E > graph,
			final GraphChangeNotifier notify,
			final Selection< V, E > selection,
			final UndoPointMarker undo )
	{
		new SelectionBehaviours<>( triggerBehaviourBindings, config, keyConfigContexts, graph, notify, selection, undo );
	}

	private final ListenableGraph< V, E > graph;

	private final GraphChangeNotifier notify;

	private final UndoPointMarker undo;

	private final Selection< V, E > selection;

	private SelectionBehaviours(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final InputTriggerConfig config,
			final String[] keyConfigContexts,
			final ListenableGraph< V, E > graph,
			final GraphChangeNotifier notify,
			final Selection< V, E > selection,
			final UndoPointMarker undo )
	{
		super( triggerBehaviourBindings, "selection", config, keyConfigContexts );
		this.graph = graph;
		this.notify = notify;
		this.selection = selection;
		this.undo = undo;

		behaviour( new DeleteSelection(), DELETE_SELECTION, DELETE_SELECTION_KEYS );
	}

	private class DeleteSelection implements ClickBehaviour
	{
		@Override
		public void click( final int x, final int y )
		{
			selection.pauseListeners();
			final RefSet< E > edges = selection.getSelectedEdges();
			final RefSet< V > vertices = selection.getSelectedVertices();
			selection.clearSelection();

			for ( final E e : edges )
				graph.remove( e );

			for ( final V v : vertices )
				graph.remove( v );

			undo.setUndoPoint();
			notify.notifyGraphChanged();
			selection.resumeListeners();
		}
	}

}
