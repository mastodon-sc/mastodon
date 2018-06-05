package org.mastodon.revised.model.feature;

import org.mastodon.properties.DoublePropertyMap;

public class WritableDoubleScalarFeature< O > extends DoubleScalarFeature< O > implements WritableFeature< O, Double >
{

	public WritableDoubleScalarFeature( final String key, final Class< O > targetClass, final DoublePropertyMap< O > propertyMap, final String units )
	{
		super( key, targetClass, propertyMap, units );
	}

	@Override
	public void set( final O o, final Double v )
	{
		if ( v == null )
			propertyMap.remove( o );
		else
			propertyMap.set( o, v.doubleValue() );
	}

	public void setValue( final O o, final double v )
	{
		propertyMap.set( o, v );
	}
}
