package net.trackmate.model;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.model.AdditionalFeatures.Feature;

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
		return String.format( "Edge( %d -> %d )", getSource().getId(), getTarget().getId() );
	}

	Edge( final AbstractEdgePool< ?, ByteMappedElement, Spot > pool, final AdditionalFeatures additionalEdgeFeatures )
	{
		super( pool );
		this.additionalFeatures = additionalEdgeFeatures;
	}
}
