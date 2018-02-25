package org.mastodon.graph.revised.ui.coloring.feature;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.ProgressListener;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeConfigPage;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.scijava.Context;

public class FeatureColorModeConfigPageTestDrive
{
	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final Context context = new Context();
		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );
		final Model model = new Model();
		final FeatureModel featureModel = model.getFeatureModel();
		service.compute( model, featureModel, new HashSet<>( service.getFeatureComputers() ), pl );

		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new FeatureColorModeConfigPage( "Feature coloring", featureColorModeManager, featureModel, Spot.class, Link.class ) );

		final JDialog dialog = new JDialog( ( Frame ) null, "Settings" );
		dialog.getContentPane().add( settings, BorderLayout.CENTER );

		settings.onOk( () -> dialog.setVisible( false ) );
		settings.onCancel( () -> dialog.setVisible( false ) );

		dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		dialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				settings.cancel();
			}
		} );

		dialog.pack();
		dialog.setVisible( true );

		featureColorModeManager.saveStyles();
	}

	private static final ProgressListener pl = new ProgressListener()
	{

		@Override
		public void showStatus( final String string )
		{
			System.out.println( " - " + string );
		}

		@Override
		public void showProgress( final int current, final int total )
		{}

		@Override
		public void clearStatus()
		{}
	};

	private FeatureColorModeConfigPageTestDrive()
	{}
}
