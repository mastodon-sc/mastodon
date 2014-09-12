package net.trackmate.model;

import static net.trackmate.util.mempool.ByteUtils.DOUBLE_SIZE;
import net.trackmate.model.abstractmodel.AbstractEdge;
import net.trackmate.model.abstractmodel.AbstractEdgePool;
import net.trackmate.model.abstractmodel.AdditionalFeatures;
import net.trackmate.model.abstractmodel.AdditionalFeatures.Feature;
import net.trackmate.util.mempool.ByteMappedElement;

public class Edge extends AbstractEdge< ByteMappedElement, Spot >
{
	protected static final int WEIGHT_OFFSET = AbstractEdge.SIZE_IN_BYTES;
	protected static final int SIZE_IN_BYTES = WEIGHT_OFFSET +  + DOUBLE_SIZE;

	private final AdditionalFeatures additionalFeatures;

	public Edge init( final double weight )
	{
		setWeight( weight );
		return this;
	}

	public double getWeight()
	{
		return access.getDouble( WEIGHT_OFFSET );
	}

	public void setWeight( final double weight )
	{
		access.putDouble( weight, WEIGHT_OFFSET );
	}

	public Spot getSourceSpot()
	{
		return super.getSourceSpot( spotPool.createEmptyRef() );
	}

	@Override
	public Spot getSourceSpot( final Spot spot )
	{
		return super.getSourceSpot( spot );
	}

	public Spot getTargetSpot()
	{
		return super.getTargetSpot( spotPool.createEmptyRef() );
	}

	@Override
	public Spot getTargetSpot( final Spot spot )
	{
		return super.getTargetSpot( spot );
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
		return String.format( "Edge( %d -> %d )", getSourceSpot().getId(), getTargetSpot().getId() );
	}

	Edge( final AbstractEdgePool< ?, ByteMappedElement, Spot > pool, final AdditionalFeatures additionalEdgeFeatures )
	{
		super( pool );
		this.additionalFeatures = additionalEdgeFeatures;
	}
}
