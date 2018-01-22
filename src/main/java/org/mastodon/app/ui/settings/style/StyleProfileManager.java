package org.mastodon.app.ui.settings.style;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage.ProfileManager;

public class StyleProfileManager< M extends StyleManager< M, S >, S extends Style< S > > implements ProfileManager< StyleProfile< S > >
{
	private final M styles;

	private final M styleManager;

	public StyleProfileManager( final M styleManager, final M editingStyleManager )
	{
		this.styleManager = styleManager;
		styles = editingStyleManager; // new AbstractStyleManager( false );
		styles.set( styleManager );
	}

	@Override
	public List< StyleProfile< S > > getProfiles()
	{
		return Stream.concat(
				styles.getBuiltinStyles().stream().map( style -> new StyleProfile<>( style, true ) ),
				styles.getUserStyles().stream().map( style -> new StyleProfile<>( style, false ) )
		).collect( Collectors.toList() );
	}

	@Override
	public StyleProfile< S > getSelectedProfile()
	{
		final S style = styles.getDefaultStyle();
		final boolean isBuiltin = styles.getBuiltinStyles().stream().anyMatch( s -> s.getName().equals( style.getName() ) );
		return new StyleProfile<>( style, isBuiltin );
	}

	@Override
	public void select( final StyleProfile< S > profile )
	{
		styles.setDefaultStyle( profile.style );
	}

	@Override
	public StyleProfile< S > duplicate( final StyleProfile< S > profile )
	{
		final S duplicate = styles.duplicate( profile.style );
		return new StyleProfile<>( duplicate, false );
	}

	@Override
	public void rename( final StyleProfile< S > profile, final String newName )
	{
		styles.rename( profile.style, newName );
	}

	@Override
	public void delete( final StyleProfile< S > profile )
	{
		final boolean wasSelected = getSelectedProfile().equals( profile );
		int newSelectedIndex = -1;
		if ( wasSelected )
		{
			final List< StyleProfile< S > > profiles = getProfiles();
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
