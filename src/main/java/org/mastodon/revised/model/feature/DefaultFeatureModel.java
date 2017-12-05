package org.mastodon.revised.model.feature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.revised.model.AbstractModel;

/**
 * Default feature model.
 *
 * @author Jean-Yves Tinevez
 * @param <AM>
 *            the type of the model over which the features stored in this
 *            feature model are defined.
 */
public class DefaultFeatureModel< AM extends AbstractModel< ?, ?, ? > > implements FeatureModel< AM >
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
		if ( null == featureSet )
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

	@SuppressWarnings( "unchecked" )
	@Override
	public void saveRaw( final File baseFolder, final Map< String, FeatureSerializer< ?, ?, AM > > serializers, final AM model, final GraphToFileIdMap< ?, ? > graphToFileIdMap )
	{
		final File featureFolder = new File( baseFolder, FEATURE_FOLDER );
		if (!featureFolder.exists())
		{
			final boolean created = featureFolder.mkdirs();
			if (!created)
			{
				System.err.println( "Could not create folder to save features in: " + featureFolder );
				return;
			}
		}
		if (!featureFolder.isDirectory())
		{
			System.err.println( "Target folder to save feature in is not a directory: " + featureFolder );
			return;
		}

		for ( final String featureKey : keyToFeature.keySet() )
		{
			final FeatureSerializer< ?, ?, AM > featureSerializer = serializers.get( featureKey );
			if ( null == featureSerializer )
			{
				System.err.println( "Cannot find a serializer to write feature " + featureKey + ". Skipped." );
				continue;
			}
			final File featureFilePath = makeFeatureFilePath( baseFolder, featureKey );
			try
			{
				@SuppressWarnings( "rawtypes" )
				final Feature feature = keyToFeature.get( featureKey );
				featureSerializer.serialize( feature, featureFilePath, model, graphToFileIdMap );
			}
			catch ( final IOException e )
			{
				System.err.println( "Could not serialize feature " + featureKey + " to file " + featureFilePath + ":\n" + e.getMessage() );
			}
		}
	}

	@Override
	public void loadRaw( final File baseFolder, final Map< String, FeatureSerializer< ?, ?, AM > > serializers, final AM model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap )
	{
		clear();
		final File featureFolder = new File( baseFolder, FEATURE_FOLDER );

		final File[] featureFiles = featureFolder.listFiles( ( pathname ) -> pathname.getName().endsWith( RAW_EXTENSION ) );
		if (null == featureFiles)
			return;

		for ( final File featureFile : featureFiles )
		{
			final String featureKey = getFeatureKeyFromFileName( featureFile );
			if ( null == featureKey )
			{
				System.err.println( "Cannot retrieve feature key from file name " + featureFile + ". Skipped." );
				continue;
			}
			final FeatureSerializer< ?, ?, AM > featureSerializer = serializers.get( featureKey );
			if ( null == featureSerializer )
			{
				System.err.println( "Cannot find a serializer to read feature " + featureKey + ". Skipped." );
				continue;
			}
			try
			{
				final Feature< ?, ? > feature = featureSerializer.deserialize( featureFile, model, fileIdToGraphMap );
				declareFeature( feature );
			}
			catch ( final IOException e )
			{
				System.err.println( "Could not deserialize feature " + featureKey + " from file " + featureFile + ":\n" + e.getMessage() );
			}
		}
	}

	private static final String getFeatureKeyFromFileName( final File featureFile )
	{
		final String name = featureFile.getName();
		final int dotIndex = name.lastIndexOf( '.' );
		return name.substring( 0, dotIndex );
	}

	private static final File makeFeatureFilePath( final File baseFolder, final String featureKey )
	{
		return new File( baseFolder, new File(FEATURE_FOLDER, featureKey + RAW_EXTENSION ).getPath() );
	}


	private static final String FEATURE_FOLDER = "features";

	private static final String RAW_EXTENSION = ".raw";

}
