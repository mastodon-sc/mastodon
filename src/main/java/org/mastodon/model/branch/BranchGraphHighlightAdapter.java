package org.mastodon.model.branch;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;
import org.scijava.listeners.Listeners;

public class BranchGraphHighlightAdapter<
	V extends Vertex< E >,
	E extends Edge< V >,
	BV extends Vertex< BE >,
	BE extends Edge< BV > >
		extends AbstractBranchGraphAdapter< V, E, BV, BE >
		implements HighlightModel< BV, BE >
{

	private final HighlightModel< V, E > highlight;

	public BranchGraphHighlightAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final HighlightModel< V, E > highlight )
	{
		super( branchGraph, graph, idmap );
		this.highlight = highlight;
	}

	@Override
	public void highlightVertex( final BV vertex )
	{
		if ( null == vertex )
		{
			highlight.highlightVertex( null );
			return;
		}

		final V vRef = graph.vertexRef();
		highlight.highlightVertex( branchGraph.getLastLinkedVertex( vertex, vRef ) );
		graph.releaseRef( vRef );
	}

	@Override
	public void highlightEdge( final BE edge )
	{
		if ( null == edge )
		{
			highlight.highlightVertex( null );
			return;
		}

		final E eRef = graph.edgeRef();
		highlight.highlightEdge( branchGraph.getLinkedEdge( edge, eRef ) );
		graph.releaseRef( eRef );
	}

	@Override
	public BV getHighlightedVertex( final BV ref )
	{
		final V vRef = graph.vertexRef();
		final E eRef = graph.edgeRef();
		try
		{
			final V highlighted = highlight.getHighlightedVertex( vRef );
			if ( null != highlighted )
				return branchGraph.getBranchVertex( highlighted, ref );

			final E highlightedEdge = highlight.getHighlightedEdge( eRef );
			if ( null != highlightedEdge )
				return branchGraph.getBranchVertex( highlightedEdge, ref );

			return null;

		}
		finally {
			graph.releaseRef( vRef );
			graph.releaseRef( eRef );
		}
	}


	@Override
	public BE getHighlightedEdge( final BE ref )
	{
		final E eRef = graph.edgeRef();
		try {
			final E highlightedEdge = highlight.getHighlightedEdge( eRef );
			if ( highlightedEdge == null )
				return null;

			return branchGraph.getBranchEdge( highlightedEdge, ref );
		}
		finally {
			graph.releaseRef( eRef );
		}
	}

	@Override
	public void clearHighlight()
	{
		highlight.clearHighlight();
	}

	@Override
	public Listeners< HighlightListener > listeners()
	{
		return highlight.listeners();
	}
}
