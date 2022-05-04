package org.mastodon.mamut.feature.branch;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
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
		final Link eref = graph.edgeRef();
		final Spot vref = graph.vertexRef();
		final BranchSpot bvref = branchGraph.vertexRef();
		for ( final BranchLink be : branchGraph.edges() )
		{
			int nspots = 0;
			Link link = branchGraph.getLinkedEdge( be, eref );
			Spot target = link.getTarget( vref );
			while ( null == branchGraph.getBranchVertex( target, bvref ) && ( !target.outgoingEdges().isEmpty() ) )
			{
				link = target.outgoingEdges().get( 0, eref );
				target = link.getTarget( vref );
				nspots++;
			}
			output.map.set( be, nspots );
		}
		branchGraph.releaseRef( bvref );
		graph.releaseRef( vref );
		graph.releaseRef( eref );
	}
}
