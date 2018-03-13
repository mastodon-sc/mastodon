package org.mastodon.revised.ui.keymap;

import org.scijava.plugin.SciJavaPlugin;

public abstract class CommandDescriptionProvider implements SciJavaPlugin
{
	private final String[] expectedContexts;

	protected CommandDescriptionProvider( final String... expectedContexts )
	{
		this.expectedContexts = expectedContexts;
	}

	public String[] getExpectedContexts()
	{
		return expectedContexts;
	}

	public abstract void getCommandDescriptions( final CommandDescriptions descriptions );
}
