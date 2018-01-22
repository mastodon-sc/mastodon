package org.mastodon.app.ui.settings.style;

import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage.Profile;

public class StyleProfile< S extends Style< S > > implements Profile
{
	S style;

	boolean isBuiltin;

	public StyleProfile( final S style, final boolean isBuiltin )
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

	public S getStyle()
	{
		return style;
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		@SuppressWarnings( "unchecked" )
		final StyleProfile< S > that = ( StyleProfile< S > ) o;
		return isBuiltin == that.isBuiltin && getName().equals( that.getName() );
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + ( isBuiltin ? 1 : 0 );
		return result;
	}
}
