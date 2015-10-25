package net.trackmate.revised.trackscheme;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;
import java.util.List;

/**
 * Layouting of a {@link TrackSchemeGraph} into layout coordinates.
 *
 * <p>
 * The layout Y coordinates of a vertex is given by its timepoint. The layout X
 * coordinates of a vertex are determined as follows: Starting from a list of
 * roots, recursively descend to leaf nodes and assign X coordinates such that
 * <ul>
 * <li>leafs are assigned layoutX = 0, 1, 2, ...
 * <li>non-leafs are centered between first and last child's layoutX
 * <li>for layout of vertices with more then one parent, only first incoming
 * edge counts as parent edge
 * <li>in-active vertices (marked with a timestamp &lt; the current
 * {@link #mark}) are marked as ghosts and treated as leafs.
 * </ul>
 *
 *
 *
 *
 * TODO TODO TODO
 *
 *
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class LineageTreeLayout
{
	private final TrackSchemeGraph< ?, ? > graph;

	/**
	 * X coordinate that will be assigned to the next leaf in the current layout.
	 */
	private double rightmost;

	/**
	 * The timestamp used in the current layout. This is incremted at the
	 * beginning of {@link #layoutX(List, int)}. It is also incremented when
	 * {@link #nextLayoutTimestamp()} is called.
	 */
	private int timestamp;

	/**
	 * The mark timestamp for the current layout. Context trackscheme to marks
	 * vertices as active before layout by setting their
	 * {@link TrackSchemeVertex#getLayoutTimestamp() layout timestamp} to a mark
	 * value that is higher than any previously assigned timestamp. During
	 * layout, in-active vertices (marked with a timestamp &lt; the current
	 * {@link #mark}) are marked as ghosts and treated as leafs.
	 */
	private int mark;

	/**
	 *  ordered list of all existing timpoints.
	 */
	private final TIntArrayList timepoints;


	// TODO: replace by TIntObjectArrayMap (assuming timepoints are more or less 0 based indices)
	/**
	 * Maps timepoint to {@link TrackSchemeVertexList} that contains all
	 * layouted vertices of that timepoint ordered by ascending layout X
	 * coordinate.
	 * <p>
	 * This is built during TODO TODO TODO
	 */
	private final TIntObjectMap< TrackSchemeVertexList > timepointToOrderedVertices;

	public LineageTreeLayout( final TrackSchemeGraph< ?, ? > graph )
	{
		this.graph = graph;
		rightmost = 0;
		timestamp = 0;
		timepoints = new TIntArrayList();
		timepointToOrderedVertices = new TIntObjectHashMap< TrackSchemeVertexList >();
	}

	/**
	 * Layout graph in trackscheme coordinates starting from specified roots.
	 * <p>
	 * This calls {@link #layoutX(List, int)} with parameter {@code mark = -1},
	 * that is, no vertices will me marked as ghosts.
	 *
	 * @param layoutRoots
	 *            root vertices from which to start layout.
	 */
	public void layoutX( final List< TrackSchemeVertex > layoutRoots )
	{
		layoutX( layoutRoots, -1 );
	}

	// TODO: add javadoc ref to context trackscheme class
	/**
	 * Layout graph in trackscheme coordinates starting from specified roots.
	 * <p>
	 * {@code mark} is used to check for active vertices. When the context
	 * trackscheme determines the set of vertices that should be visible in the
	 * layout, it sets their layout timestamp to a value higher than that used
	 * in any previous layout (see{@link #nextLayoutTimestamp()}). During
	 * layout, it is checked whether a vertex's
	 * {@link TrackSchemeVertex#getLayoutTimestamp() timestamp} is &ge;
	 * {@code mark}. Otherwise the vertex is marked as a ghost and treated as a
	 * leaf node in the layout.
	 *
	 * @param layoutRoots
	 *            root vertices from which to start layout.
	 * @param mark
	 *            timestamp value that was used to mark active vertices.
	 */
	public void layoutX( final List< TrackSchemeVertex > layoutRoots, final int mark )
	{
		++timestamp;
		rightmost = 0;
		timepoints.clear();
		timepointToOrderedVertices.clear();
		this.mark = mark;
		for ( final TrackSchemeVertex root : layoutRoots )
		{
			layoutX( root );
		}
	}

	/**
	 * Get the timestamp that was used in the last layout (the timestamp which
	 * was set in all vertices laid out during last {@link #layoutX(List)} resp.
	 * {@link #layoutX(List, int)}.)
	 *
	 * @return timestamp used in last layout.
	 */
	public int getCurrentLayoutTimestamp()
	{
		return timestamp;
	}

	// TODO: add javadoc ref to context trackscheme class
	/**
	 * Get a new layout timestamp for external use. The next layout will then
	 * use the timestamp after that. This is used by context trackscheme to mark
	 * active vertices.
	 *
	 * @return the timestamp which would have been used for the next layout.
	 */
	public int nextLayoutTimestamp()
	{
		++timestamp;
		return timestamp;
	}

	/**
	 * Recursively lay out vertices such that
	 * <ul>
	 * <li>leafs are assigned layoutX = 0, 1, 2, ...
	 * <li>non-leafs are centered between first and last child's layoutX
	 * <li>for layout of vertices with more then one parent, only first incoming
	 * edge counts as parent edge
	 * <li>in-active vertices (marked with a timestamp &lt; the current
	 * {@link #mark}) are marked as ghosts and treated as leafs.
	 * </ul>
	 *
	 * @param v
	 *            root of sub-tree to layout.
	 */
	private void layoutX( final TrackSchemeVertex v )
	{
		int numLaidOutChildren = 0;
		double firstChildX = 0;
		double lastChildX = 0;

		final boolean ghost = v.getLayoutTimestamp() < mark;
		v.setGhost( ghost );
		v.setLayoutTimestamp( timestamp );

		if ( !v.outgoingEdges().isEmpty() && !ghost )
		{
			final TrackSchemeVertex child = graph.vertexRef();
			final TrackSchemeEdge edge = graph.edgeRef();
			final Iterator< TrackSchemeEdge > iterator = v.outgoingEdges().iterator();
			while ( layoutNextChild( iterator, child, edge ) )
			{
				if ( ++numLaidOutChildren == 1 )
					firstChildX = child.getLayoutX();
				else
					lastChildX = child.getLayoutX();
			}
			graph.releaseRef( edge );
			graph.releaseRef( child );
		}

		switch( numLaidOutChildren )
		{
		case 0:
			v.setLayoutX( rightmost );
			rightmost += 1;
			break;
		case 1:
			v.setLayoutX( firstChildX );
			break;
		default:
			v.setLayoutX( ( firstChildX + lastChildX ) / 2 );
		}

		appendToOrderedVertices( v );
	}

	private boolean layoutNextChild( final Iterator< TrackSchemeEdge > iterator, final TrackSchemeVertex child, final TrackSchemeEdge edge )
	{
		while ( iterator.hasNext() )
		{
			final TrackSchemeEdge next = iterator.next();
			next.getTarget( child );
			if ( child.getLayoutTimestamp() < timestamp )
			{
				child.setLayoutInEdgeIndex( next.getInternalPoolIndex() );
				layoutX( child );
				return true;
			}
		}
		return false;
	}

	private void appendToOrderedVertices( final TrackSchemeVertex v )
	{
		final int tp = v.getTimepoint();
		TrackSchemeVertexList vlist = timepointToOrderedVertices.get( tp );
		if ( vlist == null )
		{
			vlist = new TrackSchemeVertexList( graph );
			timepointToOrderedVertices.put( tp, vlist );
			timepoints.insert( -( 1 + timepoints.binarySearch( tp ) ), tp );
		}
		vlist.add( v );
	}
}
