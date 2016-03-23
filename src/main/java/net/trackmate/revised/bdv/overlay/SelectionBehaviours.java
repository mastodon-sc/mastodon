package net.trackmate.revised.bdv.overlay;

import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour.NamedBehaviourAdder;

import bdv.viewer.TriggerBehaviourBindings;

public class SelectionBehaviours< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
{
	public static final String NAVIGATE_TO_VERTEX_NAME = "bdv click navigate to vertex";
	public static final String SELECT_NAME = "bdv click select";
	public static final String ADD_SELECT_NAME = "bdv click add to selection";

	public static final double EDGE_SELECT_DISTANCE_TOLERANCE = 5.0;

	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 5.0;

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final OverlaySelection< V, E > selection;

	private final OverlayNavigation< V, E > navigation;

	private final BehaviourMap behaviourMap;

	public SelectionBehaviours(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final OverlaySelection< V, E > selection,
			final OverlayNavigation< V, E > navigation )
	{
		this.overlayGraph = overlayGraph;
		this.renderer = renderer;
		this.selection = selection;
		this.navigation = navigation;

		behaviourMap = new BehaviourMap();
		final NamedBehaviourAdder adder = new NamedBehaviourAdder( behaviourMap );
		adder.put( new ClickNavigateBehaviour() );
		adder.put( new ClickSelectionBehaviour( SELECT_NAME, false ) );
		adder.put( new ClickSelectionBehaviour( ADD_SELECT_NAME, true ) );

	}

	public void installBehaviourBindings( final TriggerBehaviourBindings triggerbindings, final InputTriggerAdder.Factory keyConfig )
	{
		final InputTriggerMap inputMap = new InputTriggerMap();
		final InputTriggerAdder adder = keyConfig.inputTriggerAdder( inputMap, "bdv" );

		adder.put( NAVIGATE_TO_VERTEX_NAME, "double-click button1", "shift double-click button1" );
		adder.put( SELECT_NAME, "button1" );
		adder.put( ADD_SELECT_NAME, "shift button1" );

		triggerbindings.addBehaviourMap( "selection", behaviourMap );
		triggerbindings.addInputTriggerMap( "selection", inputMap );
	}

	/*
	 * PRIVATE METHODS
	 */

	private void select( final int x, final int y, final boolean addToSelection )
	{
		selection.pauseListeners();

		final V vertex = overlayGraph.vertexRef();
		final E edge = overlayGraph.edgeRef();

		// See if we can select a vertex.
		if ( renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
		{
			final boolean selected = vertex.isSelected();
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( vertex, !selected );
		}
		// See if we can select an edge.
		else if ( renderer.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			final boolean selected = edge.isSelected();
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
		public ClickNavigateBehaviour()
		{
			super( NAVIGATE_TO_VERTEX_NAME );
		}

		@Override
		public void click( final int x, final int y )
		{
			navigate( x, y );
		}
	}

}
