package org.mastodon.views.context;

import org.mastodon.util.Listeners;

/**
 * Something that provides {@link Context} to {@link ContextListener}s. It has
 * an identifying {@link #getName() name} (which is not enforced to be unique)
 * to use in {@code ContextChooserPanel} UI.
 *
 * @param <V>
 *            the type of objects the context is defined for.
 *
 * @author Tobias Pietzsch
 */
public interface ContextProvider< V >
{
	public String getName();

	/**
	 * Exposes the listeners of this context provider.
	 * <p>
	 * If a new listener is added, it will be
	 * {@link ContextListener#contextChanged(Context) updated} with the current
	 * context immediately.
	 * 
	 * @return the listeners.
	 */
	public Listeners< ContextListener< V > > listeners();
}
