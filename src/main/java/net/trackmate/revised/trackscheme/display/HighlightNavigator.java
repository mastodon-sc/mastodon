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

public class HighlightNavigator implements TransformListener< ScreenTransform >
{
	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	private final TrackSchemeHighlight highlight;

	public HighlightNavigator( final TrackSchemeGraph< ?, ? > graph, final LineageTreeLayout layout, final TrackSchemeHighlight highlight )
	{
		this.graph = graph;
		this.layout = layout;
		this.highlight = highlight;
	}

	public void child()
	{
		final int id = highlight.getHighlightedVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final Edges< TrackSchemeEdge > edges = ref.outgoingEdges();
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getTarget( ref );
			highlight.highlightVertex( current.getInternalPoolIndex() );
			navigateTo( current );
		}
		graph.releaseRef( ref );
	}

	public void parent()
	{
		final int id = highlight.getHighlightedVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final Edges< TrackSchemeEdge > edges = ref.incomingEdges();
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getSource( ref );
			highlight.highlightVertex( current.getInternalPoolIndex() );
			navigateTo( current );
		}
		graph.releaseRef( ref );
	}

	public void rightSibling()
	{
		final int id = highlight.getHighlightedVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( ref.getTimepoint() );
		final int index = vertices.binarySearch( ref.getLayoutX() );
		if ( index >= 0 && index < vertices.size()-1 )
		{
			final TrackSchemeVertex sibling = vertices.get( index + 1, ref );
			highlight.highlightVertex( sibling.getInternalPoolIndex() );
			navigateTo( sibling );
		}
		graph.releaseRef( ref );
	}

	public void leftSibling()
	{
		final int id = highlight.getHighlightedVertexId();
		final TrackSchemeVertex ref = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( ref.getTimepoint() );
		final int index = vertices.binarySearch( ref.getLayoutX() );
		if ( index > 0 && index < vertices.size() )
		{
			final TrackSchemeVertex sibling = vertices.get( index - 1, ref );
			highlight.highlightVertex( sibling.getInternalPoolIndex() );
			navigateTo( sibling );
		}
		graph.releaseRef( ref );
	}

	private void navigateTo( final TrackSchemeVertex current )
	{
		System.out.println( "Navigate to " + current );// DEBUG
	}

	private final RealPoint centerPos = new RealPoint( 2 );

	private double ratioXtoY;

	private int getPanelCenterClosestVertex()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		final TrackSchemeVertex v = layout.getClosestVertex( centerPos, ratioXtoY, ref );
		final int id = v.getInternalPoolIndex();
		graph.releaseRef( ref );
		return id;
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		centerPos.setPosition( (transform.getMaxX() + transform.getMinX() ) / 2., 0 );
		centerPos.setPosition( (transform.getMaxY() + transform.getMinY() ) / 2., 1 );
//		ratioXtoY = transform.get
	}

}
