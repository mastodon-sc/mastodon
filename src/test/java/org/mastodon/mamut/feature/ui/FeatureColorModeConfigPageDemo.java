package org.mastodon.mamut.feature.ui;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.FeatureColorModeConfigPage;
import org.mastodon.mamut.PreferencesDialog;
import org.mastodon.mamut.feature.MamutFeatureProjectionsManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.scijava.Context;

public class FeatureColorModeConfigPageDemo
{
	private static final String FEATURECOLORMODE_SETTINGSPAGE_TREEPATH = "Feature Color Modes";

	public static void main( final String[] args ) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final Context context = new Context( FeatureSpecsService.class );

		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();
		final MamutFeatureProjectionsManager featureProjectionsManager = new MamutFeatureProjectionsManager(
				context.getService( FeatureSpecsService.class ),
				featureColorModeManager );

		final PreferencesDialog settings = new PreferencesDialog( null, new Keymap(), new String[] { KeyConfigContexts.MASTODON } );
		settings.addPage( new FeatureColorModeConfigPage( FEATURECOLORMODE_SETTINGSPAGE_TREEPATH,
				featureColorModeManager,
				featureProjectionsManager ) );

		settings.pack();
		settings.setVisible( true );

		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		final Model model = new Model();
		model.loadRaw( project.openForReading() );
		featureProjectionsManager.setModel( model, 3 );
	}
}
