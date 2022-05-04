package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureComputer;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.model.AbstractModel;

public abstract class BranchLinkFeatureComputer< K extends Feature< BranchLink >, AM extends AbstractModel< ?, ?, ? > >
		implements FeatureComputer
{}
