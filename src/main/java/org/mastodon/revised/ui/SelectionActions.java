/**
 *
 */
package org.mastodon.revised.ui;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.revised.ui.selection.Selection;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;

/**
 * User-interface actions that are related to a model selection.
 *
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class SelectionActions< V extends Vertex< E >, E extends Edge< V > >
		extends Actions
{
	private static final String DELETE_SELECTION = "delete selection";

	private static final String[] DELETE_SELECTION_KEYS = new String[] { "shift DELETE" };

	private static final String SELECT_WHOLE_TRACK = "select whole track";

	private static final String SELECT_WHOLE_TRACK_KEYS = "shift SPACE";

	private static final String SELECT_TRACK_DOWNWARD = "select track downward";

	private static final String SELECT_TRACK_DOWNWARD_KEYS = "shift PAGE_DOWN";

	private static final String SELECT_TRACK_UPWARD = "select track upward";

	private static final String SELECT_TRACK_UPWARD_KEYS = "shift PAGE_UP";

	public static < V extends Vertex< E >, E extends Edge< V > > void installActionBindings(
			final InputActionBindings inputActionBindings,
			final InputTriggerConfig config,
			final String[] keyConfigContexts,
			final ListenableGraph< V, E > graph,
			final GraphChangeNotifier notify,
			final Selection< V, E > selection,
			final UndoPointMarker undo )
	{
		final SelectionActions< V, E > sa = new SelectionActions<>( config, keyConfigContexts, graph, notify, selection, undo );

		sa.runnableAction( sa.getDeleteSelectionAction(), DELETE_SELECTION, DELETE_SELECTION_KEYS );
		sa.runnableAction( sa.getSelectWholeTrackAction( true ), SELECT_WHOLE_TRACK, SELECT_WHOLE_TRACK_KEYS );
		sa.runnableAction( sa.getSelectTrackDownwardAction( true ), SELECT_TRACK_DOWNWARD, SELECT_TRACK_DOWNWARD_KEYS );
		sa.runnableAction( sa.getSelectTrackUpwardAction( true ), SELECT_TRACK_UPWARD, SELECT_TRACK_UPWARD_KEYS );

		sa.install( inputActionBindings, "selection" );
	}

	private final ListenableGraph< V, E > graph;

	private final GraphChangeNotifier notify;

	private final UndoPointMarker undo;

	private final Selection< V, E > selection;

	private final SelectionActions< V, E >.DeleteSelectionAction deleteSelectionAction;

	private SelectionActions(
			final InputTriggerConfig config,
			final String[] keyConfigContexts,
			final ListenableGraph< V, E > graph,
			final GraphChangeNotifier notify,
			final Selection< V, E > selection,
			final UndoPointMarker undo )
	{
		super( config, keyConfigContexts );
		this.graph = graph;
		this.notify = notify;
		this.selection = selection;
		this.undo = undo;

		deleteSelectionAction = new DeleteSelectionAction();
	}

	private Runnable getSelectWholeTrackAction( final boolean clear )
	{
		return new TrackSelectionAction( SearchDirection.UNDIRECTED, clear );
	}

	private Runnable getSelectTrackDownwardAction( final boolean clear )
	{
		return new TrackSelectionAction( SearchDirection.DIRECTED, clear );
	}

	private Runnable getSelectTrackUpwardAction( final boolean clear )
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
			final RefSet< V > vertices = RefCollections.createRefSet( graph.vertices() );
			vertices.addAll( selection.getSelectedVertices() );
			final V ref = graph.vertexRef();
			for ( final E e : selection.getSelectedEdges() )
			{
				vertices.add( e.getSource( ref ) );
				vertices.add( e.getTarget( ref ) );
			}
			graph.releaseRef( ref );

			if ( clear )
				selection.clearSelection();

			// Prepare the iterator.
			final RefSet< V > vSet = RefCollections.createRefSet( graph.vertices() );
			final RefSet< E > eSet = RefCollections.createRefSet( graph.edges() );
			final DepthFirstSearch< V, E > search = new DepthFirstSearch<>( graph, directivity );
			search.setTraversalListener( new SearchListener< V, E, DepthFirstSearch< V, E > >()
			{
				@Override
				public void processVertexLate( final V vertex, final DepthFirstSearch< V, E > search )
				{}

				@Override
				public void processVertexEarly( final V vertex, final DepthFirstSearch< V, E > search )
				{
					vSet.add( vertex );
				}

				@Override
				public void processEdge( final E edge, final V from, final V to, final DepthFirstSearch< V, E > search )
				{
					eSet.add( edge );
				}

				@Override
				public void crossComponent( final V from, final V to, final DepthFirstSearch< V, E > search )
				{}
			} );

			// Iterate from all vertices that were in the selection.
			for ( final V v : vertices )
			{
				if ( vSet.contains( v ) )
					continue;

				search.start( v );
			}

			// Select iterated stuff.
			selection.setVerticesSelected( vSet, true );
			selection.setEdgesSelected( eSet, true );
			selection.resumeListeners();
		}
	}
}
