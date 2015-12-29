package net.trackmate.revised.trackscheme.display;

import java.awt.event.ActionEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import bdv.behaviour.KeyStrokeAdder;
import bdv.util.AbstractNamedAction;
import bdv.util.AbstractNamedAction.NamedActionAdder;
import bdv.viewer.InputActionBindings;
import net.imglib2.RealPoint;
import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

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
public class TrackSchemeNavigator implements TransformListener< ScreenTransform >
{
	public static final String NAVIGATE_CHILD_NAME = "ts navigate to child";
	public static final String NAVIGATE_PARENT_NAME = "ts navigate to parent";
	public static final String NAVIGATE_LEFT_NAME = "ts navigate left";
	public static final String NAVIGATE_RIGHT_NAME = "ts navigate right";
	public static final String SELECT_NAVIGATE_CHILD_NAME = "ts select navigate to child";
	public static final String SELECT_NAVIGATE_PARENT_NAME = "ts select navigate to parent";
	public static final String SELECT_NAVIGATE_LEFT_NAME = "ts select navigate left";
	public static final String SELECT_NAVIGATE_RIGHT_NAME = "ts select navigate right";
	public static final String TOGGLE_FOCUS_SELECTION_NAME = "ts toggle focus selection";

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

	private final ActionMap actionMap;

	public TrackSchemeNavigator(
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final TrackSchemeFocus focus,
			final TrackSchemeNavigation navigation,
			final TrackSchemeSelection selection )
	{
		this.graph = graph;
		this.layout = layout;
		this.focus = focus;
		this.navigation = navigation;
		this.selection = selection;

		actionMap = new ActionMap();
		final NamedActionAdder adder = new NamedActionAdder( actionMap );
		adder.put( new SelectAndFocusNeighborAction( NAVIGATE_CHILD_NAME, Direction.CHILD, false ) );
		adder.put( new SelectAndFocusNeighborAction( NAVIGATE_PARENT_NAME, Direction.PARENT, false ) );
		adder.put( new SelectAndFocusNeighborAction( NAVIGATE_LEFT_NAME, Direction.LEFT_SIBLING, false ) );
		adder.put( new SelectAndFocusNeighborAction( NAVIGATE_RIGHT_NAME, Direction.RIGHT_SIBLING, false ) );
		adder.put( new SelectAndFocusNeighborAction( SELECT_NAVIGATE_CHILD_NAME, Direction.CHILD, true ) );
		adder.put( new SelectAndFocusNeighborAction( SELECT_NAVIGATE_PARENT_NAME, Direction.PARENT, true ) );
		adder.put( new SelectAndFocusNeighborAction( SELECT_NAVIGATE_LEFT_NAME, Direction.LEFT_SIBLING, true ) );
		adder.put( new SelectAndFocusNeighborAction( SELECT_NAVIGATE_RIGHT_NAME, Direction.RIGHT_SIBLING, true ) );
		adder.put( new ToggleFocusSelectionAction() );
	}

	public void installActionBindings( final InputActionBindings keybindings, final KeyStrokeAdder.Factory keyConfig )
	{
		final InputMap inputMap = new InputMap();
		final KeyStrokeAdder adder = keyConfig.keyStrokeAdder( inputMap, "ts" );

		adder.put( NAVIGATE_CHILD_NAME, "DOWN" );
		adder.put( NAVIGATE_PARENT_NAME, "UP" );
		adder.put( NAVIGATE_LEFT_NAME, "LEFT" );
		adder.put( NAVIGATE_RIGHT_NAME, "RIGHT" );
		adder.put( SELECT_NAVIGATE_CHILD_NAME, "shift DOWN" );
		adder.put( SELECT_NAVIGATE_PARENT_NAME, "shift UP" );
		adder.put( SELECT_NAVIGATE_LEFT_NAME, "shift LEFT" );
		adder.put( SELECT_NAVIGATE_RIGHT_NAME, "shift RIGHT" );
		adder.put( TOGGLE_FOCUS_SELECTION_NAME, "SPACE" );

		keybindings.addActionMap( "navigator", actionMap );
		keybindings.addInputMap( "navigator", inputMap );
	}

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
			focus.focusVertex( current );
			navigation.notifyNavigateToVertex( current );
		}
		return current;
	}

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

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		centerPos.setPosition( (transform.getMaxX() + transform.getMinX() ) / 2., 0 );
		centerPos.setPosition( (transform.getMaxY() + transform.getMinY() ) / 2., 1 );
		ratioXtoY = transform.getXtoYRatio();
	}

	/*
	 * ACTIONS
	 */

	/**
	 * Action to toggle the selected state of the currently focused vertex.
	 */
	private class ToggleFocusSelectionAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 1L;

		public ToggleFocusSelectionAction()
		{
			super( TOGGLE_FOCUS_SELECTION_NAME );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			toggleSelectionOfFocusedVertex();
		}
	}

	/**
	 * Action to focus a neighbor (parent, child, left sibling, right sibling)
	 * of the currently focused vertex. Possibly, the currently focused vertex
	 * is added to the selection.
	 */
	private class SelectAndFocusNeighborAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 1L;

		private final Direction direction;

		private final boolean select;

		/**
		 * @param name
		 *            the name of this action (used as {@link ActionMap} key).
		 * @param direction
		 *            which neighbor to focus.
		 * @param select
		 *            if {@code true}, the currently focussed vertex is added to
		 *            the selection (before moving the focus).
		 */
		public SelectAndFocusNeighborAction( final String name, final Direction direction, final boolean select )
		{
			super( name );
			this.direction = direction;
			this.select = select;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			selectAndFocusNeighbor( direction, select );
		}
	}
}
