package org.mastodon.mamut.feature.branch;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputer.class )
public class BranchDepthFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private ModelBranchGraph branchGraph;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchDepthFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchDepthFeature( new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		final BranchSpot ref = branchGraph.vertexRef();
		try
		{
			final DepthFirstIterator< BranchSpot, BranchLink > it = new DepthFirstIterator<>( branchGraph );
			final RefSet< BranchSpot > roots = RootFinder.getRoots( branchGraph );
			for ( final BranchSpot root : roots )
			{
				output.map.set( root, 0 );

				it.reset( root );
				while ( it.hasNext() )
				{
					final BranchSpot current = it.next();
					int level = 0;
					for ( final BranchLink edge : current.incomingEdges() )
					{
						final BranchSpot source = edge.getSource( ref );
						final int sl = output.map.getInt( source );
						level = Math.max( level, sl + 1 );
					}
					output.map.set( current, level );
				}
			}
		}
		finally
		{
			branchGraph.releaseRef( ref );
		}
	}
}
