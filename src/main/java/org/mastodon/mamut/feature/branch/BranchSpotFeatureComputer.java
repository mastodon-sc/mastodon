package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureComputer;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.AbstractModel;

public abstract class BranchSpotFeatureComputer< K extends Feature< BranchSpot >, AM extends AbstractModel< ?, ?, ? > >
		implements FeatureComputer
{}
