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
package org.mastodon.ui.coloring.feature;

import org.mastodon.feature.FeatureProjection;

/**
 * Provides {@link FeatureProjection}s for {@link FeatureProjectionId} to be
 * used in {@link FeatureColorMode}s.
 *
 * @author Tobias Pietzsch
 */
public interface Projections
{
	/**
	 * Get a {@code FeatureProjection} (of any target type) for the specified
	 * {@code FeatureProjectionId}.
	 *
	 * @param id
	 *            requested id
	 * @return {@code FeatureProjection} with {@code id}, if it exists.
	 *         {@code null} otherwise.
	 */
	public FeatureProjection< ? > getFeatureProjection( final FeatureProjectionId id );

	/**
	 * Get a {@code FeatureProjection} of the given target type for the
	 * specified {@code FeatureProjectionId}.
	 *
	 * @param id
	 *            requested id
	 * @param target
	 *            target type
	 * @param <T>
	 *            target type
	 * @return {@code FeatureProjection} with {@code id} and {@code target}
	 *         type, if it exists. {@code null} otherwise.
	 */
	public < T > FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id, Class< T > target );
}
