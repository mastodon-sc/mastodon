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
package org.mastodon.views.bdv.overlay.shapes.ellipsoid;

import org.mastodon.views.bdv.overlay.shapes.BdvOverlayProperties;

/**
 * Interface for generic 'ellipsoid overlay properties' classes to be used to
 * paint an ellipsoid-shaped graph in a BDV view.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the model vertex type
 * @param <E>
 *            the model edge type
 */
public interface BdvEllipsoidOverlayProperties< V, E > extends BdvOverlayProperties< V, E >
{

	/**
	 * Gets the covariance matrix of the ellipsoid representing the given
	 * vertex.
	 *
	 * @param v
	 *            the vertex.
	 * @param mat
	 *            a 2D array to store the covariance matrix (<code>double[ 3 ][
	 *            3 ]</code>).
	 */
	void getCovariance( V v, double[][] mat );

	/**
	 * Sets the covariance matrix of the ellipsoid representing the given
	 * vertex.
	 *
	 * @param v
	 *            the vertex.
	 * @param mat
	 *            a 2D array containing the covariance matrix
	 *            (<code>double[ 3 ][
	 *            3 ]</code>).
	 */
	void setCovariance( V v, double[][] mat );

	/**
	 * Creates a new vertex with a default spherical shape of given radius.
	 *
	 * @param v
	 *            the vertex ref to initialize.
	 * @param timepoint
	 *            the timepoint of the vertex.
	 * @param position
	 *            the position of the vertex.
	 * @param radius
	 *            the sphere radius.
	 * @return the initialized vertex.
	 */
	V initVertex( V v, int timepoint, double[] position, double radius );

	/**
	 * Creates a new vertex with a default ellipsoid shape of given covariance.
	 *
	 * @param v
	 *            the vertex ref to initialize.
	 * @param timepoint
	 *            the timepoint of the vertex.
	 * @param position
	 *            the position of the vertex.
	 * @param covariance
	 *            the covariance matrix defining the ellipsoid shape
	 *            (<code>double[ 3 ][
	 *            3 ]</code>).
	 * @return the initialized vertex.
	 */
	V initVertex( V v, int timepoint, double[] position, double[][] covariance );
}
