package org.mastodon.revised.model.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default feature model.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices.
 * @param <E>
 *            the type of edges.
 */
public class DefaultFeatureModel implements FeatureModel
{

	private final Map< Class< ? >, Set< Feature< ?, ?, ? > > > featureSets;

	private final Map<String, Feature< ?, ?, ? >> featureKeys;

	/**
	 * Creates a new, empty, feature model.
	 */
	public DefaultFeatureModel()
	{
		featureSets = new HashMap<>();
		featureKeys = new HashMap<>();
	}

	@Override
	public void declareFeature( final Feature< ?, ?, ? > feature )
	{
		// Features.
		final Class< ? > clazz = feature.getTargetClass();
		Set< Feature< ?, ?, ? > > featureSet = featureSets.get( clazz );
		if (null == featureSet)
		{
			featureSet = new HashSet<>();
			featureSets.put( clazz, featureSet );
		}
		featureSet.add( feature );

		// Feature keys.
		featureKeys.put( feature.getKey(), feature );
	}

	@Override
	public void clear()
	{
		featureSets.clear();
		featureKeys.clear();
	}

	@Override
	public Set< Feature< ?, ?, ? > > getFeatureSet( final Class< ? > clazz )
	{
		return featureSets.get( clazz );
	}

	@Override
	public Feature< ?, ?, ? > getFeature( final String key )
	{
		return featureKeys.get( key );
	}
}
