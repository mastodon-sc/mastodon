package net.trackmate.revised.trackscheme;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.trackscheme.ScreenEdge.ScreenEdgePool;
import net.trackmate.revised.trackscheme.ScreenVertex.ScreenVertexPool;
import net.trackmate.revised.trackscheme.ScreenVertexRange.ScreenVertexRangePool;

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
	 * The timestamp used in the current layout. This is incremented at the
	 * beginning of {@link #layout(List, int)}. It is also incremented when
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
	 * Layout graph in trackscheme coordinates starting from the graphs roots.
	 * <p>
	 * This calls {@link #layout(List, int)} with parameter {@code mark = -1},
	 * that is, no vertices will me marked as ghosts.
	 */
	public void layout()
	{
		layout( graph.getRoots(), -1 );
	}

	/**
	 * Layout graph in trackscheme coordinates starting from specified roots.
	 * <p>
	 * This calls {@link #layout(List, int)} with parameter {@code mark = -1},
	 * that is, no vertices will me marked as ghosts.
	 *
	 * @param layoutRoots
	 *            root vertices from which to start layout.
	 */
	public void layout( final Collection< TrackSchemeVertex > layoutRoots )
	{
		layout( layoutRoots, -1 );
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
	public void layout( final Collection< TrackSchemeVertex > layoutRoots, final int mark )
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
	 * was set in all vertices laid out during last {@link #layout(List)} resp.
	 * {@link #layout(List, int)}.)
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
	 * Crop a region of the current layout, transform it to screen coordinates,
	 * and create {@link ScreenEntities} for display.
	 *
	 * @param transform
	 *            specifies the transformation from layout to screen coordinates
	 *            and the crop region.
	 * @param screenEntities
	 *            the transformed screen entities (vertices, edges, ranges) are
	 *            stored here.
	 */
	public void cropAndScale(
			final ScreenTransform transform,
			final ScreenEntities screenEntities )
	{
		final double minX = transform.getMinX();
		final double maxX = transform.getMaxX();
		final double minY = transform.getMinY();
		final double maxY = transform.getMaxY();
		final double xScale = transform.getScaleX();
		final double yScale = transform.getScaleY();
		screenEntities.screenTransform().set( transform );

		final RefList< ScreenVertex > screenVertices = screenEntities.getVertices();
		final RefList< ScreenEdge > screenEdges = screenEntities.getEdges();
		final RefList< ScreenVertexRange > vertexRanges = screenEntities.getRanges();
		final ScreenVertexPool screenVertexPool = screenEntities.getVertexPool();
		final ScreenEdgePool screenEdgePool = screenEntities.getEdgePool();
		final ScreenVertexRangePool screenRangePool = screenEntities.getRangePool();

		final TrackSchemeVertex v1 = graph.vertexRef();
		final TrackSchemeVertex v2 = graph.vertexRef();
		final ScreenVertex sv = screenVertexPool.createRef();
		final ScreenEdge se = screenEdgePool.createRef();
		final ScreenVertexRange sr = screenRangePool.createRef();

		final double allowedMinD = 2.0 / xScale;

		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			if ( timepoint + 1 >= minY && timepoint - 1 <= maxY )
			{
				final int timepointStartScreenVertexIndex = screenVertices.size();
				// screen y of vertices of timepoint
				final double y = ( timepoint - minY ) * yScale;
				// screen y of vertices of (timepoint-1)
				final double prevY = ( timepoint - 1 - minY ) * yScale;
				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
				// largest index of vertex with layoutX <= minX
				int minIndex = vertexList.binarySearch( minX );
				// include vertex before that (may be appears partially on
				// screen, and may be needed to paint edge to vertex in other
				// timepoint)
				minIndex--;
				if ( minIndex < 0 )
					minIndex = 0;
				// largest index of vertex with layoutX <= maxX
				int maxIndex = vertexList.binarySearch( maxX, minIndex, vertexList.size() );
				// include vertex after that (may be appears partially on
				// screen, and may be needed to paint edge to vertex in other
				// timepoint)
				if ( maxIndex < vertexList.size() - 1 )
					maxIndex++;

				final double minLayoutX = vertexList.getMinLayoutXDistance();
				TIntArrayList denseRanges = vertexList.getDenseRanges( minIndex, maxIndex + 1, minLayoutX, allowedMinD, 3, v1 );
				if ( denseRanges == null )
					denseRanges = new TIntArrayList();
				denseRanges.add( maxIndex + 1 );

				final TIntIterator riter = denseRanges.iterator();
				int nextRangeStart = riter.next();

				double prevX = Double.NEGATIVE_INFINITY;
				double minVertexScreenDist = yScale;
				for ( int i = minIndex; i <= maxIndex; ++i )
				{
					if ( i < nextRangeStart )
					{
						vertexList.get( i, v1 );
						final int v1si = screenVertices.size();
						v1.setScreenVertexIndex( v1si );
						final int id = v1.getInternalPoolIndex();
						final double x = ( v1.getLayoutX() - minX ) * xScale;
						final boolean selected = v1.isSelected();
						final boolean ghost = v1.isGhost();
						screenVertexPool.create( sv ).init( id, x, y, selected, ghost );
						screenVertices.add( sv );

						minVertexScreenDist = Math.min( minVertexScreenDist, x - prevX );
						prevX = x;

						for ( final TrackSchemeEdge edge : v1.incomingEdges() )
						{
							edge.getSource( v2 );
							final int v2si = v2.getScreenVertexIndex();
							// TODO: additionally to checking for id ref consistency, the following should be decided by layout timestamp
							if ( v2si >= 0 && v2si < screenVertices.size() && screenVertices.get( v2si, sv ).getTrackSchemeVertexId() == v2.getInternalPoolIndex() )
							{
								final int eid = edge.getInternalPoolIndex();
								final int sourceScreenVertexIndex = v2si;
								final int targetScreenVertexIndex = v1si;
								final boolean eselected = edge.isSelected();
								screenEdgePool.create( se ).init( eid, sourceScreenVertexIndex, targetScreenVertexIndex, eselected );
								screenEdges.add( se );
								final int sei = se.getInternalPoolIndex();
								edge.setScreenEdgeIndex( sei );
							}
						}
					}
					else
					{
						final int rangeMinIndex = nextRangeStart;
						final int rangeMaxIndex = riter.next();
						nextRangeStart = riter.next();
						i = rangeMaxIndex;
						final double svMinX = ( vertexList.get( rangeMinIndex, v1 ).getLayoutX() - minX ) * xScale;
						final double svMaxX = ( vertexList.get( rangeMaxIndex, v1 ).getLayoutX() - minX ) * xScale; // TODO: make minimum width (maybe only when painting...)
						vertexRanges.add( screenRangePool.create( sr ).init( svMinX, svMaxX, prevY, y ) );
						minVertexScreenDist = 0;
					}
				}
				for ( int i = timepointStartScreenVertexIndex; i < screenVertices.size(); ++i )
				{
					screenVertices.get( i, sv ).setVertexDist( minVertexScreenDist );
				}
			}
		}

		screenEdgePool.releaseRef( se );
		screenVertexPool.releaseRef( sv );
		graph.releaseRef( v1 );
		graph.releaseRef( v2 );
	}

	/**
	 * Get the active vertex with the minimal distance to the specified layout
	 * coordinates. The distance is computed as the Euclidean distance in layout
	 * space distorted by the specified aspect ratio.
	 *
	 * @param layoutPos
	 *            layout coordinates.
	 * @param aspectRatio
	 *            The <em>X/Y</em> ratio of screen vector <em>(1,1)</em>
	 *            transformed into layout coordinates <em>(X,Y)</em>.
	 * @param v
	 *            ref to store the result.
	 * @return the closest active vertex to the specified coordinates, or
	 *         {@code null} if there are no active vertices.
	 */
	public TrackSchemeVertex getClosestActiveVertex( final RealLocalizable layoutPos, final double ratioXtoY, final TrackSchemeVertex v )
	{
		final double lx = layoutPos.getDoublePosition( 0 );
		final double ly = layoutPos.getDoublePosition( 1 );

		double closestVertexSquareDist = Double.POSITIVE_INFINITY;
		int closestVertexIndex = -1;

// TODO: intead of only forward iteration through timepoints, should pick a good starting tp and then search forwards and backwards until diffy * diffy < closestVertexSquareDist.
		final TIntIterator tpIter = timepoints.iterator();
		while( tpIter.hasNext() )
		{
			final int tp = tpIter.next();
			final double diffy = ( ly - tp ) * ratioXtoY;
			if ( diffy * diffy < closestVertexSquareDist )
			{
				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( tp );
				final int left = vertexList.binarySearch( lx );
				final int begin = Math.min( 0, left );
				final int end = Math.min( begin + 2, vertexList.size() );
				for ( int x = begin; x < end; ++x )
				{
					vertexList.get( x, v );
					final double diffx = ( lx - v.getLayoutX() );
					final double d2 = diffx * diffx + diffy * diffy;
					if ( d2 < closestVertexSquareDist )
					{
						closestVertexSquareDist = d2;
						closestVertexIndex = v.getInternalPoolIndex();
					}
				}
			}
		}

		if ( closestVertexIndex < 0 )
			return null;

		graph.getVertexPool().getByInternalPoolIndex( closestVertexIndex, v );
		return v;
	}

// TODO remove?
//	TIntArrayList getTimepoints()
//	{
//		return timepoints;
//	}
//
//	TrackSchemeVertexList getOrderedVertices( final int timepoint )
//	{
//		return timepointToOrderedVertices.get( timepoint );
//	}

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
