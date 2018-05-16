package org.mastodon.revised.mamut.feature;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.mamut.MainWindow;
import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class SpotIntensityExample
{
	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final Context context = new Context();

		final WindowManager windowManager = new WindowManager( context );
		final MainWindow mw = new MainWindow( windowManager );

		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();

		System.out.println( "Starting calculation" );
		final SpotGaussFilteredIntensityComputer computer = new SpotGaussFilteredIntensityComputer();
		computer.setSharedBigDataViewerData( windowManager.getAppModel().getSharedBdvData() );
		final Feature< Spot, DoublePropertyMap< Spot > > feature  = computer.compute( windowManager.getAppModel().getModel() );
		System.out.println( "Calculation done." );

		final DoublePropertyMap< Spot > pm = feature.getPropertyMap();
		System.out.println( "Values:" );
		for ( final Spot spot : model.getGraph().vertices() )
			System.out.println( "Spot " + spot.getLabel() + ": set = " + pm.isSet( spot ) + ", val = " + pm.get( spot ) );

		mw.setVisible( true );
	}
}
