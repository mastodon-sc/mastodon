package org.mastodon.revised.ui.selection;

import org.mastodon.util.Listeners;

/**
 * TODO
 *
 * @author Tobias Pietzsch
 */
public class TimepointModelImp implements TimepointModel
{
	private final Listeners.List< TimepointListener > listeners = new Listeners.SynchronizedList<>();

	private int timepoint = 0;

	@Override
	public void setTimepoint( final int t )
	{
		timepoint = t;
		for ( final TimepointListener l : listeners.list )
			l.timepointChanged();
	}

	@Override
	public int getTimepoint()
	{
		return timepoint;
	}

	@Override
	public Listeners< TimepointListener > listeners()
	{
		return listeners;
	}
}
