package net.trackmate.revised.trackscheme.display;

import static net.trackmate.revised.trackscheme.display.TrackSchemeNavigator.Direction.CHILD;
import static net.trackmate.revised.trackscheme.display.TrackSchemeNavigator.Direction.LEFT_SIBLING;
import static net.trackmate.revised.trackscheme.display.TrackSchemeNavigator.Direction.PARENT;
import static net.trackmate.revised.trackscheme.display.TrackSchemeNavigator.Direction.RIGHT_SIBLING;

import java.awt.event.ActionEvent;

import bdv.util.AbstractNamedAction;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class TrackSchemeActionBank
{

	/*
	 * SELECT CURRENT FOCUS
	 */

	// TODO: rename getToggleSelectionOfFocusedAction
	public static final AbstractNamedAction getToggleSelectionOfHighlightAction(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeFocus focus,
			final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "toggleSelectionOfFocus" )
		{
			private static final long serialVersionUID = 3749694022046537514L;


			@Override
			public void actionPerformed( final ActionEvent event )
			{
				final TrackSchemeVertex ref = graph.vertexRef();
				final TrackSchemeVertex v = focus.getFocusedVertex( ref );
				if ( v != null )
					selection.toggleSelected( v );
				graph.releaseRef( ref );
			}
		};
	}

	/*
	 * NAVIGATE WITH HIGHLIGHT.
	 */

	public static final AbstractNamedAction getNavigateToChildAction( final TrackSchemeNavigator navigator )
	{
		return new AbstractNamedAction( "navigateToChild" )
		{
			private static final long serialVersionUID = 2501943103873265924L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.focusNeighbor( CHILD );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToParentAction( final TrackSchemeNavigator navigator )
	{
		return new AbstractNamedAction( "navigateToParent" )
		{
			private static final long serialVersionUID = -2192196689286789410L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.focusNeighbor( PARENT );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToRightSiblingAction( final TrackSchemeNavigator navigator )
	{
		return new AbstractNamedAction( "navigateToRightSibling" )
		{
			private static final long serialVersionUID = -357160008620454907L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.focusNeighbor( RIGHT_SIBLING );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToLeftSiblingAction( final TrackSchemeNavigator navigator )
	{
		return new AbstractNamedAction( "navigateToLeftSibling" )
		{
			private static final long serialVersionUID = -8829626037712844926L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.focusNeighbor( LEFT_SIBLING );
			}
		};
	}

	public static final AbstractNamedAction getAddChildToSelectionAction(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeNavigator navigator,
			final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addChildToSelection" )
		{
			private static final long serialVersionUID = 3028016888295534595L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.selectAndFocusNeighbor( CHILD );
			}
		};
	}

	public static final AbstractNamedAction getAddParentToSelectionAction(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeNavigator navigator,
			final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addParentToSelection" )
		{
			private static final long serialVersionUID = -454383452845176573L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.selectAndFocusNeighbor( PARENT );
			}
		};
	}

	public static final AbstractNamedAction getAddRightSiblingToSelectionAction(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeNavigator navigator,
			final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addRightSiblingToSelection" )
		{
			private static final long serialVersionUID = 6379431524819061875L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.selectAndFocusNeighbor( RIGHT_SIBLING );
			}
		};
	}

	public static final AbstractNamedAction getAddLeftSiblingToSelectionAction(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeNavigator navigator,
			final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addLeftSiblingToSelection" )
		{
			private static final long serialVersionUID = -6558360734713816879L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				navigator.selectAndFocusNeighbor( LEFT_SIBLING );
			}
		};
	}

	private TrackSchemeActionBank()
	{}
}
