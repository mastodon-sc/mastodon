package org.mastodon.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;

/**
 * Collection of utilities related to manipulating a {@link FeatureModel}.
 */
public class FeatureUtils
{

	/**
	 * Returns the map of feature specs to feature stored in the specified
	 * feature model, for features that are defined on objects of the specified
	 * class.
	 * 
	 * @param <O>
	 *            the type of objects whose features we want to extract.
	 * @param featureModel
	 *            the feature model.
	 * @param clazz
	 *            the class of objects whose features we want to extract.
	 * @return a new map.
	 */
	public static final < O > Map< FeatureSpec< ?, O >, Feature< O > > collectFeatureMap( final FeatureModel featureModel, final Class< O > clazz )
	{
		final Set< FeatureSpec< ?, ? > > featureSpecs = featureModel.getFeatureSpecs().stream()
				.filter( ( fs ) -> fs.getTargetClass().isAssignableFrom( clazz ) )
				.collect( Collectors.toSet() );
		final Map< FeatureSpec< ?, O >, Feature< O > > featureMap = new HashMap<>();
		for ( final FeatureSpec< ?, ? > fs : featureSpecs )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< O > feature = ( Feature< O > ) featureModel.getFeature( fs );
			@SuppressWarnings( "unchecked" )
			final FeatureSpec< ?, O > featureSpec = ( FeatureSpec< ?, O > ) fs;
			featureMap.put( featureSpec, feature );
		}
		return featureMap;
	}

	private FeatureUtils()
	{}

}
