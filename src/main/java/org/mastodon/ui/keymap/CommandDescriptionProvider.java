package org.mastodon.ui.keymap;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Implementations of this interface, annotated with {@code	@Plugin}, are
 * discovered for automatically adding actions/behaviours to a
 * {@link CommandDescriptions} map.
 * <p>
 * (This allows to discover Plugin shortcuts which cannot be hardwired into the
 * default keymap.)
 */
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
