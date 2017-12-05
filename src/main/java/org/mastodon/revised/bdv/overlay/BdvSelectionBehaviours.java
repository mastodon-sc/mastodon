package org.mastodon.revised.bdv.overlay;

import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

public class BdvSelectionBehaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
{
	public static final String NAVIGATE_TO_VERTEX = "bdv click navigate to vertex";
	public static final String SELECT = "bdv click select";
	public static final String ADD_SELECT = "bdv click add to selection";

	private static final String[] NAVIGATE_TO_VERTEX_KEYS = new String[] { "double-click button1", "shift double-click button1" };
	private static final String[] SELECT_KEYS = new String[] { "button1" };
	private static final String[] ADD_SELECT_KEYS = new String[] { "shift button1" };

	public static final double EDGE_SELECT_DISTANCE_TOLERANCE = 5.0;
	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final ClickNavigateBehaviour navigateBehaviour;

	private final ClickSelectionBehaviour selectBehaviour;

	private final ClickSelectionBehaviour addSelectBehaviour;

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void install(
			final Behaviours behaviours,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final SelectionModel< V, E > selection,
			final NavigationHandler< V, E > navigation )
	{
		final BdvSelectionBehaviours< V, E > sb = new BdvSelectionBehaviours<>( overlayGraph, renderer, selection, navigation );

		behaviours.namedBehaviour( sb.navigateBehaviour, NAVIGATE_TO_VERTEX_KEYS );
		behaviours.namedBehaviour( sb.selectBehaviour, SELECT_KEYS );
		behaviours.namedBehaviour( sb.addSelectBehaviour, ADD_SELECT_KEYS );
	}

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final SelectionModel< V, E > selection;

	private final NavigationHandler< V, E > navigation;

	private BdvSelectionBehaviours(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final SelectionModel< V, E > selection,
			final NavigationHandler< V, E > navigation )
	{
		this.overlayGraph = overlayGraph;
		this.renderer = renderer;
		this.selection = selection;
		this.navigation = navigation;

		navigateBehaviour = new ClickNavigateBehaviour( NAVIGATE_TO_VERTEX );
		selectBehaviour = new ClickSelectionBehaviour( SELECT, false );
		addSelectBehaviour = new ClickSelectionBehaviour( ADD_SELECT, true );
	}

	private void select( final int x, final int y, final boolean addToSelection )
	{
		selection.pauseListeners();

		final V vertex = overlayGraph.vertexRef();
		final E edge = overlayGraph.edgeRef();

		// See if we can select a vertex.
		if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
		{
			final boolean selected = selection.isSelected( vertex );
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( vertex, !selected );
		}
		// See if we can select an edge.
		else if ( renderer.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			final boolean selected = selection.isSelected( vertex );
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( edge, !selected );
		}
		// Nothing found. clear selection if addToSelection == false
		else if ( !addToSelection )
			selection.clearSelection();

		overlayGraph.releaseRef( vertex );
		overlayGraph.releaseRef( edge );

		selection.resumeListeners();
	}

	private void navigate( final int x, final int y )
	{
		final V vertex = overlayGraph.vertexRef();
		final E edge = overlayGraph.edgeRef();

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

		overlayGraph.releaseRef( vertex );
		overlayGraph.releaseRef( edge );
	}

	/*
	 * BEHAVIOURS
	 */

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
