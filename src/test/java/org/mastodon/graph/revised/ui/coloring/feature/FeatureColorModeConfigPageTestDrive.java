package org.mastodon.graph.revised.ui.coloring.feature;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.mamut.feature.MamutFeatureComputer;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.ProgressListener;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeConfigPage;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.FeatureRangeCalculator;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class FeatureColorModeConfigPageTestDrive
{
	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final Context context = new Context();
		final WindowManager windowManager = new WindowManager( context );
		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();

		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );
		// Get feature keys before computing them.
		final LinkedHashMap< String, Collection< String > > vertexFeatureKeys = new LinkedHashMap<>();
		final LinkedHashMap< String, Collection< String > > edgeFeatureKeys = new LinkedHashMap<>();
		final Collection< MamutFeatureComputer > featureComputers = service.getFeatureComputers();
		for ( final MamutFeatureComputer computer : featureComputers )
		{
			if ( Spot.class.equals( computer.getTargetClass() ) )
				vertexFeatureKeys.put( computer.getKey(), computer.getProjectionKeys() );
			if ( Link.class.equals( computer.getTargetClass() ) )
				edgeFeatureKeys.put( computer.getKey(), computer.getProjectionKeys() );
		}

		// Remove one feature computer.
		final HashSet< MamutFeatureComputer > toCompute = new HashSet<>( featureComputers );
		toCompute.remove( toCompute.iterator().next() );

		// Compute features now.
		final FeatureModel featureModel = model.getFeatureModel();
		service.compute( model, featureModel, toCompute, pl );

		final FeatureRangeCalculator< Spot, Link > rangeCalculator = new FeatureRangeCalculator<>( model.getGraph(), model.getFeatureModel() );

		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new FeatureColorModeConfigPage(
				"Feature coloring",
				featureColorModeManager,
				featureModel,
				rangeCalculator,
				Spot.class,
				vertexFeatureKeys,
				Link.class,
				edgeFeatureKeys ) );

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
