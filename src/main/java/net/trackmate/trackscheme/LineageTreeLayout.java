package net.trackmate.trackscheme;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Layouting of a {@link TrackSchemeGraph} into layout coordinates.
 *
 * <p>
 * This determines the layoutX coordinates for all vertices. (The layoutY
 * coordinates of vertices are given by the timepoint.)
 *
 * <p>
 * See {@link VertexOrder}. for transforming layout coordinates to screen
 * coordinates.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class LineageTreeLayout
{
	/**
	 * Stores where a track ends in X position.
	 */
	final TDoubleArrayList columns = new TDoubleArrayList();

	List< String > columnNames = new ArrayList< String >();

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

	/**
	 * Get ordered list of roots and then call
	 * {@link #layoutX(TrackSchemeVertex)} for every root.
	 */
	public void layoutX()
	{
		reset();
		final TrackSchemeVertexList roots = VertexOrder.getOrderedRoots( graph );

		columns.clear();
		columnNames.clear();
		columns.add( 0 );
		for ( final TrackSchemeVertex root : roots )
		{
			layoutX( root );
			columns.add( rightmost );
			columnNames.add( "Root " + root.getLabel() );
		}
	}

	/**
	 * Recursively lay out vertices such that
	 * <ul>
	 * <li>leafs are assigned layoutX = 0, 1, 2, ...
	 * <li>non-leafs are centered between first and last child's layoutX
	 * <li>for layout of vertices with more then one parent, only first incoming
	 * edge counts as parent edge
	 * </ul>
	 *
	 * @param v
	 *            root of sub-tree to layout.
	 */
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
			final TrackSchemeEdge edge = graph.edgeRef();
			final Iterator< TrackSchemeEdge > iterator = v.outgoingEdges().iterator();
			int numLaidOutChildren = layoutNextChild( iterator, child, edge );
			final double firstChildX = child.getLayoutX();
			if ( iterator.hasNext() )
			{
				while ( iterator.hasNext() )
				{
					numLaidOutChildren += layoutNextChild( iterator, child, edge );
				}
				final double lastChildX = child.getLayoutX();
				if ( numLaidOutChildren > 0 )
				{
					v.setLayoutX( ( firstChildX + lastChildX ) / 2 );
				}
				else
				{
					v.setLayoutX( rightmost );
					rightmost += 1;
				}
			}
			else
				if ( numLaidOutChildren > 0 )
				{
					v.setLayoutX( firstChildX );
				}
				else
				{
					v.setLayoutX( rightmost );
					rightmost += 1;
				}
			graph.releaseRef( edge );
			graph.releaseRef( child );
		}
	}

	private int layoutNextChild( final Iterator< TrackSchemeEdge > iterator, final TrackSchemeVertex child, final TrackSchemeEdge edge )
	{
		final TrackSchemeEdge next = iterator.next();
		next.getTarget( child );
		if ( child.incomingEdges().get( 0, edge ).equals( next ) )
		{
			layoutX( child );
			return 1;
		}
		else
		{
			return 0;
		}
	}
}
