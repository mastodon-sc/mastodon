package org.mastodon.mamut.feature;

import net.imglib2.util.Cast;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

public class FeatureComputerTestUtils
{

	public static < T > Feature< T > getFeature( Context context, Model model, FeatureSpec< ? extends Feature< T >, T > spec )
	{
		final MamutFeatureComputerService featureComputerService = getMamutFeatureComputerService( context, model );
		return Cast.unchecked( featureComputerService.compute( true, spec ).get( spec ) );
	}

	public static < T > FeatureProjection< T > getFeatureProjection( Context context, Model model,
			FeatureSpec< ? extends Feature< T >, T > spec, FeatureProjectionSpec featureProjectionSpec )
	{
		Feature< T > feature = getFeature( context, model, spec );
		return feature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}

	private static MamutFeatureComputerService getMamutFeatureComputerService( Context context, Model model )
	{
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		return featureComputerService;
	}
}
