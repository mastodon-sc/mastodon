package org.mastodon.revised.mamut.feature;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureProjectors;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotFeatureComputer.class, name = "Spot N links" )
public class SpotNLinksComputer extends SpotFeatureComputer< Feature< Spot, Integer, IntPropertyMap< Spot > >, Model >
{

	private static final String KEY = "Spot N links";

	private Feature< Spot, Integer, IntPropertyMap< Spot > > feature;

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public void compute( final Model model )
	{
		final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
		final IntPropertyMap< Spot > pm = new IntPropertyMap<>( vertices, -1 );
		this.feature = new Feature<>( KEY, pm );

		for ( final Spot spot : vertices )
			pm.set( spot, spot.edges().size() );
	}

	@Override
	public Feature< Spot, Integer, IntPropertyMap< Spot > > getFeature()
	{
		return feature;
	}

	@Override
	public Map< String, FeatureProjection< Spot > > getProjections()
	{
		return Collections.singletonMap( KEY, FeatureProjectors.project( feature.getPropertyMap() ) );
	}
}