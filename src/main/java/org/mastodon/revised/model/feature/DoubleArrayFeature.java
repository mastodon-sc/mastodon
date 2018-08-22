package org.mastodon.revised.model.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mastodon.properties.DoublePropertyMap;

/**
 * A feature implementation for array where elements are {@code double}s.
 * <p>
 * Concretely, the arrays elements are stored in {@code length}-number of
 * {@link DoublePropertyMap}. This favors situation where consumers want to
 * access the same element in the array for many objects, <i>e.g.</i> to iterate
 * over all the values of a single projection.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 *            the type of objects for which the feature is defined.
 */
public class DoubleArrayFeature< O > implements Feature< O, double[] >
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
	 * The list of property maps that store feature values.
	 */
	private final List< DoublePropertyMap< O > > propertyMaps;

	/**
	 * The names of individual projection keys.
	 */
	private final List< String > projectionKeys;

	private final Map< String, FeatureProjection< O > > projections;

	private final int length;

	/**
	 * Creates an {@link DoubleArrayFeature}.
	 *
	 * @param key
	 *            the key of the feature.
	 * @param targetClass
	 *            the target class of the feature.
	 * @param propertyMaps
	 *            the list of property maps that makes this feature.
	 * @param projectionKeys
	 *            the list of projection keys.
	 * @param projectionUnits
	 *            the list of projection units.
	 */
	public DoubleArrayFeature(
			final String key,
			final Class< O > targetClass,
			final List< DoublePropertyMap< O > > propertyMaps,
			final List< String > projectionKeys,
			final List< String > projectionUnits )
	{
		this.length = propertyMaps.size();
		this.key = key;
		this.targetClass = targetClass;
		this.propertyMaps = propertyMaps;
		this.projectionKeys = projectionKeys;
		final HashMap< String, FeatureProjection< O > > pm = new HashMap<>( length );
		for ( int i = 0; i < length; i++ )
			pm.put( projectionKeys.get( i ), FeatureUtil.project( propertyMaps.get( i ), projectionUnits.get( i ) ) );
		this.projections = Collections.unmodifiableMap( pm );
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public Map< String, FeatureProjection< O > > getProjections()
	{
		return projections;
	}

	@Override
	public Class< O > getTargetClass()
	{
		return targetClass;
	}

	/**
	 * Returns the number of elements defined in this feature for each object.
	 *
	 * @return the length.
	 */
	public int getLength()
	{
		return length;
	}

	@Override
	public double[] get( final O o )
	{
		return get( o, new double[ length ] );
	}

	@Override
	public double[] get( final O o, final double[] ref )
	{
		for ( int i = 0; i < length; i++ )
			ref[ i ] = propertyMaps.get( i ).get( o );
		return ref;
	}

	/**
	 * Returns the value of element {@code index} for the specified target
	 * object.
	 *
	 * @param o
	 *            the target object.
	 * @param index
	 *            the index of the element to query.
	 * @return the value.
	 */
	public double get( final O o, final int index )
	{
		return propertyMaps.get( index ).get( o );
	}

	/**
	 * Returns the name of the element at the specified index in this feature.
	 *
	 * @param index
	 *            the index of the element to query.
	 * @return the name of the element.
	 */
	public String getName( final int index )
	{
		return projectionKeys.get( index );
	}

	@Override
	public boolean isSet( final O o )
	{
		if ( propertyMaps.isEmpty() )
			return false;
		return propertyMaps.get( 0 ).isSet( o );
	}
}
