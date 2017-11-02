package org.mastodon.adapter;

import org.mastodon.model.TimepointListener;
import org.mastodon.model.TimepointModel;

/**
 * Adapts a {@link TimepointModel} as a {@link TimepointModel}. The only point
 * of this is to track which listeners were added through this wrapper.
 *
 * @author Tobias Pietzsch
 */
public class TimepointModelAdapter implements TimepointModel
{
	private final TimepointModel timepoint;

	private final ForwardedListeners< TimepointListener > listeners;

	public TimepointModelAdapter( final TimepointModel timepoint )
	{
		this.timepoint = timepoint;
		this.listeners = new ForwardedListeners.List<>( timepoint.listeners() );
	}

	@Override
	public void setTimepoint( final int t )
	{
		timepoint.setTimepoint( t );
	}

	@Override
	public int getTimepoint()
	{
		return timepoint.getTimepoint();
	}

	@Override
	public ForwardedListeners< TimepointListener > listeners()
	{
		return listeners;
	}
}
