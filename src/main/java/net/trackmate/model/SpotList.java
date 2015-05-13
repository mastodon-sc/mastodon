package net.trackmate.model;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;

public class SpotList extends PoolObjectList< Spot >
{
	public SpotList( final SpotCollection c )
	{
		super( c.spotPool );
	}

	public SpotList( final SpotCollection c, final int initialCapacity )
	{
		super( c.spotPool, initialCapacity );
	}

	protected SpotList( final SpotList list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public SpotList subList( final int fromIndex, final int toIndex )
	{
		return new SpotList( this, ( TIntArrayList ) getIndexCollection().subList( fromIndex, toIndex ) );
	}
}
