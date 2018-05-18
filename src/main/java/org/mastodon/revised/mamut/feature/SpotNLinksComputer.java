package org.mastodon.revised.mamut.feature;

import java.util.Collections;
import java.util.Set;

import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.IntScalarFeature;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotFeatureComputer.class, name = "Spot N links" )
public class SpotNLinksComputer extends SpotIntScalarFeatureComputer
{

	public static final String KEY = "Spot N links";

	public SpotNLinksComputer()
	{
		super( KEY );
	}

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
	public IntScalarFeature< Spot > compute( final Model model )
	{
		final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
		final IntPropertyMap< Spot > pm = new IntPropertyMap<>( vertices, -1 );

		for ( final Spot spot : vertices )
			pm.set( spot, spot.edges().size() );

		return new IntScalarFeature<>( KEY, Spot.class, pm );
	}
}
