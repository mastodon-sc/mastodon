package org.mastodon.feature;

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

	/**
	 * The feature unique key.
	 */
	private final String key;

	private final FeatureProjection< O > projection;

	private final Class< O > targetClass;

	private final DoublePropertyMap< O > values;

	/**
	 * Creates a new scalar double feature instance.
	 *
	 * @param key
	 *            the feature unique key. Must be unique within the application
	 *            scope.
	 * @param pool
	 *            the pool of objects on which to define the feature.
	 * @param units
	 *            the projection units.
	 */
	public DoubleScalarFeature( final String key, final String units, final RefPool< O > pool )
	{
		this.key = key;
		this.values = new DoublePropertyMap<>( pool, Double.NaN );
		this.projection = FeatureProjections.project( values, units );
		this.targetClass = pool.getRefClass();
	}

	@Override
	public FeatureProjection< O > project( final String projectionKey )
	{
		return ( key.equals( projectionKey ) )
				? projection
				: null;
	}

	@Override
	public String[] projectionKeys()
	{
		return new String[] { key };
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

	/**
	 * Returns an undiscoverable {@link FeatureSpec} for this feature.
	 *
	 * @param info
	 *            the feature info text.
	 * @param dimension
	 *            the dimension of the quantity of this scalar feature.
	 * @return a {@link FeatureSpec}.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public FeatureSpec< DoubleScalarFeature< O >, O > createFeatureSpec( final String info, final Dimension dimension )
	{
		return new MyFeatureSpec( key, info, dimension, DoubleScalarFeature.class, targetClass );
	}

	private static final class MyFeatureSpec< F extends DoubleScalarFeature< T >, T > extends FeatureSpec< F, T >
	{

		public MyFeatureSpec( final String key, final String info, final Dimension dimension, final Class< F > featureClass, final Class< T > targetClass )
		{
			super( key, info, featureClass, targetClass, Multiplicity.SINGLE, new FeatureProjectionSpec( key, dimension ) );
		}

	}

}
