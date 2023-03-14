package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.branch.exampleGraph.AbstractExampleGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import javax.annotation.Nonnull;

public class FeatureComputerTestUtils
{
	@Nonnull
	public static FeatureProjection< BranchSpot > getBranchSpotFeatureProjection( @Nonnull Context context, @Nonnull AbstractExampleGraph exampleGraph, @Nonnull FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > spec, @Nonnull FeatureProjectionSpec featureProjectionSpec )
	{
		final MamutFeatureComputerService featureComputerService = getMamutFeatureComputerService( context, exampleGraph );
		Feature< BranchSpot > feature = ( Feature< BranchSpot > ) featureComputerService.compute( spec ).get( spec );

		return feature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}

	@Nonnull
	public static FeatureProjection< Spot > getSpotFeatureProjection( @Nonnull Context context, @Nonnull AbstractExampleGraph exampleGraph, @Nonnull FeatureSpec< ? extends Feature< Spot >, Spot > spec, @Nonnull FeatureProjectionSpec featureProjectionSpec )
	{
		final MamutFeatureComputerService featureComputerService = getMamutFeatureComputerService( context, exampleGraph );
		featureComputerService.setSharedBdvData( sharedBigDataViewerData );
		Feature< Spot > feature = ( Feature< Spot > ) featureComputerService.compute( spec ).get( spec );

		return feature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}

	@Nonnull
	private static MamutFeatureComputerService getMamutFeatureComputerService( @Nonnull Context context, @Nonnull AbstractExampleGraph exampleGraph )
	{
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( exampleGraph.getModel() );
		return featureComputerService;
	}
}
