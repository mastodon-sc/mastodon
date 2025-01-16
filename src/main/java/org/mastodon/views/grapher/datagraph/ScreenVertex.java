/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import net.imglib2.RealLocalizable;

/**
 * Layouted vertex.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenVertex extends PoolObject< ScreenVertex, ScreenVertexPool, ByteMappedElement >
		implements RealLocalizable
{
	public static class ScreenVertexLayout extends PoolObjectLayout
	{
		final IndexField origVertex = indexField();

		final DoubleField xOffset = doubleField();

		final DoubleField yOffset = doubleField();

		final DoubleField vertexDist = doubleField();

		final BooleanField selected = booleanField();

		final ByteField transition = byteField();

		final IndexField ipScreenVertex = indexField();

		final DoubleField ipRatio = doubleField();

		final IntField color = intField();
	}

	public static ScreenVertexLayout layout = new ScreenVertexLayout();

	protected ScreenVertex( final ScreenVertexPool pool )
	{
		super( pool );
	}

	public ScreenVertex init(
			final int id,
			final String label,
			final double x,
			final double y,
			final boolean selected,
			final int color )
	{
		setDataVertexId( id );
		setLabel( label );
		setX( x );
		setY( y );
		setSelected( selected );
		setTransition( NONE );
		setColor( color );
		return this;
	}

	/**
	 * Get the internal pool index of the associated data vertex.
	 *
	 * @return the internal pool index of the associated data vertex.
	 */
	public int getDataVertexId()
	{
		return pool.origVertex.get( this );
	}

	protected void setDataVertexId( final int id )
	{
		pool.origVertex.setQuiet( this, id );
	}

	/**
	 * Get the X screen coordinate of the vertex center.
	 *
	 * @return X screen coordinate.
	 */
	public double getX()
	{
		return pool.xOffset.get( this );
	}

	protected void setX( final double x )
	{
		pool.xOffset.setQuiet( this, x );
	}

	/**
	 * Get the Y screen coordinate of the vertex center.
	 *
	 * @return Y screen coordinate.
	 */
	public double getY()
	{
		return pool.yOffset.get( this );
	}

	protected void setY( final double y )
	{
		pool.yOffset.setQuiet( this, y );
	}

	/**
	 * (Estimate of) the distance to the closest vertex on screen.
	 * Center-to-center distance. This is used to determine how large the vertex
	 * should be painted.
	 *
	 * @return distance to the closest vertex on screen.
	 */
	public double getVertexDist()
	{
		return pool.vertexDist.get( this );
	}

	protected void setVertexDist( final double minVertexScreenDist )
	{
		pool.vertexDist.setQuiet( this, minVertexScreenDist );
	}

	/**
	 * Get the label of the vertex. This calls {@link DataVertex#getLabel()} of
	 * the associated data vertex.
	 *
	 * @return label of the vertex.
	 */
	public String getLabel()
	{
		return pool.label.get( this );
	}

	protected void setLabel( final String label )
	{
		pool.label.set( this, label );
	}

	/**
	 * Get the selected state of the vertex.
	 *
	 * @return true, if the vertex is selected.
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
	 * Returns the current transition state for this screen vertex.
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
	 * Get the internal pool index of the interpolated {@link ScreenVertex} for
	 * which this {@link ScreenVertex} is the interpolation target.
	 *
	 * @return internal pool index of the interpolated {@link ScreenVertex}.
	 */
	protected int getInterpolatedScreenVertexIndex()
	{
		return pool.ipScreenVertex.get( this );
	}

	protected void setInterpolatedScreenVertexIndex( final int screenVertexIndex )
	{
		pool.ipScreenVertex.setQuiet( this, screenVertexIndex );
	}

	/**
	 * Returns the interpolation completion ratio of the current transition for
	 * this screen vertex.
	 *
	 * @return the interpolation completion ratio.
	 */
	public double getInterpolationCompletionRatio()
	{
		return pool.ipRatio.get( this );
	}

	/**
	 * Sets the interpolation completion ratio of the current transition for
	 * this screen vertex.
	 *
	 * @param ratio
	 *            the interpolation completion ratio.
	 */
	protected void setInterpolationCompletionRatio( final double ratio )
	{
		pool.ipRatio.setQuiet( this, ratio );
	}

	/**
	 * Returns the color of this vertex (ARGB bytes packed into {@code int}).
	 *
	 * @return the color.
	 */
	public int getColor()
	{
		return pool.color.get( this );
	}

	/**
	 * Set the color of this vertex (ARGB bytes packed into {@code int}).
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
	 * Set all fields as in specified {@link ScreenVertex} (which is possibly
	 * from another pool).
	 * <p>
	 * ONLY USE THIS FOR {@link ScreenEntities#set(ScreenEntities)}!
	 *
	 * @param v
	 *            the vertex to clone parameters from.
	 * @return {@code this}.
	 */
	ScreenVertex cloneFrom( final ScreenVertex v )
	{
		setDataVertexId( v.getDataVertexId() );
		setLabel( v.getLabel() );
		setX( v.getX() );
		setY( v.getY() );
		setVertexDist( v.getVertexDist() );
		setSelected( v.isSelected() );
		setTransition( v.getTransition() );
		setInterpolatedScreenVertexIndex( v.getInterpolatedScreenVertexIndex() );
		setInterpolationCompletionRatio( v.getInterpolationCompletionRatio() );
		setColor( v.getColor() );
		return this;
	}

	@Override
	public String toString()
	{
		return String.format( "ScreenVertex(%d, dvid=%d, \"%s\", (%.2f, %.2f), %s%s)",
				getInternalPoolIndex(),
				getDataVertexId(),
				getLabel(),
				getX(),
				getY(),
				getTransition().toString(),
				isSelected() ? ", selected" : "" );
	}

	@Override
	public int numDimensions()
	{
		return 2;
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return ( d == 0 ) ? pool.xOffset.get( this ) : pool.yOffset.get( this );
	}
}
