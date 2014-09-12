package net.trackmate.model;

import net.trackmate.model.abstractmodel.PoolObjectSet;
import net.trackmate.util.mempool.ByteMappedElement;

public class EdgeSet extends PoolObjectSet< Edge, ByteMappedElement >
{
	public EdgeSet( final SpotCollection c )
	{
		super( c.edgePool );
	}

	public EdgeSet( final SpotCollection c, final int initialCapacity )
	{
		super( c.edgePool, initialCapacity );
	}
}
