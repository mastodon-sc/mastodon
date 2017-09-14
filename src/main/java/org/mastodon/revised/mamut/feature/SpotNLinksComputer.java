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
public class SpotNLinksComputer implements SpotFeatureComputer
{

	private static final String KEY = "Spot N links";

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public Feature< Spot, Integer, IntPropertyMap< Spot > > compute( final Model model )
	{
		final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
		final IntPropertyMap< Spot > pm = new IntPropertyMap<>( vertices, -1 );

		for ( final Spot spot : vertices )
			pm.set( spot, spot.edges().size() );

		final Map< String, FeatureProjection< Spot > > projections = Collections.singletonMap( KEY, FeatureProjectors.project( pm ) );
		final Feature< Spot, Integer, IntPropertyMap< Spot > > feature = new Feature<>( KEY, Spot.class, pm, projections );
		return feature;
	}
}
