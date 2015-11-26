package net.trackmate.revised.trackscheme.display;

import net.imglib2.RealPoint;
import net.imglib2.ui.TransformListener;
import net.trackmate.graph.Edges;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.TrackSchemeVertexList;

public class TrackSchemeNavigator implements TransformListener< ScreenTransform >
{
	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	private final TrackSchemeNavigation navigation;

	private final TrackSchemeFocus focus;

	public TrackSchemeNavigator(
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final TrackSchemeFocus focus,
			final TrackSchemeNavigation navigation )
	{
		this.graph = graph;
		this.layout = layout;
		this.focus = focus;
		this.navigation = navigation;
	}

	public int child()
	{
		final int id = getVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final Edges< TrackSchemeEdge > edges = ref.outgoingEdges();
		int childId;
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getTarget( ref );
			focus.focusVertex( current.getInternalPoolIndex() );
			navigateTo( current );
			childId = current.getInternalPoolIndex();
		}
		else
		{
			childId = -1;
		}
		graph.releaseRef( ref );
		return childId;
	}

	public int parent()
	{
		final int id = getVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final Edges< TrackSchemeEdge > edges = ref.incomingEdges();
		final int childId;
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getSource( ref );
			focus.focusVertex( current.getInternalPoolIndex() );
			navigateTo( current );
			childId = current.getInternalPoolIndex();
		}
		else
		{
			childId = -1;
		}
		graph.releaseRef( ref );
		return childId;
	}

	public int rightSibling()
	{
		final int id = getVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( ref.getTimepoint() );
		final int index = vertices.binarySearch( ref.getLayoutX() );
		final int siblingId;
		if ( index >= 0 && index < vertices.size()-1 )
		{
			final TrackSchemeVertex sibling = vertices.get( index + 1, ref );
			focus.focusVertex( sibling.getInternalPoolIndex() );
			navigateTo( sibling );
			siblingId = sibling.getInternalPoolIndex();
		}
		else
		{
			siblingId = -1;
		}
		graph.releaseRef( ref );
		return siblingId;
	}

	public int leftSibling()
	{
		final int id = getVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( ref.getTimepoint() );
		final int index = vertices.binarySearch( ref.getLayoutX() );
		int siblingId;
		if ( index > 0 && index < vertices.size() )
		{
			final TrackSchemeVertex sibling = vertices.get( index - 1, ref );
			focus.focusVertex( sibling.getInternalPoolIndex() );
			navigateTo( sibling );
			siblingId = sibling.getInternalPoolIndex();
		}
		else
		{
			siblingId = -1;
		}
		graph.releaseRef( ref );
		return siblingId;
	}

	private void navigateTo( final TrackSchemeVertex current )
	{
		navigation.notifyListeners( current );
	}

	private final RealPoint centerPos = new RealPoint( 2 );

	private double ratioXtoY;

	private int getVertexId()
	{
		final int id = focus.getFocusedVertexId();
		if ( id < 0 ) { return getPanelCenterClosestVertexId(); }
		return id;
	}

	private int getPanelCenterClosestVertexId()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		final TrackSchemeVertex v = layout.getClosestActiveVertex( centerPos, ratioXtoY, ref );
		final int id = v.getInternalPoolIndex();
		graph.releaseRef( ref );
		return id;
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		centerPos.setPosition( (transform.getMaxX() + transform.getMinX() ) / 2., 0 );
		centerPos.setPosition( (transform.getMaxY() + transform.getMinY() ) / 2., 1 );
		ratioXtoY = transform.getXtoYRatio();
	}

}
