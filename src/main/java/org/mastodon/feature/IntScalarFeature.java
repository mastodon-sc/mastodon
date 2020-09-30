package org.mastodon.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.properties.IntPropertyMap;

/**
 * Feature made of an int scalar value.
 * <p>
 * They are not connected to a feature computer and are used to wrap a map that
 * stores static values.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 *            the feature target.
 */
public abstract class IntScalarFeature< O > implements Feature< O >
{

	private final FeatureProjection< O > projection;

	final IntPropertyMap< O > values;

	/**
	 * Creates a new scalar integer feature instance.
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
	public IntScalarFeature( final String key, final Dimension dimension, final String units, final RefPool< O > pool )
	{
		this( key, dimension, units, new IntPropertyMap<>( pool, Integer.MIN_VALUE ) );
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
	protected IntScalarFeature( final String key, final Dimension dimension, final String units, final IntPropertyMap< O > map )
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
		return values.getInt( o );
	}

	public void set( final O o, final int value )
	{
		values.set( o, value );
	}

	@Override
	public void invalidate( final O o )
	{
		values.remove( o );
	}
}
