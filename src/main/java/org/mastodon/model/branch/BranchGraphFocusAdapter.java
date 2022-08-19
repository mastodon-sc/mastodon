package org.mastodon.model.branch;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch_v2.BranchGraphV2;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.scijava.listeners.Listeners;

public class BranchGraphFocusAdapter<
	V extends Vertex< E >,
	E extends Edge< V >,
	BV extends Vertex< BE >,
	BE extends Edge< BV > >
		extends AbstractBranchGraphAdapter< V, E, BV, BE >
		implements FocusModel< BV, BE >
{

	private final FocusModel< V, E > focus;

	public BranchGraphFocusAdapter(
			final BranchGraphV2< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final FocusModel< V, E > focus )
	{
		super( branchGraph, graph, idmap );
		this.focus = focus;
	}

	@Override
	public void focusVertex( final BV vertex )
	{
		if ( null == vertex )
			focus.focusVertex( null );
		else
		{
			final V vRef = graph.vertexRef();
			final V v = branchGraph.getLastLinkedVertex( vertex, vRef );
			focus.focusVertex( v );
			graph.releaseRef( vRef );
		}
	}

	@Override
	public BV getFocusedVertex( final BV ref )
	{
		final V vref = graph.vertexRef();
		final V focused = focus.getFocusedVertex( vref );
		if ( focused == null )
		{
			graph.releaseRef( vref );
			return null;
		}

		final BV bv = branchGraph.getBranchVertex( focused, ref );
		graph.releaseRef( vref );
		return bv;
	}

	@Override
	public Listeners< FocusListener > listeners()
	{
		return focus.listeners();
	}
}
