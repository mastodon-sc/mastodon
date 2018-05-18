package org.mastodon.revised.model.feature;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
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
	private final DoublePropertyMap< O > propertyMap;

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
	 */
	public DoubleScalarFeature( final String key, final Class< O > targetClass, final DoublePropertyMap< O > propertyMap )
	{
		this.key = key;
		this.targetClass = targetClass;
		this.propertyMap = propertyMap;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public Map< String, FeatureProjection< O > > getProjections()
	{
		return Collections.singletonMap( key, FeatureProjectors.project( propertyMap ) );
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

	@Override
	public void serialize( final File file, final ObjectToFileIdMap< O > idmap ) throws IOException
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(
						new FileOutputStream( file ), 1024 * 1024 ) ))
		{
			final DoublePropertyMapSerializer< O > serializer = new DoublePropertyMapSerializer<>( propertyMap );
			serializer.writePropertyMap( idmap, oos );
		}
	}
}
