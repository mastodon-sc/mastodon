package org.mastodon.revised.model.feature;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.PropertyMapSerializer;
import org.mastodon.io.properties.PropertyMapSerializers;
import org.mastodon.io.properties.RawPropertyIO;

/**
 * Default feature model.
 *
 * @author Jean-Yves Tinevez
 */
public class DefaultFeatureModel implements FeatureModel
{

	private final Map< Class< ? >, Set< Feature< ?, ? > > > targetClassToFeatures;

	private final Map< String, Feature< ?, ? > > keyToFeature;

	/**
	 * Creates a new, empty, feature model.
	 */
	public DefaultFeatureModel()
	{
		targetClassToFeatures = new HashMap<>();
		keyToFeature = new HashMap<>();
	}

	@Override
	public void declareFeature( final Feature< ?, ? > feature )
	{
		// Features.
		final Class< ? > clazz = feature.getTargetClass();
		Set< Feature< ?, ? > > featureSet = targetClassToFeatures.get( clazz );
		if (null == featureSet)
		{
			featureSet = new HashSet<>();
			targetClassToFeatures.put( clazz, featureSet );
		}
		featureSet.add( feature );

		// Feature keys.
		keyToFeature.put( feature.getKey(), feature );
	}

	@Override
	public void clear()
	{
		targetClassToFeatures.clear();
		keyToFeature.clear();
	}

	@Override
	public Set< Feature< ?, ? > > getFeatureSet( final Class< ? > targetClass )
	{
		return targetClassToFeatures.get( targetClass );
	}

	@Override
	public Feature< ?, ? > getFeature( final String key )
	{
		return keyToFeature.get( key );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public void writeRaw( final ObjectOutputStream oos, final Map< Class< ? >, ObjectToFileIdMap< ? > > fileIdMaps ) throws IOException
	{
		// Collect feature to write and order them by target class.
		final Map<Class< ? >, PropertyMapSerializers< ? > > serializerMap = new HashMap<>();
		for ( final String featureKey : keyToFeature.keySet() )
		{
			final Feature< ?, ? > feature = keyToFeature.get( featureKey );
			final Class< ? > targetClass = feature.getTargetClass();

			PropertyMapSerializers< ? > propertyMapSerializers = serializerMap.get( targetClass );
			if ( null == propertyMapSerializers )
			{
				propertyMapSerializers = new PropertyMapSerializers<>();
				serializerMap.put( targetClass, propertyMapSerializers );
			}

			final PropertyMapSerializer serializer = feature.getSerializer();
			propertyMapSerializers.put( featureKey, serializer );
		}

		// Write several blocks of features, one by target class.
		// Order imposed by the key of fileIdMaps.
		for ( final Class< ? > targetClass : fileIdMaps.keySet() )
		{
			final PropertyMapSerializers serializers = serializerMap.get( targetClass );
			if ( null == serializers )
				continue;

			final ObjectToFileIdMap idmap = fileIdMaps.get( targetClass );
			if (null == idmap)
			{
				System.err.println( "Error while writing features. Do not have a file id map for target class " + targetClass );
				continue;
			}
			RawPropertyIO.writePropertyMaps( idmap, serializers, oos );
		}
	}

}
