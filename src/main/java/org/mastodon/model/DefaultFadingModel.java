package org.mastodon.model;

import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.listeners.Listeners;

public class DefaultFadingModel
		implements FadingModel< BranchSpot, BranchLink >, TimepointListener
{
	private final TimepointModel timepointModel;

	private final Listeners.List< FadingListener > listeners;

	/**
	 * Creates a new Fading Model for the specified graph.
	 * 
	 * @param timepointModel
	 *            the timepoint model to base this fading model on.
	 */
	public DefaultFadingModel( final TimepointModel timepointModel )
	{
		this.timepointModel = timepointModel;
		this.timepointModel.listeners().add( this );
		listeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public boolean isFaded( final BranchSpot branchSpot )
	{
		return branchSpot.getFirstTimePoint() > timepointModel.getTimepoint();
	}

	@Override
	public boolean isFaded( final BranchLink branchLink )
	{
		return branchLink.getTarget().getFirstTimePoint() > timepointModel.getTimepoint();
	}

	@Override
	public Listeners< FadingListener > listeners()
	{
		return listeners;
	}

	@Override
	public void timepointChanged()
	{
		listeners.list.forEach( FadingListener::fadingChanged );
	}
}
