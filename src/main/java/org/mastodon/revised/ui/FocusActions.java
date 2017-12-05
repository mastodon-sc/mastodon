package org.mastodon.revised.ui;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.mastodon.Ref;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

public class FocusActions< V extends Vertex< E > & Ref< V >, E extends Edge< V > & Ref< E > >
{
	public static final String NAVIGATE_CHILD = "navigate to child";
	public static final String NAVIGATE_PARENT = "navigate to parent";
	public static final String NAVIGATE_SIBLING = "navigate to sibling";
	public static final String NAVIGATE_BRANCH_CHILD = "navigate to branch child";
	public static final String NAVIGATE_BRANCH_PARENT = "navigate to branch parent";

	public static final String[] NAVIGATE_CHILD_KEYS = new String[] { "ctrl DOWN" };
	public static final String[] NAVIGATE_PARENT_KEYS = new String[] { "ctrl UP" };
	public static final String[] NAVIGATE_SIBLING_KEYS = new String[] { "ctrl LEFT", "ctrl RIGHT" };
	public static final String[] NAVIGATE_BRANCH_CHILD_KEYS = new String[] { "ctrl shift DOWN" };
	public static final String[] NAVIGATE_BRANCH_PARENT_KEYS = new String[] { "ctrl shift UP" };

	private final RunnableAction navigateToChildAction;

	private final RunnableAction navigateToParentAction;

	private final RunnableAction navigateToSiblingAction;

	private final RunnableAction navigateToBranchChildAction;

	private final RunnableAction navigateToBranchParentAction;

	// TODO: rename to "install" (in all similar classes)
	public static < V extends Vertex< E > & Ref< V >, E extends Edge< V > & Ref< E > > void installActionBindings(
			final Actions actions,
			final Graph< V, E > graph,
			final ReentrantReadWriteLock lock,
			final FocusModel< V, E > focus,
			final SelectionModel< V, E > selection,
			final NavigationHandler< V, E > navigation )
	{
		final FocusActions< V, E > fa = new FocusActions<>( graph, lock, focus, selection, navigation );

		actions.namedAction( fa.navigateToChildAction, NAVIGATE_CHILD_KEYS );
		actions.namedAction( fa.navigateToParentAction, NAVIGATE_PARENT_KEYS );
		actions.namedAction( fa.navigateToSiblingAction, NAVIGATE_SIBLING_KEYS );
		actions.namedAction( fa.navigateToBranchChildAction, NAVIGATE_BRANCH_CHILD_KEYS );
		actions.namedAction( fa.navigateToBranchParentAction, NAVIGATE_BRANCH_PARENT_KEYS );
	}

	private enum Direction
	{
		CHILD,
		PARENT,
		SIBLING,
	}

	private final Graph< V, E > graph;

	// TODO: use readLock...
	private final ReentrantReadWriteLock lock;

	private final FocusModel< V, E > focus;

	private final SelectionModel< V, E > selection;

	private final NavigationHandler< V, E > navigation;

	public FocusActions(
			final Graph< V, E > graph,
			final ReentrantReadWriteLock lock,
			final FocusModel< V, E > focus,
			final SelectionModel< V, E > selection,
			final NavigationHandler< V, E > navigation )
	{
		this.graph = graph;
		this.lock = lock;
		this.focus = focus;
		this.selection = selection;
		this.navigation = navigation;

		navigateToChildAction = new RunnableAction( NAVIGATE_CHILD, () -> selectAndFocusNeighbor( Direction.CHILD, false ) );
		navigateToParentAction = new RunnableAction( NAVIGATE_PARENT, () -> selectAndFocusNeighbor( Direction.PARENT, false ) );
		navigateToSiblingAction = new RunnableAction( NAVIGATE_SIBLING, () -> selectAndFocusNeighbor( Direction.SIBLING, false ) );
		navigateToBranchChildAction = new RunnableAction( NAVIGATE_BRANCH_CHILD, () -> selectAndFocusBranchNeighbor( Direction.CHILD, false )  );
		navigateToBranchParentAction = new RunnableAction( NAVIGATE_BRANCH_PARENT, () -> selectAndFocusBranchNeighbor( Direction.PARENT, false )  );
	}

	/**
	 * Focus a neighbor (parent, child, sibling) of the
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
		final V ref1 = graph.vertexRef();
		final V ref2 = graph.vertexRef();
		try
		{
			final V vertex = focus.getFocusedVertex( ref1 );
			if ( vertex == null )
				return;

			if ( select )
				selection.setSelected( vertex, true );

			final V current;
			switch ( direction )
			{
			case CHILD:
				current = firstChild( vertex, ref2 );
				break;
			case PARENT:
				current = firstParent( vertex, ref2 );
				break;
			case SIBLING: default:
				current = nextSibling( vertex, ref2 );
				break;
			}

			if ( current != null )
				navigation.notifyNavigateToVertex( current );
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
		}
	}

	private void selectAndFocusBranchNeighbor( final Direction direction, final boolean select )
	{
		final V ref1 = graph.vertexRef();
		final V ref2 = graph.vertexRef();
		try
		{
			final V vertex = focus.getFocusedVertex( ref1 );
			if ( vertex == null )
				return;

			if ( select )
				selection.setSelected( vertex, true );

			final V current;
			switch ( direction )
			{
			case CHILD:
				current = firstBranchChild( vertex, ref2 );
				break;
			case PARENT: default:
				current = firstBranchParent( vertex, ref2 );
				break;
			}

			if ( current != null )
				navigation.notifyNavigateToVertex( current );
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
		}
	}

	private V firstChild( V vertex, V ref )
	{
		final Edges< E > outgoing = vertex.outgoingEdges();
		return outgoing.isEmpty() ? null : outgoing.iterator().next().getTarget( ref );
	}

	private V firstParent( V vertex, V ref )
	{
		final Edges< E > incoming = vertex.incomingEdges();
		return incoming.isEmpty() ? null : incoming.iterator().next().getSource( ref );
	}

	private V firstBranchChild( V vertex, V ref )
	{
		V v = vertex;
		int d = 0;
		while ( true )
		{
			if ( v.outgoingEdges().isEmpty() )
				break;
			v = firstChild( v, ref );
			++d;
			if ( v.outgoingEdges().size() > 1 )
				break;
		}
		return v;
	}

	private V firstBranchParent( V vertex, V ref )
	{
		V v = vertex;
		int d = 0;
		while ( true )
		{
			if ( v.incomingEdges().isEmpty() )
				break;
			v = firstParent( v, ref );
			++d;
			if ( v.outgoingEdges().size() > 1 )
				break;
		}
		return v;
	}

	private V nextSibling( V vertex, V ref )
	{
		V ref1 = graph.vertexRef();
		V ref2 = graph.vertexRef();
		try
		{
			V child = ref1;
			V parent = ref2;
			child.refTo( vertex );
			int d = 0;
			while ( true )
			{
				if ( child.incomingEdges().isEmpty() )
					return null;
				parent.refTo( firstParent( child, ref ) );
				if ( parent.outgoingEdges().size() > 1 )
					break;
				child.refTo( parent );
				++d;
			}

			final Edges< E > outgoing = parent.outgoingEdges();
			final Iterator< E > iter = outgoing.iterator();
			while ( iter.hasNext() )
			{
				if ( iter.next().getTarget( ref ).equals( child ) )
				{
					child = iter.hasNext()
							? iter.next().getTarget( ref )
							: outgoing.iterator().next().getTarget( ref );

					for ( ; d > 0 && child != null; --d )
						child = firstChild( child, ref );

					return child;
				}
			}

			return null;
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
		}
	}
}
