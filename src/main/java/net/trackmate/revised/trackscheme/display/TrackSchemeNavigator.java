package net.trackmate.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;

import net.imglib2.RealPoint;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import net.trackmate.collection.RefSet;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;

import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour.NamedBehaviourAdder;

import bdv.util.AbstractActions;
import bdv.viewer.InputActionBindings;
import bdv.viewer.TriggerBehaviourBindings;

/**
 * TODO: RENAME.
 *
 * TODO: All focus/selection related stuff could be moved to this class.
 *
 * TODO: Decide on Focus/Selection behaviour. There are two options:
 *
 * 1) Finder-like: Selection is tied to the focus. When moving the focus with
 * arrow keys, the selection moves with the focus. When clicking a vertex it is
 * focused and selected, The focused vertex is always selected. Focus still
 * exists independent of selection: multiple vertices can be selected, but only
 * one of them can have the focus. When extending the selection with shift+arrow
 * keys, the vertex to which the focus moves should be selected. When box
 * selection is drawn, the selected vertex closest to the position where the
 * drag ended should receive the focus.
 *
 * 2) Norton-Commander-like: Selection is independent of focus. Moveing the
 * focus with arrow keys doesn't alter selection. Space key toggles selection of
 * focused vertex. When extending the selection with shift+arrow keys, the
 * selection of the currently focused vertex is toggled, then the focus is
 * moved.
 *
 * Both options should be implemented both options and leave it to the user to
 * enable whatever he prefers.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class TrackSchemeNavigator implements TransformListener< ScreenTransform >, OffsetHeadersListener
{
	public static final String NAVIGATE_CHILD = "ts navigate to child";
	public static final String NAVIGATE_PARENT = "ts navigate to parent";
	public static final String NAVIGATE_LEFT = "ts navigate left";
	public static final String NAVIGATE_RIGHT = "ts navigate right";
	public static final String SELECT_NAVIGATE_CHILD = "ts select navigate to child";
	public static final String SELECT_NAVIGATE_PARENT = "ts select navigate to parent";
	public static final String SELECT_NAVIGATE_LEFT = "ts select navigate left";
	public static final String SELECT_NAVIGATE_RIGHT = "ts select navigate right";
	public static final String TOGGLE_FOCUS_SELECTION = "ts toggle focus selection";
	public static final String FOCUS_VERTEX = "ts click focus vertex";
	public static final String NAVIGATE_TO_VERTEX = "ts click navigate to vertex";
	public static final String SELECT = "ts click select";
	public static final String ADD_SELECT = "ts click add to selection";
	public static final String BOX_SELECT = "ts box selection";
	public static final String BOX_ADD_SELECT = "ts box add to selection";

	public static final String[] NAVIGATE_CHILD_KEYS = new String[] { "DOWN" };
	public static final String[] NAVIGATE_PARENT_KEYS = new String[] { "UP" };
	public static final String[] NAVIGATE_LEFT_KEYS = new String[] { "LEFT" };
	public static final String[] NAVIGATE_RIGHT_KEYS = new String[] { "RIGHT" };
	public static final String[] SELECT_NAVIGATE_CHILD_KEYS = new String[] { "shift DOWN" };
	public static final String[] SELECT_NAVIGATE_PARENT_KEYS = new String[] { "shift UP" };
	public static final String[] SELECT_NAVIGATE_LEFT_KEYS = new String[] { "shift LEFT" };
	public static final String[] SELECT_NAVIGATE_RIGHT_KEYS = new String[] { "shift RIGHT" };
	public static final String[] TOGGLE_FOCUS_SELECTION_KEYS = new String[] { "SPACE" };
	public static final String[] FOCUS_VERTEX_KEYS = new String[] { "button1", "shift button1" };
	public static final String[] NAVIGATE_TO_VERTEX_KEYS = new String[] { "double-click button1", "shift double-click button1" };
	public static final String[] SELECT_KEYS = new String[] { "button1"};
	public static final String[] ADD_SELECT_KEYS = new String[] { "shift button1"};
	public static final String[] BOX_SELECT_KEYS = new String[] { "button1"};
	public static final String[] BOX_ADD_SELECT_KEYS = new String[] { "shift button1"};

	public static final double EDGE_SELECT_DISTANCE_TOLERANCE = 5.0;

	public static enum Direction
	{
		CHILD,
		PARENT,
		LEFT_SIBLING,
		RIGHT_SIBLING
	}

	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	private final TrackSchemeNavigation navigation;

	private final TrackSchemeFocus focus;

	private final TrackSchemeSelection selection;

	private final ScreenTransform screenTransform;

	/**
	 * Current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * Current height of horizontal header.
	 */
	private int headerHeight;

	private final AbstractTrackSchemeOverlay graphOverlay;

	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final BehaviourMap behaviourMap;

	private final BoxSelectionBehaviour boxSelect;

	private final BoxSelectionBehaviour boxSelectAdd;

	public TrackSchemeNavigator(
			final InteractiveDisplayCanvasComponent< ScreenTransform > display,
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final AbstractTrackSchemeOverlay graphOverlay,
			final TrackSchemeFocus focus,
			final TrackSchemeNavigation navigation,
			final TrackSchemeSelection selection )
	{
		this.display = display;
		this.graph = graph;
		this.layout = layout;
		this.graphOverlay = graphOverlay;
		this.focus = focus;
		this.navigation = navigation;
		this.selection = selection;

		behaviourMap = new BehaviourMap();
		final NamedBehaviourAdder adder = new NamedBehaviourAdder( behaviourMap );
		adder.put( new ClickFocusBehaviour() );
		adder.put( new ClickNavigateBehaviour() );
		adder.put( new ClickSelectionBehaviour( SELECT, false ) );
		adder.put( new ClickSelectionBehaviour( ADD_SELECT, true ) );
		boxSelect = new BoxSelectionBehaviour( BOX_SELECT, false );
		adder.put( boxSelect );
		boxSelectAdd = new BoxSelectionBehaviour( BOX_ADD_SELECT, true );
		adder.put( boxSelectAdd );

		screenTransform = new ScreenTransform();
	}

	public void installActionBindings( final InputActionBindings keybindings, final KeyStrokeAdder.Factory keyConfig )
	{
		final AbstractActions actions = new AbstractActions( keybindings, "navigator", keyConfig, new String[] { "ts" } );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.CHILD, false ), NAVIGATE_CHILD, NAVIGATE_CHILD_KEYS );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.PARENT, false ), NAVIGATE_PARENT, NAVIGATE_PARENT_KEYS );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.LEFT_SIBLING, false ), NAVIGATE_LEFT, NAVIGATE_LEFT_KEYS );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.RIGHT_SIBLING, false ), NAVIGATE_RIGHT, NAVIGATE_RIGHT_KEYS );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.CHILD, true ), SELECT_NAVIGATE_CHILD, SELECT_NAVIGATE_CHILD_KEYS );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.PARENT, true ), SELECT_NAVIGATE_PARENT, SELECT_NAVIGATE_PARENT_KEYS );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.LEFT_SIBLING, true ), SELECT_NAVIGATE_LEFT, SELECT_NAVIGATE_LEFT_KEYS );
		actions.runnableAction( () -> selectAndFocusNeighbor( Direction.RIGHT_SIBLING, true ), SELECT_NAVIGATE_RIGHT, SELECT_NAVIGATE_RIGHT_KEYS );
		actions.runnableAction( () -> toggleSelectionOfFocusedVertex(), TOGGLE_FOCUS_SELECTION, TOGGLE_FOCUS_SELECTION_KEYS );
	}

	public void installBehaviourBindings( final TriggerBehaviourBindings triggerbindings, final InputTriggerAdder.Factory keyConfig )
	{
		final InputTriggerMap inputMap = new InputTriggerMap();
		final InputTriggerAdder adder = keyConfig.inputTriggerAdder( inputMap, "ts" );
		adder.put( FOCUS_VERTEX, "button1", "shift button1" );
		adder.put( NAVIGATE_TO_VERTEX, "double-click button1", "shift double-click button1" );
		adder.put( SELECT, "button1" );
		adder.put( ADD_SELECT, "shift button1" );
		adder.put( BOX_SELECT, "button1" );
		adder.put( BOX_ADD_SELECT, "shift button1" );

		triggerbindings.addBehaviourMap( "selection", behaviourMap );
		triggerbindings.addInputTriggerMap( "selection", inputMap );
	}

	/**
	 * Focus a neighbor (parent, child, left sibling, right sibling) of the
	 * currently focused vertex. Possibly, the currently focused vertex is added
	 * to the selection.
	 *
	 * @param direction
	 *            which neighbor to focus.
	 * @param select
	 *            if {@code true}, the currently focussed vertex is added to the
	 *            selection (before moving the focus).
	 */
	private void selectAndFocusNeighbor( final Direction direction, final boolean select )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		selectAndFocusNeighbor( direction, select, ref );
		graph.releaseRef( ref );
	}

	private TrackSchemeVertex selectAndFocusNeighbor( final Direction direction, final boolean select, final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = getFocusedVertex( ref );
		if ( vertex == null )
			return null;

		if ( select )
			selection.setSelected( vertex, true );

		final TrackSchemeVertex current;
		switch ( direction )
		{
		case CHILD:
			current = layout.getFirstActiveChild( vertex, ref );
			break;
		case PARENT:
			current = layout.getFirstActiveParent( vertex, ref );
			break;
		case LEFT_SIBLING:
			current = layout.getLeftSibling( vertex, ref );
			break;
		case RIGHT_SIBLING: default:
			current = layout.getRightSibling( vertex, ref );
			break;
		}

		if ( current != null )
		{
//			focus.focusVertex( current ); // TODO: can this be safely removed? focus is set through navigation.notifyNavigateToVertex() --> TrackSchemePanel.navigateToVertext()
			navigation.notifyNavigateToVertex( current );
		}
		return current;
	}

	/**
	 * Toggle the selected state of the currently focused vertex.
	 */
	private void toggleSelectionOfFocusedVertex()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		final TrackSchemeVertex v = focus.getFocusedVertex( ref );
		if ( v != null )
			selection.toggleSelected( v );
		graph.releaseRef( ref );
	}

	private final RealPoint centerPos = new RealPoint( 2 );

	private double ratioXtoY;

	private TrackSchemeVertex getFocusedVertex( final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = focus.getFocusedVertex( ref );
		return ( vertex != null )
				? vertex
				: layout.getClosestActiveVertex( centerPos, ratioXtoY, ref );
	}

	/*
	 * PRIVATE METHODS
	 */

	private void selectWithin( final int x1, final int y1, final int x2, final int y2, final boolean addToSelection )
	{
		selection.pauseListeners();

		if ( !addToSelection )
			selection.clearSelection();

		final double lx1, ly1, lx2, ly2;
		synchronized ( screenTransform )
		{
			lx1 = screenTransform.screenToLayoutX( x1 );
			ly1 = screenTransform.screenToLayoutY( y1 );
			lx2 = screenTransform.screenToLayoutX( x2 );
			ly2 = screenTransform.screenToLayoutY( y2 );
		}

		final RefSet< TrackSchemeVertex > vs = layout.getVerticesWithin( lx1, ly1, lx2, ly2 );
		final TrackSchemeVertex vertexRef = graph.vertexRef();
		for ( final TrackSchemeVertex v : vs )
		{
			selection.setSelected( v, true );
			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				final TrackSchemeVertex t = e.getTarget( vertexRef );
				if ( vs.contains( t ) )
					selection.setSelected( e, true );
			}
		}
		graph.releaseRef( vertexRef );

		selection.resumeListeners();
	}

	private void select( final int x, final int y, final boolean addToSelection )
	{
		selection.pauseListeners();

		final TrackSchemeVertex vertex = graph.vertexRef();
		final TrackSchemeEdge edge = graph.edgeRef();

		// See if we can select a vertex.
		if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
		{
			final boolean selected = vertex.isSelected();
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( vertex, !selected );
		}
		// See if we can select an edge.
		else if ( graphOverlay.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			final boolean selected = edge.isSelected();
			if ( !addToSelection )
				selection.clearSelection();
			selection.setSelected( edge, !selected );
		}
		// Nothing found. clear selection if addToSelection == false
		else if ( !addToSelection )
			selection.clearSelection();

		graph.releaseRef( vertex );
		graph.releaseRef( edge );

		selection.resumeListeners();
	}

	private void navigate( final int x, final int y )
	{
		final TrackSchemeVertex vertex = graph.vertexRef();
		final TrackSchemeEdge edge = graph.edgeRef();

		// See if we can select a vertex.
		if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
		{
			navigation.notifyNavigateToVertex( vertex );
		}
		// See if we can select an edge.
		else if ( graphOverlay.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
		{
			navigation.notifyNavigateToEdge( edge );
		}

		graph.releaseRef( vertex );
		graph.releaseRef( edge );
	}

	/*
	 * BEHAVIOURS
	 */

	/**
	 * Behaviour to focus a vertex with a mouse click. If the click happens
	 * outside of a vertex, the focus is cleared.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickFocusBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickFocusBehaviour()
		{
			super( FOCUS_VERTEX );
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			final TrackSchemeVertex ref = graph.vertexRef();
			final TrackSchemeVertex vertex = graphOverlay.getVertexAt( x, y, ref );
			if ( vertex != null )
			{
				focus.focusVertex( vertex );
			}
			else
			{
				// Click outside. We clear the focus.
				focus.focusVertex( null );
			}
			graph.releaseRef( ref );
		}
	}

	/**
	 * Behaviour to navigate to a vertex with a mouse click.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
	 */
	private class ClickNavigateBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{
		public ClickNavigateBehaviour()
		{
			super( NAVIGATE_TO_VERTEX );
		}

		@Override
		public void click( final int x, final int y )
		{
			if ( x < headerWidth || y < headerHeight )
				return;

			navigate( x, y );
		}
	}

	/**
	 * Behaviour to select a vertex with a mouse click.
	 * <p>
	 * Note that this only applies to vertices that are individually painted on
	 * the screen. Vertices inside dense ranges are ignored.
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
			if ( x < headerWidth || y < headerHeight )
				return;

			select( x, y, addToSelection );
		}
	}

	/**
	 * Behaviour to select vertices and edges inside a bounding box with a mouse
	 * drag.
	 * <p>
	 * The selection happens in layout space, so it also selects vertices inside
	 * dense ranges. A vertex is inside the bounding box if its layout
	 * coordinate is inside the bounding box.
	 */
	private class BoxSelectionBehaviour extends AbstractNamedBehaviour implements DragBehaviour, OverlayRenderer
	{
		/**
		 * Coordinates where mouse dragging started.
		 */
		private int oX, oY;

		/**
		 * Coordinates where mouse dragging currently is.
		 */
		private int eX, eY;

		private boolean dragging = false;

		private boolean ignore = false;

		private final boolean addToSelection;

		public BoxSelectionBehaviour( final String name, final boolean addToSelection )
		{
			super( name );
			this.addToSelection = addToSelection;
		}

		@Override
		public void init( final int x, final int y )
		{
			oX = x;
			oY = y;
			dragging = false;
			ignore = x < headerWidth || y < headerHeight;
		}

		@Override
		public void drag( final int x, final int y )
		{
			if ( ignore )
				return;

			eX = x;
			eY = y;
			if ( !dragging )
			{
				dragging = true;
				display.addOverlayRenderer( this );
			}
			display.repaint();
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( ignore )
				return;

			if ( dragging )
			{
				dragging = false;
				display.removeOverlayRenderer( this );
				display.repaint();
				selectWithin(
						oX - headerWidth,
						oY - headerHeight,
						eX - headerWidth,
						eY - headerHeight,
						addToSelection );
			}
		}

		/**
		 * Draws the selection box, if there is one.
		 */
		@Override
		public void drawOverlays( final Graphics g )
		{
			g.setColor( Color.RED );
			final int x = Math.min( oX, eX );
			final int y = Math.min( oY, eY );
			final int width = Math.abs( eX - oX );
			final int height = Math.abs( eY - oY );
			g.drawRect( x, y, width, height );
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		synchronized ( screenTransform )
		{
			screenTransform.set( transform );
			centerPos.setPosition( ( transform.getMaxX() + transform.getMinX() ) / 2., 0 );
			centerPos.setPosition( ( transform.getMaxY() + transform.getMinY() ) / 2., 1 );
			ratioXtoY = transform.getXtoYRatio();
		}
	}

	@Override
	public void updateHeadersVisibility( boolean isVisibleX, int width, boolean isVisibleY, int height )
	{
		headerWidth = isVisibleX ? width : 0;
		headerHeight = isVisibleY ? height : 0;
	}
}
