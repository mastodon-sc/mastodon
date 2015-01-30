package net.trackmate.trackscheme;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.trackscheme.ScreenVertex.ScreenVertexPool;

public class ScreenVertexList extends PoolObjectList< ScreenVertex, ByteMappedElement >
{
	public ScreenVertexList( final ScreenVertexPool pool )
	{
		super( pool );
	}

	public ScreenVertexList( final ScreenVertexPool pool, final int initialCapacity )
	{
		super( pool, initialCapacity );
	}

	protected ScreenVertexList( final ScreenVertexList list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public ScreenVertexList subList( final int fromIndex, final int toIndex )
	{
		return new ScreenVertexList( this, ( TIntArrayList ) getIndexCollection().subList( fromIndex, fromIndex ) );
	}
}
