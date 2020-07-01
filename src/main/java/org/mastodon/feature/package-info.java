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
