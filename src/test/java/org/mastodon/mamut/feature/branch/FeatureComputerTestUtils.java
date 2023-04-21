package org.mastodon.mamut.feature.branch;

import net.imglib2.util.Cast;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.branch.exampleGraph.AbstractExampleGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import javax.annotation.Nonnull;

public class FeatureComputerTestUtils
{

	@Nonnull
	public static Feature< BranchSpot > getBranchSpotFeature( @Nonnull Context context,
			@Nonnull AbstractExampleGraph exampleGraph, @Nonnull FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > spec )
	{
		final MamutFeatureComputerService featureComputerService = getMamutFeatureComputerService( context, exampleGraph );
		return Cast.unchecked( featureComputerService.compute( true, spec ).get( spec ) );
	}

	@Nonnull
	public static Feature< Spot > getSpotFeature( @Nonnull Context context, @Nonnull AbstractExampleGraph exampleGraph,
			@Nonnull FeatureSpec< ? extends Feature< Spot >, Spot > spec )
	{
		final MamutFeatureComputerService featureComputerService = getMamutFeatureComputerService( context, exampleGraph );
		return Cast.unchecked( featureComputerService.compute( true, spec ).get( spec ) );
	}

	@Nonnull
	public static FeatureProjection< BranchSpot > getBranchSpotFeatureProjection( @Nonnull Context context,
			@Nonnull AbstractExampleGraph exampleGraph, @Nonnull FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > spec,
			@Nonnull FeatureProjectionSpec featureProjectionSpec )
	{
		Feature< BranchSpot > feature = getBranchSpotFeature( context, exampleGraph, spec );
		return feature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}

	@Nonnull
	private static MamutFeatureComputerService getMamutFeatureComputerService( @Nonnull Context context,
			@Nonnull AbstractExampleGraph exampleGraph )
	{
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( exampleGraph.getModel() );
		return featureComputerService;
	}
}
