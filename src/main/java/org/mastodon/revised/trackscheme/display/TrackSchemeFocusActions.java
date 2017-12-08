package org.mastodon.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;
import net.imglib2.RealPoint;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.trackscheme.LineageTreeLayout;
import org.mastodon.revised.trackscheme.ScreenTransform;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

/**
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 */
public class TrackSchemeFocusActions
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

	private static final String[] NAVIGATE_CHILD_KEYS = new String[] { "DOWN" };
	private static final String[] NAVIGATE_PARENT_KEYS = new String[] { "UP" };
	private static final String[] NAVIGATE_LEFT_KEYS = new String[] { "LEFT" };
	private static final String[] NAVIGATE_RIGHT_KEYS = new String[] { "RIGHT" };
	private static final String[] SELECT_NAVIGATE_CHILD_KEYS = new String[] { "shift DOWN" };
	private static final String[] SELECT_NAVIGATE_PARENT_KEYS = new String[] { "shift UP" };
	private static final String[] SELECT_NAVIGATE_LEFT_KEYS = new String[] { "shift LEFT" };
	private static final String[] SELECT_NAVIGATE_RIGHT_KEYS = new String[] { "shift RIGHT" };
	private static final String[] TOGGLE_FOCUS_SELECTION_KEYS = new String[] { "SPACE" };

	private static enum Direction
	{
		CHILD,
		PARENT,
		LEFT_SIBLING,
		RIGHT_SIBLING
	}

	public static enum NavigatorEtiquette
	{
		/**
		 * SelectionModel is tied to the focus. When moving the focus with arrow
		 * keys, the selection moves with the focus. When clicking a vertex it
		 * is focused and selected, The focused vertex is always selected. Focus
		 * still exists independent of selection: multiple vertices can be
		 * selected, but only one of them can have the focus. When extending the
		 * selection with shift+arrow keys, the vertex to which the focus moves
		 * should be selected. When box selection is drawn, the selected vertex
		 * closest to the position where the drag ended should receive the
		 * focus.
		 */
		FINDER_LIKE,
		/**
		 * SelectionModel is independent of focus. Moving the focus with arrow keys
		 * doesn't alter selection. Space key toggles selection of focused
		 * vertex. When extending the selection with shift+arrow keys, the
		 * selection of the currently focused vertex is toggled, then the focus
		 * is moved.
		 */
		MIDNIGHT_COMMANDER_LIKE;
	}

	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	private final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection;

	private final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus;

	public TrackSchemeFocusActions(
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
			final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection )
	{
		this.graph = graph;
		this.layout = layout;
		this.focus = focus;
		this.selection = selection;
	}

	public static < V extends Vertex< E >, E extends Edge< V > > void install(
			final Actions actions,
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
			final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection,
			final NavigatorEtiquette etiquette ) // TODO
	{
		final TrackSchemeFocusActions tsfa = new TrackSchemeFocusActions( graph, layout, focus, selection );
		switch ( etiquette )
		{
		case MIDNIGHT_COMMANDER_LIKE:
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.CHILD, false ), NAVIGATE_CHILD, NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.PARENT, false ), NAVIGATE_PARENT, NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.LEFT_SIBLING, false ), NAVIGATE_LEFT, NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.RIGHT_SIBLING, false ), NAVIGATE_RIGHT, NAVIGATE_RIGHT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.CHILD, true ), SELECT_NAVIGATE_CHILD, SELECT_NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.PARENT, true ), SELECT_NAVIGATE_PARENT, SELECT_NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.LEFT_SIBLING, true ), SELECT_NAVIGATE_LEFT, SELECT_NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighbor( Direction.RIGHT_SIBLING, true ), SELECT_NAVIGATE_RIGHT, SELECT_NAVIGATE_RIGHT_KEYS );
			actions.runnableAction( () -> tsfa.toggleSelectionOfFocusedVertex(), TOGGLE_FOCUS_SELECTION, TOGGLE_FOCUS_SELECTION_KEYS );
			break;
		case FINDER_LIKE:
		default:
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.CHILD, true ), NAVIGATE_CHILD, NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.PARENT, true ), NAVIGATE_PARENT, NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.LEFT_SIBLING, true ), NAVIGATE_LEFT, NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.RIGHT_SIBLING, true ), NAVIGATE_RIGHT, NAVIGATE_RIGHT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.CHILD, false ), SELECT_NAVIGATE_CHILD, SELECT_NAVIGATE_CHILD_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.PARENT, false ), SELECT_NAVIGATE_PARENT, SELECT_NAVIGATE_PARENT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.LEFT_SIBLING, false ), SELECT_NAVIGATE_LEFT, SELECT_NAVIGATE_LEFT_KEYS );
			actions.runnableAction( () -> tsfa.selectAndFocusNeighborFL( Direction.RIGHT_SIBLING, false ), SELECT_NAVIGATE_RIGHT, SELECT_NAVIGATE_RIGHT_KEYS );
			break;
		}
	}

	/*
	 * COMMANDER-LIKE ETIQUETTE METHODS.
	 */

	/**
	 * Focus a neighbor (parent, child, left sibling, right sibling) of the
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
		final TrackSchemeVertex ref = graph.vertexRef();
		selectAndFocusNeighbor( direction, select, ref );
		graph.releaseRef( ref );
	}

	private TrackSchemeVertex selectAndFocusNeighbor( final Direction direction, final boolean select, final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = focus.getFocusedVertex( ref );
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
			focus.focusVertex( current );

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
			selection.toggle( v );
		graph.releaseRef( ref );
	}

	/*
	 * FINDER-LIKE ETIQUETTE METHODS.
	 */

	/**
	 * Focus and select a neighbor (parent, child, left sibling, right sibling)
	 * of the currently focused vertex. The selection can be cleared before
	 * moving focus.
	 *
	 * @param direction
	 *            which neighbor to focus.
	 * @param clearSelection
	 *            if {@code true}, the selection is cleared before moving the
	 *            focus.
	 */
	private void selectAndFocusNeighborFL( final Direction direction, final boolean clearSelection )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		selectAndFocusNeighborFL( direction, clearSelection, ref );
		graph.releaseRef( ref );
	}

	private TrackSchemeVertex selectAndFocusNeighborFL( final Direction direction, final boolean clearSelection, final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex =focus.getFocusedVertex( ref );
		if ( vertex == null )
			return null;

		selection.pauseListeners();

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
		case RIGHT_SIBLING:
		default:
			current = layout.getRightSibling( vertex, ref );
			break;
		}

		if ( current != null )
		{
			focus.focusVertex( current );
			if ( clearSelection )
				selection.clearSelection();
			selection.setSelected( current, true );
		}

		selection.resumeListeners();
		return current;
	}

	/**
	 * A {@code FocusModel} for TrackScheme that
	 * <ul>
	 *     <li>on {@code getFocusedVertex()} automatically focuses a vertex near the center of the window if none is focused.</li>
	 *     <li>on {@code focusVertex()} calls {@code notifyNavigateToVertex()}.</li>
	 * </ul>
	 */
	public static class TrackSchemeAutoFocus implements FocusModel< TrackSchemeVertex, TrackSchemeEdge >, TransformListener< ScreenTransform >
	{
		private final LineageTreeLayout layout;

		private final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus;

		private final NavigationHandler< TrackSchemeVertex, TrackSchemeEdge > navigation;

		private final ScreenTransform screenTransform = new ScreenTransform();

		private final RealPoint centerPos = new RealPoint( 2 );

		private double ratioXtoY = 1;

		public TrackSchemeAutoFocus(
				final LineageTreeLayout layout,
				final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
				final NavigationHandler< TrackSchemeVertex, TrackSchemeEdge > navigation )
		{
			this.layout = layout;
			this.focus = focus;
			this.navigation = navigation;
		}

		@Override
		public void focusVertex( final TrackSchemeVertex vertex )
		{
			navigation.notifyNavigateToVertex( vertex );
		}

		@Override
		public TrackSchemeVertex getFocusedVertex( final TrackSchemeVertex ref )
		{
			TrackSchemeVertex vertex = focus.getFocusedVertex( ref );
			if ( vertex != null )
				return vertex;

			vertex = layout.getClosestActiveVertex( centerPos, ratioXtoY, ref );
			if ( vertex != null )
				focus.focusVertex( vertex );

			return vertex;
		}

		@Override
		public Listeners< FocusListener > listeners()
		{
			return focus.listeners();
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
	}
}
