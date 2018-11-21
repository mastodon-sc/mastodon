package org.mastodon.feature.ui;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.mamut.PreferencesDialog;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.coloring.feature.DefaultFeatureRangeCalculator;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.FeatureRangeCalculator;
import org.mastodon.revised.ui.coloring.feature.Projections;
import org.mastodon.revised.ui.coloring.feature.ProjectionsFromFeatureModel;
import org.mastodon.revised.ui.keymap.Keymap;
import org.mastodon.revised.ui.keymap.KeymapManager;
import org.scijava.Context;

public class FeatureColorModeConfigPageDemo
{
	private static final String FEATURECOLORMODE_SETTINGSPAGE_TREEPATH = "Feature Color Modes";

	public static void main( final String[] args ) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final KeymapManager keymapManager = new KeymapManager();
		final Keymap keymap = keymapManager.getForwardDefaultKeymap();
		final PreferencesDialog settings = new PreferencesDialog( null, keymap, new String[] { KeyConfigContexts.MASTODON } );

		final Context context = new Context( FeatureSpecsService.class );

		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		final Model model = new Model();
		model.loadRaw( project.openForReading() );

		final Projections projections = new ProjectionsFromFeatureModel( model.getFeatureModel() );

		final FeatureRangeCalculator vertexFeatureRangeCalculator =
				new DefaultFeatureRangeCalculator<>( model.getGraph().vertices(), projections );
		final FeatureRangeCalculator edgeFeatureRangeCalculator =
				new DefaultFeatureRangeCalculator<>( model.getGraph().edges(), projections );

		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();
		settings.addPage( new FeatureColorModeConfigPage( FEATURECOLORMODE_SETTINGSPAGE_TREEPATH,
				featureColorModeManager,
				context.getService( FeatureSpecsService.class ),
				3,
				model.getFeatureModel(),
				vertexFeatureRangeCalculator,
				edgeFeatureRangeCalculator,
				Spot.class,
				Link.class ) );

		settings.pack();
		settings.setVisible( true );
	}
}
