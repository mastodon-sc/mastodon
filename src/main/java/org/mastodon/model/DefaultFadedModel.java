package org.mastodon.model;

import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.listeners.Listeners;

public class DefaultFadedModel
		implements FadedModel< BranchSpot, BranchLink >
{

	private final TimepointModel timepointModel;

	/**
	 * Creates a new Fading Model for the specified graph.
	 */
	public DefaultFadedModel( final TimepointModel timepointModel )
	{
		this.timepointModel = timepointModel;
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
	public Listeners< TimepointListener > listeners()
	{
		return this.timepointModel.listeners();
	}
}
