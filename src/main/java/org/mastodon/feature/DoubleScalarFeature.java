package org.mastodon.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.properties.DoublePropertyMap;

/**
 * Feature made of a double scalar value.
 * <p>
 * They are not connected to a feature computer and are used to wrap a map that
 * stores static values.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 *            the feature target.
 */
public abstract class DoubleScalarFeature< O > implements Feature< O >
{

	private final FeatureProjection< O > projection;

	final DoublePropertyMap< O > values;

	/**
	 * Creates a new scalar double feature instance.
	 *
	 * @param key
	 *            the feature unique key. Must be unique within the application
	 *            scope.
	 * @param dimension
	 *            the dimension of the quantity of this scalar feature.
	 * @param units
	 *            the projection units.
	 * @param pool
	 *            the pool of objects on which to define the feature.
	 */
	public DoubleScalarFeature( final String key, final Dimension dimension, final String units, final RefPool< O > pool )
	{
		this( key, dimension, units, new DoublePropertyMap<>( pool, Double.NaN ) );
	}

	/**
	 * Only used for deserialization.
	 *
	 * @param key
	 *            the feature unique key. Must be unique within the application
	 *            scope.
	 * @param dimension
	 *            the dimension of the quantity of this scalar feature.
	 * @param units
	 *            the projection units.
	 * @param map
	 *            the values to store in this feature.
	 */
	protected DoubleScalarFeature( final String key, final Dimension dimension, final String units, final DoublePropertyMap< O > map )
	{
		final FeatureProjectionSpec projectionSpec = new FeatureProjectionSpec( key, dimension );
		this.values = map;
		this.projection = FeatureProjections.project( key( projectionSpec ), values, units );
	}

	@Override
	public FeatureProjection< O > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< O > > projections()
	{
		return Collections.singleton( projection );
	}

	public boolean isSet( final O o )
	{
		return values.isSet( o );
	}

	public double value( final O o )
	{
		return values.getDouble( o );
	}

	public void set( final O o, final double value )
	{
		values.set( o, value );
	}

	@Override
	public void invalidate( final O o )
	{
		values.remove( o );
	}

	/**
	 * Returns the values of the feature as an array of double values. Changes
	 * to the array of values will not be reflected in the feature nor
	 * vice-versa.
	 *
	 * @return the values of the map as an array of double values.
	 */
	public double[] values()
	{
		return values.getMap().values();
	}
}
