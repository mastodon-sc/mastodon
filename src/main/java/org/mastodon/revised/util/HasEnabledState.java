package org.mastodon.revised.util;

import java.awt.Desktop.Action;

import org.mastodon.util.Listeners;

/**
 * Something that can be enabled or disabled.
 *
 * Intended use is to be implemented by {@code Action}s that know whether they
 * are executable at the moment. Menu items (or other UI elements) triggering
 * the {@link Action} can then be disabled or enabled according to its
 * {@code HasEnabledState}.
 *
 * @author Tobias Pietzsch
 */
public interface HasEnabledState
{
	public boolean isEnabled();

	public Listeners< Listener > enableListeners();

	public interface Listener
	{
		public void setEnabled( boolean enabled );
	}
}
