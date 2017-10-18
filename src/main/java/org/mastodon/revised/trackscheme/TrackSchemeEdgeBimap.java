package org.mastodon.revised.trackscheme;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;

public class TrackSchemeEdgeBimap< V extends Vertex< E >, E extends Edge< V > >
		implements RefBimap< E, TrackSchemeEdge >
{
	private final GraphIdBimap< V, E > idmap;

	private final TrackSchemeGraph< V, E > tsgraph;

	public TrackSchemeEdgeBimap(
			final TrackSchemeGraph< V, E > tsgraph )
	{
		this.idmap = tsgraph.getGraphIdBimap();
		this.tsgraph = tsgraph;
	}

	@Override
	public E getLeft( final TrackSchemeEdge right )
	{
		return right == null ? null : idmap.getEdge( right.getModelEdgeId(), reusableLeftRef( right ) );
	}

	@Override
	public TrackSchemeEdge getRight( final E left, final TrackSchemeEdge ref )
	{
		return left == null ? null : tsgraph.getTrackSchemeEdgeForModelId( idmap.getEdgeId( left ), ref );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public E reusableLeftRef( final TrackSchemeEdge ref )
	{
		return ( E ) ref.modelEdge.getReusableRef();
	}

	@Override
	public TrackSchemeEdge reusableRightRef()
	{
		return tsgraph.edgeRef();
	}

	@Override
	public void releaseRef( final TrackSchemeEdge ref )
	{
		tsgraph.releaseRef( ref );
	}
}
