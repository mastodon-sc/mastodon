package net.trackmate.model;

import gnu.trove.list.TIntList;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.mempool.ByteMappedElement;

public class EdgeList extends PoolObjectList< Edge, ByteMappedElement >
{
	public EdgeList( final SpotCollection c )
	{
		super( c.edgePool );
	}

	public EdgeList( final SpotCollection c, final int initialCapacity )
	{
		super( c.edgePool, initialCapacity );
	}

	protected EdgeList( final EdgeList list, final TIntList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public EdgeList subList( final int fromIndex, final int toIndex )
	{
		return new EdgeList( this, getIndexCollection().subList( fromIndex, fromIndex ) );
	}
}
