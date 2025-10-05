package org.mastodon.mamut.views.trackscheme;

import org.mastodon.app.views.trackscheme.BranchTrackScheme.HierarchyTrackSchemeOverlayFactory;
import org.mastodon.app.views.trackscheme.MastodonViewTrackScheme2;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.MastodonModel;
import org.mastodon.views.trackscheme.LongEdgesLineageTreeLayout;

/**
 * Mamut app-specific TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewHierarchyTrackScheme2 extends MastodonViewTrackScheme2<
		MastodonModel< ModelBranchGraph, BranchSpot, BranchLink >,
		ModelBranchGraph,
		BranchSpot,
		BranchLink >
{

	private static final int MINIMUM_NUMBER_OF_HIERARCHY_LEVELS_SCROLLABLE = 30;

	public MamutViewHierarchyTrackScheme2( final MamutAppModel appModel )
	{
		super(
				appModel.dataModel().branchModel(),
				appModel.uiModel(),
				new MamutHierarchyTrackSchemeProperties( appModel.dataModel().branchModel().getGraph() ),
				new HierarchyTrackSchemeOverlayFactory(),
				LongEdgesLineageTreeLayout::new,
				0,
				Math.max( getMaxDepth( appModel.dataModel().getGraph() ), MINIMUM_NUMBER_OF_HIERARCHY_LEVELS_SCROLLABLE ) );
	}

	/**
	 * Returns the max depth of the graph, i.e. the maximum number of branching
	 * points on any path from a root to a leaf, when iterating depth-first, in
	 * a directed manner.
	 * 
	 * TODO: Use Graphs.maxDepth() once mastodon-graph is updated.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param <E>
	 *            the edge type
	 * @param graph
	 *            the graph to analyze.
	 * @return the max depth of the graph.
	 */
	private static < V extends Vertex< E >, E extends Edge< V > > int getMaxDepth( ReadOnlyGraph< V, E > graph )
	{
		DepthFirstIterator< V, E > it = new DepthFirstIterator<>( graph );
		RefSet< V > roots = RootFinder.getRoots( graph );
		int maxDepth = 0;
		for ( V root : roots )
		{
			it.reset( root );
			int depth = 0;
			while ( it.hasNext() )
			{
				V v = it.next();
				if ( v.outgoingEdges().size() > 1 )
					depth++;
			}
			if ( depth > maxDepth )
				maxDepth = depth;

		}
		return maxDepth;
	}
}
