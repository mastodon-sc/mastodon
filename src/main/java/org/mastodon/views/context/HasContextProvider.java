package org.mastodon.views.context;

import org.mastodon.mamut.model.Spot;

/**
 * Interface for classes that can return a {@link ContextProvider}.
 */
public interface HasContextProvider
{

	/**
	 * Returns the {@link ContextProvider} of this class.
	 * 
	 * @return the {@link ContextProvider}.
	 */
	public ContextProvider< Spot > getContextProvider();

}
