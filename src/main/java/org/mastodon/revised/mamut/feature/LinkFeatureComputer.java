package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.feature.FeatureTarget;
import org.mastodon.revised.model.mamut.Link;

public abstract class LinkFeatureComputer< K extends Feature< Link, ?, ? >, AM extends AbstractModel< ?, ?, ? > > implements FeatureComputer< K, Link, AM >
{
	@Override
	public FeatureTarget getTarget()
	{
		return FeatureTarget.EDGE;
	}

}