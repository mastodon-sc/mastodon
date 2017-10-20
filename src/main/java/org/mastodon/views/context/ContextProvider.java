package org.mastodon.views.context;

import org.mastodon.util.Listeners;

/**
 * Something that provides {@link Context<V>} to {@link ContextListener}s. It
 * has an identifying {@link #getName() name} (which is not enforced to be
 * unique) to use in {@code ContextChooserPanel} UI.
 *
 * @param <V>
 *
 * @author Tobias Pietzsch
 */
public interface ContextProvider< V >
{
	public String getName();

	/**
	 * If a new listener is added, it will be
	 * {@link ContextListener#contextChanged(Context) updated} with the current
	 * context immediately.
	 */
	public Listeners< ContextListener< V > > listeners();
}
