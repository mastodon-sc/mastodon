package net.trackmate.trackscheme;

import gnu.trove.list.TIntList;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.mempool.ByteMappedElement;

public class TrackSchemeVertexList extends PoolObjectList< TrackSchemeVertex, ByteMappedElement >
{
	public TrackSchemeVertexList( final TrackSchemeGraph graph )
	{
		super( graph.getVertexPool() );
	}

	public TrackSchemeVertexList( final TrackSchemeGraph graph, final int initialCapacity )
	{
		super( graph.getVertexPool(), initialCapacity );
	}

	protected TrackSchemeVertexList( final TrackSchemeVertexList list, final TIntList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public TrackSchemeVertexList subList( final int fromIndex, final int toIndex )
	{
		return new TrackSchemeVertexList( this, getIndexCollection().subList( fromIndex, toIndex ) );
	}
}
