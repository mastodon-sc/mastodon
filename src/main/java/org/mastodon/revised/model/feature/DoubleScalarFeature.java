package org.mastodon.revised.model.feature;

import java.util.Collections;
import java.util.Map;
import org.mastodon.properties.DoublePropertyMap;

/**
 * Feature made of a double scalar value.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 */
public class DoubleScalarFeature< O > implements Feature< O, Double >
{

	/**
	 * The feature unique key.
	 */
	private final String key;

	/**
	 * The class of the feature target.
	 */
	private final Class< O > targetClass;

	/**
	 * The property map that stores feature values.
	 */
	protected final DoublePropertyMap< O > propertyMap;

	private final Map< String, FeatureProjection< O > > proj;

	private final String units;

	/**
	 * Creates a new immutable feature instance.
	 *
	 * @param key
	 *            The feature unique key. Must be unique within the application
	 *            scope.
	 * @param targetClass
	 *            The class of the feature target.
	 * @param propertyMap
	 *            The feature property map.
	 * @param units
	 *            the projection units.
	 */
	public DoubleScalarFeature( final String key, final Class< O > targetClass, final DoublePropertyMap< O > propertyMap, final String units )
	{
		this.key = key;
		this.targetClass = targetClass;
		this.propertyMap = propertyMap;
		this.units = units;
		this.proj = Collections.singletonMap( key, FeatureUtil.project( propertyMap, units ) );
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public Map< String, FeatureProjection< O > > getProjections()
	{
		return proj;
	}

	@Override
	public Class< O > getTargetClass()
	{
		return targetClass;
	}

	@Override
	public Double get( final O o, final Double ref )
	{
		return propertyMap.get( o );
	}

	@Override
	public Double get( final O o )
	{
		return propertyMap.get( o );
	}

	@Override
	public boolean isSet( final O o )
	{
		return propertyMap.isSet( o );
	}

	public double getValue( final O o )
	{
		return propertyMap.getDouble( o );
	}
}
