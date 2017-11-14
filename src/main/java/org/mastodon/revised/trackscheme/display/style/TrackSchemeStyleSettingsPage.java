package org.mastodon.revised.trackscheme.display.style;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage.Profile;
import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleSettingsPage.TrackSchemeProfile;
import org.mastodon.util.Listeners; 

public class TrackSchemeStyleSettingsPage extends SelectAndEditProfileSettingsPage< TrackSchemeProfile >
{
	/**
	 * @param treePath
	 * 		path of this page in the settings tree.
	 */
	public TrackSchemeStyleSettingsPage( final String treePath, final TrackSchemeStyleManager styleManager )
	{
		super( treePath, new TrackSchemeStyleProfileManager( styleManager ), new TrackSchemeProfileEditPanel() );
	}

	static class TrackSchemeProfileEditPanel implements TrackSchemeStyle.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< TrackSchemeProfile >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final TrackSchemeStyle editedStyle;

		private final TrackSchemeStyleEditorPanel styleEditorPanel;

		public TrackSchemeProfileEditPanel()
		{
			editedStyle = TrackSchemeStyle.defaultStyle().copy( "Edited" );
			styleEditorPanel = new TrackSchemeStyleEditorPanel( editedStyle );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.addUpdateListener( this );
		}

		private boolean trackModifications = true;

		@Override
		public void trackSchemeStyleChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final TrackSchemeProfile profile )
		{
			trackModifications = false;
			editedStyle.set( profile.style );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final TrackSchemeProfile profile )
		{
			trackModifications = false;
			editedStyle.name( profile.style.getName() );
			trackModifications = true;
			profile.style.set( editedStyle );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public JPanel getJPanel()
		{
			return styleEditorPanel;
		}
	}

	public static class TrackSchemeProfile implements Profile
	{
		TrackSchemeStyle style;

		boolean isBuiltin;

		public TrackSchemeProfile( final TrackSchemeStyle style, final boolean isBuiltin )
		{
			this.style = style;
			this.isBuiltin = isBuiltin;
		}

		@Override
		public boolean isBuiltin()
		{
			return isBuiltin;
		}

		@Override
		public String getName()
		{
			return style.getName();
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;

			final TrackSchemeProfile that = ( TrackSchemeProfile ) o;
			return isBuiltin == that.isBuiltin && style.getName().equals( that.style.getName() );
		}

		@Override
		public int hashCode()
		{
			int result = style.hashCode();
			result = 31 * result + ( isBuiltin ? 1 : 0 );
			return result;
		}
	}

	static class TrackSchemeStyleProfileManager implements ProfileManager< TrackSchemeProfile >
	{
		private final TrackSchemeStyleManager styles;

		private final TrackSchemeStyleManager styleManager;

		public TrackSchemeStyleProfileManager( final TrackSchemeStyleManager styleManager )
		{
			this.styleManager = styleManager;
			styles = new TrackSchemeStyleManager( false );
			styles.set( styleManager );
		}

		@Override
		public List< TrackSchemeProfile > getProfiles()
		{
			return Stream.concat(
					styles.getBuiltinStyles().stream().map( style -> new TrackSchemeProfile( style, true ) ),
					styles.getUserStyles().stream().map( style -> new TrackSchemeProfile( style, false ) )
			).collect( Collectors.toList() );
		}

		@Override
		public TrackSchemeProfile getSelectedProfile()
		{
			final TrackSchemeStyle style = styles.getDefaultStyle();
			final boolean isBuiltin = styles.getBuiltinStyles().stream().anyMatch( s -> s.getName().equals( style.getName() ) );
			return new TrackSchemeProfile( style, isBuiltin );
		}

		@Override
		public void select( final TrackSchemeProfile profile )
		{
			styles.setDefaultStyle( profile.style );
		}

		@Override
		public TrackSchemeProfile duplicate( final TrackSchemeProfile profile )
		{
			final TrackSchemeStyle duplicate = styles.duplicate( profile.style );
			return new TrackSchemeProfile( duplicate, false );
		}

		@Override
		public void rename( final TrackSchemeProfile profile, final String newName )
		{
			styles.rename( profile.style, newName );
		}

		@Override
		public void delete( final TrackSchemeProfile profile )
		{
			final boolean wasSelected = getSelectedProfile().equals( profile );
			int newSelectedIndex = -1;
			if ( wasSelected )
			{
				final List< TrackSchemeProfile > profiles = getProfiles();
				newSelectedIndex = Math.max( 0, profiles.indexOf( profile ) - 1 );
			}
			styles.remove( profile.style );
			if ( wasSelected )
			{
				select( getProfiles().get( newSelectedIndex ) );
			}
		}

		@Override
		public void apply()
		{
			styleManager.set( styles );
			styleManager.saveStyles();
		}

		@Override
		public void cancel()
		{
			styles.set( styleManager );
		}
	}

	public static void main( final String[] args )
	{
		final TrackSchemeStyleManager styleManager = new TrackSchemeStyleManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new TrackSchemeStyleSettingsPage( "Style > TrackScheme", styleManager ) );

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
	}
}
