package org.mastodon.mamut.feature;

import org.mastodon.revised.model.mamut.Model;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkUpdateStackComputer.class, visible = false )
public class LinkUpdateStackComputer implements MamutFeatureComputer
{

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private LinkUpdateStack output;

	@Override
	public void createOutput()
	{
		output = LinkUpdateStack.getOrCreate( model.getFeatureModel(), model.getGraph().edges() );
	}

	@Override
	public void run()
	{
		// Do nothing.
	}
}
