/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.trackscheme;

import java.util.Collection;
import java.util.List;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edges;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.views.trackscheme.ScreenEdge.ScreenEdgePool;
import org.mastodon.views.trackscheme.ScreenVertex.ScreenVertexPool;
import org.mastodon.views.trackscheme.ScreenVertexRange.ScreenVertexRangePool;
import org.scijava.listeners.Listeners;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
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
 * <li>vertices marked with a timestamp &lt; the current {@code #mark} are
 * marked as ghosts.
 * <li>additionally, vertices marked with a timestamp &lt; the current
 * {@code #mark}<em>-1</em> are treated as leafs.
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
public class LineageTreeLayoutImp implements LineageTreeLayout
{

	protected final TrackSchemeGraph< ?, ? > graph;

	protected final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection;

	private final Listeners.List< LayoutListener > listeners;

	/**
	 * X coordinate that will be assigned to the next leaf in the current layout.
	 */
	protected double rightmost;

	/**
	 * The timestamp used in the current layout. This is incremented at the
	 * beginning of {@link #layout(Collection, int)}. It is also incremented
	 * when {@link #nextLayoutTimestamp()} is called.
	 */
	protected int timestamp;

	/**
	 * The mark timestamp for the current layout. Context trackscheme marks
	 * vertices that should be laid out by setting their
	 * {@link TrackSchemeVertex#getLayoutTimestamp() layout timestamp} to a mark
	 * value that is higher than any previously assigned timestamp. During
	 * layout, vertices marked with a timestamp &lt; {@link #mark}) are marked
	 * as ghosts. Additionally, vertices marked with a timestamp &lt;
	 * {@link #mark}<em>-1</em> are treated as leafs.
	 */
	protected int mark;

	protected final TrackSchemeVertexTable vertexTable;
	/**
	 * the minimum layoutX coordinate assigned to any vertex in the current
	 * layout.
	 */
	protected double currentLayoutMinX;

	/**
	 * the maximum layoutX coordinate assigned to any vertex in the current
	 * layout.
	 */
	protected double currentLayoutMaxX;

	/**
	 * The width below which a column line will not generate a screen entity.
	 */
	protected static final double MIN_COLUMN_WIDTH = 30;

	/**
	 * The column layout X coordinates.
	 */
	protected final TDoubleList currentLayoutColumnX;

	/**
	 * The list of roots for each column.
	 */
	protected final RefList< TrackSchemeVertex > currentLayoutColumnRoot;

	public LineageTreeLayoutImp(
			final TrackSchemeGraph< ?, ? > graph,
			final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection )
	{
		this.graph = graph;
		this.selection = selection;
		listeners = new Listeners.SynchronizedList<>();
		rightmost = 0;
		timestamp = 0;
		vertexTable = new TrackSchemeVertexTable( graph );
		currentLayoutColumnX = new TDoubleArrayList();
		currentLayoutColumnRoot = RefCollections.createRefList( graph.vertices() );
	}

	/**
	 * Layout graph in trackscheme coordinates starting from the graphs roots.
	 * <p>
	 * This calls {@link #layout(Collection, int)} with parameter {@code mark = -1},
	 * that is, no vertices will me marked as ghosts.
	 */
	@Override
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
	@Override
	public void layout( final Collection<TrackSchemeVertex> layoutRoots )
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
	@Override
	public void layout( final Collection<TrackSchemeVertex> layoutRoots, final int mark )
	{
		++timestamp;
		rightmost = 0;
		vertexTable.clear();
		currentLayoutColumnX.clear();
		currentLayoutColumnRoot.clear();
		final TrackSchemeVertex previousGraphRoot = graph.vertexRef();
		final TrackSchemeVertex currentGraphRoot = graph.vertexRef();
		this.mark = mark;
		boolean first = true;
		currentLayoutColumnX.add( rightmost );
		for ( final TrackSchemeVertex root : layoutRoots )
		{
			layoutX( root );
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	 * @param colorGenerator
	 *            the color generator used to generate vertex and edge colors.
	 */
	@Override
	public void cropAndScale( final ScreenTransform transform, final ScreenEntities screenEntities, final int decorationsOffsetX, final int decorationsOffsetY, final GraphColorGenerator<TrackSchemeVertex, TrackSchemeEdge> colorGenerator )
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

		final TIntIterator iter = vertexTable.getTimepoints().iterator();
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
				final TrackSchemeVertexList vertexList = vertexTable.getOrderedVertices( timepoint );
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
						final double x = ( v1.getLayoutX() - minX ) * xScale + decorationsOffsetX;
						addScreenVertex( colorGenerator, screenVertices, screenVertexPool, v1, sv, x, y );

						minVertexScreenDist = Math.min( minVertexScreenDist, x - prevX );
						prevX = x;

						for ( final TrackSchemeEdge edge : v1.incomingEdges() )
						{
							edge.getSource( v2 );

							if(v2.getLayoutTimestamp() != timestamp)
								continue;

							int v2si = v2.getScreenVertexIndex();
							if ( v2si < 0 || v2si >= screenVertices.size() || screenVertices.get( v2si, sv ).getTrackSchemeVertexId() != v2.getInternalPoolIndex() )
							{
								// ScreenVertex for v2 not found. Adding one...
								final double nx = ( v2.getLayoutX() - minX ) * xScale + decorationsOffsetX;
								final double ny = ( v2.getTimepoint() - minY ) * yScale + decorationsOffsetY;
								addScreenVertex( colorGenerator, screenVertices, screenVertexPool, v2, sv, nx, ny );
							}

							final int eid = edge.getInternalPoolIndex();
							final int sourceScreenVertexIndex = v2.getScreenVertexIndex();
							final int targetScreenVertexIndex = v1.getScreenVertexIndex();
							final boolean eselected = selection.isSelected( edge );
							screenEdgePool.create( se ).init( eid, sourceScreenVertexIndex, targetScreenVertexIndex, eselected, colorGenerator.color( edge, v2, v1 ) );
							screenEdges.add( se );
							final int sei = se.getInternalPoolIndex();
							edge.setScreenEdgeIndex( sei );
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
						minVertexScreenDist = 0; // TODO: WHY = 0?
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

		buildScreenColumns( screenEntities, decorationsOffsetX, minX, maxX, xScale );
	}

	protected void addScreenVertex( GraphColorGenerator<TrackSchemeVertex, TrackSchemeEdge> colorGenerator, RefList<ScreenVertex> screenVertices, ScreenVertexPool screenVertexPool, TrackSchemeVertex v1, ScreenVertex sv, double x, double y )
	{
		final int v1si = screenVertices.size();
		v1.setScreenVertexIndex( v1si );
		final int id = v1.getInternalPoolIndex();
		final String label = v1.getLabel();
		final boolean selected = selection.isSelected( v1 );
		final boolean ghost = v1.isGhost();
		screenVertexPool.create( sv ).init( id, label, x, y, selected, ghost, colorGenerator.color( v1 ) );
		screenVertices.add( sv );
	}

	protected void buildScreenColumns( ScreenEntities screenEntities, int decorationsOffsetX, double minX, double maxX, double xScale )
	{
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
	@Override
	public TrackSchemeVertex getClosestActiveVertex( final RealLocalizable layoutPos, final double aspectRatioXtoY, final TrackSchemeVertex ref )
	{
		return vertexTable.getClosestVertex( layoutPos, aspectRatioXtoY, ref );
	}

	/**
	 * Of all active vertices in the rectangle with two corners
	 * {@code (lx1, ly1)} and {@code (lx2, ly2)} in layout
	 * coordinates, get the one with the minimal distance to {@code (lx2, ly2)}.
	 * The distance is computed as the Euclidean distance in layout
	 * space distorted by the specified aspect ratio.
	 *
	 * @param lx1
	 *            the x coordinate of the first corner.
	 * @param ly1
	 *            the y coordinate of the first corner.
	 * @param lx2
	 *            the x coordinate of the second corner.
	 * @param ly2
	 *            the y coordinate of the second corner.
	 * @param aspectRatioXtoY
	 *            The <em>X/Y</em> ratio of screen vector <em>(1,1)</em>
	 *            transformed into layout coordinates <em>(X,Y)</em>.
	 * @param ref
	 *            ref to store the result.
	 * @return the closest active vertex to the specified coordinates, or
	 *         {@code null} if there are no active vertices.
	 */
	@Override
	public TrackSchemeVertex getClosestActiveVertexWithin( final double lx1, final double ly1, final double lx2, final double ly2, final double aspectRatioXtoY, final TrackSchemeVertex ref )
	{
		return vertexTable.getClosestVertexWithin( lx1, ly1, lx2, ly2, aspectRatioXtoY, ref );
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
	@Override
	public RefSet< TrackSchemeVertex > getActiveVerticesWithin( final double lx1, final double ly1, final double lx2, final double ly2 )
	{
		return vertexTable.getVerticesWithin(lx1, ly1, lx2, ly2);
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
	@Override
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
	@Override
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
	@Override
	public TrackSchemeVertex getLeftSibling( final TrackSchemeVertex vertex, final TrackSchemeVertex ref )
	{
		return vertexTable.getLeftSibling( vertex, ref );
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
	@Override
	public TrackSchemeVertex getRightSibling( final TrackSchemeVertex vertex, final TrackSchemeVertex ref )
	{
		return vertexTable.getRightSibling( vertex, ref );
	}

	@Override
	public Listeners< LayoutListener > layoutListeners()
	{
		return listeners;
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
	 * @param root
	 *            root of sub-tree to layout.
	 */
	protected void layoutX( final TrackSchemeVertex root )
	{
		DepthFirstIteration<TrackSchemeVertex> df = new DepthFirstIteration<>( graph );
		df.setExcludeNodeAction( ( TrackSchemeVertex v ) -> {
			vertexTable.add( v );
			final boolean ghost = v.getLayoutTimestamp() < mark;
			final boolean terminate = v.getLayoutTimestamp() < mark - 1;
			v.setGhost( ghost );
			v.setLayoutTimestamp( timestamp );
			if ( terminate )
			{
				// This node, and it children will not be visited by the depth
				// first iteration, but we still won't it to appear in the layout
				// as a leaf.
				v.setLayoutX( rightmost++ );
			}
			return terminate;
		} );
		df.setVisitLeafAction( leaf -> {
			leaf.setLayoutX( rightmost++ );
		} );
		df.setVisitNodeAfterChildrenAction( (node, children) -> {
			double firstX = children.get( 0 ).getLayoutX();
			double lastX = children.get( children.size() - 1 ).getLayoutX();
			node.setLayoutX( ( firstX + lastX ) / 2 );
		} );
		df.runForRoot( root );
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

	protected void notifyListeners()
	{
		for ( final LayoutListener l : listeners.list )
			l.layoutChanged( this );
	}
}
