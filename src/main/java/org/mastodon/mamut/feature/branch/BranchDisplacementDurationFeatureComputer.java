package org.mastodon.mamut.feature.branch;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputer.class )
public class BranchDisplacementDurationFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchDisplacementDurationFeature output;

	@Override
	public void createOutput()
	{
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		if ( null == output )
			output = new BranchDisplacementDurationFeature(
					new DoublePropertyMap<>( branchGraph.edges().getRefPool(), Double.NaN ),
					new DoublePropertyMap<>( branchGraph.edges().getRefPool(), Double.NaN ),
					model.getSpaceUnits() );
	}

	@Override
	public void run()
	{
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		final BranchSpot ref1 = branchGraph.vertexRef();
		final BranchSpot ref2 = branchGraph.vertexRef();
		try
		{
			for ( final BranchLink bl : branchGraph.edges() )
			{
				final BranchSpot source = bl.getSource( ref1 );
				final BranchSpot target = bl.getTarget( ref2 );
				double d2 = 0.;
				for ( int d = 0; d < 3; d++ )
				{
					final double dx = source.getDoublePosition( d ) - target.getDoublePosition( d );
					d2 += dx * dx;
				}
				output.dispMap.set( bl, Math.sqrt( d2 ) );
				final double t1 = target.getTimepoint();
				final double t2 = source.getTimepoint();
				output.durMap.set( bl, Math.abs( t2 - t1 ) );
			}
		}
		finally
		{
			branchGraph.releaseRef( ref1 );
			branchGraph.releaseRef( ref2 );
		}
	}
}
