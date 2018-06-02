package org.mastodon.revised.mamut.feature;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.feature.DoubleArrayFeature;
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
		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();

		System.out.println( "Starting calculation" );
		final long start = System.currentTimeMillis();
		final SpotGaussFilteredIntensityComputer computer = new SpotGaussFilteredIntensityComputer();
		computer.setSharedBigDataViewerData( windowManager.getAppModel().getSharedBdvData() );
		final DoubleArrayFeature< Spot > feature = computer.compute( windowManager.getAppModel().getModel() );
		final int nSources = feature.getLength() / 2;
		final long end = System.currentTimeMillis();
		System.out.println( String.format(
				"Calculation done, mean and std calculated over %d sources in %.1f seconds.",
				nSources, ( end - start ) / 1000. ) );

		final StringBuilder rowStr = new StringBuilder();
		final StringBuilder headerStr = new StringBuilder();
		headerStr.append( String.format( "%10s |", "Spot" ) );
		rowStr.append( "%10s |" );
		for ( int i = 0; i < nSources; i++ )
		{
			rowStr.append( " %8.1f ± %8.1f |" );
			headerStr.append( String.format( " %19s |", "Channel " + i ) );
		}
		headerStr.append( '\n'
				+ new String( new char[ nSources * 11 ] ).replace( '\0', '-' )
				+ '|'
				+ new String( new char[ nSources * 22 - 1 ] ).replace( '\0', '-' )
				+ '|' );

		System.out.println( "Values:" );
		System.out.println( headerStr.toString() );
		final double[] tmp = new double[ feature.getLength() ];
		final Object[] objs = new Object[ 1 + tmp.length ];
		for ( final Spot spot : model.getGraph().vertices() )
		{
			final double[] vals = feature.get( spot, tmp );
			objs[ 0 ] = spot.getLabel();
			for ( int i = 0; i < vals.length; i++ )
				objs[ i + 1 ] = vals[ i ];
			System.out.println( String.format( rowStr.toString(), objs ) );
		}
	}
}
