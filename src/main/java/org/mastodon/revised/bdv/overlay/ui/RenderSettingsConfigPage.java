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

		private final RenderSettings editedRenderSettings;

		private final RenderSettingsPanel renderSettingsPanel;

		public RenderSettingsProfileEditPanel()
		{
			editedRenderSettings = RenderSettings.defaultStyle().copy( "Edited" );
			renderSettingsPanel = new RenderSettingsPanel( editedRenderSettings );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedRenderSettings.addUpdateListener( this );
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
			editedRenderSettings.set( profile.rs );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final RenderSettingsProfile profile )
		{
			trackModifications = false;
			editedRenderSettings.setName( profile.rs.getName() );
			trackModifications = true;
			profile.rs.set( editedRenderSettings );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public JPanel getJPanel()
		{
			return renderSettingsPanel;
		}
	}

	public static class RenderSettingsProfile implements SelectAndEditProfileSettingsPage.Profile
	{
		RenderSettings rs;

		boolean isBuiltin;

		public RenderSettingsProfile( final RenderSettings rs, final boolean isBuiltin )
		{
			this.rs = rs;
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
			return rs.getName();
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;

			final RenderSettingsProfile that = ( RenderSettingsProfile ) o;
			return isBuiltin == that.isBuiltin && rs.getName().equals( that.rs.getName() );
		}

		@Override
		public int hashCode()
		{
			int result = rs.hashCode();
			result = 31 * result + ( isBuiltin ? 1 : 0 );
			return result;
		}
	}

	static class RenderSettingsProfileManager implements ProfileManager< RenderSettingsProfile >
	{
		private final RenderSettingsManager rs;

		private final RenderSettingsManager rsManager;

		public RenderSettingsProfileManager( final RenderSettingsManager styleManager )
		{
			this.rsManager = styleManager;
			rs = new RenderSettingsManager( false );
			rs.set( styleManager );
		}

		@Override
		public List< RenderSettingsProfile > getProfiles()
		{
			return Stream.concat(
					rs.getBuiltinRenderSettings().stream().map( style -> new RenderSettingsProfile( style, true ) ),
					rs.getUserRenderSettings().stream().map( style -> new RenderSettingsProfile( style, false ) )
			).collect( Collectors.toList() );
		}

		@Override
		public RenderSettingsProfile getSelectedProfile()
		{
			final RenderSettings style = rs.getDefaultRenderSettings();
			final boolean isBuiltin = rs.getBuiltinRenderSettings().stream().anyMatch( s -> s.getName().equals( style.getName() ) );
			return new RenderSettingsProfile( style, isBuiltin );
		}

		@Override
		public void select( final RenderSettingsProfile profile )
		{
			rs.setDefaultStyle( profile.rs );
		}

		@Override
		public RenderSettingsProfile duplicate( final RenderSettingsProfile profile )
		{
			final RenderSettings duplicate = rs.duplicate( profile.rs );
			return new RenderSettingsProfile( duplicate, false );
		}

		@Override
		public void rename( final RenderSettingsProfile profile, final String newName )
		{
			rs.rename( profile.rs, newName );
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
			rs.remove( profile.rs );
			if ( wasSelected )
			{
				select( getProfiles().get( newSelectedIndex ) );
			}
		}

		@Override
		public void apply()
		{
			rsManager.set( rs );
			rsManager.saveRenderSettings();
		}

		@Override
		public void cancel()
		{
			rs.set( rsManager );
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
