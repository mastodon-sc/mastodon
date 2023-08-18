package org.mastodon.app.ui;

/**
 * Interface for views that have a view frame.
 */
public interface HasFrame
{

	/**
	 * Exposes the {@link ViewFrame} in which this view is displayed,
	 * 
	 * @return the {@link ViewFrame}.
	 */
	public ViewFrame getFrame();
}
