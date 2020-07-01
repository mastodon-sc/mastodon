package org.mastodon.mamut.feature;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelUtils;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.util.StopWatch;

public class SerializeFeatureExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		// Load project.
		final WindowManager windowManager = new WindowManager( new Context() );
		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.mastodon" );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();
		final FeatureModel featureModel = model.getFeatureModel();

		// Compute features.
		final MamutFeatureComputerService featureComputerService = windowManager.getContext().getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		System.out.println( "\nComputing features..." );
		final StopWatch stopWatch = StopWatch.createAndStart();
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute(	featureComputerService.getFeatureSpecs() );
		featureModel.clear();
		features.values().forEach( featureModel::declareFeature );
		stopWatch.stop();
		System.out.println( String.format( "Done in %.1f s.", stopWatch.nanoTime() / 1e9 ) );

		final File targetFile = new File("samples/featureserialized.mastodon");

		System.out.println( "\nResaving." );
		windowManager.getProjectManager().saveProject( targetFile );
		System.out.println( "Done." );

		System.out.println( "\nReloading." );
		windowManager.getProjectManager().open( new MamutProjectIO().load( targetFile.getAbsolutePath() ) );
		System.out.println( "Done." );

		System.out.println( "\n" + ModelUtils.dump( windowManager.getAppModel().getModel(), 4 ) );
	}
}
