package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.model.feature.AbstractFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.plugin.Plugin;

@Plugin(type = MamutFeatureComputerService.class)
public class DefaultMamutFeatureComputerService extends AbstractFeatureComputerService< Model > implements MamutFeatureComputerService
{
	@Override
	public void initialize()
	{
		initializeFeatureComputers( SpotFeatureComputer.class );
		initializeFeatureComputers( LinkFeatureComputer.class );
	}
}
