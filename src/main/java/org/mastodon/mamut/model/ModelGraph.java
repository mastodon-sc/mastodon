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
package org.mastodon.mamut.model;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.io.properties.StringPropertyMapSerializer;
import org.mastodon.model.AbstractModelGraph;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.properties.PropertyChangeListener;

public class ModelGraph extends AbstractModelGraph< ModelGraph, SpotPool, LinkPool, Spot, Link, ByteMappedElement >
{
	public ModelGraph()
	{
		this( 1000 );
	}

	public ModelGraph( final int initialCapacity )
	{
		super( new LinkPool( initialCapacity, new SpotPool( initialCapacity ) ) );

		vertexPropertySerializers.put( "label", new StringPropertyMapSerializer<>( vertexPool.label ) );
	}

	SpotPool getVertexPool()
	{
		return vertexPool;
	}

	LinkPool getEdgePool()
	{
		return edgePool;
	}

	GraphIdBimap< Spot, Link > idmap()
	{
		return idmap;
	}

	/**
	 * Register a {@link PropertyChangeListener} that will be notified when
	 * a {@code Spot}s label is changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		return vertexPool.label.propertyChangeListeners().add( listener );
	}
	
	/**
	 * Register a {@link PropertyChangeListener} that will be notified when
	 * a {@code Spot}s covariance is changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addCovarianceLabelListener( final PropertyChangeListener< Spot > listener )
	{
		return vertexPool.covariance.propertyChangeListeners().add( listener );
	}

	/**
	 * Removes the specified {@link PropertyChangeListener} from the set of
	 * VertexLabel listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		return vertexPool.label.propertyChangeListeners().remove( listener );
	}
	
	/**
	 * Removes the specified {@link PropertyChangeListener} from the set of
	 * vertex covariance listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeVertexCovarianceListener( final PropertyChangeListener< Spot > listener )
	{
		return vertexPool.covariance.propertyChangeListeners().remove( listener );
	}
}
}
