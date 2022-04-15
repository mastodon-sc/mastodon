package org.mastodon.views.bdv.overlay.wrap;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.views.bdv.overlay.OverlayBranchGraph;

public class OverlayBranchGraphWrapper< 
	BV extends Vertex< BE >, 
	BE extends Edge< BV >,
	V extends Vertex< E >,
	E extends Edge< V >> 
		extends OverlayGraphWrapper< BV, BE >
		implements OverlayBranchGraph< OverlayVertexWrapper< BV, BE >, OverlayEdgeWrapper< BV, BE >, OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{

	private final BranchGraph< BV, BE, V, E > branchGraph;

	private final OverlayGraphWrapper< V, E > graphWrapper;

	public OverlayBranchGraphWrapper(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final GraphIdBimap< BV, BE > idmap,
			final SpatioTemporalIndex< BV > graphIndex,
			final ReentrantReadWriteLock lock,
			final OverlayProperties< BV, BE > overlayProperties,
			final OverlayGraphWrapper< V, E > graphWrapper )
	{
		super( branchGraph, idmap, graphIndex, lock, overlayProperties );
		this.branchGraph = branchGraph;
		this.graphWrapper = graphWrapper;
	}

	/**
	 * Exposes the overlay graph wrapper for the core graph (not the branch
	 * graph wrapper).
	 * 
	 * @return the overlay graph wrapper.
	 */
	public OverlayGraphWrapper< V, E > getGraphWrapper()
	{
		return graphWrapper;
	}

	@Override
	public OverlayEdgeWrapper< V, E > getLinkedEdge( final OverlayEdgeWrapper< BV, BE > obe, final OverlayEdgeWrapper< V, E > edge )
	{
		edge.we = branchGraph.getLinkedEdge( obe.we, edge.ref );
		return edge.orNull();
	}

	@Override
	public OverlayVertexWrapper< V, E > getLinkedVertex( final OverlayVertexWrapper< BV, BE > obv, final OverlayVertexWrapper< V, E > vertex )
	{
		vertex.wv = branchGraph.getLinkedVertex( obv.wv, vertex.ref );
		return vertex.orNull();
	}

	@Override
	public OverlayEdgeWrapper< BV, BE > getBranchEdge( final OverlayEdgeWrapper< V, E > edge, final OverlayEdgeWrapper< BV, BE > be )
	{
		be.we = branchGraph.getBranchEdge( edge.we, be.ref );
		return be.orNull();
	}

	@Override
	public OverlayEdgeWrapper< BV, BE > getBranchEdge( final OverlayVertexWrapper< V, E > vertex, final OverlayEdgeWrapper< BV, BE > be )
	{
		be.we = branchGraph.getBranchEdge( vertex.wv, be.ref );
		return be.orNull();
	}

	@Override
	public OverlayVertexWrapper< BV, BE > getBranchVertex( final OverlayVertexWrapper< V, E > vertex, final OverlayVertexWrapper< BV, BE > bv )
	{
		bv.wv = branchGraph.getBranchVertex( vertex.wv, bv.ref );
		return bv.orNull();
	}

	@Override
	public Iterator< OverlayVertexWrapper< V, E > > vertexBranchIterator( final OverlayEdgeWrapper< BV, BE > edge )
	{
		final Iterator< V > it = branchGraph.vertexBranchIterator( edge.we );
		final OverlayVertexWrapper< V, E > ref = graphWrapper.vertexRef();
		return new Iterator< OverlayVertexWrapper< V, E > >()
		{

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public OverlayVertexWrapper< V, E > next()
			{
				ref.wv = it.next();
				return ref;
			}
		};
	}

	@Override
	public Iterator< OverlayEdgeWrapper< V, E > > edgeBranchIterator( final OverlayEdgeWrapper< BV, BE > edge )
	{
		final Iterator< E > it = branchGraph.edgeBranchIterator( edge.we );
		final OverlayEdgeWrapper< V, E > ref = graphWrapper.edgeRef();
		return new Iterator< OverlayEdgeWrapper< V, E > >()
		{

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public OverlayEdgeWrapper< V, E > next()
			{
				ref.we = it.next();
				return ref;
			}
		};
	}
}
