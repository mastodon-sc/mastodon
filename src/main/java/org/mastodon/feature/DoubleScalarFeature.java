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
public class DoubleScalarFeature< O > implements Feature< O >
{
	private final FeatureSpec< DoubleScalarFeature< O >, O > spec;

	private final FeatureProjection< O > projection;

	final DoublePropertyMap< O > values;

	/**
	 * Creates a new scalar double feature instance.
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
	public DoubleScalarFeature( final String key, final String info, final Dimension dimension, final String units, final RefPool< O > pool )
	{
		this( key, info, dimension, units, new DoublePropertyMap<>( pool, Double.NaN ), pool.getRefClass() );
	}

	/**
	 * Only used for deserialization.
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
	 * @param map
	 *            the values to store in this feature.
	 * @param targetClass
	 *            the target class of this feature.
	 */
	protected DoubleScalarFeature( final String key, final String info, final Dimension dimension, final String units, final DoublePropertyMap< O > map, final Class< O > targetClass )
	{
		final FeatureProjectionSpec projectionSpec = new FeatureProjectionSpec( key, dimension );
		this.spec = new MyFeatureSpec<>( key, info, targetClass, projectionSpec );
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
		return values.getDouble( o );
	}

	public void set( final O o, final double value )
	{
		values.set( o, value );
	}

	public void clear( final O o )
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

	private static final class MyFeatureSpec< T > extends FeatureSpec< DoubleScalarFeature< T >, T >
	{
		@SuppressWarnings( "unchecked" )
		public MyFeatureSpec( final String key, final String info, final Class< T > targetClass, final FeatureProjectionSpec projectionSpec )
		{
			super( key, info, ( Class< DoubleScalarFeature< T > > ) ( Class< ? > ) DoubleScalarFeature.class, targetClass, Multiplicity.SINGLE, projectionSpec );
		}
	}
}
