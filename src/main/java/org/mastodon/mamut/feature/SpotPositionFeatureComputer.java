package org.mastodon.mamut.feature;

import static org.scijava.ItemIO.OUTPUT;

import org.mastodon.feature.Dimension;
import org.mastodon.mamut.model.Model;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputer.class )
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
		{
			final String units = Dimension.POSITION.getUnits( model.getSpaceUnits(), model.getTimeUnits() );
			output = new SpotPositionFeature( units );
		}
	}

	@Override
	public void run()
	{
		// Nothing to do.
	}
}
