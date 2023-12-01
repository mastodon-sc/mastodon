/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

import org.mastodon.collection.ref.RefArrayList;

import gnu.trove.list.array.TIntArrayList;

/**
 * A list of {@link TrackSchemeVertex}. The vertices are assumed to be ordered
 * by {@link TrackSchemeVertex#getLayoutX() layoutX} and belong to the same
 * timepoint. {@link TrackSchemeVertexList} provides binary search (by
 * {@link TrackSchemeVertex#getLayoutX() layoutX}), computation of
 * {@code layoutX} range and density, and computation of dense vertex ranges.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class TrackSchemeVertexList extends RefArrayList< TrackSchemeVertex >
{
	private final TrackSchemeGraph< ?, ? > graph;

	private double cachedMinLayoutXDistance;

	// TODO: needs to be reset to false when the graph is laid out again.
	// TODO: needs to be reset when the list is modified.
	private boolean cachedMinLayoutXDistanceValid;

	public TrackSchemeVertexList( final TrackSchemeGraph< ?, ? > graph )
	{
		super( graph.getVertexPool() );
		this.graph = graph;
		cachedMinLayoutXDistanceValid = false;
	}

	public TrackSchemeVertexList( final TrackSchemeGraph< ?, ? > graph, final int initialCapacity )
	{
		super( graph.getVertexPool(), initialCapacity );
		this.graph = graph;
		cachedMinLayoutXDistanceValid = false;
	}

	protected TrackSchemeVertexList( final TrackSchemeVertexList list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
		this.graph = list.graph;
		cachedMinLayoutXDistanceValid = false;
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
	 * <p>
	 * For the returned index <em>i</em>, it holds that <em>-1 &le; i &lt;</em>
	 * {@code size()}.
	 *
	 * @param value
	 *            the value to search for
	 * @return the largest index of a vertex with {@code layoutX <= value} in
	 *         the sorted list.
	 */
	public int binarySearch( final double value )
	{
		return binarySearch( value, 0, size() );
	}

	/**
	 * Performs a binary search for {@code value} in the specified range. Finds
	 * the largest index of vertex with {@link TrackSchemeVertex#getLayoutX()
	 * layoutX} {@code <= value}. This assumes that the
	 * {@link TrackSchemeVertexList} is ordered by
	 * {@link TrackSchemeVertex#getLayoutX() layoutX}.
	 * <p>
	 * For the returned index <em>i</em>, it holds that {@code fromIndex}
	 * <em>- 1 &le; i &lt;</em> {@code toIndex}.
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

	protected TIntArrayList getDenseRanges(
			final int fromIndex,
			final int toIndex,
			final double minLayoutX,
			final double allowedMinD,
			final int minSubDivSize,
			final TrackSchemeVertex vref )
	{
		if ( fromIndex < 0 )
			throw new ArrayIndexOutOfBoundsException( fromIndex );
		if ( toIndex > size() )
			throw new ArrayIndexOutOfBoundsException( toIndex );

		final int i = fromIndex;
		final int j = toIndex - 1;

		final double xi = get( i, vref ).getLayoutX();
		final double xj = get( j, vref ).getLayoutX();

		if ( ( xj - xi ) - minLayoutX * ( j - i - 1 ) < allowedMinD )
		{
			final TIntArrayList ranges = new TIntArrayList();
			ranges.add( i );
			ranges.add( j );
			return ranges;
		}
		else
		{
			if ( toIndex - fromIndex < minSubDivSize )
				return null;
			final int k = ( i + j ) / 2;
			final TIntArrayList rangesL = getDenseRanges( i, k + 1, minLayoutX, allowedMinD, minSubDivSize, vref );
			final TIntArrayList rangesR = getDenseRanges( k, j + 1, minLayoutX, allowedMinD, minSubDivSize, vref );
			if ( rangesL == null )
				return rangesR;
			else if ( rangesR == null )
				return rangesL;
			else
			{
				if ( rangesL.get( rangesL.size() - 1 ) == rangesR.get( 0 ) )
				{
					rangesL.set( rangesL.size() - 1, rangesR.get( 1 ) );
					for ( int r = 2; r < rangesR.size(); ++r )
						rangesL.add( rangesR.get( r ) );
				}
				else
				{
					rangesL.addAll( rangesR );
				}
				return rangesL;
			}
		}
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
		if ( !cachedMinLayoutXDistanceValid )
		{
			cachedMinLayoutXDistance = getMinLayoutXDistance( 0, size() );
			cachedMinLayoutXDistanceValid = true;
		}
		return cachedMinLayoutXDistance;
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
