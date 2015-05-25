package net.trackmate.trackscheme;

import net.trackmate.graph.Edges;
import net.trackmate.graph.collection.RefSet;

public class SelectionNavigator
{
	private final ShowTrackScheme trackscheme;

	private final SelectionHandler selectionHandler;

	private SelectionListener selectionListener;

	public SelectionNavigator( final SelectionHandler selectionHandler, final ShowTrackScheme trackscheme )
	{
		this.selectionHandler = selectionHandler;
		this.trackscheme = trackscheme;
		takeDefaultVertex();
	}

	private TrackSchemeVertex takeDefaultVertex()
	{
		final RefSet< TrackSchemeVertex > vertices = selectionHandler.getSelectionModel().getSelectedVertices();
		if ( vertices.size() > 0 )
		{
			return vertices.iterator().next();
		}
		else
		{
			return trackscheme.graph.vertexIterator().next();
		}
	}

	public void child( final boolean clear )
	{
		final Edges< TrackSchemeEdge > edges = takeDefaultVertex().outgoingEdges();
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getTarget();
			if ( clear )
			{
				selectionHandler.clearSelection();
			}
			selectionHandler.select( current, false );
			trackscheme.centerOn( current );
		}
	}

	public void parent( final boolean clear )
	{
		final Edges< TrackSchemeEdge > edges = takeDefaultVertex().incomingEdges();
		if ( edges.size() > 0 )
		{
			final TrackSchemeVertex current = edges.get( 0 ).getSource();
			if ( clear )
			{
				selectionHandler.clearSelection();
			}
			selectionHandler.select( current, false );
			trackscheme.centerOn( current );
		}
	}

	public void rightSibbling( final boolean clear )
	{
		final TrackSchemeVertex current = takeDefaultVertex();
		final TrackSchemeVertexList vertices = trackscheme.order.timepointToOrderedVertices.get( current.getTimePoint() );
		final int index = vertices.binarySearch( current.getLayoutX() );
		if ( index >= 0 && index < vertices.size()-1 )
		{
			final TrackSchemeVertex sibbling = vertices.get( index + 1 );
			if ( clear )
			{
				selectionHandler.clearSelection();
			}
			selectionHandler.select( sibbling, false );
			trackscheme.centerOn( sibbling );
		}
	}

	public void leftSibbling( final boolean clear )
	{
		final TrackSchemeVertex current = takeDefaultVertex();
		final TrackSchemeVertexList vertices = trackscheme.order.timepointToOrderedVertices.get( current.getTimePoint() );
		final int index = vertices.binarySearch( current.getLayoutX() );
		if ( index > 0 && index < vertices.size() )
		{
			final TrackSchemeVertex sibbling = vertices.get( index - 1 );
			if ( clear )
			{
				selectionHandler.clearSelection();
			}
			selectionHandler.select( sibbling, false );
			trackscheme.centerOn( sibbling );
		}
	}

	public void setSelectionListener( final SelectionListener selectionListener )
	{
		this.selectionListener = selectionListener;
	}

}
