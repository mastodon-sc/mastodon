package net.trackmate.trackscheme;

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

	public VertexOrder( final TrackSchemeGraph graph )
	{
		this.graph = graph;
		timepoints = new TIntArrayList();
		timepointToOrderedVertices = new TIntObjectHashMap< TrackSchemeVertexList >();
	}

	public void build()
	{
		timepoints.clear();
		timepointToOrderedVertices.clear();

		final TrackSchemeVertexList roots = getRoots( graph );
		roots.getIndexCollection().sort();
		// TODO sort roots by something meaningful...

		for ( final TrackSchemeVertex root : roots )
			build( root );
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
		// TODO reuse vetex ref?
		final TrackSchemeVertex vertex = graph.vertexRef();
		double min = Double.MAX_VALUE;
		if ( getMinVertex( timepoint, vertex ) != null )
			min = vertex.getLayoutX();
		graph.releaseRef( vertex );
		return min;
	}

	public double getMaxX( final int timepoint )
	{
		// TODO reuse vetex ref?
		final TrackSchemeVertex vertex = graph.vertexRef();
		double max = Double.MIN_VALUE;
		if ( getMaxVertex( timepoint, vertex ) != null )
			max = vertex.getLayoutX();
		graph.releaseRef( vertex );
		return max;
	}

	public double getMinX()
	{
		// TODO reuse vetex ref?
		final TrackSchemeVertex vertex = graph.vertexRef();
		double min = Double.MAX_VALUE;
		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			if ( getMinVertex( timepoint, vertex ) != null )
				min = Math.min( min, vertex.getLayoutX() );
		}
		graph.releaseRef( vertex );
		return min;
	}

	public double getMaxX()
	{
		// TODO reuse vetex ref?
		final TrackSchemeVertex vertex = graph.vertexRef();
		double max = Double.MIN_VALUE;
		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			if ( getMaxVertex( timepoint, vertex ) != null )
				max = Math.max( max, vertex.getLayoutX() );
		}
		graph.releaseRef( vertex );
		return max;
	}

	public double getMinDistance( final double minX, final double maxX, final int timepoint )
	{
		// TODO
		return 0;
	}

	public double getMinDistance( final TrackSchemeVertex first, final TrackSchemeVertex last, final int timepoint )
	{
		// TODO
		return 0;
	}

	public double getMinDistance( final int firstIndex, final int lastIndex, final int timepoint )
	{
		// TODO
		return 0;
	}


	// TODO add binarySearch, min, max, etc to TrackSchemeVertexList !?

	// find largest index of vertex with vertex.getLayoutX() <= layoutX
    public int binarySearch( final double layoutX, final int fromIndex, final int toIndex, final int timepoint ) {
		final TrackSchemeVertexList vlist = timepointToOrderedVertices.get( timepoint );
		if ( vlist == null )
            throw new ArrayIndexOutOfBoundsException( timepoint );
		if ( fromIndex < 0 )
            throw new ArrayIndexOutOfBoundsException( fromIndex );
        if ( toIndex > vlist.size() )
            throw new ArrayIndexOutOfBoundsException( toIndex );

        int low = fromIndex;
        int high = toIndex - 1;

		final TrackSchemeVertex vertex = graph.vertexRef();
		while ( low <= high )
		{
			final int mid = ( low + high ) >>> 1;
			final double midX = vlist.get( mid, vertex ).getLayoutX();

			System.out.println( "low=" + low + " mid=" + mid + " high=" + high );
			if ( midX <= layoutX )
				low = mid + 1;
			else
				high = mid - 1;
			System.out.println( "low=" + low + " mid=" + mid + " high=" + high );
			System.out.println();
		}
		graph.releaseRef( vertex );
		return low;
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



	public static TrackSchemeVertexList getRoots( final TrackSchemeGraph graph )
	{
		final TrackSchemeVertexList roots = new TrackSchemeVertexList( graph );
		for ( final TrackSchemeVertex v : graph.vertices() )
		{
			if ( v.incomingEdges().isEmpty() )
				roots.add( v );
		}
		return roots;
	}

	public static void main( final String[] args )
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		final TrackSchemeVertex v0 = graph.addVertex().init( "0", 0, false );
		final TrackSchemeVertex v1 = graph.addVertex().init( "1", 0, false );
		final TrackSchemeVertex v2 = graph.addVertex().init( "2", 0, false );;
		final TrackSchemeVertex v3 = graph.addVertex().init( "3", 0, false );;
		final TrackSchemeVertex v4 = graph.addVertex().init( "4", 0, false );;
		final TrackSchemeVertex v5 = graph.addVertex().init( "5", 0, false );;

		final LineageTreeLayout layout = new LineageTreeLayout( graph );
		layout.reset();
		layout.layoutX();
		System.out.println( graph );

		final VertexOrder order = new VertexOrder( graph );
		order.build();
		final TrackSchemeVertexList vlist = order.timepointToOrderedVertices.get( 0 );
		for ( final TrackSchemeVertex v : vlist )
		{
			System.out.print( v.getLayoutX() + "  " );
		}
		System.out.println();
		System.out.println();

		order.binarySearch( 4.1, 0, vlist.size(), 0 );
	}
}
