package org.mastodon.mamut.feature.branch;

import java.util.Iterator;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputer.class )
public class BranchNSpotsFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private ModelGraph graph;

	@Parameter
	private ModelBranchGraph branchGraph;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchNSpotsFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchNSpotsFeature( new IntPropertyMap<>( branchGraph.edges().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		for ( final BranchLink be : branchGraph.edges() )
		{
			int nspots = 0;
			final Iterator< Spot > it = branchGraph.vertexBranchIterator( be );
			while ( it.hasNext() )
			{
				it.next();
				nspots++;
			}
			output.map.set( be, nspots );
		}
	}
}
