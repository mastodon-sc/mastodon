package org.mastodon.feature;

/**
 * Marker interface for projections that actually only have <code>int</code>
 * values exposed as <code>double</code>s.
 * <p>
 * This is only useful in application where the view of features can be nicely
 * formatted to reflect that their projections are integer valued.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O> the type of objects for which the feature is defined.
 */
public interface IntFeatureProjection< O > extends FeatureProjection< O >
{}
