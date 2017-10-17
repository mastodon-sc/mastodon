package org.mastodon.model;

/**
 * Interface for listeners of a {@link SelectionModel}.
 *
 */
public interface SelectionListener
{
	/**
	 * Notifies when the selection has changed.
	 */
	public void selectionChanged();
}
