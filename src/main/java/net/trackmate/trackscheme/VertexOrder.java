package net.trackmate.trackscheme;

import java.util.ArrayList;
import java.util.List;

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

	public List< ScreenVertex > cropAndScale( final double minX, final double maxX, final double minY, final double maxY, final int screenWidth, final int screenHeight )
	{
		final TrackSchemeVertex v1 = graph.vertexRef();
		final ArrayList< ScreenVertex > screenVertices = new ArrayList< ScreenVertex >();

		final double yScale = ( double ) ( screenHeight - 1 ) / ( maxY - minY );
		final double xScale = ( double ) ( screenWidth - 1 ) / ( maxX - minX );

		final TIntIterator iter = timepoints.iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			if ( timepoint + 1 >= minY && timepoint - 1 <= maxY )
			{
				System.out.println( "use timepoint " + timepoint );

				final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
				int minIndex = vertexList.binarySearch( minX );
				minIndex--;
				if ( minIndex < 0 )
					minIndex = 0;
				int maxIndex = vertexList.binarySearch( maxX, minIndex, vertexList.size() );
				if ( maxIndex < vertexList.size() - 1 )
					maxIndex++;
				for ( int i = minIndex; i <= maxIndex; ++i )
				{
					vertexList.get( i, v1 );
					final int id = v1.getInternalPoolIndex();
					final double x = ( v1.getLayoutX() - minX ) * xScale;
					final double y = ( v1.getTimePoint() - minY ) * yScale;
					final String label = v1.getLabel();
					final boolean selected = false;
					final ScreenVertex sv = new ScreenVertex( id, x, y, label, selected );
					screenVertices.add( sv );
				}
			}
		}
		graph.releaseRef( v1 );
		return screenVertices;
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
