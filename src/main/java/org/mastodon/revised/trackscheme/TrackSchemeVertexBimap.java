package org.mastodon.revised.trackscheme;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class TrackSchemeVertexBimap< V extends Vertex< E >, E extends Edge< V > >
		implements RefBimap< V, TrackSchemeVertex >
{
	public TrackSchemeVertexBimap()
	{

	}

	@Override
	public V getLeft( final TrackSchemeVertex right )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrackSchemeVertex getRight( final V left, final TrackSchemeVertex ref )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V extractLeftRef( final TrackSchemeVertex ref )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
