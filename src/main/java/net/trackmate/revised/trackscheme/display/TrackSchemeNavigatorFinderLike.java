/**
 *
 */
package net.trackmate.revised.trackscheme.display;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

import org.scijava.ui.behaviour.KeyStrokeAdder;

import bdv.util.AbstractActions;
import bdv.viewer.InputActionBindings;

/**
 * TrackSchemeNavigator that implements the 'Finder-like' behaviour.
 * <p>
 * Selection is tied to the focus. When moving the focus with arrow keys, the
 * selection moves with the focus. When clicking a vertex it is focused and
 * selected, The focused vertex is always selected. Focus still exists
 * independent of selection: multiple vertices can be selected, but only one of
 * them can have the focus. When extending the selection with shift+arrow keys,
 * the vertex to which the focus moves should be selected. When box selection is
 * drawn, the selected vertex closest to the position where the drag ended
 * should receive the focus.
 *
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class TrackSchemeNavigatorFinderLike extends TrackSchemeNavigator
{

	public TrackSchemeNavigatorFinderLike( InteractiveDisplayCanvasComponent< ScreenTransform > display, TrackSchemeGraph< ?, ? > graph, LineageTreeLayout layout, AbstractTrackSchemeOverlay graphOverlay, TrackSchemeFocus focus, TrackSchemeNavigation navigation, TrackSchemeSelection selection )
	{
		super( display, graph, layout, graphOverlay, focus, navigation, selection );
	}

	@Override
	public void installActionBindings( final InputActionBindings keybindings, final KeyStrokeAdder.Factory keyConfig )
	{
		final AbstractActions actions = new AbstractActions( keybindings, "navigator", keyConfig, new String[] { "ts" } );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.CHILD, true ), NAVIGATE_CHILD, NAVIGATE_CHILD_KEYS );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.PARENT, true ), NAVIGATE_PARENT, NAVIGATE_PARENT_KEYS );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.LEFT_SIBLING, true ), NAVIGATE_LEFT, NAVIGATE_LEFT_KEYS );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.RIGHT_SIBLING, true ), NAVIGATE_RIGHT, NAVIGATE_RIGHT_KEYS );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.CHILD, false ), SELECT_NAVIGATE_CHILD, SELECT_NAVIGATE_CHILD_KEYS );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.PARENT, false ), SELECT_NAVIGATE_PARENT, SELECT_NAVIGATE_PARENT_KEYS );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.LEFT_SIBLING, false ), SELECT_NAVIGATE_LEFT, SELECT_NAVIGATE_LEFT_KEYS );
		actions.runnableAction( ( ) -> selectAndFocusNeighborFL( Direction.RIGHT_SIBLING, false ), SELECT_NAVIGATE_RIGHT, SELECT_NAVIGATE_RIGHT_KEYS );
	}

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
		final TrackSchemeVertex vertex = getFocusedVertex( ref );
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
			navigation.notifyNavigateToVertex( current );
			if ( clearSelection )
				selection.clearSelection();
			selection.setSelected( current, true );
		}

		selection.resumeListeners();
		return current;
	}
}
