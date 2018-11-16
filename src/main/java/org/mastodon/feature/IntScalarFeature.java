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
public class IntScalarFeature< O > implements Feature< O >
{
	private final FeatureSpec< IntScalarFeature< O >, O > spec;

	private final FeatureProjection< O > projection;

	private final IntPropertyMap< O > values;

	/**
	 * Creates a new scalar integer feature instance.
	 *
	 * @param key
	 *            the feature unique key. Must be unique within the application
	 *            scope.
	 * @param info
	 *            the feature info text.
	 * @param dimension
	 *            the dimension of the quantity of this scalar feature.
	 * @param units
	 *            the projection units.
	 * @param pool
	 *            the pool of objects on which to define the feature.
	 */
	public IntScalarFeature( final String key, final String info, final Dimension dimension, final String units, final RefPool< O > pool )
	{
		FeatureProjectionSpec projectionSpec = new FeatureProjectionSpec( key, dimension );
		this.spec = new MyFeatureSpec<>( key, info, pool.getRefClass(), projectionSpec );
		this.values = new IntPropertyMap<>( pool, Integer.MIN_VALUE );
		this.projection = FeatureProjections.project( key( projectionSpec ), values, units );
	}

	@Override
	public FeatureProjection< O > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjectionKey > projectionKeys()
	{
		return Collections.singleton( projection.getKey() );
	}

	@Override
	public FeatureSpec< ? extends Feature< O >, O > getSpec()
	{
		return spec;
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

	public void clear( final O o )
	{
		values.remove( o );
	}

	private static final class MyFeatureSpec< T > extends FeatureSpec< IntScalarFeature< T >, T >
	{
		@SuppressWarnings( "unchecked" )
		public MyFeatureSpec( final String key, final String info, final Class< T > targetClass, final FeatureProjectionSpec projectionSpec )
		{
			super( key, info, ( Class< IntScalarFeature< T > > ) ( Class< ? > ) IntScalarFeature.class, targetClass, Multiplicity.SINGLE, projectionSpec );
		}
	}
}
