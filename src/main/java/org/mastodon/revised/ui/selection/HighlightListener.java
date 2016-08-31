package org.mastodon.revised.ui.selection;

/**
 * Interface for listener of the {@link HighlightModel}.
 */
public interface HighlightListener
{
	/**
	 * Notifies when the vertex highlighted in a registered highlight model has
	 * changed.
	 */
	public void highlightChanged();
}
