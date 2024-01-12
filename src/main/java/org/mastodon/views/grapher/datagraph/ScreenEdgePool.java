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

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.BooleanAttribute;
import org.mastodon.pool.attributes.ByteAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.pool.attributes.IntAttribute;

public class ScreenEdgePool extends Pool< ScreenEdge, ByteMappedElement >
{
	final IndexAttribute< ScreenEdge > origEdge = new IndexAttribute<>( ScreenEdge.layout.origEdge, this );

	final IndexAttribute< ScreenEdge > sourceScreenVertex =
			new IndexAttribute<>( ScreenEdge.layout.sourceScreenVertex, this );

	final IndexAttribute< ScreenEdge > targetScreenVertex =
			new IndexAttribute<>( ScreenEdge.layout.targetScreenVertex, this );

	final BooleanAttribute< ScreenEdge > selected = new BooleanAttribute<>( ScreenEdge.layout.selected, this );

	final ByteAttribute< ScreenEdge > transition = new ByteAttribute<>( ScreenEdge.layout.transition, this );

	final DoubleAttribute< ScreenEdge > ipRatio = new DoubleAttribute<>( ScreenEdge.layout.ipRatio, this );

	final IntAttribute< ScreenEdge > color = new IntAttribute<>( ScreenEdge.layout.color, this );

	public ScreenEdgePool( final int initialCapacity )
	{
		super( initialCapacity, ScreenEdge.layout, ScreenEdge.class,
				SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
	}

	@Override
	protected ScreenEdge createEmptyRef()
	{
		return new ScreenEdge( this );
	}

	@Override
	public ScreenEdge create( final ScreenEdge edge )
	{
		return super.create( edge );
	}

	@Override
	public void delete( final ScreenEdge edge )
	{
		super.delete( edge );
	}
}
