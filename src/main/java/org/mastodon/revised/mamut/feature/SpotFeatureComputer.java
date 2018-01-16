package org.mastodon.revised.mamut.feature;

import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

/**
 * Marker interface for MaMuT feature computers to sort between different
 * targets.
 *
 * @author Jean-Yves Tinevez
 */
public interface SpotFeatureComputer< M extends PropertyMap< Spot, ? > > extends FeatureComputer< Spot, M, Model >
{}
