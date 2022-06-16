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

import java.util.List;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.views.trackscheme.ScreenEdge.ScreenEdgePool;
import org.mastodon.views.trackscheme.ScreenVertex.ScreenVertexPool;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

/**
 * A version of {@link LineageTreeLayoutImp} made specially for branch graphs.
 * <p>
 * {@link LineageTreeLayoutImp} generates Screen objects only for the vertices that
 * are in the visible rectangle specified in the transform when calling
 * {@link #cropAndScale(ScreenTransform, ScreenEntities, int, int, GraphColorGenerator)}.
 * This is ok when the TrackSchemeGraph is dense, as in for the core graph, but
 * not for the branch graph. Indeed, the branch graph has few vertices and long
 * edges. When a branch does not have its vertices on the visible rectangle, it
 * is not painted. This version solves this issue by forcing the generation of
 * Screen objects ignoring the Y bounds of the visible rectangle.
 */
public class LongEdgesLineageTreeLayout extends LineageTreeLayoutImp
{

	public LongEdgesLineageTreeLayout(
			final TrackSchemeGraph< ?, ? > graph,
			final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection )
	{
		super( graph, selection );
	}

	@Override
	public void cropAndScale(
			final ScreenTransform transform,
			final ScreenEntities screenEntities,
			final int decorationsOffsetX,
			final int decorationsOffsetY,
			final GraphColorGenerator< TrackSchemeVertex, TrackSchemeEdge > colorGenerator )
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
		final ScreenVertexPool screenVertexPool = screenEntities.getVertexPool();
		final ScreenEdgePool screenEdgePool = screenEntities.getEdgePool();

		final TrackSchemeVertex v1 = graph.vertexRef();
		final TrackSchemeVertex v2 = graph.vertexRef();
		final ScreenVertex sv = screenVertexPool.createRef();
		final ScreenEdge se = screenEdgePool.createRef();

		final double allowedMinD = 2.0 / xScale;

		final TIntIterator iter = vertexTable.getTimepoints().iterator();
		while ( iter.hasNext() )
		{
			final int timepoint = iter.next();
			final int timepointStartScreenVertexIndex = screenVertices.size();
			// screen y of vertices of timepoint
			final double y = ( timepoint - minY ) * yScale + decorationsOffsetY;
			// screen y of vertices of (timepoint-1)
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

			double prevX = Double.NEGATIVE_INFINITY;
			double minVertexScreenDist = yScale;
			for ( int i = minIndex; i <= maxIndex; ++i )
			{
				vertexList.get( i, v1 );
				final double x = ( v1.getLayoutX() - minX ) * xScale + decorationsOffsetX;
				addScreenVertex( colorGenerator, screenVertices, screenVertexPool, v1, sv, x, y );

				minVertexScreenDist = Math.min( minVertexScreenDist, x - prevX );
				prevX = x;

				final int tp1 = v1.getTimepoint();
				for ( final TrackSchemeEdge edge : v1.incomingEdges() )
				{
					edge.getSource( v2 );

					if( v2.getLayoutTimestamp() != timestamp )
						continue;

					// Check if the edge has some parts on the screen.
					final int tp2 = v2.getTimepoint();
					if ( ( tp1 > maxY && tp2 > maxY ) || ( tp1 < minY && tp2 < minY ) )
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
			for ( int i = timepointStartScreenVertexIndex; i < screenVertices.size(); ++i )
			{
				screenVertices.get( i, sv ).setVertexDist( minVertexScreenDist );
			}
		}

		screenEdgePool.releaseRef( se );
		screenVertexPool.releaseRef( sv );
		graph.releaseRef( v1 );
		graph.releaseRef( v2 );

		buildScreenColumns( screenEntities, decorationsOffsetX, minX, maxX, xScale );
	}
}
