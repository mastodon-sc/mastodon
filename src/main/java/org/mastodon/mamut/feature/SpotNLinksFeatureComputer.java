package org.mastodon.mamut.feature;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputer.class )
public class SpotNLinksFeatureComputer implements MamutFeatureComputer
{

	@Parameter( type = ItemIO.OUTPUT )
	private SpotNLinksFeature output;

	@Override
	public void run()
	{
		// Nothing to do, feature is virtual.
	}

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotNLinksFeature();
	}
}
