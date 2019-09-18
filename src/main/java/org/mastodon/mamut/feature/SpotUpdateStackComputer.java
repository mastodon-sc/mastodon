package org.mastodon.mamut.feature;

import org.mastodon.revised.model.mamut.Model;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotUpdateStackComputer.class, visible = false )
public class SpotUpdateStackComputer implements MamutFeatureComputer
{

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotUpdateStack output;

	@Override
	public void createOutput()
	{
		output = SpotUpdateStack.getOrCreate( model.getFeatureModel(), model.getGraph().vertices() );
	}

	@Override
	public void run()
	{
		// Do nothing.
	}
}
