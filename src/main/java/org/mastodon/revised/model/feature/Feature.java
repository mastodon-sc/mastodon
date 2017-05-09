package org.mastodon.revised.model.feature;

import org.mastodon.properties.PropertyMap;

public interface Feature< O, T > extends PropertyMap< O, T >
{

	/**
	 * Returns the key of this feature.
	 * 
	 * @return the feature key.
	 */
	public String getKey();

}
