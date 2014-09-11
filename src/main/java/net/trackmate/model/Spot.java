package net.trackmate.model;

import static net.trackmate.util.mempool.ByteUtils.DOUBLE_SIZE;
import net.trackmate.model.abstractmodel.AbstractSpot;
import net.trackmate.model.abstractmodel.AbstractSpotPool;
import net.trackmate.model.abstractmodel.AdditionalFeatures;
import net.trackmate.model.abstractmodel.AdditionalFeatures.Feature;
import net.trackmate.model.abstractmodel.AllSpotEdges;
import net.trackmate.model.abstractmodel.IncomingSpotEdges;
import net.trackmate.model.abstractmodel.OutgoingSpotEdges;
import net.trackmate.util.mempool.ByteMappedElement;

public class Spot extends AbstractSpot< ByteMappedElement, Edge >
{
	protected static final int X_OFFSET = AbstractSpot.SIZE_IN_BYTES;
	protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;
	protected static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = Z_OFFSET + DOUBLE_SIZE;

	private final AdditionalFeatures additionalFeatures;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
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

	public void putFeature( final String feature, final double value )
	{
		additionalFeatures.putFeature( feature, value, getInternalPoolIndex() );
	}

	public Feature getFeature( final String feature, final Feature value )
	{
		return additionalFeatures.getFeature( feature, getInternalPoolIndex(), value );
	}

	public Double getFeature( final String feature )
	{
		return additionalFeatures.getFeature( feature, getInternalPoolIndex() );
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

	Spot( final AbstractSpotPool< Spot, ByteMappedElement, ? > pool, final AdditionalFeatures additionalSpotFeatures )
	{
		super( pool );
		this.additionalFeatures = additionalSpotFeatures;
	}
}
