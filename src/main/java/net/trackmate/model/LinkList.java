package net.trackmate.model;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;

public class LinkList extends PoolObjectList< Link >
{
	public LinkList( final SpotCollection c )
	{
		super( c.linkPool );
	}

	public LinkList( final SpotCollection c, final int initialCapacity )
	{
		super( c.linkPool, initialCapacity );
	}

	protected LinkList( final LinkList list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public LinkList subList( final int fromIndex, final int toIndex )
	{
		return new LinkList( this, ( TIntArrayList ) getIndexCollection().subList( fromIndex, fromIndex ) );
	}
}
