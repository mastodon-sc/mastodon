package org.mastodon.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

/**
 * Helper class to discover and hold {@link FeatureSpec}s.
 */
public class FeatureSpecsService extends AbstractService
{
	@Parameter
	private PluginService pluginService;

	private final List< FeatureSpec< ?, ? > > specs = new ArrayList<>();
	private final Map< Class< ? >, List< FeatureSpec< ?, ? > > > targetToSpecs = new HashMap<>();
	private final Map< String, FeatureSpec< ?, ? > > keyToSpec = new HashMap<>();
	private final Map< Class< ? extends Feature< ? > > , FeatureSpec< ?, ? > > featureClassToSpec = new HashMap<>();

	@Override
	public void initialize()
	{
		clear();
		discover();
	}

	private void clear()
	{
		specs.clear();
		targetToSpecs.clear();
		keyToSpec.clear();
		featureClassToSpec.clear();
	}

	/**
	 * Add all {@link FeatureSpec}s that are found by the {@code PluginService} to the index.
	 */
	private void discover()
	{
		for ( final FeatureSpec< ?, ? > spec : pluginService.createInstancesOfType( FeatureSpec.class ) )
			add( spec );
	}

	/**
	 * Add a {@link FeatureSpec} to the index.
	 *
	 * @throws IllegalArgumentException if a {@link FeatureSpec} with the same name as {@code spec} is already present
	 */
	private void add( final FeatureSpec< ?, ? > spec ) throws IllegalArgumentException
	{
		if ( keyToSpec.containsKey( spec.getKey() ) )
			throw new IllegalArgumentException( "Trying to add " + spec + ". A feature with that name is already present." );
		specs.add( spec );
		targetToSpecs.computeIfAbsent( spec.getTargetClass(), k -> new ArrayList<>() ).add( spec );
		keyToSpec.put( spec.getKey(), spec );
		featureClassToSpec.put( spec.getFeatureClass(), spec );
	}

	public FeatureSpec< ?, ? > getSpec( final String key )
	{
		return keyToSpec.get( key );
	}

	public FeatureSpec< ?, ? > getSpec( final Class< ? extends Feature< ? > > featureClass )
	{
		return featureClassToSpec.get( featureClass );
	}

	@SuppressWarnings( "unchecked" )
	public < T > List< FeatureSpec< ?, T > > getSpecs( final Class< T > target )
	{
		final List< ? > list = targetToSpecs.get( target );
		return list == null
				? Collections.emptyList()
				: Collections.unmodifiableList( ( List< FeatureSpec< ?, T > > ) list );
	}
}
