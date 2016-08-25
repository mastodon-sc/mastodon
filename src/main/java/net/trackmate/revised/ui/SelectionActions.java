/**
 *
 */
package net.trackmate.revised.ui;

import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.util.AbstractActions;
import bdv.viewer.InputActionBindings;
import net.trackmate.collection.RefList;
import net.trackmate.collection.RefSet;
import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphChangeNotifier;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.algorithm.traversal.DepthFirstSearch;
import net.trackmate.graph.algorithm.traversal.GraphSearch.SearchDirection;
import net.trackmate.graph.algorithm.traversal.SearchListener;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.undo.UndoPointMarker;

/**
 * User-interface actions that are related to a model selection.
 *
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class SelectionActions< V extends Vertex< E >, E extends Edge< V > >
		extends AbstractActions
{

	private static final String DELETE_SELECTION = "delete selection";

	private static final String[] DELETE_SELECTION_KEYS = new String[] { "shift DELETE" };

	public static < V extends Vertex< E >, E extends Edge< V > > void installActionBindings(
			final InputActionBindings inpputActionBindings,
			final InputTriggerConfig config,
			final String[] keyConfigContexts,
			final ListenableGraph< V, E > graph,
			final GraphChangeNotifier notify,
			final Selection< V, E > selection,
			final UndoPointMarker undo )
	{
		final SelectionActions< V, E > sa = new SelectionActions<>( inpputActionBindings, config, keyConfigContexts, graph, notify, selection, undo );
		sa.runnableAction( sa.getDeleteSelectionAction(), DELETE_SELECTION, DELETE_SELECTION_KEYS );
	}

	private final ListenableGraph< V, E > graph;

	private final GraphChangeNotifier notify;

	private final UndoPointMarker undo;

	private final Selection< V, E > selection;

	private final SelectionActions< V, E >.DeleteSelectionAction deleteSelectionAction;

	private SelectionActions(
			final InputActionBindings inputActionBindings,
			final InputTriggerConfig config,
			final String[] keyConfigContexts,
			final ListenableGraph< V, E > graph,
			final GraphChangeNotifier notify,
			final Selection< V, E > selection,
			final UndoPointMarker undo )
	{
		super( inputActionBindings, "selection", config, keyConfigContexts );
		this.graph = graph;
		this.notify = notify;
		this.selection = selection;
		this.undo = undo;

		deleteSelectionAction = new DeleteSelectionAction();
	}

	public Runnable getSelectWholeTrackAction(final boolean clear)
	{
		return new TrackSelectionAction( SearchDirection.UNDIRECTED, clear );
	}

	public Runnable getSelectTrackDownardAction( final boolean clear )
	{
		return new TrackSelectionAction( SearchDirection.DIRECTED, clear );
	}

	public Runnable getSelectTrackUpwardAction( final boolean clear )
	{
		return new TrackSelectionAction( SearchDirection.REVERSED, clear );
	}

	private DeleteSelectionAction getDeleteSelectionAction()
	{
		return deleteSelectionAction;
	}

	private class DeleteSelectionAction implements Runnable
	{
		@Override
		public void run()
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

	private class TrackSelectionAction implements Runnable
	{
		private final SearchDirection directivity;

		private final boolean clear;

		private TrackSelectionAction( final SearchDirection directivity, final boolean clear )
		{
			this.directivity = directivity;
			this.clear = clear;
		}

		@Override
		public void run()
		{
			selection.pauseListeners();
			final RefSet< V > vertices = selection.getSelectedVertices();
			if ( clear )
				selection.clearSelection();

			// Prepare the iterator.
			final RefList< V > vList = CollectionUtils.createRefList( graph.vertices() );
			final RefList< E > eList = CollectionUtils.createRefList( graph.edges() );
			final DepthFirstSearch< V, E > search = new DepthFirstSearch<>( graph, directivity );
			search.setTraversalListener( new SearchListener< V, E, DepthFirstSearch< V, E > >()
			{

				@Override
				public void processVertexLate( final V vertex, final DepthFirstSearch< V, E > search )
				{}

				@Override
				public void processVertexEarly( final V vertex, final DepthFirstSearch< V, E > search )
				{
					vList.add( vertex );
				}

				@Override
				public void processEdge( final E edge, final V from, final V to, final DepthFirstSearch< V, E > search )
				{
					eList.add( edge );
				}
			} );

			// Iterate from all vertices that were in the selection.
			for ( final V v : vertices )
				search.start( v );

			// Select iterated stuff.
			selection.setVerticesSelected( vList, true );
			selection.setEdgesSelected( eList, true );

			undo.setUndoPoint();
			notify.notifyGraphChanged();
			selection.resumeListeners();
		}
	}

}
