package net.trackmate.revised.trackscheme.display;

import net.imglib2.RealPoint;
import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class TrackSchemeNavigator implements TransformListener< ScreenTransform >
{
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

	public static enum Direction
	{
		CHILD,
		PARENT,
		LEFT_SIBLING,
		RIGHT_SIBLING
	}

	public void focusNeighbor( final Direction direction )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		selectAndFocusNeighborImpl( direction, false, ref );
		graph.releaseRef( ref );
	}

//	public TrackSchemeVertex focusNeighbor( final Direction direction, final TrackSchemeVertex ref )
//	{
//		return selectAndFocusNeighborImpl( direction, false, ref );
//	}

	public void selectAndFocusNeighbor( final Direction direction )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		selectAndFocusNeighborImpl( direction, true, ref );
		graph.releaseRef( ref );
	}

//	public TrackSchemeVertex selectAndFocusNeighbor( final Direction direction, final TrackSchemeVertex ref )
//	{
//		return selectAndFocusNeighborImpl( direction, true, ref );
//	}

	private TrackSchemeVertex selectAndFocusNeighborImpl( final Direction direction, final boolean select, final TrackSchemeVertex ref )
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
			navigateTo( current );
		}
		return current;
	}

	private void navigateTo( final TrackSchemeVertex current )
	{
		if ( current != null )
			navigation.notifyNavigateToVertex( current );
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
