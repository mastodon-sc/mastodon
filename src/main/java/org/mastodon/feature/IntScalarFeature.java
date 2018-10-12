package org.mastodon.feature;

import org.mastodon.collection.RefIntMap;

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

	/**
	 * Creates a new immutable feature instance.
	 *
	 * @param key
	 *            The feature unique key. Must be unique within the application
	 *            scope.
	 * @param targetClass
	 *            The class of the feature target.
	 * @param values
	 *            The feature property map.
	 * @param units
	 *            the projection units.
	 */
	public IntScalarFeature( final String key, final RefIntMap< O > values, final Class< O > targetClass, final String units )
	{
		this.key = key;
		this.targetClass = targetClass;
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
