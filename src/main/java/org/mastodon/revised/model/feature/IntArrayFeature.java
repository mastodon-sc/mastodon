package org.mastodon.revised.model.feature;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.properties.IntPropertyMap;

/**
 * A feature implementation for array where elements are <code>int</code>s.
 * <p>
 * Concretely, the arrays elements are stored in <code>length</code>-number of
 * {@link IntPropertyMap}. This favors situation where consumers want to
 * access the same element in the array for many objects, <i>e.g.</i> to iterate
 * over all the values of a single projection.

 * @author Jean-Yves Tinevez
 *
 * @param <O>
 *            the type of objects for which the feature is defined.
 */
public class IntArrayFeature< O > implements Feature< O, int[] >
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
	private final List< IntPropertyMap< O > > propertyMaps;

	/**
	 * The names of individual projection keys.
	 */
	private final List< String > projectionKeys;

	private final Map< String, FeatureProjection< O > > projections;

	private final int length;

	public IntArrayFeature( final String key, final Class< O > targetClass, final List< IntPropertyMap< O > > propertyMaps, final List< String > projectionKeys )
	{
		this.length = propertyMaps.size();
		this.key = key;
		this.targetClass = targetClass;
		this.propertyMaps = propertyMaps;
		this.projectionKeys = projectionKeys;
		final HashMap< String, IntFeatureProjection< O > > pm = new HashMap<>( length );
		for ( int i = 0; i < length; i++ )
			pm.put( projectionKeys.get( i ), FeatureProjectors.project( propertyMaps.get( i ) ) );
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
	public int[] get( final O o )
	{
		return get( o, new int[ length ] );
	}

	@Override
	public int[] get( final O o, final int[] ref )
	{
		for ( int i = 0; i < length; i++ )
			ref[ i ] = propertyMaps.get( i ).get( o );
		return ref;
	}

	/**
	 * Returns the value of element <code>index</code> for the specified target
	 * object.
	 *
	 * @param o
	 *            the target object.
	 * @param index
	 *            the index of the element to query.
	 * @return the value.
	 */
	public int get( final O o, final int index )
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

	@Override
	public void serialize( final File file, final ObjectToFileIdMap< O > idmap ) throws IOException
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(
						new FileOutputStream( file ), 1024 * 1024 ) ))
		{
			// NUMBER OF ELEMENTS
			oos.writeInt( length );
			for ( int i = 0; i < length; i++ )
			{
				// NAME OF ENTRIES
				oos.writeUTF( projectionKeys.get( i ) );
				// NUMBER OF ENTRIES and ENTRIES
				final IntPropertyMapSerializer< O > serializer = new IntPropertyMapSerializer<>( propertyMaps.get( i ) );
				serializer.writePropertyMap( idmap, oos );
			}
		}
	}
}
