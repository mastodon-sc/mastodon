package net.trackmate.model;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;

public class LinkList extends PoolObjectList< Link >
{
	public LinkList( final ModelGraph c )
	{
		super( c.getLinkPool() );
	}

	public LinkList( final ModelGraph c, final int initialCapacity )
	{
		super( c.getLinkPool(), initialCapacity );
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
