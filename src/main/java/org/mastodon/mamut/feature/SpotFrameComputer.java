package org.mastodon.mamut.feature;

import static org.scijava.ItemIO.OUTPUT;

import org.mastodon.feature.FeatureComputer;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotFrameComputer.class )
public class SpotFrameComputer implements FeatureComputer
{

	@Parameter( type = OUTPUT )
	private SpotFrameFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotFrameFeature();
	}

	@Override
	public void run()
	{
		// Nothing to do.
	}
}
