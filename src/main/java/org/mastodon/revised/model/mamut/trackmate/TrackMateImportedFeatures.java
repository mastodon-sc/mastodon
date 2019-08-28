package org.mastodon.revised.model.mamut.trackmate;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

public abstract class TrackMateImportedFeatures< O > implements Feature< O >
{

	private final Map< FeatureProjectionKey, FeatureProjection< O > > projectionMap;

	final Map< FeatureProjectionKey, DoublePropertyMap< O > > doublePropertyMapMap;

	final Map< FeatureProjectionKey, IntPropertyMap< O > > intPropertyMapMap;

	public TrackMateImportedFeatures()
	{
		this.projectionMap = new HashMap<>();
		this.doublePropertyMapMap = new HashMap<>();
		this.intPropertyMapMap = new HashMap<>();
	}

	void store( final String key, final Dimension dimension, final String units, final DoublePropertyMap< O > values )
	{
		final FeatureProjectionSpec spec = new FeatureProjectionSpec( key, dimension );
		final FeatureProjectionKey fpkey = FeatureProjectionKey.key( spec );
		projectionMap.put( fpkey, FeatureProjections.project( fpkey, values, units ) );
		doublePropertyMapMap.put( fpkey, values );
	}

	void store( final String key, final Dimension dimension, final String units, final IntPropertyMap< O > values )
	{
		final FeatureProjectionSpec spec = new FeatureProjectionSpec( key, dimension );
		final FeatureProjectionKey fpkey = FeatureProjectionKey.key( spec );
		projectionMap.put( fpkey, FeatureProjections.project( fpkey, values, units ) );
		intPropertyMapMap.put( fpkey, values );
	}

	@Override
	public FeatureProjection< O > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< O > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}
}
