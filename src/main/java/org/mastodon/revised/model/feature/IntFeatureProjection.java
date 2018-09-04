package org.mastodon.revised.model.feature;

/**
 * Marker interface for projections that actually only have <code>int</code>
 * values exposed as <code>double</code>s.
 * <p>
 * This is only useful in application where the view of features can be nicely
 * formatted to reflect that their projections are integer valued.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <T> the type of objects for which the feature is defined.
 */
public interface IntFeatureProjection< T > extends FeatureProjection< T >
{}
