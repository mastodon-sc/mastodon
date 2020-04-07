package org.mastodon.model;

import org.scijava.listeners.Listeners;

/**
 * TODO
 *
 * @author Tobias Pietzsch
 */
public interface TimepointModel
{
	public void setTimepoint( final int t );

	public int getTimepoint();

	public Listeners< TimepointListener > listeners();
}
