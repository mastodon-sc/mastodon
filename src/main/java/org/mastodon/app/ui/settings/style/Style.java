package org.mastodon.app.ui.settings.style;

public interface Style< S extends Style< S > >
{
	default S copy()
	{
		return copy( getName() );
	}

	S copy( String newName );

	String getName();

	void setName( String name );
}
