package org.mastodon.model.branch;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.scijava.listeners.Listeners;

public class BranchGraphFocusAdapter<
	V extends Vertex< E >,
	E extends Edge< V >,
	BV extends Vertex< BE >,
	BE extends Edge< BV > >
		implements FocusModel< BV, BE >
{

	private final BranchGraph< BV, BE, V, E > branchGraph;

	private final ReadOnlyGraph< V, E > graph;

	private final FocusModel< V, E > focus;

	public BranchGraphFocusAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final FocusModel< V, E > focus )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
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
			final V v = branchGraph.getLinkedVertex( vertex, vRef );
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
