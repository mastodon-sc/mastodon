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
package org.mastodon.feature.ui;

import java.util.Collection;

import org.mastodon.feature.Multiplicity;
import org.mastodon.ui.coloring.feature.TargetType;

import gnu.trove.list.TIntList;

/**
 * Lists the features and projections (as their respective ({@code String}
 * keys), as well as available source indices (for features with multiplicity).
 *
 * @author Tobias Pietzsch
 */
public interface AvailableFeatureProjections
{
	/**
	 * Get ordered list of source indices available for selection as feature
	 * source.
	 *
	 * @return ordered list of source indices
	 */
	public TIntList getSourceIndices();

	/**
	 * Get ordered list of feature keys for the given {@code targetType}.
	 *
	 * @param targetType
	 *            whether vertex or edge features should be returned
	 * @return ordered list of feature keys
	 */
	public Collection< String > featureKeys( final TargetType targetType );

	/**
	 * Get ordered list of projection keys available for the feature with the
	 * specified {@code targetType} and {@code String} key.
	 *
	 * @param targetType
	 *            whether a vertex or edge feature is queried
	 * @param featureKey
	 *            key of the feature to query
	 * @return ordered list of projection keys
	 */
	public Collection< String > projectionKeys( final TargetType targetType, final String featureKey );

	/**
	 * Get the multiplicity of the feature with the specified {@code targetType}
	 * and {@code String} key.
	 *
	 * @param targetType
	 *            whether a vertex or edge feature is queried
	 * @param featureKey
	 *            key of the feature to query
	 * @return multiplicity of specified feature
	 */
	public Multiplicity multiplicity( final TargetType targetType, final String featureKey );
}
