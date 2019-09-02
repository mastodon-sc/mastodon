package org.mastodon.mamut.feature;

import static org.scijava.ItemIO.OUTPUT;

import org.mastodon.revised.model.mamut.Model;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotPositionFeatureComputer.class )
public class SpotPositionFeatureComputer implements MamutFeatureComputer
{
	@Parameter
	private Model model;

	@Parameter( type = OUTPUT )
	private SpotPositionFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotPositionFeature( model.getSpaceUnits() );
	}

	@Override
	public void run()
	{
		// Nothing to do.
	}
}
