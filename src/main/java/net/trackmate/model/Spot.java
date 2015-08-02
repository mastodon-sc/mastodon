package net.trackmate.model;

import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;

/**
 * Plain, generic-free, implementation of {@link AbstractSpot}.
 *
 * @author Jean-Yves Tinevez
 */
public class Spot extends AbstractSpot< Spot >
{

	Spot( final AbstractVertexPool< Spot, Link< Spot >, ByteMappedElement > pool )
	{
		super( pool );
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( %d, X=%.2f, Y=%.2f, Z=%.2f, tp=%d )", getInternalPoolIndex(), getX(), getY(), getZ(), getTimePoint() );
	}

}
