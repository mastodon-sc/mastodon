package net.trackmate.model;

import net.trackmate.model.abstractmodel.AbstractEdge;
import net.trackmate.model.abstractmodel.AbstractEdgePool;
import net.trackmate.util.mempool.ByteMappedElement;

public class Edge extends AbstractEdge< ByteMappedElement, Spot >
{
	private Edge( final AbstractEdgePool< ?, ByteMappedElement, Spot > pool )
	{
		super( pool );
	}

	public static final AbstractEdge.Factory< Edge, ByteMappedElement, Spot > factory = new AbstractEdge.Factory< Edge, ByteMappedElement, Spot >()
	{
		@Override
		public int getEdgeSizeInBytes()
		{
			return SIZE_IN_BYTES;
		}

		@Override
		public Edge createEmptyEdgeRef( final AbstractEdgePool< ?, ByteMappedElement, Spot > pool )
		{
			return new Edge( pool );
		}
	};

	public Spot getSourceSpot()
	{
		return super.getSourceSpot( spotPool.createEmptySpotRef() );
	}

	@Override
	public Spot getSourceSpot( final Spot spot )
	{
		return super.getSourceSpot( spot );
	}

	public Spot getTargetSpot()
	{
		return super.getTargetSpot( spotPool.createEmptySpotRef() );
	}

	@Override
	public Spot getTargetSpot( final Spot spot )
	{
		return super.getTargetSpot( spot );
	}

	@Override
	public String toString()
	{
		return String.format( "Edge( %d -> %d )", getSourceSpot().getId(), getTargetSpot().getId() );
	}

}
