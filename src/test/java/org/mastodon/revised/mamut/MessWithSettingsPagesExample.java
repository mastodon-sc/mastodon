package org.mastodon.revised.mamut;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.SimpleSettingsPage;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MessWithSettingsPagesExample
{

	public static void main( final String[] args ) throws IOException, SpimDataException, InvocationTargetException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final WindowManager windowManager = new WindowManager( new Context() );
		final MainWindow mainWindow = new MainWindow( windowManager );
		mainWindow.setVisible( true );
		mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		windowManager.projectManager.open( project );

		windowManager.settings.removePage( "Feature Color Modes" );
		windowManager.settings.removePage( "Keymap" );
		windowManager.settings.removePage( "Stupid" );

		final JPanel panelTobias = new JPanel();
		panelTobias.add( new JLabel( "Tobi" ) );
		windowManager.settings.addPage( new SimpleSettingsPage( "Founders > Tobias", panelTobias ) );
		final JPanel panelJY = new JPanel();
		panelJY.add( new JLabel( "JY" ) );
		windowManager.settings.addPage( new SimpleSettingsPage( "Founders > Jean-Yves", panelJY ) );
		final JPanel panelBob = new JPanel();
		panelBob.add( new JLabel( "Bob" ) );
		windowManager.settings.addPage( new SimpleSettingsPage( "Founders > Bob", panelBob ) );

		windowManager.settings.removePage( "Founders > Bob" );
	}

	private MessWithSettingsPagesExample()
	{}
}
