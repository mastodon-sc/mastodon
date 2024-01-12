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
package org.mastodon.model;

import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertexPool;
import org.mastodon.pool.MappedElement;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.attributes.IntAttribute;
import org.mastodon.pool.attributes.RealPointAttribute;

import net.imglib2.EuclideanSpace;

public abstract class AbstractSpotPool<
		V extends AbstractSpot< V, E, ?, T, G >,
		E extends AbstractListenableEdge< E, V, ?, T >,
		T extends MappedElement,
		G extends AbstractModelGraph< ?, ?, ?, V, E, T > >
		extends AbstractListenableVertexPool< V, E, T > implements EuclideanSpace
{
	public static class AbstractSpotLayout extends AbstractVertexLayout
	{
		final DoubleArrayField position;

		final IntField timepoint;

		public AbstractSpotLayout( final int numDimensions )
		{
			position = doubleArrayField( numDimensions );
			timepoint = intField();
		}
	}

	final AbstractSpotLayout layout;

	protected final RealPointAttribute< V > position;

	protected final IntAttribute< V > timepoint;

	protected G modelGraph;

	public AbstractSpotPool(
			final int initialCapacity,
			final AbstractSpotLayout layout,
			final Class< V > vertexClass,
			final MemPool.Factory< T > memPoolFactory )
	{
		super( initialCapacity, layout, vertexClass, memPoolFactory );
		this.layout = layout;
		position = new RealPointAttribute<>( layout.position, this );
		timepoint = new IntAttribute<>( layout.timepoint, this );
	}

	@Override
	public int numDimensions()
	{
		return layout.position.numElements();
	}

	void linkModelGraph( G modelGraph )
	{
		this.modelGraph = modelGraph;
	}

	/*
	 * Debug helper. Uncomment to do additional verifyInitialized() whenever a
	 * Ref is pointed to a vertex.
	 */
	//	@Override
	//	public V getObject( final int index, final V obj )
	//	{
	//		final V v = super.getObject( index, obj );
	//		v.verifyInitialized();
	//		return v;
	//	}
}
