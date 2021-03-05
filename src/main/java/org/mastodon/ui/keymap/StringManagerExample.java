package org.mastodon.ui.keymap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.SingleSettingsPanel;
import org.mastodon.app.ui.settings.style.AbstractStyle;
import org.mastodon.app.ui.settings.style.AbstractStyleManager;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.scijava.listeners.Listeners;

public class StringManagerExample
{

	/*
	 * A "configurable profile" is a "named bundle of (configurable) settings".
	 * An example is configurable keymaps found in many programs: You can choose
	 * between one of several named keymaps, and you can also tweak the
	 * individual bindings in each keymap.
	 *
	 * To represent a configurable profile we need to implement the Style interface,
	 * which comprises methods to copy the style and get/set name.
	 * (AbstractStyle handles the name getting/setting.)
	 *
	 * In this example we want to make Greeting style which only comprises one
	 * setting: a greeting text. We only have to implement a copy() method,
	 * which makes a copy of these particular settings with a new name.
	 */
	static class Greeting extends AbstractStyle< Greeting >
	{
		public String text;

		public Greeting( final String name, final String text )
		{
			super( name );
			this.text = text;
		}

		@Override
		public Greeting copy( final String newName )
		{
			return new Greeting( newName, text );
		}
	}

	/*
	 * Next we need a StyleManager that manages a list of our Greeting styles.
	 *
	 * TODO: setting the active style
	 *
	 * TODO: handles builtin / user styles
	 *
	 * TODO: snapshots
	 */

	static class GreetingManager extends AbstractStyleManager< GreetingManager, Greeting >
	{
		@Override
		protected List< Greeting > loadBuiltinStyles()
		{
			return Arrays.asList(
					new Greeting( "hello_en", "Hello world!" ),
					new Greeting( "hello_de", "Hallo Welt!" ) );
		}

		@Override
		public void saveStyles()
		{
			System.out.println( "GreetingManager.saveStyles" );
		}
	}

	static class GreetingSettingsPage extends JPanel implements SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< Greeting > >
	{
		private final Listeners.List< ModificationListener > modificationListeners = new Listeners.SynchronizedList<>();

		private final JTextArea textArea;

		private boolean trackModifications = true;

		GreetingSettingsPage()
		{
			super( new BorderLayout() );
			textArea = new JTextArea();
			textArea.setPreferredSize( new Dimension( 400, 200 ) );
			add( new JLabel( "greeting text:" ), BorderLayout.NORTH );
			add( textArea, BorderLayout.CENTER );

			textArea.getDocument().addDocumentListener( new DocumentListener()
			{
				@Override
				public void removeUpdate( DocumentEvent e )
				{
					notifyModified();
				}

				@Override
				public void insertUpdate( DocumentEvent e )
				{
					notifyModified();
				}

				@Override
				public void changedUpdate( DocumentEvent arg0 )
				{
					notifyModified();
				}

				private void notifyModified()
				{
					if ( trackModifications )
						modificationListeners.list.forEach( ModificationListener::setModified );
				}
			} );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public void loadProfile( final StyleProfile< Greeting > profile )
		{
			trackModifications = false;
			textArea.setText( profile.getStyle().text );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< Greeting > profile )
		{
			profile.getStyle().text = textArea.getText();
		}

		@Override
		public JPanel getJPanel()
		{
			return this;
		}
	}



	public static void main( String[] args )
	{
		final GreetingManager greetingManager = new GreetingManager();
		final StyleProfileManager< GreetingManager, Greeting > pm = new StyleProfileManager<>( greetingManager, new GreetingManager() );
		final SelectAndEditProfileSettingsPage< StyleProfile< Greeting > > page = new SelectAndEditProfileSettingsPage<>( "greeting", pm, new GreetingSettingsPage() );

		final JFrame frame = new JFrame( "Preferences" );
		frame.add( new SingleSettingsPanel( page ) );
		frame.pack();
		frame.setVisible( true );
	}
}
