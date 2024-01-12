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
package org.mastodon.feature;

import java.util.Set;

/**
 * Interface for Mastodon features.
 * <p>
 * A feature is a read-only quantity calculated for a model at runtime. They
 * store values that are defined for a model but are not required for the model
 * consistency. They are typically defined for a specific application by the
 * user; they are optional and their selection and calculation are triggered at
 * runtime.
 * <p>
 * A Feature is defined for a specific target in the model (vertex, edge, graph,
 * etc.) specified as a type parameter of this class.
 * <p>
 *
 * @param <T>
 *            target the type of the object this feature is defined for.
 */
public interface Feature< T >
{
	/**
	 * Get the projection with the specified {@link FeatureProjectionKey}.
	 *
	 * @param key
	 *            the requested projection
	 * @return the specified projection, or {@code null} if the projection is
	 *         not available.
	 */
	public FeatureProjection< T > project( final FeatureProjectionKey key );

	/**
	 * Get all {@link FeatureProjection}s that this feature currently provides.
	 *
	 * @return set of all projections.
	 */
	public Set< FeatureProjection< T > > projections();

	public FeatureSpec< ? extends Feature< T >, T > getSpec();

	/**
	 * Invalidates the value of this feature for the specified object.
	 * <p>
	 * This method is used to invalidate a feature value when an object has been
	 * modified under it.
	 *
	 * @param obj
	 *            the object to remove.
	 */
	public void invalidate( T obj );
}
