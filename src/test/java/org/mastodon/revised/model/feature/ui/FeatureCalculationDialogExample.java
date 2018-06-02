package org.mastodon.revised.model.feature.ui;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class FeatureCalculationDialogExample
{

	public static void main( final String[] args ) throws IOException, SpimDataException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final Context context = new Context();

		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		final WindowManager windowManager = new WindowManager( context );
		windowManager.getProjectManager().open( project );

		final Model model = windowManager.getAppModel().getModel();
		final FeatureModel featureModel = model.getFeatureModel();
		final MamutFeatureComputerService computerService = context.getService( MamutFeatureComputerService.class );
		computerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		new FeatureCalculationDialog<>( null, computerService, model, featureModel ).setVisible( true );
	}
}
