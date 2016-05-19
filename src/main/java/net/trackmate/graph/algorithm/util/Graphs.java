package net.trackmate.graph.algorithm.util;

import java.util.Comparator;

import net.trackmate.collection.RefList;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

/**
 * A collection of utilities to assist with graph manipulation.
 */
public class Graphs
{
	/**
	 * Gets the vertex opposite another vertex across an edge.
	 * <p>
	 * If the specified vertex does not have the specified edge in its list of
	 * edges, one or the other vertex of the specified edge is returned.
	 * 
	 * @param edge
	 *            the edge to inspect.
	 * @param vertex
	 *            the vertex on the undesired side of the edge.
	 * @param tmp
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the vertex opposite to the specified vertex across the specified
	 *         edge.
	 */
	public static final < V extends Vertex< E >, E extends Edge< V > > V getOppositeVertex( final E edge, final V vertex, final V tmp )
	{
		final V other = edge.getSource( tmp );
		if ( other.equals( vertex ) ) { return edge.getTarget( tmp ); }
		return other;
	}

	/**
	 * Sorts two {@link RefList}s following the order of the first one set by a
	 * specified {@link Comparator}.
	 * <p>
	 * More precisely:
	 * <ul>
	 * <li>The first list is sorted according to the specified comparator.
	 * <li>The second list has its elements rearranged so that is {@code O}
	 * and {@code P} share the same index in {@code listO} and
	 * {@code listP}, then they also share the same index after sort.
	 * </ul>
	 * 
	 * @param listO
	 *            the first list, that will be sorted.
	 * @param comparatorO
	 *            the comparator used to sort the first list. {@code null}
	 *            is not permitted.
	 * @param listP
	 *            the second list, rearranged according to the sort order of the
	 *            first list. Must be of the same size than the first list.
	 * @throws IllegalArgumentException
	 *             is the two lists are not of the same size.
	 */
	public static final < O, P > void sort( final RefList< O > listO, final Comparator< O > comparatorO, final RefList< P > listP )
	{
		if ( listO.size() != listP.size() ) { throw new IllegalArgumentException( "The two lists do not have the same size." ); }
		if ( listO.size() < 2 )
			return;
		
		final O tmpO1 = listO.createRef();
		final O tmpO2 = listO.createRef();
		
		quicksort( 0, listO.size() - 1, listO, comparatorO, listP, tmpO1, tmpO2 );

		listO.releaseRef( tmpO1 );
		listO.releaseRef( tmpO2 );
	}

	private static < O, P > void quicksort( final int low, final int high, final RefList< O > listO, final Comparator< O > comparatorO, final RefList< P > listP,
			final O tmpO1, final O tmpO2 )
	{
		final O pivot = listO.get( ( low + high ) / 2, tmpO1 );

		int i = low;
		int j = high;

		do
		{
			while ( comparatorO.compare( listO.get( i, tmpO2 ), pivot ) < 0 )
				i++;
			while ( comparatorO.compare( pivot, listO.get( j, tmpO2 ) ) < 0 )
				j--;

			if ( i <= j )
			{
				listO.swap( i, j );
				listP.swap( i, j );

				i++;
				j--;
			}
		}
		while ( i <= j );

		if ( low < j )
			quicksort( low, j, listO, comparatorO, listP, tmpO1, tmpO2 );
		if ( i < high )
			quicksort( i, high, listO, comparatorO, listP, tmpO1, tmpO2 );
	}
	
	private Graphs()
	{}

}
