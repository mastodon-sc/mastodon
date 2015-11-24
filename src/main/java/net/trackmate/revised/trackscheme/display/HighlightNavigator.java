package net.trackmate.revised.trackscheme.display;

import net.imglib2.RealPoint;
import net.imglib2.ui.TransformListener;
import net.trackmate.graph.Edges;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.TrackSchemeVertexList;
import net.trackmate.revised.ui.selection.NavigationGroupEmitter;
import net.trackmate.revised.ui.selection.NavigationHandler;

public class HighlightNavigator implements TransformListener< ScreenTransform >
{
	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	private final TrackSchemeHighlight highlight;

	private final NavigationHandler navigationHandler;

	private final NavigationGroupEmitter navigationGroup;

	public HighlightNavigator(
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final TrackSchemeHighlight highlight,
			final NavigationGroupEmitter navigationGroup,
			final NavigationHandler navigationHandler )
	{
		this.graph = graph;
		this.layout = layout;
		this.highlight = highlight;
		this.navigationGroup = navigationGroup;
		this.navigationHandler = navigationHandler;
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
			highlight.highlightVertex( current.getInternalPoolIndex() );
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
			highlight.highlightVertex( current.getInternalPoolIndex() );
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
			highlight.highlightVertex( sibling.getInternalPoolIndex() );
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
			highlight.highlightVertex( sibling.getInternalPoolIndex() );
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
		navigationHandler.notifyListeners( navigationGroup.getGroups(), current.getModelVertexId() );
	}

	private final RealPoint centerPos = new RealPoint( 2 );

	private double ratioXtoY;

	private int getVertexId()
	{
		final int id = highlight.getHighlightedVertexId();
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
