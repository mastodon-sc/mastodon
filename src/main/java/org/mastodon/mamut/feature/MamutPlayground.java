package org.mastodon.mamut.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MamutPlayground
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		final Context context = new Context();
		final MamutProject project = new MamutProjectIO().load( "../TrackMate3/samples/mamutproject.mastodon" );
		final WindowManager windowManager = new WindowManager( context );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();

		System.out.println( "\n\n\n___________________________________\nData loaded.\n" );

		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features =
				featureComputerService.compute( SpotGaussFilteredIntensityFeature.SPEC );

		final FeatureSpecsService specsService = context.getService( FeatureSpecsService.class );
		printForTarget( Spot.class, model.getGraph().vertices(), specsService, features );
		printForTarget( Link.class, model.getGraph().edges(), specsService, features );
	}

	private static < T > void printForTarget( final Class< T > target, final Collection< T > collection, final FeatureSpecsService specsService, final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel )
	{
		System.out.println( "\n\nFeatures that have " + target.getSimpleName() + " as target:" );
		final List< FeatureSpec< ?, T > > specs = specsService.getSpecs( target );
		for ( final FeatureSpec< ?, T > spec : specs )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< T > feature = ( Feature< T > ) featureModel.get( spec );
			if (null == feature)
			{
				System.out.println( "\n - Feature " + spec.getKey() + " is not computed." );
				continue;
			}
			final Set< FeatureProjection< T > > projections = feature.projections();
			if ( null == projections )
				continue;
			System.out.println( "\n - Feature " + spec.getKey() +". Has " + projections.size() + " projections:" );
			for ( final FeatureProjection< T > projection : projections )
			{
				System.out.println( "   - Projection " + projection.getKey() );
				for ( final T obj : collection )
					System.out.println( "       - " + obj + ": " + projection.value( obj ) );
			}
		}
	}
}
