package net.trackmate.revised.trackscheme.display;

import gnu.trove.set.TIntSet;
import net.trackmate.graph.Edges;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.TrackSchemeVertexList;

public class SelectionNavigator
{
	private int lastOne = -1;

	private final TrackSchemeSelection selection;

	private final TrackSchemeGraph< ?, ? > graph;

	private final LineageTreeLayout layout;

	public SelectionNavigator( final TrackSchemeGraph< ?, ? > graph, final LineageTreeLayout layout, final TrackSchemeSelection selection )
	{
		this.graph = graph;
		this.layout = layout;
		this.selection = selection;
		takeDefaultVertex();
	}

	private int takeDefaultVertex()
	{
		final TIntSet ids = selection.getSelectedVertexIds();
		if ( ids.size() > 0 )
		{
			if ( lastOne >= 0 && ids.contains( lastOne ) )
			{
				return lastOne;
			}
			else
			{
				return ids.iterator().next();
			}
		}
		else
		{
			return 0;
		}
	}

	public void child( final boolean clear )
	{
		final TrackSchemeVertex v = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( takeDefaultVertex(), v );

		final Edges< TrackSchemeEdge > edges = v.outgoingEdges();
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getTarget();
			if ( clear )
			{
				selection.clearSelection();
			}
			selection.setVertexSelected( current.getInternalPoolIndex(), true );
			lastOne = current.getInternalPoolIndex();
		}
		graph.releaseRef( v );
	}

	public void parent( final boolean clear )
	{
		final TrackSchemeVertex v = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( takeDefaultVertex(), v );

		final Edges< TrackSchemeEdge > edges = v.incomingEdges();
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getSource();
			if ( clear )
			{
				selection.clearSelection();
			}
			selection.setVertexSelected( current.getInternalPoolIndex(), true );
			lastOne = current.getInternalPoolIndex();
		}
		graph.releaseRef( v );
	}

	public void rightSibling( final boolean clear )
	{
		final TrackSchemeVertex v = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( takeDefaultVertex(), v );
		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( v.getTimepoint() );
		final int index = vertices.binarySearch( v.getLayoutX() );
		if ( index >= 0 && index < vertices.size()-1 )
		{
			final TrackSchemeVertex sibling = vertices.get( index + 1 );
			if ( clear )
			{
				selection.clearSelection();
			}
			selection.setVertexSelected( sibling.getInternalPoolIndex(), true );
			lastOne = sibling.getInternalPoolIndex();
		}
	}

	public void leftSibling( final boolean clear )
	{
		final TrackSchemeVertex v = graph.vertexRef();
		graph.getVertexPool().getByInternalPoolIndex( takeDefaultVertex(), v );
		final TrackSchemeVertexList vertices = layout.getTimepointToOrderedVertices().get( v.getTimepoint() );
		final int index = vertices.binarySearch( v.getLayoutX() );
		if ( index > 0 && index < vertices.size() )
		{
			final TrackSchemeVertex sibling = vertices.get( index - 1 );
			if ( clear )
			{
				selection.clearSelection();
			}
			selection.setVertexSelected( sibling.getInternalPoolIndex(), true );
			lastOne = sibling.getInternalPoolIndex();
		}
	}
}
