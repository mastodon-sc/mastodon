package org.mastodon.revised.model.feature;

import org.mastodon.properties.IntPropertyMap;

public class WritableIntScalarFeature< O > extends IntScalarFeature< O > implements WritableFeature< O, Integer >
{

	public WritableIntScalarFeature( final String key, final Class< O > targetClass, final IntPropertyMap< O > propertyMap, final String units )
	{
		super( key, targetClass, propertyMap, units );
	}

	@Override
	public void set( final O o, final Integer v )
	{
		if ( v == null )
			propertyMap.remove( o );
		else
			propertyMap.set( o, v.intValue() );
	}

	public void setValue( final O o, final int v )
	{
		propertyMap.set( o, v );
	}
}
