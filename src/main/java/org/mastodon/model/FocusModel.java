package org.mastodon.model;

import org.mastodon.util.Listeners;

/**
 * Class to manage the object that has the "focus", regardless of how this focus
 * is used.
 *
 * @param <T>
 *            type of object that might receive the focus.
 */
public interface FocusModel< T >
{
	public void focusVertex( final T vertex );

	public T getFocusedVertex( final T ref );

	public Listeners< FocusListener > listeners();
}
