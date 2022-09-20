/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
