package net.trackmate.trackscheme;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;

public class LineageTreeLayout< V extends TrackSchemeVertexI< V, E >, E extends Edge< V > >
{
	private double rightmost;

	private final Graph< V, E > graph;

	public LineageTreeLayout( final Graph< V, E > graph )
	{
		this.graph = graph;
		rightmost = 0;
	}

	public void reset()
	{
		rightmost = 0;
	}

	public void layoutX( final V v )
	{
		if ( v.outgoingEdges().isEmpty() )
		{
			v.setLayoutX( rightmost );
			rightmost += 1;
		}
		else
		{
			final V child = graph.vertexRef();
			final Iterator< E > iterator = v.outgoingEdges().iterator();
			layoutX( iterator.next().getTarget( child ) );
			final double firstChildX = child.getLayoutX();
			if ( iterator.hasNext() )
			{
				while ( iterator.hasNext() )
					layoutX( iterator.next().getTarget( child ) );
				final double lastChildX = child.getLayoutX();
				v.setLayoutX( ( firstChildX + lastChildX ) / 2 );
			}
			else
				v.setLayoutX( firstChildX );
			graph.releaseRef( child );
		}
	}
}
