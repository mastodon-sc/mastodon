package org.mastodon.revised.util;

import java.awt.Desktop.Action;

import org.scijava.listeners.Listeners;

/**
 * Something that can be selected or unselected.
 *
 * Intended use is to be implemented by {@code Action}s that toggle things
 * on/off. Menu items (or other UI elements) triggering the {@link Action} can
 * then be checked or unchecked according to its {@code HasSelectedState}.
 *
 * @author Tobias Pietzsch
 */
public interface HasSelectedState
{
	public boolean isSelected();

	public Listeners< Listener > selectListeners();

	public interface Listener
	{
		public void setSelected( boolean selected );
	}
}
