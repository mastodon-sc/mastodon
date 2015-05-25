package net.trackmate.trackscheme;

import net.trackmate.graph.Edges;
import net.trackmate.graph.collection.RefSet;

public class SelectionNavigator
{
	private final ShowTrackScheme trackscheme;

	private final SelectionHandler selectionHandler;

	public SelectionNavigator( SelectionHandler selectionHandler, ShowTrackScheme trackscheme )
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

	public void child( boolean clear )
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
		}
	}

	public void parent( boolean clear )
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
		}
	}

}
