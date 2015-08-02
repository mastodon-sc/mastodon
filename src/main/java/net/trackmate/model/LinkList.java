package net.trackmate.model;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;

public class LinkList< V extends AbstractSpot< V > > extends PoolObjectList< Link< V > >
{
	public LinkList( final ModelGraph< V > c )
	{
		super( c.getLinkPool() );
	}

	public LinkList( final ModelGraph< V > c, final int initialCapacity )
	{
		super( c.getLinkPool(), initialCapacity );
	}

	protected LinkList( final LinkList< V > list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public LinkList< V > subList( final int fromIndex, final int toIndex )
	{
		return new LinkList< V >( this, ( TIntArrayList ) getIndexCollection().subList( fromIndex, fromIndex ) );
	}
}
