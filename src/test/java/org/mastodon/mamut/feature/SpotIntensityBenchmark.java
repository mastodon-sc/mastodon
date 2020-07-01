package org.mastodon.mamut.feature;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.util.StopWatch;

public class SpotIntensityBenchmark
{

	public static void main( final String[] args ) throws IOException, JDOMException, SpimDataException
	{
		/*
		 * 1. Load a regular Mastodon project.
		 */

		final MamutProject project = new MamutProjectIO().load( "/Users/tinevez/Projects/JYTinevez/MaMuT/Mastodon-dataset/MaMuT_Parhyale_demo.mastodon" );
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();

		// Just keep the 1st time-point, or else....
		System.out.println( "Removing all time-points but the first one." );
		model.getSpatioTemporalIndex().getSpatialIndex( 0 );
		final int minTimepoint = windowManager.getAppModel().getMinTimepoint();
		final int maxTimepoint = windowManager.getAppModel().getMaxTimepoint();
		for ( int t = minTimepoint + 1; t < maxTimepoint; t++ )
		{
			for ( final Spot spot : model.getSpatioTemporalIndex().getSpatialIndex( t ) )
				model.getGraph().remove( spot );
		}
		System.out.println( "Done." );


		/*
		 * 1.1a. Compute spot intensity feature for all.
		 */

		final Context context = windowManager.getContext();
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		System.out.println( "Computing spot intensity..." );
		for ( int i = 0; i < 5; i++ )
		{
			final StopWatch stopWatch = StopWatch.createAndStart();
			featureComputerService.compute( SpotGaussFilteredIntensityFeature.SPEC );
			stopWatch.stop();
			System.out.println( String.format( "Done in %.2f s.", stopWatch.nanoTime() / 1e9 ) );
		}
	}
}
