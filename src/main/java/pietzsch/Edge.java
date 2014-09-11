package pietzsch;

import pietzsch.mappedelementpool.ByteMappedElement;
import pietzsch.mappedelementpool.Pool;
import pietzsch.spots.AbstractEdge;
import pietzsch.spots.AbstractSpot;
import pietzsch.spots.AbstractSpotPool;

public class Edge extends AbstractEdge< ByteMappedElement >
{
	private Edge( final Pool< ByteMappedElement > pool )
	{
		super( pool.createAccess() );
	}

	// TODO: remove. this is just for debugging
	public < S extends AbstractSpot< ?, ? > > String toString( final AbstractSpotPool< S, ?, ? > spots )
	{
		final S source = spots.createEmptySpotRef();
		spots.getByInternalPoolIndex( getSourceSpotInternalPoolIndex(), source );
		final S target = spots.createEmptySpotRef();
		spots.getByInternalPoolIndex( getTargetSpotInternalPoolIndex(), target );
		return String.format( "Edge( source=%d, target=%d )", source.getId(), target.getId() );
	}

	public static final AbstractEdge.Factory< Edge, ByteMappedElement > factory = new AbstractEdge.Factory< Edge, ByteMappedElement >()
	{
		@Override
		public int getEdgeSizeInBytes()
		{
			return SIZE_IN_BYTES;
		}

		@Override
		public Edge createEmptyEdgeRef( final Pool< ByteMappedElement > pool )
		{
			return new Edge( pool );
		}
	};
}
