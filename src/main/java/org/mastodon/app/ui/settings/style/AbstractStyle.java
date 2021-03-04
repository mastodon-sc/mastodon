package org.mastodon.app.ui.settings.style;

import org.mastodon.ui.keymap.StringManagerExample;

// TODO move to package org.mastodon.app.ui.settings.style
// TODO (Is this a bit overkill?)
public abstract class AbstractStyle< S extends AbstractStyle< S > > implements Style< S>
{
	private String name;

	protected AbstractStyle( final String name )
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName( final String name )
	{
		this.name = name;
	}
}
