package org.mastodon.revised.bdv.overlay.ui;

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
import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsConfigPage.RenderSettingsProfile;
import org.mastodon.util.Listeners;

public class RenderSettingsConfigPage extends SelectAndEditProfileSettingsPage< RenderSettingsProfile >
{
	/**
	 * @param treePath
	 * 		path of this page in the settings tree.
	 */
	public RenderSettingsConfigPage( final String treePath, final RenderSettingsManager renderSettingsManager )
	{
		super( treePath, new RenderSettingsProfileManager( renderSettingsManager ), new RenderSettingsProfileEditPanel() );
	}

	static class RenderSettingsProfileEditPanel implements RenderSettings.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< RenderSettingsProfile >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final RenderSettings editedStyle;

		private final RenderSettingsPanel styleEditorPanel;

		public RenderSettingsProfileEditPanel()
		{
			editedStyle = RenderSettings.defaultStyle().copy( "Edited" );
			styleEditorPanel = new RenderSettingsPanel( editedStyle );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.addUpdateListener( this );
		}

		private boolean trackModifications = true;

		@Override
		public void renderSettingsChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final RenderSettingsProfile profile )
		{
			trackModifications = false;
			editedStyle.set( profile.style );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final RenderSettingsProfile profile )
		{
			trackModifications = false;
			editedStyle.setName( profile.style.getName() );
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

	public static class RenderSettingsProfile implements Profile
	{
		RenderSettings style;

		boolean isBuiltin;

		public RenderSettingsProfile( final RenderSettings style, final boolean isBuiltin )
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

			final RenderSettingsProfile that = ( RenderSettingsProfile ) o;
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

	static class RenderSettingsProfileManager implements ProfileManager< RenderSettingsProfile >
	{
		private final RenderSettingsManager styles;

		private final RenderSettingsManager styleManager;

		public RenderSettingsProfileManager( final RenderSettingsManager styleManager )
		{
			this.styleManager = styleManager;
			styles = new RenderSettingsManager( false );
			styles.set( styleManager );
		}

		@Override
		public List< RenderSettingsProfile > getProfiles()
		{
			return Stream.concat(
					styles.getBuiltinStyles().stream().map( style -> new RenderSettingsProfile( style, true ) ),
					styles.getUserStyles().stream().map( style -> new RenderSettingsProfile( style, false ) )
			).collect( Collectors.toList() );
		}

		@Override
		public RenderSettingsProfile getSelectedProfile()
		{
			final RenderSettings style = styles.getDefaultStyle();
			final boolean isBuiltin = styles.getBuiltinStyles().stream().anyMatch( s -> s.getName().equals( style.getName() ) );
			return new RenderSettingsProfile( style, isBuiltin );
		}

		@Override
		public void select( final RenderSettingsProfile profile )
		{
			styles.setDefaultStyle( profile.style );
		}

		@Override
		public RenderSettingsProfile duplicate( final RenderSettingsProfile profile )
		{
			final RenderSettings duplicate = styles.duplicate( profile.style );
			return new RenderSettingsProfile( duplicate, false );
		}

		@Override
		public void rename( final RenderSettingsProfile profile, final String newName )
		{
			styles.rename( profile.style, newName );
		}

		@Override
		public void delete( final RenderSettingsProfile profile )
		{
			final boolean wasSelected = getSelectedProfile().equals( profile );
			int newSelectedIndex = -1;
			if ( wasSelected )
			{
				final List< RenderSettingsProfile > profiles = getProfiles();
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
		final RenderSettingsManager styleManager = new RenderSettingsManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new RenderSettingsConfigPage( "Style > BDV", styleManager ) );

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
