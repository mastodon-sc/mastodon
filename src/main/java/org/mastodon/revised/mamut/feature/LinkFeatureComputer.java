package org.mastodon.revised.mamut.feature;

import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;

/**
 * Marker interface for MaMuT feature computers to sort between different
 * targets.
 *
 * @author Jean-Yves Tinevez
 */
public interface LinkFeatureComputer< M extends PropertyMap< Link, ? > > extends FeatureComputer< Link, M, Model >
{}
