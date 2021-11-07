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
package org.mastodon.views.grapher.datagraph;

import static org.mastodon.views.trackscheme.ScreenVertex.Transition.NONE;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.PoolObjectLayout;
import org.mastodon.views.trackscheme.ScreenVertex.Transition;
import org.mastodon.views.trackscheme.TrackSchemeEdge;

/**
 * Layouted edge.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenEdge extends PoolObject< ScreenEdge, ScreenEdgePool, ByteMappedElement >
{
	public static class ScreenEdgeLayout extends PoolObjectLayout
	{
		final IndexField origEdge = indexField();
		final IndexField sourceScreenVertex = indexField();
		final IndexField targetScreenVertex = indexField();
		final BooleanField selected = booleanField();
		final ByteField transition = byteField();
		final DoubleField ipRatio = doubleField();
		final IntField color = intField();
	}

	public static ScreenEdgeLayout layout = new ScreenEdgeLayout();

	protected ScreenEdge( final ScreenEdgePool pool )
	{
		super( pool );
	}

	public ScreenEdge init(
			final int id,
			final int sourceScreenVertexIndex,
			final int targetScreenVertexIndex,
			final boolean selected,
			final int color )
	{
		setDataEdgeId( id );
		setSourceScreenVertexIndex( sourceScreenVertexIndex );
		setTargetScreenVertexIndex( targetScreenVertexIndex );
		setSelected( selected );
		setTransition( NONE );
		setColor( color );
		return this;
	}

	/**
	 * Returns the current transition state for this screen edge.
	 *
	 * @return the transition state.
	 */
	public Transition getTransition()
	{
		return Transition.values()[ pool.transition.get( this ) ];
	}

	protected void setTransition( final Transition t )
	{
		pool.transition.setQuiet( this, t.toByte() );
	}

	/**
	 * Returns the interpolation completion ratio of the current transition for
	 * this screen edge.
	 *
	 * @return the interpolation completion ratio.
	 */
	public double getInterpolationCompletionRatio()
	{
		return pool.ipRatio.get( this );
	}

	/**
	 * Sets the interpolation completion ratio of the current transition for
	 * this screen edge.
	 *
	 * @param ratio
	 *            the interpolation completion ratio.
	 */
	protected void setInterpolationCompletionRatio( final double ratio )
	{
		pool.ipRatio.setQuiet( this, ratio );
	}

	/**
	 * Get the internal pool index of the associated {@link TrackSchemeEdge}.
	 *
	 * @return the internal pool index of the associated
	 *         {@link TrackSchemeEdge}.
	 */
	public int getDataEdgeId()
	{
		return pool.origEdge.get( this );
	}

	protected void setDataEdgeId( final int id )
	{
		pool.origEdge.setQuiet( this, id );
	}

	/**
	 * Get the index of the source ("from") {@link ScreenVertex} in the screen
	 * vertex list {@link ScreenEntities#getVertices()}. This is at the same
	 * time the internal pool index of the source {@link ScreenVertex}.
	 *
	 * @return internal pool index of the source {@link ScreenVertex}.
	 */
	public int getSourceScreenVertexIndex()
	{
		return pool.sourceScreenVertex.get( this );
	}

	protected void setSourceScreenVertexIndex( final int index )
	{
		pool.sourceScreenVertex.setQuiet( this, index );
	}

	/**
	 * Get the index of the target ("to") {@link ScreenVertex} in the screen
	 * vertex list {@link ScreenEntities#getVertices()}. This is at the same
	 * time the internal pool index of the target {@link ScreenVertex}.
	 *
	 * @return internal pool index of the target {@link ScreenVertex}.
	 */
	public int getTargetScreenVertexIndex()
	{
		return pool.targetScreenVertex.get( this );
	}

	protected void setTargetScreenVertexIndex( final int index )
	{
		pool.targetScreenVertex.setQuiet( this, index );
	}

	/**
	 * Get the selected state of the edge.
	 *
	 * @return true, if the edge is selected.
	 */
	public boolean isSelected()
	{
		return pool.selected.get( this );
	}

	protected void setSelected( final boolean selected )
	{
		pool.selected.setQuiet( this, selected );
	}

	/**
	 * Returns the color of this edge (ARGB bytes packed into {@code int}).
	 *
	 * @return the color.
	 */
	public int getColor()
	{
		return pool.color.get( this );
	}

	/**
	 * Set the color of this edge (ARGB bytes packed into {@code int}).
	 *
	 * @param color
	 *            the color as ARGB bytes packed into {@code int}
	 */
	protected void setColor( final int color )
	{
		pool.color.setQuiet( this, color );
	}

	@Override
	protected void setToUninitializedState()
	{}

	/**
	 * Set all fields as in specified {@link ScreenEdge} (which is possibly
	 * from another pool).
	 * <p>
	 * ONLY USE THIS FOR {@link ScreenEntities#set(ScreenEntities)}!
	 *
	 * @param e
	 * @return {@code this}.
	 */
	ScreenEdge cloneFrom( final ScreenEdge e )
	{
		setDataEdgeId( e.getDataEdgeId() );
		setSourceScreenVertexIndex( e.getSourceScreenVertexIndex() );
		setTargetScreenVertexIndex( e.getTargetScreenVertexIndex() );
		setSelected( e.isSelected() );
		setTransition( e.getTransition() );
		setInterpolationCompletionRatio( e.getInterpolationCompletionRatio() );
		setColor( e.getColor() );
		return this;
	}

// TODO REMOVE? should be covered by base class.
//	@Override
//	public boolean equals( final Object obj )
//	{
//		return obj instanceof ScreenEdge &&
//				access.equals( ( ( ScreenEdge ) obj ).access );
//	}
//
//	@Override
//	public int hashCode()
//	{
//		return access.hashCode();
//	}
}
