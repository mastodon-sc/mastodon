package org.mastodon.mamut.feature.branch;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

public class BranchNSpotsFeature implements Feature< BranchLink >
{
	public static final String KEY = "Branch N spots";

	private static final String INFO_STRING = "Returns the number of spots in a branch.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec SPEC = new Spec();

	final IntPropertyMap< BranchLink > map;

	private final IntFeatureProjection< BranchLink > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchNSpotsFeature, BranchLink >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					BranchNSpotsFeature.class,
					BranchLink.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	BranchNSpotsFeature( final IntPropertyMap< BranchLink > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	public int get( final BranchLink branch )
	{
		return map.getInt( branch );
	}

	@Override
	public FeatureProjection< BranchLink > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< BranchLink > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final BranchLink spot )
	{
		map.remove( spot );
	}
}
