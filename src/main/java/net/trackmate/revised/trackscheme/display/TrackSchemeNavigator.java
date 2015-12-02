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

	public void child()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		child( ref );
		graph.releaseRef( ref );
	}

	public TrackSchemeVertex child( final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = getFocusedVertex( ref );
		if ( vertex == null )
			return null;

		final Edges< TrackSchemeEdge > edges = vertex.outgoingEdges();
		final TrackSchemeVertex current = edges.isEmpty()
				? null
				: edges.get( 0 ).getTarget( ref );
		focus.focusVertex( current );
		navigateTo( current );
		return current;
	}

	public void parent()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		parent( ref );
		graph.releaseRef( ref );
	}

	public TrackSchemeVertex parent( final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = getFocusedVertex( ref );
		if ( vertex == null )
			return null;

		final Edges< TrackSchemeEdge > edges = vertex.incomingEdges();
		final TrackSchemeVertex current = edges.isEmpty()
				? null
				: edges.get( 0 ).getSource( ref );
		focus.focusVertex( current );
		navigateTo( current );
		return current;
	}

	public void rightSibling()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		rightSibling( ref );
		graph.releaseRef( ref );
	}

	public TrackSchemeVertex rightSibling( final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = getFocusedVertex( ref );
		if ( vertex == null )
			return null;

		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( vertex.getTimepoint() );
		final int index = vertices.binarySearch( vertex.getLayoutX() );
		final TrackSchemeVertex sibling = ( index < vertices.size() - 1 )
				? vertices.get( index + 1, ref )
				: null;
		focus.focusVertex( sibling );
		navigateTo( sibling );
		return sibling;
	}

	public void leftSibling()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		leftSibling( ref );
		graph.releaseRef( ref );
	}

	public TrackSchemeVertex leftSibling( final TrackSchemeVertex ref )
	{
		final TrackSchemeVertex vertex = getFocusedVertex( ref );
		if ( vertex == null )
			return null;

		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( vertex.getTimepoint() );
		final int index = vertices.binarySearch( vertex.getLayoutX() );
		final TrackSchemeVertex sibling = ( index > 0 )
				? vertices.get( index - 1, ref )
				: null;
		focus.focusVertex( sibling );
		navigateTo( sibling );
		return sibling;
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
