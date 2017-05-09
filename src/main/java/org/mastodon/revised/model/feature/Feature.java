package org.mastodon.revised.model.feature;

import org.mastodon.properties.PropertyMap;

public class Feature< O, T, K extends PropertyMap< O, T > >
{

	private final String key;

	private final K propertyMap;

	public Feature( final String key, final K properytMap )
	{
		this.key = key;
		this.propertyMap = properytMap;
	}

	/**
	 * Returns the key of this feature.
	 * 
	 * @return the feature key.
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * Returns the property map storing the values of this feature.
	 * 
	 * @return the property map.
	 */
	public K getPropertyMap()
	{
		return propertyMap;
	}
}
