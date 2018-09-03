package org.mastodon.mamut.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
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
		final MamutProject project = new MamutProjectIO().load( "../TrackMate3/samples/mamutproject" );
		final WindowManager windowManager = new WindowManager( context );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();
		System.out.println( "\n\n\n___________________________________\nData loaded. " );

		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel =
				featureComputerService.compute( "Link velocity" );

		final FeatureSpecsService specsService = context.getService( FeatureSpecsService.class );
		printForTarget( Spot.class, model.getGraph().vertices(), specsService, featureModel );
		printForTarget( Link.class, model.getGraph().edges(), specsService, featureModel );
	}

	private static < T > void printForTarget( final Class< T > target, final Collection< T > collection, final FeatureSpecsService specsService, final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel )
	{
		System.out.println( "\n\nFeatures that have " + target.getSimpleName() + " as target:" );
		final List< FeatureSpec< ?, T > > specs = specsService.getSpecs( target );
		for ( final FeatureSpec< ?, ? > spec : specs )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Spot > feature = ( Feature< Spot > ) featureModel.get( spec );
			if (null == feature)
			{
				System.out.println( "\n - Feature " + spec.getKey() + " is not computed." );
				continue;
			}
			final String[] projections = spec.getProjections();
			System.out.println( "\n - Feature " + spec.getKey() +". Has " + projections.length + " projections:" );
			for ( final String projectionKey : projections )
			{
				@SuppressWarnings( "unchecked" )
				final FeatureProjection< T > projection = ( FeatureProjection< T > ) feature.project( projectionKey );
				if (null == projection)
				{
					System.out.println( "   - Projection " + projectionKey  + " is not set, skipping." );
					continue;
				}
				System.out.println( "   - Projection " + projectionKey );
				for ( final T obj : collection )
					System.out.println( "       - " + obj + ": " + projection.value( obj ) );
			}
		}
	}
}
