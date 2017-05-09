package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.feature.FeatureTarget;
import org.mastodon.revised.model.mamut.Spot;

public abstract class SpotFeatureComputer< K extends Feature< Spot, ?, ? >, AM extends AbstractModel< ?, ?, ? > > implements FeatureComputer< K, Spot, AM >
{

	@Override
	public FeatureTarget getTarget()
	{
		return FeatureTarget.VERTEX;
	}

}