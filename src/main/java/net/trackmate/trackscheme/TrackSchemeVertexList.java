package net.trackmate.trackscheme;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.mempool.ByteMappedElement;

public class TrackSchemeVertexList extends PoolObjectList< TrackSchemeVertex, ByteMappedElement >
{
	private final TrackSchemeGraph graph;

	public TrackSchemeVertexList( final TrackSchemeGraph graph )
	{
		super( graph.getVertexPool() );
		this.graph = graph;
	}

	public TrackSchemeVertexList( final TrackSchemeGraph graph, final int initialCapacity )
	{
		super( graph.getVertexPool(), initialCapacity );
		this.graph = graph;
	}

	protected TrackSchemeVertexList( final TrackSchemeVertexList list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
		this.graph = list.graph;
	}

	@Override
	public TrackSchemeVertexList subList( final int fromIndex, final int toIndex )
	{
		return new TrackSchemeVertexList( this, ( TIntArrayList ) getIndexCollection().subList( fromIndex, toIndex ) );
	}

	/**
	 * Performs a binary search for {@code value} in the entire list. Finds the
	 * largest index of vertex with {@link TrackSchemeVertex#getLayoutX()
	 * layoutX} {@code <= value}. This assumes that the
	 * {@link TrackSchemeVertexList} is ordered by
	 * {@link TrackSchemeVertex#getLayoutX() layoutX}.
	 *
	 * @param value
	 *            the value to search for
	 * @return the largest index of a vertex with {@code layoutX <= value} in
	 *         the sorted list.
	 */
	protected int binarySearch( final double value )
	{
		return binarySearch( value, 0, size() );
	}

	/**
	 * Performs a binary search for {@code value} in the specified range. Finds
	 * the largest index of vertex with {@link TrackSchemeVertex#getLayoutX()
	 * layoutX} {@code <= value}. This assumes that the
	 * {@link TrackSchemeVertexList} is ordered by
	 * {@link TrackSchemeVertex#getLayoutX() layoutX}.
	 *
	 * @param value
	 *            the value to search for
	 * @param fromIndex
	 *            the lower boundary of the range (inclusive)
	 * @param toIndex
	 *            the upper boundary of the range (exclusive)
	 * @return the largest index of a vertex with {@code layoutX <= value} in
	 *         the sorted list.
	 */
	protected int binarySearch( final double value, final int fromIndex, final int toIndex )
	{
		if ( fromIndex < 0 )
			throw new ArrayIndexOutOfBoundsException( fromIndex );
		if ( toIndex > size() )
			throw new ArrayIndexOutOfBoundsException( toIndex );

		int low = fromIndex;
		int high = toIndex - 1;

		final TrackSchemeVertex vertex = graph.vertexRef();
		while ( low <= high )
		{
			final int mid = ( low + high ) >>> 1;
			final double midX = get( mid, vertex ).getLayoutX();

			if ( midX <= value )
				low = mid + 1;
			else
				high = mid - 1;
		}
		graph.releaseRef( vertex );
		return high;
	}

	protected double getMinLayoutX()
	{
		if ( isEmpty() )
			return 0;
		else
		{
			final TrackSchemeVertex vertex = get( 0, graph.vertexRef() );
			final double min = vertex.getLayoutX();
			graph.releaseRef( vertex );
			return min;
		}
	}

	protected double getMaxLayoutX()
	{
		if ( isEmpty() )
			return 0;
		else
		{
			final TrackSchemeVertex vertex = graph.vertexRef();
			final double max = get( size() - 1, vertex ).getLayoutX();
			graph.releaseRef( vertex );
			return max;
		}
	}

	/**
	 * Get minimal {@link TrackSchemeVertex#getLayoutX() layoutX} distance
	 * between two neighboring vertices in the entire list. This assumes
	 * that the {@link TrackSchemeVertexList} is ordered by
	 * {@link TrackSchemeVertex#getLayoutX() layoutX}.
	 *
	 * @return minimal layoutX distance between neighboring vertices.
	 */
	protected double getMinLayoutXDistance()
	{
		return getMinLayoutXDistance( 0, size() );
	}

	/**
	 * Get minimal {@link TrackSchemeVertex#getLayoutX() layoutX} distance
	 * between two neighboring vertices in the specified range. This assumes
	 * that the {@link TrackSchemeVertexList} is ordered by
	 * {@link TrackSchemeVertex#getLayoutX() layoutX}.
	 *
	 * @param fromIndex
	 *            the lower boundary of the range (inclusive)
	 * @param toIndex
	 *            the upper boundary of the range (exclusive)
	 * @return minimal layoutX distance between neighboring vertices.
	 */
	protected double getMinLayoutXDistance( final int fromIndex, final int toIndex )
	{
		if ( fromIndex < 0 )
			throw new ArrayIndexOutOfBoundsException( fromIndex );
		if ( toIndex > size() )
			throw new ArrayIndexOutOfBoundsException( toIndex );

		final TrackSchemeVertex vertex = graph.vertexRef();
		double prevLayoutX = get( fromIndex, vertex ).getLayoutX();
		double d = Double.POSITIVE_INFINITY;
		for ( int i = fromIndex + 1; i < toIndex; ++i )
		{
			final double x = get( i, vertex ).getLayoutX();
			d = Math.min( d, x - prevLayoutX );
			prevLayoutX = x;
		}
		graph.releaseRef( vertex );
		return d;
	}
}
