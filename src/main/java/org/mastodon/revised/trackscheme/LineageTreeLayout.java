package org.mastodon.revised.trackscheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edges;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.trackscheme.ScreenEdge.ScreenEdgePool;
import org.mastodon.revised.trackscheme.ScreenVertex.ScreenVertexPool;
import org.mastodon.revised.trackscheme.ScreenVertexRange.ScreenVertexRangePool;
import org.mastodon.util.Listeners;

import gnu.trove.iterator.TIntAlternatingIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectArrayMap;
import gnu.trove.map.TIntObjectMap;
import net.imglib2.RealLocalizable;

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
 * <li>for layout of vertices with more than one parent, only first incoming
 * edge counts as parent edge
 * <li>vertices marked with a timestamp &lt; the current
 * {@link #mark} are marked as ghosts.
 * <li>additionally, vertices marked with a timestamp &lt; the current
 * {@link #mark}<em>-1</em> are treated as leafs.
 * </ul>
 *
 * We call vertices contained in the current layout <em>active</em>.
 *
 *
 *
 * TODO TODO TODO
 *
 *
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class LineageTreeLayout
{
	public interface LayoutListener
	{

		/**
		 * Notifies after the layout has been done.
		 *
		 * @param layout
		 *            the layout.
		 */
		public void layoutChanged( LineageTreeLayout layout );
	}

	private final TrackSchemeGraph< ?, ? > graph;

	private final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection;

	private final Listeners.List< LayoutListener > listeners;

	/**
	 * X coordinate that will be assigned to the next leaf in the current layout.
	 */
	private double rightmost;

	/**
	 * The timestamp used in the current layout. This is incremented at the
	 * beginning of {@link #layout(Collection, int)}. It is also incremented
	 * when {@link #nextLayoutTimestamp()} is called.
	 */
	private int timestamp;

	/**
	 * The mark timestamp for the current layout. Context trackscheme marks
	 * vertices that should be laid out by setting their
	 * {@link TrackSchemeVertex#getLayoutTimestamp() layout timestamp} to a mark
	 * value that is higher than any previously assigned timestamp. During
	 * layout, vertices marked with a timestamp &lt; {@link #mark}) are marked
	 * as ghosts. Additionally, vertices marked with a timestamp &lt;
	 * {@link #mark}<em>-1</em> are treated as leafs.
	 */
	private int mark;

	/**
	 *  ordered list of all existing timpoints.
	 */
	private final TIntArrayList timepoints;

	/**
	 * Maps timepoint to {@link TrackSchemeVertexList} that contains all
	 * layouted vertices of that timepoint ordered by ascending layout X
	 * coordinate.
	 * <p>
	 * This is built during TODO TODO TODO
	 */
	private final TIntObjectMap< TrackSchemeVertexList > timepointToOrderedVertices;

	/**
	 * the minimum layoutX coordinate assigned to any vertex in the current
	 * layout.
	 */
	private double currentLayoutMinX;

	/**
	 * the maximum layoutX coordinate assigned to any vertex in the current
	 * layout.
	 */
	private double currentLayoutMaxX;

	/**
	 * The width below which a column line will not generate a screen entity.
	 */
	private static final double MIN_COLUMN_WIDTH = 30;

	/**
	 * The column layout X coordinates.
	 */
	private final TDoubleList currentLayoutColumnX;

	/**
	 * The list of roots for each column.
	 */
	private final RefList< TrackSchemeVertex > currentLayoutColumnRoot;

	public LineageTreeLayout(
			final TrackSchemeGraph< ?, ? > graph,
			final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection )
	{
		this.graph = graph;
		this.selection = selection;
		listeners = new Listeners.SynchronizedList<>();
		rightmost = 0;
		timestamp = 0;
		timepoints = new TIntArrayList();
		timepointToOrderedVertices = new TIntObjectArrayMap< >();
		currentLayoutColumnX = new TDoubleArrayList();
		currentLayoutColumnRoot = RefCollections.createRefList( graph.vertices() );
		stack = new ArrayList<>();
		stack.add( new StackFrame( null ) );
	}

	/**
	 * Layout graph in trackscheme coordinates starting from the graphs roots.
	 * <p>
	 * This calls {@link #layout(Collection, int)} with parameter {@code mark = -1},
	 * that is, no vertices will me marked as ghosts.
	 */
	public void layout()
	{
		layout( LexicographicalVertexOrder.sort( graph, graph.getRoots() ), -1 );
	}

	/**
	 * Layout graph in trackscheme coordinates starting from specified roots.
	 * <p>
	 * This calls {@link #layout(Collection, int)} with parameter {@code mark = -1},
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
	 * {@code mark} is used to check for vertices to be laid out. When the
	 * context trackscheme determines the set of vertices that should be visible
	 * in the layout, it sets their layout timestamp to a value higher than that
	 * used in any previous layout (see {@link #nextLayoutTimestamp()}). During
	 * layout, it is checked whether a vertex's
	 * {@link TrackSchemeVertex#getLayoutTimestamp() timestamp} &lt;
	 * {@code mark}. In this case, the vertex is marked as a ghost. If
	 * additionally the vertex's {@link TrackSchemeVertex#getLayoutTimestamp()
	 * timestamp} &lt; {@code mark-1}, it is treated as a leaf node in the
	 * layout.
	 *
	 * @param layoutRoots
	 *            root vertices from which to start layout.
	 * @param mark
	 *            timestamp value that was used to mark vertices to be laid out.
	 *            (Ghost vertices were marked with {@code mark-1}.)
	 */
	public void layout( final Collection< TrackSchemeVertex > layoutRoots, final int mark )
	{
		++timestamp;
		rightmost = 0;
		timepoints.clear();
		timepointToOrderedVertices.clear();
		currentLayoutColumnX.clear();
		currentLayoutColumnRoot.clear();
		final TrackSchemeVertex previousGraphRoot = graph.vertexRef();
		final TrackSchemeVertex currentGraphRoot = graph.vertexRef();
		this.mark = mark;
		boolean first = true;
		currentLayoutColumnX.add( rightmost );
		for ( final TrackSchemeVertex root : layoutRoots )
		{
			layoutX_iterative( root );
			getGraphRoot( root, currentGraphRoot );
			if ( first || !currentGraphRoot.equals( previousGraphRoot ) )
			{
				currentLayoutColumnRoot.add( currentGraphRoot );
				currentLayoutColumnX.add( rightmost );
				first = false;
				previousGraphRoot.refTo( currentGraphRoot );
			}
		}
		currentLayoutMinX = 0;
		currentLayoutMaxX = rightmost - 1;
		graph.releaseRef( previousGraphRoot );
		graph.releaseRef( currentGraphRoot );
		notifyListeners();
	}

	/**
	 * Get the minimum layoutX coordinate assigned to any vertex in the current
	 * layout (last call of one of the {@code layout(...)} methods).
	 *
	 * @return the minimum layoutX coordinate assigned to any vertex in the
	 *         current layout
	 */
	public double getCurrentLayoutMinX()
	{
		return currentLayoutMinX;
	}

	/**
	 * Get the maximum layoutX coordinate assigned to any vertex in the current
	 * layout (last call of one of the {@code layout(...)} methods).
	 *
	 * @return the maximum layoutX coordinate assigned to any vertex in the
	 *         current layout
	 */
	public double getCurrentLayoutMaxX()
	{
		return currentLayoutMaxX;
	}

	/**
	 * Get the timestamp that was used in the last layout (the timestamp which
	 * was set in all vertices laid out during last {@link #layout(Collection)} resp.
	 * {@link #layout(Collection, int)}.)
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
	 * vertices to be laid out.
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
	 * @param decorationsOffsetX
	 *            the screen entities are shifted in X by this amount.
	 * @param decorationsOffsetY
	 *            the screen entities are shifted in Y by this amount.
	 */
	public void cropAndScale(
			final ScreenTransform transform,
			final ScreenEntities screenEntities,
			final int decorationsOffsetX,
			final int decorationsOffsetY )
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
				final double y = ( timepoint - minY ) * yScale + decorationsOffsetY;
				// screen y of vertices of (timepoint-1)
				final double prevY = ( timepoint - 1 - minY ) * yScale + decorationsOffsetY;
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
						final String label = v1.getLabel();
						final double x = ( v1.getLayoutX() - minX ) * xScale + decorationsOffsetX;
						final boolean selected = selection.isSelected( v1 );
						final boolean ghost = v1.isGhost();
						screenVertexPool.create( sv ).init( id, label, x, y, selected, ghost );
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
								final boolean eselected = selection.isSelected( edge );
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
						final double svMinX = ( vertexList.get( rangeMinIndex, v1 ).getLayoutX() - minX ) * xScale + decorationsOffsetX;
						final double svMaxX = ( vertexList.get( rangeMaxIndex, v1 ).getLayoutX() - minX ) * xScale + decorationsOffsetX; // TODO: make minimum width (maybe only when painting...)
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

		/*
		 * Columns
		 */

		final List< ScreenColumn > screenColumns = screenEntities.getColumns();
		int minC = currentLayoutColumnX.binarySearch( minX );
		if ( minC < 0 )
		{
			minC = -1 - minC;
		}
		minC = Math.max( 0, minC - 1 ); // at least 1 column out

		int maxC = currentLayoutColumnX.binarySearch( maxX + 0.5, minC, currentLayoutColumnX.size() );
		if ( maxC < 0 )
		{
			maxC = -1 - maxC;
		}
		maxC = Math.min( currentLayoutColumnX.size(), maxC + 1 );

		// Build screen columns.
		final double scaledMinWidth = MIN_COLUMN_WIDTH / xScale;
		for ( int ic = minC + 1; ic < maxC; ic++ )
		{
			final double cLeft = currentLayoutColumnX.get( ic - 1 );
			final double cRight = currentLayoutColumnX.get( ic );
			if ( cRight - cLeft < scaledMinWidth )
				continue;

			final int xRight = ( int ) ( ( cRight - minX - 0.5 ) * xScale + decorationsOffsetX );
			final int xLeft = ( int ) ( ( cLeft - minX - 0.5 ) * xScale + decorationsOffsetX );
			final int columnWidth = xRight - xLeft;

			final TrackSchemeVertex root = currentLayoutColumnRoot.get( ic - 1 );
			final ScreenColumn column = new ScreenColumn( root.getLabel(), xLeft, columnWidth );
			screenColumns.add( column );
		}
	}

	/**
	 * Get the active vertex with the minimal distance to the specified layout
	 * coordinates. The distance is computed as the Euclidean distance in layout
	 * space distorted by the specified aspect ratio.
	 *
	 * @param layoutPos
	 *            layout coordinates.
	 * @param aspectRatioXtoY
	 *            The <em>X/Y</em> ratio of screen vector <em>(1,1)</em>
	 *            transformed into layout coordinates <em>(X,Y)</em>.
	 * @param ref
	 *            ref to store the result.
	 * @return the closest active vertex to the specified coordinates, or
	 *         {@code null} if there are no active vertices.
	 */
	public TrackSchemeVertex getClosestActiveVertex( final RealLocalizable layoutPos, final double aspectRatioXtoY, final TrackSchemeVertex ref )
	{
		final double lx = layoutPos.getDoublePosition( 0 );
		final double ly = layoutPos.getDoublePosition( 1 );

		double closestVertexSquareDist = Double.POSITIVE_INFINITY;
		int closestVertexIndex = -1;

		final TIntIterator tpIter = new TIntAlternatingIterator( timepoints, ( int ) ly );
		while( tpIter.hasNext() )
		{
			final int tp = tpIter.next();
			final double diffy = ( ly - tp ) * aspectRatioXtoY;
			if ( diffy * diffy < closestVertexSquareDist )
			{
				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( tp );
				final int left = vertexList.binarySearch( lx );
				final int begin = Math.max( 0, left );
				final int end = Math.min( begin + 2, vertexList.size() );
				for ( int x = begin; x < end; ++x )
				{
					vertexList.get( x, ref );
					final double diffx = ( lx - ref.getLayoutX() );
					final double d2 = diffx * diffx + diffy * diffy;
					if ( d2 < closestVertexSquareDist )
					{
						closestVertexSquareDist = d2;
						closestVertexIndex = ref.getInternalPoolIndex();
					}
				}
			}
		}

		if ( closestVertexIndex < 0 )
			return null;

		graph.getVertexPool().getObject( closestVertexIndex, ref );
		return ref;
	}

	/**
	 * Returns the set of all active vertices in the rectangle with two corners
	 * {@code (lx1, ly1)} and {@code (lx2, ly2)} in layout
	 * coordinates.
	 *
	 * @param lx1
	 *            the x coordinate of the first corner.
	 * @param ly1
	 *            the y coordinate of the first corner.
	 * @param lx2
	 *            the x coordinate of the second corner.
	 * @param ly2
	 *            the y coordinate of the second corner.
	 * @return a new set.
	 */
	public RefSet< TrackSchemeVertex > getActiveVerticesWithin( final double lx1, final double ly1, final double lx2, final double ly2 )
	{
		final int tStart = ( int ) Math.ceil( Math.min( ly1, ly2 ) );
		final int tEnd = ( int ) Math.floor( Math.max( ly1, ly2 ) ) + 1;
		final double x1 = Math.min( lx1, lx2 );
		final double x2 = Math.max( lx1, lx2 );

		final RefSet< TrackSchemeVertex > vertexSet = RefCollections.createRefSet( graph.vertices() );
		int start = timepoints.binarySearch( tStart );
		if ( start < 0 )
			start = -start - 1;
		int end = timepoints.binarySearch( tEnd );
		if ( end < 0 )
			end = -end - 1;
		for ( int tpIndex = start; tpIndex < end; ++tpIndex )
		{
			final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoints.get( tpIndex ) );
			final int left = vertexList.binarySearch( x1 ) + 1;
			final int right = vertexList.binarySearch( x2, left, vertexList.size() );
			vertexSet.addAll( vertexList.subList( left, right + 1 ) );
		}
		return vertexSet;
	}

	/**
	 * Get the first active child of {@code vertex}.
	 *
	 * @param vertex
	 * 			  query vertex.
	 * @param ref
	 *            ref to store the result.
	 * @return the first active child of {@code vertex}, or
	 *         {@code null} if {@code vertex} has no active children.
	 */
	public TrackSchemeVertex getFirstActiveChild( final TrackSchemeVertex vertex, final TrackSchemeVertex ref )
	{
		final Edges< TrackSchemeEdge > edges = vertex.outgoingEdges();
		for ( final TrackSchemeEdge edge : edges )
		{
			final TrackSchemeVertex child = edge.getTarget( ref );
			final boolean active = child.getLayoutTimestamp() == timestamp;
			if ( active )
				return child;
		}
		return null;
	}

	/**
	 * Get the first active parent of {@code vertex}.
	 *
	 * @param vertex
	 * 			  query vertex.
	 * @param ref
	 *            ref to store the result.
	 * @return the first active parent of {@code vertex}, or
	 *         {@code null} if {@code vertex} has no active parents.
	 */
	public TrackSchemeVertex getFirstActiveParent( final TrackSchemeVertex vertex, final TrackSchemeVertex ref )
	{
		final Edges< TrackSchemeEdge > edges = vertex.incomingEdges();
		for ( final TrackSchemeEdge edge : edges )
		{
			final TrackSchemeVertex parent = edge.getSource( ref );
			final boolean active = parent.getLayoutTimestamp() == timestamp;
			if ( active )
				return parent;
		}
		return null;
	}

	/**
	 * Get the active vertex laid out left of {@code vertex}.
	 *
	 * @param vertex
	 * 			  query vertex.
	 * @param ref
	 *            ref to store the result.
	 * @return the active vertex laid out left of {@code vertex}, or
	 *         {@code null} if {@code vertex} is the left-most active vertex.
	 */
	public TrackSchemeVertex getLeftSibling( final TrackSchemeVertex vertex, final TrackSchemeVertex ref )
	{
		final TrackSchemeVertexList vertices = timepointToOrderedVertices.get( vertex.getTimepoint() );
		final int index = vertices.binarySearch( vertex.getLayoutX() );
		return ( index > 0 )
				? vertices.get( index - 1, ref )
				: null;
	}

	/**
	 * Get the active vertex laid out right of {@code vertex}.
	 *
	 * @param vertex
	 * 			  query vertex.
	 * @param ref
	 *            ref to store the result.
	 * @return the active vertex laid out right of {@code vertex}, or
	 *         {@code null} if {@code vertex} is the right-most active vertex.
	 */
	public TrackSchemeVertex getRightSibling( final TrackSchemeVertex vertex, final TrackSchemeVertex ref )
	{
		final TrackSchemeVertexList vertices = timepointToOrderedVertices.get( vertex.getTimepoint() );
		final int index = vertices.binarySearch( vertex.getLayoutX() );
		return ( index < vertices.size() - 1 )
				? vertices.get( index + 1, ref )
				: null;
	}

	// TODO: REMOVE?
	public TIntObjectMap< TrackSchemeVertexList > getTimepointToOrderedVertices()
	{
		return timepointToOrderedVertices;
	}

	// TODO: REMOVE?
	public TIntArrayList getTimepoints()
	{
		return timepoints;
	}

	public Listeners< LayoutListener > layoutListeners()
	{
		return listeners;
	}

	private class StackFrame
	{
		int numLaidOutChildren;
		double firstChildX;
		double lastChildX;
		TrackSchemeVertex current;
		TrackSchemeVertex child;
		Iterator< TrackSchemeEdge > edges;
		boolean done;

		StackFrame( final TrackSchemeVertex current )
		{
			numLaidOutChildren = 0;
			firstChildX = 0;
			lastChildX = 0;
			this.current = current;
			child = graph.vertexRef();
			edges = null;
			done = true;
		}
	}

	private final ArrayList< StackFrame > stack;

	private void growStackIfNecessary( final int depth )
	{
		if ( stack.size() == depth )
			stack.add( new StackFrame( stack.get( depth - 1 ).child ) );
	}

	/**
	 * Non-recursive version of {@link #layoutX(TrackSchemeVertex)}. Faster,
	 * throws no {@link StackOverflowError}, but impenetrable... Look at the
	 * recursive version to understand what's going on.
	 *
	 * <p>
	 * Recursively lay out vertices such that
	 * <ul>
	 * <li>leafs are assigned layoutX = 0, 1, 2, ...
	 * <li>non-leafs are centered between first and last child's layoutX
	 * <li>for layout of vertices with more than one parent, only first incoming
	 * edge counts as parent edge
	 * <li>vertices marked with a timestamp &lt; the current {@link #mark} are
	 * marked as ghosts.
	 * <li>additionally, vertices marked with a timestamp &lt; the current
	 * {@link #mark}<em>-1</em> are treated as leafs.
	 * </ul>
	 *
	 * @param root
	 *            root of sub-tree to layout.
	 */
	private void layoutX_iterative( final TrackSchemeVertex root )
	{
		stack.get( 0 ).current = root;
		int depth = 0;

A:		while ( true )
		{
			final StackFrame f = stack.get( depth );
			final TrackSchemeVertex v = f.current;

			// init first time
			if ( f.done )
			{
				f.numLaidOutChildren = 0;
				final boolean ghost = v.getLayoutTimestamp() < mark;
				final boolean terminate = v.getLayoutTimestamp() < mark - 1;
				v.setGhost( ghost );
				v.setLayoutTimestamp( timestamp );

				if ( !terminate )
				{
					f.edges = v.outgoingEdges().iterator();
					f.done = false;
				}
			}

			// iteratively go to children
			if ( !f.done )
			{
				while ( f.edges.hasNext() )
				{
					final TrackSchemeEdge edge = f.edges.next();
					edge.getTarget( f.child );
					if ( f.child.getLayoutTimestamp() < timestamp )
					{
						// unused currently...
//						f.child.setLayoutInEdgeIndex( edge.getInternalPoolIndex() );
						growStackIfNecessary( ++depth );
						continue A;
					}
				}
			}

			// done. assign layoutX and backtrack
			f.done = true;
			double layoutX;
			switch ( f.numLaidOutChildren )
			{
			case 0:
				layoutX = rightmost;
				rightmost += 1;
				break;
			case 1:
				layoutX = f.firstChildX;
				break;
			default:
				layoutX = ( f.firstChildX + f.lastChildX ) / 2;
			}
			v.setLayoutX( layoutX );

			appendToOrderedVertices( v );

			if ( depth == 0 )
			{
				return;
			}
			else
			{
				// set stuff in parent
				final StackFrame p = stack.get( --depth );
				if ( ++p.numLaidOutChildren == 1 )
					p.firstChildX = layoutX;
				else
					p.lastChildX = layoutX;
			}
		}
	}

	/**
	 * Recursively lay out vertices such that
	 * <ul>
	 * <li>leafs are assigned layoutX = 0, 1, 2, ...
	 * <li>non-leafs are centered between first and last child's layoutX
	 * <li>for layout of vertices with more than one parent, only first incoming
	 * edge counts as parent edge
	 * <li>vertices marked with a timestamp &lt; the current {@link #mark} are
	 * marked as ghosts.
	 * <li>additionally, vertices marked with a timestamp &lt; the current
	 * {@link #mark}<em>-1</em> are treated as leafs.
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
		final boolean terminate = v.getLayoutTimestamp() < mark - 1;
		v.setGhost( ghost );
		v.setLayoutTimestamp( timestamp );

		if ( !terminate )
		{
			final TrackSchemeVertex child = graph.vertexRef();
			for ( final TrackSchemeEdge edge : v.outgoingEdges() )
			{
				edge.getTarget( child );
				if ( child.getLayoutTimestamp() < timestamp )
				{
					// unused currently...
//					child.setLayoutInEdgeIndex( edge.getInternalPoolIndex() );
					layoutX( child );
					if ( ++numLaidOutChildren == 1 )
						firstChildX = child.getLayoutX();
					else
						lastChildX = child.getLayoutX();
				}
			}
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

	/**
	 * Layout roots are not graph roots necessarily. This helper finds the
	 * <em>graph</em> root of a given vertex.
	 */
	private void getGraphRoot( final TrackSchemeVertex v, final TrackSchemeVertex graphRoot )
	{
		graphRoot.refTo( v );
		while ( ! graphRoot.incomingEdges().isEmpty() )
			graphRoot.incomingEdges().iterator().next().getSource( graphRoot );
	}

	private void notifyListeners()
	{
		for ( final LayoutListener l : listeners.list )
			l.layoutChanged( this );
	}
}
