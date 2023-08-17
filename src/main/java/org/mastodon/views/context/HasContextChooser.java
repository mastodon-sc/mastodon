package org.mastodon.views.context;

/**
 * Interface for views that have a context chooser.
 * 
 * @param <T>
 *            the type of object the context is defined on.
 */
public interface HasContextChooser< T >
{
	/**
	 * Returns the context chooser of this view.
	 * 
	 * @return the context chooser.
	 */
	public ContextChooser< T > getContextChooser();
}
