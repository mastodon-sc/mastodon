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

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.PoolObjectLayout;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.views.trackscheme.ScreenVertexRange.ScreenVertexRangePool;

/**
 * Layouted dense vertex area.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenVertexRange extends PoolObject< ScreenVertexRange, ScreenVertexRangePool, ByteMappedElement >
{
	public static class ScreenVertexRangeLayout extends PoolObjectLayout
	{
		final DoubleField minX = doubleField();
		final DoubleField maxX = doubleField();
		final DoubleField minY = doubleField();
		final DoubleField maxY = doubleField();
	}

	public static ScreenVertexRangeLayout layout = new ScreenVertexRangeLayout();

	public static class ScreenVertexRangePool extends Pool< ScreenVertexRange, ByteMappedElement >
	{
		final DoubleAttribute< ScreenVertexRange > minX = new DoubleAttribute<>( layout.minX, this );
		final DoubleAttribute< ScreenVertexRange > maxX = new DoubleAttribute<>( layout.maxX, this );
		final DoubleAttribute< ScreenVertexRange > minY = new DoubleAttribute<>( layout.minY, this );
		final DoubleAttribute< ScreenVertexRange > maxY = new DoubleAttribute<>( layout.maxY, this );

		public ScreenVertexRangePool( final int initialCapacity )
		{
			super( initialCapacity, layout, ScreenVertexRange.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		}

		@Override
		protected ScreenVertexRange createEmptyRef()
		{
			return new ScreenVertexRange( this );
		}

		@Override
		public ScreenVertexRange create( final ScreenVertexRange vertex )
		{
			return super.create( vertex );
		}

		@Override
		public void delete( final ScreenVertexRange vertex )
		{
			super.delete( vertex );
		}
	}

	protected ScreenVertexRange( final ScreenVertexRangePool pool )
	{
		super( pool );
	}

	public ScreenVertexRange init( final double minX, final double maxX, final double minY, final double maxY )
	{
		setMinX( minX );
		setMaxX( maxX );
		setMinY( minY );
		setMaxY( maxY );
		return this;
	}

	public double getMinX()
	{
		return pool.minX.get( this );
	}

	protected void setMinX( final double minX )
	{
		pool.minX.setQuiet( this, minX );
	}

	public double getMaxX()
	{
		return pool.maxX.get( this );
	}

	protected void setMaxX( final double maxX )
	{
		pool.maxX.setQuiet( this, maxX );
	}

	public double getMinY()
	{
		return pool.minY.get( this );
	}

	protected void setMinY( final double minY )
	{
		pool.minY.setQuiet( this, minY );
	}

	public double getMaxY()
	{
		return pool.maxY.get( this );
	}

	protected void setMaxY( final double maxY )
	{
		pool.maxY.setQuiet( this, maxY );
	}

	@Override
	protected void setToUninitializedState()
	{}

	/**
	 * Set all fields as in specified {@link ScreenVertexRange} (which is
	 * possibly from another pool).
	 *
	 * @param r
	 * @return {@code this}.
	 */
	ScreenVertexRange cloneFrom( final ScreenVertexRange r )
	{
		setMinX( r.getMinX() );
		setMaxX( r.getMaxX() );
		setMinY( r.getMinY() );
		setMaxY( r.getMaxY() );
		return this;
	}
}
