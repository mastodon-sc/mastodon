package org.mastodon.model;

/**
 * Listens to {@link HighlightState} changes.
 */
public interface HighlightListener
{
	/**
	 * Invoked when the {@link HighlightState} changed.
	 */
	public void highlightChanged();
}
