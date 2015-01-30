package net.trackmate.trackscheme;

import net.trackmate.trackscheme.ScreenEdge.ScreenEdgePool;
import net.trackmate.trackscheme.ScreenVertex.ScreenVertexPool;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

// TODO: build/maintain while adding nodes and edges to the TrackSchemeGraph
public class VertexOrder
{
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

	public VertexOrder( final TrackSchemeGraph graph )
	{
		this.graph = graph;
		timepoints = new TIntArrayList();
		timepointToOrderedVertices = new TIntObjectHashMap< TrackSchemeVertexList >();


		screenVertexPool = new ScreenVertex.ScreenVertexPool( 1000000 );
		screenVertexPool2 = new ScreenVertex.ScreenVertexPool( 1000000 );

		screenVertices = new ScreenVertexList( screenVertexPool, 1000000 );
		screenVertices2 = new ScreenVertexList( screenVertexPool2, 1000000 );

		screenEdgePool = new ScreenEdge.ScreenEdgePool( 1000000 );
		screenEdgePool2 = new ScreenEdge.ScreenEdgePool( 1000000 );

		screenEdges = new ScreenEdgeList( screenEdgePool, 1000000 );
		screenEdges2 = new ScreenEdgeList( screenEdgePool2, 1000000 );
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
				System.out.print( vertex.getId() + ":" + vertex.getLayoutX() + " " );
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
		for ( final TrackSchemeEdge edge : v.outgoingEdges() )
			build( edge.getTarget( child ) );
		graph.releaseRef( child );
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

	public ScreenEntities cropAndScale( final double minX, final double maxX, final double minY, final double maxY, final int screenWidth, final int screenHeight )
	{
		swapPools();

		final TrackSchemeVertex v1 = graph.vertexRef();
		final TrackSchemeVertex v2 = graph.vertexRef();
		final ScreenVertex sv = screenVertexPool.createRef();
		final ScreenEdge se = screenEdgePool.createRef();

		final double yScale = ( double ) ( screenHeight - 1 ) / ( maxY - minY );
		final double xScale = ( double ) ( screenWidth - 1 ) / ( maxX - minX );

		final long t0 = System.currentTimeMillis();
		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			if ( timepoint + 1 >= minY && timepoint - 1 <= maxY )
			{
				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
				int minIndex = vertexList.binarySearch( minX );
				minIndex--;
				if ( minIndex < 0 )
					minIndex = 0;
				int maxIndex = vertexList.binarySearch( maxX, minIndex, vertexList.size() );
				if ( maxIndex < vertexList.size() - 1 )
					maxIndex++;
//				final double minLayoutXDistance = xScale * vertexList.getMinLayoutXDistance( minIndex, maxIndex + 1 );
//				System.out.println( "timepoint " + timepoint + ": " + ( maxIndex - minIndex + 1 ) + " vertices, " + minLayoutXDistance + " min Distance" );
				for ( int i = minIndex; i <= maxIndex; ++i )
				{
					vertexList.get( i, v1 );
					final int v1si = screenVertices.size();
					v1.setScreenVertexIndex( v1si );
					final int id = v1.getInternalPoolIndex();
					final double x = ( v1.getLayoutX() - minX ) * xScale;
					final double y = ( v1.getTimePoint() - minY ) * yScale;
//					final String label = v1.getLabel();
					final boolean selected = false;
					screenVertexPool.create( sv ).init( id, x, y, selected );
					screenVertices.add( sv );

					for ( final TrackSchemeEdge edge : v1.incomingEdges() )
					{
						edge.getSource( v2 );
						final int v2si = v2.getScreenVertexIndex();
						if ( v2si < screenVertices.size() && screenVertices.get( v2si, sv ).getId() == v2.getInternalPoolIndex() )
						{
							final int eid = edge.getInternalPoolIndex();
							final int sourceScreenVertexIndex = v2si;
							final int targetScreenVertexIndex = v1si;
							final boolean eselected = false;
							screenEdgePool.create( se ).init( eid, sourceScreenVertexIndex, targetScreenVertexIndex, eselected );
							screenEdges.add( se );
						}
					}
				}
			}
		}
//		System.out.println( "screenVertices.size() = " + screenVertices.size() );
//		final long t1 = System.currentTimeMillis();
//		System.out.println( ( t1 - t0 ) + "ms" );

		screenEdgePool.releaseRef( se );
		screenVertexPool.releaseRef( sv );
		graph.releaseRef( v1 );
		graph.releaseRef( v2 );

		return new ScreenEntities( screenVertices, screenEdges );
	}

	public static TrackSchemeVertexList getOrderedRoots( final TrackSchemeGraph graph )
	{
		final TrackSchemeVertexList roots = new TrackSchemeVertexList( graph );
		for ( final TrackSchemeVertex v : graph.vertices() )
		{
			if ( v.incomingEdges().isEmpty() )
				roots.add( v );
		}
		roots.getIndexCollection().sort(); // TODO sort roots by something meaningful...
		return roots;
	}
}
