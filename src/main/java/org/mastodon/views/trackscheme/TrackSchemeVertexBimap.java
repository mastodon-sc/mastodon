package org.mastodon.views.trackscheme;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;

public class TrackSchemeVertexBimap< V extends Vertex< E >, E extends Edge< V > >
		implements RefBimap< V, TrackSchemeVertex >
{
	private final GraphIdBimap< V, E > idmap;

	private final TrackSchemeGraph< V, E > tsgraph;

	public TrackSchemeVertexBimap( final TrackSchemeGraph< V, E > tsgraph )
	{
		this.idmap = tsgraph.getGraphIdBimap();
		this.tsgraph = tsgraph;
	}

	@Override
	public V getLeft( final TrackSchemeVertex right )
	{
		return right == null ? null : idmap.getVertex( right.getModelVertexId(), reusableLeftRef( right ) );
	}

	@Override
	public TrackSchemeVertex getRight( final V left, final TrackSchemeVertex ref )
	{
		return left == null ? null : tsgraph.getTrackSchemeVertexForModelId( idmap.getVertexId( left ), ref );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public V reusableLeftRef( final TrackSchemeVertex ref )
	{
		return ( V ) ref.modelVertex.getReusableRef();
	}

	@Override
	public TrackSchemeVertex reusableRightRef()
	{
		return tsgraph.vertexRef();
	}

	@Override
	public void releaseRef( final TrackSchemeVertex ref )
	{
		tsgraph.releaseRef( ref );
	}
}
