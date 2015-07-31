package net.trackmate.model;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.model.AdditionalFeatures.Feature;

public class Link extends AbstractEdge< Link, Spot, ByteMappedElement >
{
	protected static final int WEIGHT_OFFSET = AbstractEdge.SIZE_IN_BYTES;
	protected static final int SIZE_IN_BYTES = WEIGHT_OFFSET + DOUBLE_SIZE;

	private final AdditionalFeatures additionalFeatures;

	public Link init( final double weight )
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
		return String.format( "Link( %d -> %d )", getSource().getId(), getTarget().getId() );
	}

	Link( final AbstractEdgePool< Link, Spot, ByteMappedElement > pool, final AdditionalFeatures additionalLinkFeatures )
	{
		super( pool );
		this.additionalFeatures = additionalLinkFeatures;
	}
}
