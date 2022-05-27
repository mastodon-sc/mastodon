package org.mastodon.mamut.feature.branch;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

public class BranchDisplacementDurationFeature implements Feature< BranchLink >
{
	public static final String KEY = "Branch duration and displacement";

	private static final String INFO_STRING = "The displacement and duration of a branch.";

	public static final FeatureProjectionSpec DISPLACEMENT_PROJECTION_SPEC = new FeatureProjectionSpec( "Displacement", Dimension.LENGTH );

	public static final FeatureProjectionSpec DURATION_PROJECTION_SPEC = new FeatureProjectionSpec( "Duration", Dimension.NONE );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchDisplacementDurationFeature, BranchLink >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					BranchDisplacementDurationFeature.class,
					BranchLink.class,
					Multiplicity.SINGLE,
					DISPLACEMENT_PROJECTION_SPEC,
					DURATION_PROJECTION_SPEC );
		}
	}

	private final Map< FeatureProjectionKey, FeatureProjection< BranchLink > > projectionMap;

	final DoublePropertyMap< BranchLink > dispMap;

	final DoublePropertyMap< BranchLink > durMap;

	BranchDisplacementDurationFeature( final DoublePropertyMap< BranchLink > dispMap, final DoublePropertyMap< BranchLink > durMap, final String lengthUnits )
	{
		this.dispMap = dispMap;
		this.durMap = durMap;
		this.projectionMap = new LinkedHashMap<>( 2 );
		projectionMap.put( key( DISPLACEMENT_PROJECTION_SPEC ), FeatureProjections.project( key( DISPLACEMENT_PROJECTION_SPEC ), dispMap, lengthUnits ) );
		projectionMap.put( key( DURATION_PROJECTION_SPEC ), FeatureProjections.project( key( DURATION_PROJECTION_SPEC ), durMap, Dimension.NONE_UNITS ) );
	}

	public double getDuration( final BranchLink branch )
	{
		return durMap.get( branch );
	}

	public double getDisplacement( final BranchLink branch )
	{
		return dispMap.get( branch );
	}

	@Override
	public FeatureProjection< BranchLink > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< BranchLink > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final BranchLink branch )
	{
		dispMap.remove( branch );
		durMap.remove( branch );
	}
}
