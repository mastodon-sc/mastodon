package net.trackmate.revised.trackscheme.display;

import org.scijava.ui.behaviour.KeyStrokeAdder;

import bdv.util.AbstractActions;
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
	public static final String NAVIGATE_CHILD = "ts navigate to child";
	public static final String NAVIGATE_PARENT = "ts navigate to parent";
	public static final String NAVIGATE_LEFT = "ts navigate left";
	public static final String NAVIGATE_RIGHT = "ts navigate right";
	public static final String SELECT_NAVIGATE_CHILD = "ts select navigate to child";
	public static final String SELECT_NAVIGATE_PARENT = "ts select navigate to parent";
	public static final String SELECT_NAVIGATE_LEFT = "ts select navigate left";
	public static final String SELECT_NAVIGATE_RIGHT = "ts select navigate right";
	public static final String TOGGLE_FOCUS_SELECTION = "ts toggle focus selection";

	public static final String[] NAVIGATE_CHILD_KEYS = new String[] { "DOWN" };
	public static final String[] NAVIGATE_PARENT_KEYS = new String[] { "UP" };
	public static final String[] NAVIGATE_LEFT_KEYS = new String[] { "LEFT" };
	public static final String[] NAVIGATE_RIGHT_KEYS = new String[] { "RIGHT" };
	public static final String[] SELECT_NAVIGATE_CHILD_KEYS = new String[] { "shift DOWN" };
	public static final String[] SELECT_NAVIGATE_PARENT_KEYS = new String[] { "shift UP" };
	public static final String[] SELECT_NAVIGATE_LEFT_KEYS = new String[] { "shift LEFT" };
	public static final String[] SELECT_NAVIGATE_RIGHT_KEYS = new String[] { "shift RIGHT" };
	public static final String[] TOGGLE_FOCUS_SELECTION_KEYS = new String[] { "SPACE" };

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

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		centerPos.setPosition( (transform.getMaxX() + transform.getMinX() ) / 2., 0 );
		centerPos.setPosition( (transform.getMaxY() + transform.getMinY() ) / 2., 1 );
		ratioXtoY = transform.getXtoYRatio();
	}
}
