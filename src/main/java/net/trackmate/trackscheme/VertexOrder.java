package net.trackmate.trackscheme;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import net.trackmate.graph.collection.RefSet;
import net.trackmate.trackscheme.ScreenEdge.ScreenEdgePool;
import net.trackmate.trackscheme.ScreenVertex.ScreenVertexPool;

// TODO: build/maintain while adding nodes and edges to the TrackSchemeGraph
public class VertexOrder
{

	public static final double maxDisplayVertexSize = 100.0;

	private final TrackSchemeGraph graph;

	/**
	 *  ArrayList of all existing timpoints
	 */
	private final TIntArrayList timepoints;

	/**
	 * Maps timepoint to vertex list.
	 *
	 * <p>
	 * vertex list: all vertices of a timepoint ordered by natural tree order
	 * tree roots (including unconnected vertices) are ordered by ID (for now)
	 */
	final TIntObjectHashMap< TrackSchemeVertexList > timepointToOrderedVertices;

	private ScreenEntities screenEntities;

	private double lastLayoutMinX;

	private double lastLayoutMaxX;

	private double lastLayoutMinY;

	private double lastLayoutMaxY;

	private int lastLayoutScreenWidth;

	private int lastLayoutScreenHeight;

	private int timestamp;

	public VertexOrder( final TrackSchemeGraph graph )
	{
		this.graph = graph;
		timepoints = new TIntArrayList();
		timepointToOrderedVertices = new TIntObjectHashMap< TrackSchemeVertexList >();
	}

	public void build( final List< TrackSchemeVertex > layoutRoots, final int timestamp )
	{
		timepoints.clear();
		timepointToOrderedVertices.clear();
		this.timestamp = timestamp;

		for ( final TrackSchemeVertex root : layoutRoots )
			build( root );
	}

	public void print()
	{
		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			System.out.println( "tp = " + timepoint );
			final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
			for ( final TrackSchemeVertex vertex : vertexList )
			{
				System.out.print( vertex.getModelVertexId() + ":" + vertex.getLayoutX() + " " );
			}
			System.out.println();
			System.out.println();
		}
	}

	private void build( final TrackSchemeVertex v )
	{
		if ( v.getLayoutTimestamp() != timestamp )
			return;

		final int tp = v.getTimePoint();
		TrackSchemeVertexList vlist = timepointToOrderedVertices.get( tp );
		if ( vlist == null )
		{
			vlist = new TrackSchemeVertexList( graph );
			timepointToOrderedVertices.put( tp, vlist );
			timepoints.insert( -( 1 + timepoints.binarySearch( tp ) ), tp );
		}
		vlist.add( v );

		final TrackSchemeVertex child = graph.vertexRef();
		for ( final TrackSchemeEdge next : v.outgoingEdges() )
			buildNextChild( next, child );
		graph.releaseRef( child );
	}

	private void buildNextChild( final TrackSchemeEdge next, final TrackSchemeVertex child )
	{
		next.getTarget( child );
		if ( child.getLayoutInEdgeIndex() == next.getInternalPoolIndex() )
			build( child );
	}

	public TrackSchemeVertex getMinVertex( final int timepoint, final TrackSchemeVertex vertex )
	{
		final TrackSchemeVertexList vlist = timepointToOrderedVertices.get( timepoint );
		if ( vlist == null )
			return null;
		else
			return vlist.get( 0, vertex );
	}

	public TrackSchemeVertex getMaxVertex( final int timepoint, final TrackSchemeVertex vertex )
	{
		final TrackSchemeVertexList vlist = timepointToOrderedVertices.get( timepoint );
		if ( vlist == null )
			return null;
		else
			return vlist.get( vlist.size() - 1, vertex );
	}

	public double getMinX( final int timepoint )
	{
		final TrackSchemeVertexList vlist = timepointToOrderedVertices.get( timepoint );
		return vlist == null ? Double.POSITIVE_INFINITY : vlist.getMinLayoutX();
	}

	public double getMaxX( final int timepoint )
	{
		final TrackSchemeVertexList vlist = timepointToOrderedVertices.get( timepoint );
		return vlist == null ? Double.NEGATIVE_INFINITY : vlist.getMaxLayoutX();
	}

	public double getMinX()
	{
		double min = Double.POSITIVE_INFINITY;
		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			min = Math.min( min, getMinX( timepoint ) );
		}
		return min;
	}

	public double getMaxX()
	{
		double max = Double.NEGATIVE_INFINITY;
		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			max = Math.max( max, getMaxX( timepoint ) );
		}
		return max;
	}

	public int getMinTimepoint()
	{
		if ( timepoints.isEmpty() )
			return 0;
		else
			return timepoints.get( 0 );
	}

	public int getMaxTimepoint()
	{
		if ( timepoints.isEmpty() )
			return 0;
		else
			return timepoints.get( timepoints.size() - 1 );
	}

	// TODO: (unused) REMOVE?
	public ScreenVertex getScreenVertexFor( final TrackSchemeVertex v )
	{
		final int si = v.getScreenVertexIndex();
		final ScreenVertexList screenVertices = screenEntities.getVertices();
		final ScreenVertex sv = screenVertices.createRef();
		if ( si >= 0 && si < screenVertices.size()
				&& screenVertices.get( si, sv ).getTrackSchemeVertexId() == v.getInternalPoolIndex() ) { return sv; }
		return null;
	}

	// TODO: (unused) REMOVE?
	public ScreenEdge getScreenEdgeFor( final TrackSchemeEdge e )
	{
		final int si = e.getScreenEdgeIndex();
		final ScreenEdgeList screenEdges = screenEntities.getEdges();
		final ScreenEdge se = screenEdges.createRef();
		if ( si >= 0 && si < screenEdges.size()
				&& screenEdges.get( si, se ).getTrackSchemeEdgeId() == e.getInternalPoolIndex() ) { return se; }
		return null;
	}

	public TrackSchemeVertex getClosestVertex( final double lx, final double ly, final double tolerance, final TrackSchemeVertex v )
	{
		final ScreenVertexList screenVertices = screenEntities.getVertices();
		final ScreenVertex sv = screenVertices.createRef();

		final double yScale = ( lastLayoutScreenHeight - 1 ) / ( lastLayoutMaxY - lastLayoutMinY );
		final double xScale = ( lastLayoutScreenWidth - 1 ) / ( lastLayoutMaxX - lastLayoutMinX );

		final int closestY = ( int ) Math.round( ly );

		double closestVertexD = Double.POSITIVE_INFINITY;
		int closestVertexIndex = -1;
		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			if ( timepoint >= closestY - 1 && timepoint <= closestY + 1 )
			{
				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
				int left = vertexList.binarySearch( lx );
				if ( left < 0 )
					left = 0;
				int right = left + 1;
				if ( right >= vertexList.size() )
					right = vertexList.size() - 1;

				vertexList.get( left, v );
				double diffx = ( lx - v.getLayoutX() ) * xScale;
				double diffy = ( ly - v.getTimePoint() ) * yScale;
				double d = Math.sqrt( diffx * diffx + diffy * diffy );
				if ( d < closestVertexD )
				{
					closestVertexD = d;
					closestVertexIndex = v.getInternalPoolIndex();
				}

				vertexList.get( right, v );
				diffx = ( lx - v.getLayoutX() ) * xScale;
				diffy = ( ly - v.getTimePoint() ) * yScale;
				d = Math.sqrt( diffx * diffx + diffy * diffy );
				if ( d < closestVertexD )
				{
					closestVertexD = d;
					closestVertexIndex = v.getInternalPoolIndex();
				}
			}
		}
		if ( closestVertexIndex >= 0 )
		{
			graph.getVertexPool().getByInternalPoolIndex( closestVertexIndex, v );

			final int si = v.getScreenVertexIndex();
			if ( si >= 0 && si < screenVertices.size() && screenVertices.get( si, sv ).getTrackSchemeVertexId() == v.getInternalPoolIndex() )
			{
				// FIXME move to common method in LAF.
				final double spotdiameter = Math.min( sv.getVertexDist() - 10.0, maxDisplayVertexSize );

				final double spotradius = ( int ) ( spotdiameter / 2 );
				if ( closestVertexD < spotradius + tolerance )
				{
					screenVertices.releaseRef( sv );
					return v;
				}
			}
		}

		return null;
	}

	public TrackSchemeEdge getClosestEdge( final double lx, final double ly, final double tolerance, final TrackSchemeEdge e )
	{
		final ScreenEdgeList screenEdges = screenEntities.getEdges();
		final double yScale = ( lastLayoutScreenHeight - 1 ) / ( lastLayoutMaxY - lastLayoutMinY );
		final double xScale = ( lastLayoutScreenWidth - 1 ) / ( lastLayoutMaxX - lastLayoutMinX );

		final double x0 = lx * xScale;
		final double y0 = ly * yScale;

		for ( final ScreenEdge se : screenEdges )
		{
			final int eid = se.getTrackSchemeEdgeId();
			graph.getEdgePool().getByInternalPoolIndex( eid, e );
			final TrackSchemeVertex s = e.getSource();
			final TrackSchemeVertex t = e.getTarget();

			final double x1 = s.getLayoutX() * xScale;
			final double x2 = t.getLayoutX() * xScale;

			if ( ( x0 < x1 - tolerance && x0 < x2 - tolerance )
					|| ( x0 > x1 + tolerance && x0 > x2 + tolerance ) )
			{
				continue;
			}

			final double y1 = s.getTimePoint() * yScale;
			final double y2 = t.getTimePoint() * yScale;

			if ( ( y0 < y1 - tolerance && y0 < y2 - tolerance )
					|| ( y0 > y1 + tolerance && y0 > y2 + tolerance ) )
			{
				continue;
			}

			final double d = lineDist( x0, y0, x1, y1, x2, y2 );

			if ( d < tolerance )
				return e;
		}
		return null;
	}

	public RefSet< TrackSchemeVertex > getVerticesWithin( final double lx1, final double ly1, final double lx2, final double ly2 )
	{
		final int tStart = ( int ) Math.ceil( Math.min( ly1, ly2 ) );
		final int tEnd = ( int ) Math.floor( Math.max( ly1, ly2 ) );
		final double x1 = Math.min( lx1, lx2 );
		final double x2 = Math.max( lx1, lx2 );

		final RefSet< TrackSchemeVertex > vertexSet = graph.createVertexSet();
		TrackSchemeVertex v = graph.vertexRef();

		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			if ( timepoint >= tStart && timepoint <= tEnd )
			{
				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
				int left = vertexList.binarySearch( x1 ) + 1;
				if ( left < 0 )
					left = 0;

				int right = vertexList.binarySearch( x2, left, vertexList.size() );
				if ( right >= vertexList.size() )
					right = vertexList.size() - 1;

				for ( int i = left; i <= right; i++ )
				{
					v = vertexList.get( i, v );
					vertexSet.add( v );
				}
			}
		}
		return vertexSet;
	}

	private long numCropAndScales = 0;
	private long sumCropAndScaleTimes = 0;
	private long sumVerticesToPaint = 0;
	private long sumVertexRangesToPaint = 0;
	private final long printEveryNRuns = 100;

	public void cropAndScale(
			final double minX,
			final double maxX,
			final double minY,
			final double maxY,
			final int screenWidth,
			final int screenHeight,
			final ScreenEntities screenEntities )
	{
		this.screenEntities = screenEntities;
		final long t0 = System.currentTimeMillis();

		this.lastLayoutMinX = minX;
		this.lastLayoutMaxX = maxX;
		this.lastLayoutMinY = minY;
		this.lastLayoutMaxY = maxY;
		this.lastLayoutScreenWidth = screenWidth;
		this.lastLayoutScreenHeight = screenHeight;

		final ScreenVertexList screenVertices = screenEntities.getVertices();
		final ScreenEdgeList screenEdges = screenEntities.getEdges();
		final List< ScreenVertexRange > vertexRanges = screenEntities.getVertexRanges();
		final ScreenVertexPool screenVertexPool = screenEntities.getVertexPool();
		final ScreenEdgePool screenEdgePool = screenEntities.getEdgePool();

		final TrackSchemeVertex v1 = graph.vertexRef();
		final TrackSchemeVertex v2 = graph.vertexRef();
		final ScreenVertex sv = screenVertexPool.createRef();
		final ScreenEdge se = screenEdgePool.createRef();


		final double yScale = ( screenHeight - 1 ) / ( maxY - minY );
		final double xScale = ( screenWidth - 1 ) / ( maxX - minX );
		final double allowedMinD = 2.0 / xScale;

//		System.out.println();
//		System.out.println( xScale + " xScale" );
//		System.out.println( allowedMinD + " allowedMinD" );

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

//				System.out.println( "timepoint " + timepoint + ": " + ( maxIndex - minIndex + 1 ) + " vertices, " + minLayoutX + " min Distance" );
//				System.out.println( "numranges = " + ( ( denseRanges.size() - 1 ) / 2 ) );

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
						final double svMaxX = ( vertexList.get( rangeMaxIndex, v1 ).getLayoutX() - minX ) * xScale;
						vertexRanges.add( new ScreenVertexRange( svMinX, svMaxX, prevY, y ) );
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

		final long t1 = System.currentTimeMillis();

		numCropAndScales++;
		sumCropAndScaleTimes += ( t1 - t0 );
		sumVerticesToPaint += screenVertices.size();
		sumVertexRangesToPaint += vertexRanges.size();
		if ( numCropAndScales == printEveryNRuns )
		{
			System.out.println( "crop and scale time = " + ( sumCropAndScaleTimes / numCropAndScales ) + "ms" );
			System.out.println( "painting " + ( sumVerticesToPaint / numCropAndScales ) + " vertices" );
			System.out.println( "painting " + ( sumVertexRangesToPaint / numCropAndScales ) + " dense vertex ranges" );
			System.out.println( "(averages over last " + printEveryNRuns + " runs)" );
			System.out.println();

			numCropAndScales = 0;
			sumCropAndScaleTimes = 0;
			sumVerticesToPaint = 0;
			sumVertexRangesToPaint = 0;
		}
	}

	/**
	 * Computes the distance of a point <code>A0 (x0, y0)</code> to a line
	 * defined by two points <code>A1 (x1, y1)</code> and
	 * <code>A2 (x2, y2)</code>. Returns <code>infinity</code> if the projection
	 * of <code>A0</code> on the line does not lie between <code>A1</code> and
	 * <code>A2</code>.
	 *
	 * @return the distance from a line to a point.
	 */
	private static final double lineDist( final double x0, final double y0, final double x1, final double y1, final double x2, final double y2 )
	{
		final double l12sq = ( x2 - x1 ) * ( x2 - x1 ) + ( y2 - y1 ) * ( y2 - y1 );

		final double x = ( ( x0 - x1 ) * ( x2 - x1 ) + ( y0 - y1 ) * ( y2 - y1 ) ) / l12sq;
		if ( x < 0 || x > 1 ) { return Double.POSITIVE_INFINITY; }

		final double d = Math.abs(
				( y2 - y1 ) * x0 - ( x2 - x1 ) * y0 + x2 * y1 - y2 * x1
				) / Math.sqrt( l12sq );
		return d;
	}
}
