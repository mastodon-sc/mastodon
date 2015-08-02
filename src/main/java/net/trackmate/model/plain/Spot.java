package net.trackmate.model.plain;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.model.AbstractSpot;
import net.trackmate.model.Link;

/**
 * Plain, generic-free, implementation of {@link AbstractSpot}.
 * <p>
 * The spot shape is set solely by a radius.
 *
 * @author Jean-Yves Tinevez
 */
public class Spot extends AbstractSpot< Spot >
{
	// Copied to be package-visible.
	protected static final int X_OFFSET = AbstractSpot.X_OFFSET; 
	protected static final int RADIUS_OFFSET = TP_OFFSET + DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = RADIUS_OFFSET + DOUBLE_SIZE;

	Spot( final AbstractVertexPool< Spot, Link< Spot >, ByteMappedElement > pool )
	{
		super( pool );
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( %d, X=%.2f, Y=%.2f, Z=%.2f, tp=%d )", getInternalPoolIndex(), getX(), getY(), getZ(), getTimePoint() );
	}

	Spot init( final int timepointId, final double x, final double y, final double z, final double radius )
	{
		setX( x );
		setY( y );
		setZ( z );
		setTimePointId( timepointId );
		setRadius( radius );
		return this;
	}

	public void setRadius( final double radius )
	{
		access.putDouble( radius, RADIUS_OFFSET );
	}

	public double getRadius()
	{
		return access.getDouble( RADIUS_OFFSET );
	}

}
