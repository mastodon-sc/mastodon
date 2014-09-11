package pietzsch;

import static pietzsch.mappedelementpool.ByteUtils.DOUBLE_SIZE;
import pietzsch.mappedelementpool.ByteMappedElement;
import pietzsch.spots.AbstractSpot;
import pietzsch.spots.AbstractSpotPool;
import pietzsch.spots.IncomingSpotEdges;
import pietzsch.spots.OutgoingSpotEdges;
import pietzsch.spots.AllSpotEdges;

public class Spot extends AbstractSpot< ByteMappedElement, Edge >
{
	public static final int X_OFFSET = AbstractSpot.SIZE_IN_BYTES;
	public static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;
	public static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;
	public static final int SIZE_IN_BYTES = Z_OFFSET + DOUBLE_SIZE;

	@Override
	protected void init()
	{
		super.init();
		setX( 0 );
		setY( 0 );
		setZ( 0 );
	}

	@Override
	public int getId()
	{
		return super.getId();
	}

	public double getX()
	{
		return access.getDouble( X_OFFSET );
	}

	public void setX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	public double getY()
	{
		return access.getDouble( Y_OFFSET );
	}

	public void setY( final double y )
	{
		access.putDouble( y, Y_OFFSET );
	}

	public double getZ()
	{
		return access.getDouble( Z_OFFSET );
	}

	public void setZ( final double z )
	{
		access.putDouble( z, Z_OFFSET );
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( ID=%d, X=%.2f, Y=%.2f, Z=%.2f )", getId(), getX(), getY(), getZ() );
	}

	@Override
	public IncomingSpotEdges< Edge > incomingEdges()
	{
		return super.incomingEdges();
	}

	@Override
	public OutgoingSpotEdges< Edge > outgoingEdges()
	{
		return super.outgoingEdges();
	}

	@Override
	public AllSpotEdges< Edge > edges()
	{
		return super.edges();
	}

	private Spot( final AbstractSpotPool< Spot, ByteMappedElement, ? > pool )
	{
		super( pool );
	}

	public static final AbstractSpot.Factory< Spot, ByteMappedElement > factory = new AbstractSpot.Factory< Spot, ByteMappedElement >()
	{
		@Override
		public int getSpotSizeInBytes()
		{
			return SIZE_IN_BYTES;
		}

		@Override
		public Spot createEmptySpotRef( final AbstractSpotPool< Spot, ByteMappedElement, ? > pool )
		{
			return new Spot( pool );
		}
	};
}
