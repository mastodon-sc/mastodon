package net.trackmate.model;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.PoolObjectList;

public class SpotList< V extends AbstractSpot< V >> extends PoolObjectList< V >
{
	public SpotList( final ModelGraph< V > c )
	{
		super( c.getVertexPool() );
	}

	public SpotList( final ModelGraph< V > c, final int initialCapacity )
	{
		super( c.getVertexPool(), initialCapacity );
	}

	protected SpotList( final SpotList< V > list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
	}

	@Override
	public SpotList< V > subList( final int fromIndex, final int toIndex )
	{
		return new SpotList< V >( this, ( TIntArrayList ) getIndexCollection().subList( fromIndex, toIndex ) );
	}
}
