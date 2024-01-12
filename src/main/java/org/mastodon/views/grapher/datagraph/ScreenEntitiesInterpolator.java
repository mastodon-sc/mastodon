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
package org.mastodon.views.grapher.datagraph;

import static org.mastodon.views.trackscheme.ScreenVertex.Transition.APPEAR;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.DESELECTING;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.DISAPPEAR;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.NONE;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.SELECTING;

import org.mastodon.collection.ref.IntRefHashMap;

public class ScreenEntitiesInterpolator
{
	private final ScreenEntities start;

	private final ScreenEntities end;

	private final IntRefHashMap< ScreenVertex > idToStartVertex;

	private final IntRefHashMap< ScreenVertex > idToEndVertex;

	private final IntRefHashMap< ScreenEdge > idToStartEdge;

	private final IntRefHashMap< ScreenEdge > idToEndEdge;

	private final ScreenTransform incrementalStartTransform;

	/**
	 * Create an interpolator between two sets of {@link ScreenEntities}.
	 *
	 * @param start
	 *            start of the interpolation.
	 * @param end
	 *            end of the interpolation.
	 */
	public ScreenEntitiesInterpolator( final ScreenEntities start, final ScreenEntities end )
	{
		this( start, end, null );
	}

	/**
	 * Create an interpolator between two sets of {@link ScreenEntities}.
	 * Optionally, an incremental {@link ScreenTransform} can be specified, that
	 * is applied to the {@code start} {@link ScreenEntities} (on top of the
	 * transform that was used to create them).
	 *
	 * @param start
	 *            start of the interpolation.
	 * @param end
	 *            end of the interpolation.
	 * @param incrementalStartTransform
	 *            optional incremental transform of start entities.
	 */
	public ScreenEntitiesInterpolator( final ScreenEntities start, final ScreenEntities end,
			final ScreenTransform incrementalStartTransform )
	{
		this.start = start;
		this.end = end;

		idToStartVertex = new IntRefHashMap< ScreenVertex >( start.getVertexPool(), -1, start.getVertices().size() );
		for ( final ScreenVertex v : start.getVertices() )
			idToStartVertex.put( v.getDataVertexId(), v );

		idToEndVertex = new IntRefHashMap< ScreenVertex >( end.getVertexPool(), -1, end.getVertices().size() );
		for ( final ScreenVertex v : end.getVertices() )
			idToEndVertex.put( v.getDataVertexId(), v );

		idToStartEdge = new IntRefHashMap< ScreenEdge >( start.getEdgePool(), -1, start.getEdges().size() );
		for ( final ScreenEdge e : start.getEdges() )
			idToStartEdge.put( e.getDataEdgeId(), e );

		idToEndEdge = new IntRefHashMap< ScreenEdge >( end.getEdgePool(), -1, end.getEdges().size() );
		for ( final ScreenEdge e : end.getEdges() )
			idToEndEdge.put( e.getDataEdgeId(), e );

		this.incrementalStartTransform = incrementalStartTransform;
	}

	public static ScreenTransform getIncrementalY( final ScreenEntities start, final ScreenEntities end )
	{
		final ScreenTransform t = end.screenTransform().concatenate( start.screenTransform().inverse() );
		t.set( 0, t.getScreenWidth() - 1, t.getMinY(), t.getMaxY(), t.getScreenWidth(), t.getScreenHeight() );
		return t;
	}

	public static ScreenTransform getIncrementalXY( final ScreenEntities start, final ScreenEntities end )
	{
		return end.screenTransform().concatenate( start.screenTransform().inverse() );
	}

	public void interpolate( final double currentRatio, final ScreenEntities current )
	{
		final double accelRatio = Math.sin( Math.PI * Math.sin( Math.PI * currentRatio / 2 ) / 2 );

		// Interpolate vertices
		// ====================
		// Each interpolated vertex either moves, appears, disappears, gets selected or gets de-selected.
		final ScreenVertex vCurrent = current.getVertexPool().createRef();
		final ScreenVertex vStart = start.getVertexPool().createRef();
		final ScreenVertex vEnd = end.getVertexPool().createRef();
		for ( final ScreenVertex v : start.getVertices() )
		{
			final int vId = v.getDataVertexId();
			if ( vId < 0 )
				continue;

			current.getVertices().add( current.getVertexPool().create( vCurrent ) );
			if ( idToEndVertex.get( vId, vEnd ) != null )
				interpolate( v, vEnd, accelRatio, vCurrent );
			else
				disappear( v, accelRatio, vCurrent );
		}
		for ( final ScreenVertex v : end.getVertices() )
		{
			if ( idToStartVertex.get( v.getDataVertexId(), vStart ) == null )
			{
				current.getVertices().add( current.getVertexPool().create( vCurrent ) );
				appear( v, accelRatio, vCurrent );
			}
		}

		// Interpolate edges
		// =================
		// For now, only edges between non-disappearing interpolated vertices
		// are added.
		final ScreenEdge eCurrent = current.getEdgePool().createRef();
		final ScreenEdge eStart = start.getEdgePool().createRef();
		final ScreenEdge eEnd = end.getEdgePool().createRef();
		for ( final ScreenEdge e : end.getEdges() )
		{
			final int sourceIndex =
					end.getVertices().get( e.getSourceScreenVertexIndex(), vEnd ).getInterpolatedScreenVertexIndex();
			final int targetIndex =
					end.getVertices().get( e.getTargetScreenVertexIndex(), vEnd ).getInterpolatedScreenVertexIndex();
			final boolean endSelected = e.isSelected();
			current.getEdges().add( current.getEdgePool().create( eCurrent ).init(
					e.getDataEdgeId(),
					sourceIndex,
					targetIndex,
					endSelected,
					e.getColor() ) );
			if ( idToStartEdge.get( e.getDataEdgeId(), eStart ) != null )
			{
				// changing selection state?
				if ( endSelected != eStart.isSelected() )
				{
					eCurrent.setTransition( endSelected ? SELECTING : DESELECTING );
					eCurrent.setInterpolationCompletionRatio( accelRatio );
				}
			}
			else
			{
				// appearing?
				eCurrent.setTransition( APPEAR );
				eCurrent.setInterpolationCompletionRatio( accelRatio );
			}
		}
		for ( final ScreenEdge e : start.getEdges() )
		{
			if ( idToEndEdge.get( e.getDataEdgeId(), eEnd ) == null )
			{
				final int sourceIndex = start.getVertices().get( e.getSourceScreenVertexIndex(), vStart )
						.getInterpolatedScreenVertexIndex();
				if ( sourceIndex >= current.getVertices().size() )
					continue;
				// No matching current vertex. Don't paint.

				final int targetIndex = start.getVertices().get( e.getTargetScreenVertexIndex(), vStart )
						.getInterpolatedScreenVertexIndex();
				if ( targetIndex >= current.getVertices().size() )
					continue;

				final boolean endSelected = e.isSelected();
				current.getEdges().add( current.getEdgePool().create( eCurrent ).init(
						e.getDataEdgeId(),
						sourceIndex,
						targetIndex,
						endSelected,
						e.getColor() ) );
				eCurrent.setTransition( DISAPPEAR );
				eCurrent.setInterpolationCompletionRatio( accelRatio );
			}
		}

		// Interpolate screenTransform
		// ===========================
		ScreenTransform startTransform = start.screenTransform();
		if ( incrementalStartTransform != null )
			startTransform = incrementalStartTransform.concatenate( start.screenTransform() );
		current.screenTransform().interpolate( startTransform, end.screenTransform(), accelRatio );

		// clean up
		current.getVertexPool().releaseRef( vCurrent );
		start.getVertexPool().releaseRef( vStart );
		end.getVertexPool().releaseRef( vEnd );
		current.getEdgePool().releaseRef( eCurrent );
		start.getEdgePool().releaseRef( eStart );
		end.getEdgePool().releaseRef( eEnd );
	}

	private void interpolate( final ScreenVertex vStart, final ScreenVertex vEnd, final double ratio,
			final ScreenVertex vCurrent )
	{
		vCurrent.setDataVertexId( vEnd.getDataVertexId() );
		vCurrent.setLabel( vEnd.getLabel() );
		final boolean endSelected = vEnd.isSelected();
		vCurrent.setSelected( endSelected );
		vCurrent.setVertexDist( vEnd.getVertexDist() );
		double startX = vStart.getX();
		double startY = vStart.getY();
		if ( incrementalStartTransform != null )
		{
			startX = incrementalStartTransform.layoutToScreenX( startX );
			startY = incrementalStartTransform.layoutToScreenY( startY );
		}
		vCurrent.setX( ratio * vEnd.getX() + ( 1 - ratio ) * startX );
		vCurrent.setY( ratio * vEnd.getY() + ( 1 - ratio ) * startY );
		vCurrent.setTransition(
				( vStart.isSelected() == endSelected )
						? NONE
						: ( endSelected
								? SELECTING
								: DESELECTING ) );
		vCurrent.setColor( vEnd.getColor() );
		vCurrent.setInterpolationCompletionRatio( ratio );
		vEnd.setInterpolatedScreenVertexIndex( vCurrent.getInternalPoolIndex() );
		vStart.setInterpolatedScreenVertexIndex( vCurrent.getInternalPoolIndex() );
	}

	private void disappear( final ScreenVertex vStart, final double ratio, final ScreenVertex vCurrent )
	{
		vCurrent.setDataVertexId( -1 );
		vCurrent.setLabel( vStart.getLabel() );
		vCurrent.setSelected( vStart.isSelected() );
		vCurrent.setVertexDist( vStart.getVertexDist() );
		double startX = vStart.getX();
		double startY = vStart.getY();
		if ( incrementalStartTransform != null )
		{
			startX = incrementalStartTransform.layoutToScreenX( startX );
			startY = incrementalStartTransform.layoutToScreenY( startY );
		}
		vCurrent.setX( startX );
		vCurrent.setY( startY );
		vCurrent.setTransition( DISAPPEAR );
		vCurrent.setInterpolationCompletionRatio( ratio );
		vCurrent.setColor( vStart.getColor() );
		vStart.setInterpolatedScreenVertexIndex( vCurrent.getInternalPoolIndex() );
	}

	private void appear( final ScreenVertex vEnd, final double ratio, final ScreenVertex vCurrent )
	{
		vCurrent.setDataVertexId( vEnd.getDataVertexId() );
		vCurrent.setLabel( vEnd.getLabel() );
		vCurrent.setSelected( vEnd.isSelected() );
		vCurrent.setVertexDist( vEnd.getVertexDist() );
		vCurrent.setX( vEnd.getX() );
		vCurrent.setY( vEnd.getY() );
		vCurrent.setTransition( APPEAR );
		vCurrent.setInterpolationCompletionRatio( ratio );
		vCurrent.setColor( vEnd.getColor() );
		vEnd.setInterpolatedScreenVertexIndex( vCurrent.getInternalPoolIndex() );
	}
}
