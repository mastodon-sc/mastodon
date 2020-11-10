package org.mastodon.views.dbvv;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

public class DBvvSelectionBehaviours
{
	public static final String FOCUS_VERTEX = "bvv click focus vertex";
	public static final String NAVIGATE_TO_VERTEX = "bvv click navigate to vertex";
	public static final String SELECT = "bvv click select";
	public static final String ADD_SELECT = "bvv click add to selection";

	private static final String[] FOCUS_VERTEX_KEYS = new String[] { "button1", "shift button1" };
	private static final String[] NAVIGATE_TO_VERTEX_KEYS = new String[] { "double-click button1", "shift double-click button1", "alt button1" };
	private static final String[] SELECT_KEYS = new String[] { "button1" };
	private static final String[] ADD_SELECT_KEYS = new String[] { "shift button1" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.BIGVOLUMEVIEWER );
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

	public static void install(
			final Behaviours behaviours,
			final ModelGraph graph,
			final DBvvRenderer scene,
			final SelectionModel< Spot, Link > selection,
			final FocusModel< Spot, Link > focus,
			final NavigationHandler< Spot, Link > navigation )
	{
		final DBvvSelectionBehaviours sb = new DBvvSelectionBehaviours( graph, scene, selection, focus, navigation );

		behaviours.namedBehaviour( sb.focusVertexBehaviour, FOCUS_VERTEX_KEYS );
		behaviours.namedBehaviour( sb.navigateBehaviour, NAVIGATE_TO_VERTEX_KEYS );
		behaviours.namedBehaviour( sb.selectBehaviour, SELECT_KEYS );
		behaviours.namedBehaviour( sb.addSelectBehaviour, ADD_SELECT_KEYS );
	}

	private final ModelGraph graph;

	private final ReentrantReadWriteLock lock;

	private final DBvvRenderer scene;

	private final SelectionModel< Spot, Link > selection;

	private final FocusModel< Spot, Link > focus;

	private final NavigationHandler< Spot, Link > navigation;

	private DBvvSelectionBehaviours(
			final ModelGraph graph,
			final DBvvRenderer scene,
			final SelectionModel< Spot, Link > selection,
			final FocusModel< Spot, Link > focus,
			final NavigationHandler< Spot, Link > navigation )
	{
		this.graph = graph;
		this.lock = graph.getLock();
		this.scene = scene;
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
		final Spot vertex = graph.vertexRef();
		final Link edge = graph.edgeRef();
		lock.readLock().lock();
		try
		{
			final DBvvRenderer.Closest< Link > closestEdge = scene.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge );
			final DBvvRenderer.Closest< Spot > closestVertex = scene.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex );

			// See if we can select an edge.
			if ( closestEdge.get() != null && closestEdge.distance() <= closestVertex.distance() )
			{
				final boolean selected = selection.isSelected( edge );
				if ( !addToSelection )
					selection.clearSelection();
				selection.setSelected( edge, !selected );
			}
			// See if we can select a vertex.
			else if ( closestVertex.get() != null && closestVertex.distance() <= closestEdge.distance() )
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
			graph.releaseRef( vertex );
			graph.releaseRef( edge );
			lock.readLock().unlock();
			selection.resumeListeners();
		}
	}

	private void navigate( final int x, final int y )
	{
		final Spot vertex = graph.vertexRef();
		final Link edge = graph.edgeRef();
		lock.readLock().lock();
		try
		{
			final DBvvRenderer.Closest< Link > closestEdge = scene.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge );
			final DBvvRenderer.Closest< Spot > closestVertex = scene.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex );

			// See if we can find a vertex.
			if ( closestVertex.get() != null && closestVertex.distance() <= closestEdge.distance() )
			{
				System.out.println("navigateToVertex " + vertex);
				navigation.notifyNavigateToVertex( vertex );
			}
			// See if we can find an edge.
			else if ( closestEdge.get() != null && closestEdge.distance() <= closestVertex.distance() )
			{
				System.out.println("navigateToEdge " + edge);
				navigation.notifyNavigateToEdge( edge );
			}
		}
		finally
		{
			lock.readLock().unlock();
			graph.releaseRef( edge );
			graph.releaseRef( vertex );
		}
	}

	private void focus( final int x, final int y )
	{
		final Spot vertex = graph.vertexRef();
		lock.readLock().lock();
		try
		{
			final DBvvRenderer.Closest< Spot > closestVertex = scene.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex );
			focus.focusVertex( closestVertex.get() ); // if clicked outside, closestVertex.get() == null, clears the focus.
		}
		finally
		{
			lock.readLock().unlock();
			graph.releaseRef( vertex );
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
