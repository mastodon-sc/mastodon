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
package org.mastodon.views.grapher.datagraph;

import org.mastodon.RefPool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.BooleanAttribute;
import org.mastodon.pool.attributes.ByteAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.pool.attributes.IntAttribute;
import org.mastodon.properties.ObjPropertyMap;

public class ScreenVertexPool extends Pool< ScreenVertex, ByteMappedElement >
{
	final RefPool< DataVertex > dataVertexPool;

	final IndexAttribute< ScreenVertex > origVertex = new IndexAttribute<>( ScreenVertex.layout.origVertex, this );

	final DoubleAttribute< ScreenVertex > xOffset = new DoubleAttribute<>( ScreenVertex.layout.xOffset, this );

	final DoubleAttribute< ScreenVertex > yOffset = new DoubleAttribute<>( ScreenVertex.layout.yOffset, this );

	final DoubleAttribute< ScreenVertex > vertexDist = new DoubleAttribute<>( ScreenVertex.layout.vertexDist, this );

	final BooleanAttribute< ScreenVertex > selected = new BooleanAttribute<>( ScreenVertex.layout.selected, this );

	final ByteAttribute< ScreenVertex > transition = new ByteAttribute<>( ScreenVertex.layout.transition, this );

	final IndexAttribute< ScreenVertex > ipScreenVertex =
			new IndexAttribute<>( ScreenVertex.layout.ipScreenVertex, this );

	final DoubleAttribute< ScreenVertex > ipRatio = new DoubleAttribute<>( ScreenVertex.layout.ipRatio, this );

	final ObjPropertyMap< ScreenVertex, String > label = new ObjPropertyMap<>( this );

	final IntAttribute< ScreenVertex > color = new IntAttribute<>( ScreenVertex.layout.color, this );

	public ScreenVertexPool( final int initialCapacity, final RefPool< DataVertex > dataVertexPool )
	{
		super( initialCapacity, ScreenVertex.layout, ScreenVertex.class,
				SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		this.dataVertexPool = dataVertexPool;
	}

	@Override
	protected ScreenVertex createEmptyRef()
	{
		return new ScreenVertex( this );
	}

	@Override
	public ScreenVertex create( final ScreenVertex vertex )
	{
		return super.create( vertex );
	}

	@Override
	public void delete( final ScreenVertex vertex )
	{
		super.delete( vertex );
	}
}
