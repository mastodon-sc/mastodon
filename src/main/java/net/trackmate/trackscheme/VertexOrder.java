package net.trackmate.trackscheme;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;

import net.trackmate.graph.AllEdges;
import net.trackmate.graph.util.Graphs;
import net.trackmate.trackscheme.ScreenEdge.ScreenEdgePool;
import net.trackmate.trackscheme.ScreenVertex.ScreenVertexPool;

// TODO: build/maintain while adding nodes and edges to the TrackSchemeGraph
public class VertexOrder
{
	/**
	 * Initial capacity value to use when instantiating the screen pools.
	 */
	private static final int INITIAL_CAPACITY = 1000;

	private final TrackSchemeGraph graph;

	// ArrayList of all existing timpoints
	private final TIntArrayList timepoints;

	// Map timepoint -> vertex list
	// vertex list: all vertices of a timepoint ordered by natural tree order
	//              tree roots (including unconnected vertices) are ordered by ID (for now)
	private final TIntObjectHashMap< TrackSchemeVertexList > timepointToOrderedVertices;

	private ScreenVertexPool screenVertexPool;

	private ScreenVertexPool screenVertexPool2;

	private ScreenEdgePool screenEdgePool;

	private ScreenEdgePool screenEdgePool2;

	private ScreenVertexList screenVertices;

	private ScreenVertexList screenVertices2;

	private ScreenEdgeList screenEdges;

	private ScreenEdgeList screenEdges2;

	private double lastLayoutMinX;

	private double lastLayoutMaxX;

	private double lastLayoutMinY;

	private double lastLayoutMaxY;

	private int lastLayoutScreenWidth;

	private int lastLayoutScreenHeight;

	public VertexOrder( final TrackSchemeGraph graph )
	{
		this.graph = graph;
		timepoints = new TIntArrayList();
		timepointToOrderedVertices = new TIntObjectHashMap< TrackSchemeVertexList >();


		screenVertexPool = new ScreenVertex.ScreenVertexPool( INITIAL_CAPACITY, graph.getVertexPool() );
		screenVertexPool2 = new ScreenVertex.ScreenVertexPool( INITIAL_CAPACITY, graph.getVertexPool() );

		screenVertices = new ScreenVertexList( screenVertexPool, INITIAL_CAPACITY );
		screenVertices2 = new ScreenVertexList( screenVertexPool2, INITIAL_CAPACITY );

		screenEdgePool = new ScreenEdge.ScreenEdgePool( INITIAL_CAPACITY );
		screenEdgePool2 = new ScreenEdge.ScreenEdgePool( INITIAL_CAPACITY );

		screenEdges = new ScreenEdgeList( screenEdgePool, INITIAL_CAPACITY );
		screenEdges2 = new ScreenEdgeList( screenEdgePool2, INITIAL_CAPACITY );
	}

	private void swapPools()
	{
		{
			final ScreenVertexPool tmp = screenVertexPool;
			screenVertexPool = screenVertexPool2;
			screenVertexPool2 = tmp;
			screenVertexPool.clear();
		}

		{
			final ScreenVertexList tmp = screenVertices;
			screenVertices = screenVertices2;
			screenVertices2 = tmp;
			screenVertices.resetQuick();
		}

		{
			final ScreenEdgePool tmp = screenEdgePool;
			screenEdgePool = screenEdgePool2;
			screenEdgePool2 = tmp;
			screenEdgePool.clear();
		}

		{
			final ScreenEdgeList tmp = screenEdges;
			screenEdges = screenEdges2;
			screenEdges2 = tmp;
			screenEdges.resetQuick();
		}
	}

	public void build()
	{
		timepoints.clear();
		timepointToOrderedVertices.clear();

		final TrackSchemeVertexList roots = getOrderedRoots( graph );

		for ( final TrackSchemeVertex root : roots )
		{
			build( root );
		}
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
		final TrackSchemeEdge edge = graph.edgeRef();
		for ( final TrackSchemeEdge next : v.outgoingEdges() )
		{
			buildNextChild( next, child, edge );
		}
		graph.releaseRef( edge );
		graph.releaseRef( child );
	}

	private void buildNextChild( final TrackSchemeEdge next, final TrackSchemeVertex child, final TrackSchemeEdge edge )
	{
		next.getTarget( child );
		if ( child.incomingEdges().get( 0, edge ).equals( next ) )
		{
			build( child );
		}
	}

	public TrackSchemeVertex getMinVertex( final int timepoint, final TrackSchemeVertex vertex )
	{
		final TrackSchemeVertexList vlist = timepointToOrderedVertices.get( timepoint );
		if ( vlist == null ) {
			return null;
		}
		else
		{
			return vlist.get( 0, vertex );
		}
	}

	public TrackSchemeVertex getMaxVertex( final int timepoint, final TrackSchemeVertex vertex )
	{
		final TrackSchemeVertexList vlist = timepointToOrderedVertices.get( timepoint );
		if ( vlist == null ) {
			return null;
		}
		else
		{
			return vlist.get( vlist.size() - 1, vertex );
		}
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
		if ( timepoints.isEmpty() ) {
			return 0;
		}
		else
		{
			return timepoints.get( 0 );
		}
	}

	public int getMaxTimepoint()
	{
		if ( timepoints.isEmpty() ) {
			return 0;
		}
		else
		{
			return timepoints.get( timepoints.size() - 1 );
		}
	}

	private static final double SELECT_DISTANCE_TOLERANCE = 5.0;

	public void selectClosest( final double lx, final double ly )
	{
		final TrackSchemeVertex v = graph.vertexRef();
		final ScreenVertex sv = screenVertexPool.createRef();

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
				{
					left = 0;
				}
				int right = left + 1;
				if ( right >= vertexList.size() )
				{
					right = vertexList.size() - 1;
				}

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
				final double spotdiameter = Math.min( sv.getVertexDist() - 10.0, GraphLayoutOverlay.maxDisplayVertexSize );
				final double spotradius = ( int ) ( spotdiameter / 2 );
				if ( closestVertexD < spotradius + SELECT_DISTANCE_TOLERANCE )
				{
					final boolean selected = ! v.isSelected();
					v.setSelected( selected );
					sv.setSelected( selected );
					screenVertexPool.releaseRef( sv );
					graph.releaseRef( v );
					return;
				}
			}

			/*
			 * Look for an edge.
			 */

			final double x0 = lx * xScale;
			final double y0 = ly * yScale;
			final double x1 = v.getLayoutX() * xScale;
			final double y1 = v.getTimePoint() * yScale;

			final AllEdges< TrackSchemeEdge > edges = v.edges();
			TrackSchemeVertex o = graph.vertexRef();
			for ( final TrackSchemeEdge edge : edges )
			{
				o = Graphs.getOppositeVertex( edge, v, o );
				final double x2 = o.getLayoutX() * xScale;
				final double y2 = o.getTimePoint() * yScale;
				final double d = lineDist( x0, y0, x1, y1, x2, y2 );

				if ( d < SELECT_DISTANCE_TOLERANCE )
				{
					final boolean selected = ! edge.isSelected();
					edge.setSelected( selected );

					ScreenEdge sedge = screenEdges.createRef();
					sedge = screenEdges.get( edge.getInternalPoolIndex(), sedge );
					sedge.setSelected( selected );

					graph.releaseRef( o );
					screenEdges.releaseRef( sedge );
					break;
				}
			}

		}
		screenVertexPool.releaseRef( sv );
		graph.releaseRef( v );

	}

	//	private long numCropAndScales = 0;
	//	private long sumCropAndScaleTimes = 0;
	//	private long sumVerticesToPaint = 0;
	//	private long sumVertexRangesToPaint = 0;
	//	private final long printEveryNRuns = 100;

	public ScreenEntities cropAndScale(
			final double minX,
			final double maxX,
			final double minY,
			final double maxY,
			final int screenWidth,
			final int screenHeight )
	{
		//		final long t0 = System.currentTimeMillis();

		this.lastLayoutMinX = minX;
		this.lastLayoutMaxX = maxX;
		this.lastLayoutMinY = minY;
		this.lastLayoutMaxY = maxY;
		this.lastLayoutScreenWidth = screenWidth;
		this.lastLayoutScreenHeight = screenHeight;

		swapPools();

		final TrackSchemeVertex v1 = graph.vertexRef();
		final TrackSchemeVertex v2 = graph.vertexRef();
		final ScreenVertex sv = screenVertexPool.createRef();
		final ScreenEdge se = screenEdgePool.createRef();

		final ArrayList< ScreenVertexRange > vertexRanges = new ArrayList< ScreenVertexRange >();

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
				final double y = ( timepoint - minY ) * yScale; // screen y of vertices of timepoint
				final double prevY = ( timepoint - 1 - minY ) * yScale; // screen y of vertices of (timepoint-1)
				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
				// largest index of vertex with layoutX <= minX
				int minIndex = vertexList.binarySearch( minX );
				// include vertex before that (may be appears partially on screen, and may be needed to paint edge to vertex in other timepoint)
				minIndex--;
				if ( minIndex < 0 )
				{
					minIndex = 0;
				}
				// largest index of vertex with layoutX <= maxX
				int maxIndex = vertexList.binarySearch( maxX, minIndex, vertexList.size() );
				// include vertex after that (may be appears partially on screen, and may be needed to paint edge to vertex in other timepoint)
				if ( maxIndex < vertexList.size() - 1 )
				{
					maxIndex++;
				}

				final double minLayoutX = vertexList.getMinLayoutXDistance();
				TIntArrayList denseRanges = vertexList.getDenseRanges( minIndex, maxIndex + 1, minLayoutX, allowedMinD, 3, v1 );
				if ( denseRanges == null )
				{
					denseRanges = new TIntArrayList();
				}
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
						screenVertexPool.create( sv ).init( id, x, y, selected );
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

		//		final long t1 = System.currentTimeMillis();
		//
		//		numCropAndScales++;
		//		sumCropAndScaleTimes += ( t1 - t0 );
		//		sumVerticesToPaint += screenVertices.size();
		//		sumVertexRangesToPaint += vertexRanges.size();
		//		if ( numCropAndScales == printEveryNRuns )
		//		{
		//			System.out.println( "crop and scale time = " + ( sumCropAndScaleTimes / numCropAndScales ) + "ms" );
		//			System.out.println( "painting " + ( sumVerticesToPaint / numCropAndScales ) + " vertices" );
		//			System.out.println( "painting " + ( sumVertexRangesToPaint / numCropAndScales ) + " dense vertex ranges" );
		//			System.out.println( "(averages over last " + printEveryNRuns + " runs)");
		//			System.out.println();
		//
		//			numCropAndScales = 0;
		//			sumCropAndScaleTimes = 0;
		//			sumVerticesToPaint = 0;
		//			sumVertexRangesToPaint = 0;
		//		}

		return new ScreenEntities( screenVertices, screenEdges, vertexRanges );
	}

	public static TrackSchemeVertexList getOrderedRoots( final TrackSchemeGraph graph )
	{
		final TrackSchemeVertexList roots = new TrackSchemeVertexList( graph );
		for ( final TrackSchemeVertex v : graph.vertices() )
		{
			if ( v.incomingEdges().isEmpty() )
			{
				roots.add( v );
			}
		}
		roots.getIndexCollection().sort(); // TODO sort roots by something meaningful...
		return roots;
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
	private static final double lineDist( double x0, double y0, double x1, double y1, double x2, double y2 )
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
