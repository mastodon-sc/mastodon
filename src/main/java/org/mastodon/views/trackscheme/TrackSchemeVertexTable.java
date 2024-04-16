/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import gnu.trove.iterator.TIntAlternatingIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectArrayMap;
import gnu.trove.map.TIntObjectMap;
import net.imglib2.RealLocalizable;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;

/**
 * A "table" that contains vertices ({@link TrackSchemeVertex}).
 * The "table" has a "row" for each time point, that contains all the vertices
 * of this time point. Each "row" in the table is basically a {@link TrackSchemeVertexList}.
 * These lists are sorted in ascending order with respect to the {@link TrackSchemeVertex#getLayoutX() layout X} coordinate.
 */
public class TrackSchemeVertexTable
{
	/**
	 *  Ordered list of all existing timepoints.
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

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeVertexTable( TrackSchemeGraph< ?, ? > graph )
	{
		this.graph = graph;
		timepoints = new TIntArrayList();
		timepointToOrderedVertices = new TIntObjectArrayMap<>();
	}

	public void clear()
	{
		timepoints.clear();
		timepointToOrderedVertices.clear();
	}

	public TIntList getTimepoints()
	{
		return timepoints;
	}

	public TrackSchemeVertexList getOrderedVertices( int timepoint )
	{
		return timepointToOrderedVertices.get( timepoint );
	}

	public void add( TrackSchemeVertex v )
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

	public RefSet< TrackSchemeVertex > getVerticesWithin( double lx1, double ly1, double lx2, double ly2 )
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
			final int timepoint = timepoints.get( tpIndex );
			final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( timepoint );
			final int left = vertexList.binarySearch( x1 ) + 1;
			final int right = vertexList.binarySearch( x2, left, vertexList.size() );
			vertexSet.addAll( vertexList.subList( left, right + 1 ) );
		}
		return vertexSet;
	}

	public TrackSchemeVertex getClosestVertex( RealLocalizable layoutPos, double aspectRatioXtoY,
			TrackSchemeVertex ref )
	{
		final double lx = layoutPos.getDoublePosition( 0 );
		final double ly = layoutPos.getDoublePosition( 1 );

		double closestVertexSquareDist = Double.POSITIVE_INFINITY;
		int closestVertexIndex = -1;

		final TIntIterator tpIter = new TIntAlternatingIterator( timepoints, ( int ) ly );
		while ( tpIter.hasNext() )
		{
			final int tp = tpIter.next();
			final double diffy = ( ly - tp ) * aspectRatioXtoY;
			if ( diffy * diffy >= closestVertexSquareDist )
				break;

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

		if ( closestVertexIndex < 0 )
			return null;

		graph.getVertexPool().getObject( closestVertexIndex, ref );
		return ref;
	}

	public TrackSchemeVertex getClosestVertexWithin( double lx1, double ly1, double lx2, double ly2,
			double aspectRatioXtoY, TrackSchemeVertex ref )
	{
		final int tStart = ( int ) Math.ceil( Math.min( ly1, ly2 ) );
		final int tEnd = ( int ) Math.floor( Math.max( ly1, ly2 ) ) + 1;
		final double x1 = Math.min( lx1, lx2 );
		final double x2 = Math.max( lx1, lx2 );

		double closestVertexSquareDist = Double.POSITIVE_INFINITY;
		int closestVertexIndex = -1;

		int start = timepoints.binarySearch( tStart );
		if ( start < 0 )
			start = -start - 1;
		int end = timepoints.binarySearch( tEnd );
		if ( end < 0 )
			end = -end - 1;

		final int tpIndexFirst = ly1 < ly2 ? end - 1 : start;
		final int tpIndexLast = ly1 < ly2 ? start - 1 : end;
		final int tpIndexInc = ly1 < ly2 ? -1 : 1;
		for ( int tpIndex = tpIndexFirst; tpIndex != tpIndexLast; tpIndex += tpIndexInc )
		{
			final double diffy = ( ly2 - tpIndex ) * aspectRatioXtoY;
			if ( diffy * diffy >= closestVertexSquareDist )
				break;

			int time = timepoints.get( tpIndex );
			final TrackSchemeVertexList vertexList = timepointToOrderedVertices.get( time );
			final int left = vertexList.binarySearch( x1 ) + 1;
			final int right = vertexList.binarySearch( x2, left, vertexList.size() );
			if ( right > left )
			{
				final int candidate = lx1 < lx2 ? right : left;
				final TrackSchemeVertex v = vertexList.get( candidate, ref );
				final double diffx = ( lx2 - v.getLayoutX() );
				final double d2 = diffx * diffx + diffy * diffy;
				if ( d2 < closestVertexSquareDist )
				{
					closestVertexSquareDist = d2;
					closestVertexIndex = v.getInternalPoolIndex();
				}
			}
		}

		if ( closestVertexIndex < 0 )
			return null;

		return graph.getVertexPool().getObject( closestVertexIndex, ref );
	}

	public TrackSchemeVertex getLeftSibling( TrackSchemeVertex vertex, TrackSchemeVertex ref )
	{
		final TrackSchemeVertexList vertices = timepointToOrderedVertices.get( vertex.getTimepoint() );
		final int index = vertices.binarySearch( vertex.getLayoutX() );
		return ( index > 0 )
				? vertices.get( index - 1, ref )
				: null;
	}

	public TrackSchemeVertex getRightSibling( TrackSchemeVertex vertex, TrackSchemeVertex ref )
	{
		final TrackSchemeVertexList vertices = timepointToOrderedVertices.get( vertex.getTimepoint() );
		final int index = vertices.binarySearch( vertex.getLayoutX() );
		return ( index < vertices.size() - 1 )
				? vertices.get( index + 1, ref )
				: null;
	}
}
