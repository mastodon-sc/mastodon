package org.mastodon.feature;

import org.scijava.command.Command;

public interface FeatureComputer extends Command
{
	void createOutput();
}
