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
package org.mastodon.views.bdv.overlay.shapes;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.spatial.VertexPositionListener;
import org.mastodon.views.bdv.overlay.OverlayGraph;

/**
 * Interface for generic 'overlay properties' classes to be used to paint a
 * graph in a BDV view..
 * <p>
 * Concrete implementations of this interface provide access to vertex and edge
 * properties of the model graph needed to render the graph in the BDV overlay,
 * using the {@link OverlayGraph} view graph.
 * <p>
 * This interface however is a base interface that does not make any assumption
 * on the shape of the vertices. More specific interfaces must extend this one
 * to provide shape-specific properties. Typically, this interface can be
 * extended with methods that are specific to a shape, and used to:
 * <ul>
 * <li>create new vertices, according to a default shape. E.g.
 *
 * <pre>
 * V initVertex( V v, int timepoint, double[] position, ... ); // shape specific params
 * </pre>
 *
 * <li>get and set shape-specific properties, e.g. get or set the covariance for
 * ellipsoid shapes:
 *
 * <pre>
 * void getCovariance( V v, double[][] mat );
 *
 * void setCovariance( V v, double[][] mat );
 * </pre>
 * </ul>
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the model vertex type.
 * @param <E>
 *            the model edge type.
 */
public interface BdvOverlayProperties< V, E >
{
	public void localize( V v, final double[] position );

	public double getDoublePosition( V v, final int d );

	public void setPosition( V v, double position, int d );

	public void setPosition( V v, final double[] position );

	public String getLabel( V v );

	public void setLabel( V v, String label );

	public double getBoundingSphereRadiusSquared( V v );

	public int getTimepoint( V v );

	public double getMaxBoundingSphereRadiusSquared( int timepoint );

	public V addVertex( V ref );

	public E addEdge( V source, V target, E ref );

	public E insertEdge( V source, final int sourceOutIndex, V target, final int targetInIndex, final E ref );

	public E initEdge( E e );

	public void removeEdge( E e );

	public void removeVertex( V v );

	public void notifyGraphChanged();

	public ReentrantReadWriteLock getLock();

	/**
	 * Register a listener that will be notified when vertex labels change.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered,
	 */
	public boolean addVertexLabelListener( final PropertyChangeListener< V > listener );

	/**
	 * Removes the specified vertex label listener from the set of listeners.
	 *
	 * @param vertexLabelListener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 */
	public boolean removeVertexLabelListener( PropertyChangeListener< V > vertexLabelListener );

	/**
	 * Register a {@link VertexPositionListener} that will be notified when
	 * position of vertices are changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addVertexPositionListener( final VertexPositionListener< V > listener );

	/**
	 * Removes the specified {@link VertexPositionListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeVertexPositionListener( final VertexPositionListener< V > listener );

}
