package net.trackmate.trackscheme;

import java.util.Iterator;

public class LineageTreeLayout
{
	private double rightmost;

	private final TrackSchemeGraph graph;

	public LineageTreeLayout( final TrackSchemeGraph graph )
	{
		this.graph = graph;
		rightmost = 0;
	}

	public void reset()
	{
		rightmost = 0;
	}

	public void layoutX()
	{
		reset();
		final TrackSchemeVertexList roots = VertexOrder.getRoots( graph );
		roots.getIndexCollection().sort();
		// TODO sort roots by something meaningful...

		for ( final TrackSchemeVertex root : roots )
			layoutX( root );
	}

	public void layoutX( final TrackSchemeVertex v )
	{
		if ( v.outgoingEdges().isEmpty() )
		{
			v.setLayoutX( rightmost );
			rightmost += 1;
		}
		else
		{
			final TrackSchemeVertex child = graph.vertexRef();
			final Iterator< TrackSchemeEdge > iterator = v.outgoingEdges().iterator();
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
