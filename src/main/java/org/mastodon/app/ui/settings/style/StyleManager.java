package org.mastodon.app.ui.settings.style;

import java.util.List;

public interface StyleManager< M extends StyleManager< M, S >, S extends Style< S > >
{
	void set( M styles );

	List< S > getBuiltinStyles();

	List< S > getUserStyles();

	void setDefaultStyle( final S style );

	S getDefaultStyle();

	S duplicate( final S style );

	void rename( final S style, final String newName );

	void remove( final S style );

	void saveStyles();
}
