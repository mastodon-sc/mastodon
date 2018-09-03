package org.mastodon.mamut.feature;

import static org.scijava.ItemIO.OUTPUT;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotFrameComputer.class )
public class SpotFrameComputer implements MamutFeatureComputer
{

	private static final String KEY = "Spot frame";

	@Parameter( type = OUTPUT )
	private SpotFrameFeature output;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotFrameFeature, Spot >
	{
		public Spec()
		{
			super( KEY, SpotFrameFeature.class, Spot.class, KEY );
		}
	}

	@Override
	public void createOutput()
	{
		output = new SpotFrameFeature();
	}

	@Override
	public void run()
	{
		// Nothing to do.
	}
}
