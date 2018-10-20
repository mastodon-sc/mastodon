package org.mastodon.feature;

import org.mastodon.RefPool;
import org.mastodon.properties.IntPropertyMap;

/**
 * Feature made of a int scalar value.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 */
public class IntScalarFeature< O > implements Feature< O >
{

	/**
	 * The feature unique key.
	 */
	private final String key;

	private final IntFeatureProjection< O > projection;

	private final Class< O > targetClass;

	private final IntPropertyMap< O > values;

	/**
	 * Creates a new scalar integer feature instance.
	 *
	 * @param key
	 *            the feature unique key. Must be unique within the application
	 *            scope.
	 * @param units
	 *            the projection units.
	 * @param pool
	 *            the pool of objects on which to define the feature.
	 */
	public IntScalarFeature( final String key, final String units, final RefPool< O > pool )
	{
		this.key = key;
		this.values = new IntPropertyMap<>( pool, Integer.MIN_VALUE );
		this.targetClass = pool.getRefClass();
		this.projection = FeatureProjections.project( values, units );
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
		return new MyFeatureSpec( key, info, dimension, IntScalarFeature.class, targetClass );
	}

	private static final class MyFeatureSpec< F extends DoubleScalarFeature< T >, T > extends FeatureSpec< F, T >
	{

		public MyFeatureSpec( final String key, final String info, final Dimension dimension, final Class< F > featureClass, final Class< T > targetClass )
		{
			super( key, info, featureClass, targetClass, FeatureProjectionSpec.standard( key, dimension ) );
		}
	}
}
