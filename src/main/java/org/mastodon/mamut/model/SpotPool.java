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
package org.mastodon.mamut.model;

import org.mastodon.model.AbstractSpotPool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.MultiArrayMemPool;
import org.mastodon.pool.attributes.DoubleArrayAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.properties.Property;

public class SpotPool extends AbstractSpotPool< Spot, Link, ByteMappedElement, ModelGraph >
{
	public static class SpotLayout extends AbstractSpotLayout
	{
		public SpotLayout()
		{
			super( 3 );
		}

		final DoubleArrayField covariance = doubleArrayField( 6 );

		final DoubleField boundingSphereRadiusSqu = doubleField();
	}

	public static final SpotLayout layout = new SpotLayout();

	final DoubleArrayAttribute< Spot > covariance = new DoubleArrayAttribute<>( layout.covariance, this );

	final DoubleAttribute< Spot > boundingSphereRadiusSqu =
			new DoubleAttribute<>( layout.boundingSphereRadiusSqu, this );

	final ObjPropertyMap< Spot, String > label;

	SpotPool( final int initialCapacity )
	{
		super( initialCapacity, layout, Spot.class, MultiArrayMemPool.factory( ByteMappedElementArray.factory ) );
		label = new ObjPropertyMap<>( this );
		registerPropertyMap( label );
	}

	@Override
	protected Spot createEmptyRef()
	{
		return new Spot( this );
	}

	public final Property< Spot > covarianceProperty()
	{
		return covariance;
	}

	public final Property< Spot > boundingSphereRadiusSquProperty()
	{
		return boundingSphereRadiusSqu;
	}

	public final Property< Spot > positionProperty()
	{
		return position;
	}

	public final Property< Spot > labelProperty()
	{
		return label;
	}
}
