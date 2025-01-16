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
/**
 * Base classes for Mastodon features and their computation.
 * <p>
 * 'Features' are the way Mastodon associate values to an object in a model. For
 * instance, we use a feature to store the mean fluorescence intensity in a
 * cell.
 * <p>
 * A {@link org.mastodon.feature.Feature} is a read-only quantity calculated for
 * a model at runtime. They store values that are defined for a model but are
 * not required for the model consistency. They are typically defined for a
 * specific application by the user; they are optional and their selection and
 * calculation are triggered at runtime. A Feature is defined for a specific
 * target in the model (vertex, edge, graph, etc.) specified as a type parameter
 * of this class.
 * <p>
 * Because a feature is not necessarily a scalar, the
 * {@link org.mastodon.feature.FeatureComputer} interface offers to decompose a
 * value into its projection, with the
 * {@link org.mastodon.feature.FeatureProjection} interface. Feature projections
 * are scalar and real values that can decompose or project a feature on a real
 * axis. How they are defined is up to the person that created the feature
 * computer. For instance, a feature that gives the velocity vector of a link
 * will reasonably expose 3 projections, one for each of the X, Y and Z
 * component of the vector. Or maybe the polar angle, azimuthal angle and norm
 * of this vector. Or maybe the 6 projections since they can be calculated on
 * the fly. A complex feature value will reasonably expose 2 projections, one
 * for the real part, one of the imaginary part. Etc.
 *
 * <p>
 * Numerical feature values are calculated by feature computers, derived from
 * the {@link org.mastodon.feature.FeatureComputer} interface.
 */
package org.mastodon.feature;
