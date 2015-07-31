package net.trackmate.model;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import net.imglib2.RealLocalizable;
import net.trackmate.graph.AbstractIdVertex;
import net.trackmate.graph.AbstractIdVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.model.AdditionalFeatures.Feature;

public class Spot extends AbstractIdVertex< Spot, Link, ByteMappedElement > implements RealLocalizable
{
	protected static final int X_OFFSET = AbstractIdVertex.SIZE_IN_BYTES;
	protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;
	protected static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;
	protected static final int RADIUS_OFFSET = Z_OFFSET + DOUBLE_SIZE;
	protected static final int QUALITY_OFFSET = RADIUS_OFFSET + DOUBLE_SIZE;
	protected static final int FRAME_OFFSET = QUALITY_OFFSET + DOUBLE_SIZE;
	protected static final int VISIBILITY_OFFSET = FRAME_OFFSET + INT_SIZE;
	protected static final int SIZE_IN_BYTES = VISIBILITY_OFFSET + BOOLEAN_SIZE;

	private final AdditionalFeatures additionalFeatures;
	private final AbstractIdVertexPool< Spot, ?, ByteMappedElement > pool;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
	}

	public Spot init( final double x, final double y, final double z, final double radius, final double quality )
	{
		setX( x );
		setY( y );
		setZ( z );
		setRadius( radius );
		setQuality( quality );
		setVisibility( false );
		return this;
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

	public double getRadius()
	{
		return access.getDouble( RADIUS_OFFSET );
	}

	public void setRadius( final double radius )
	{
		access.putDouble( radius, RADIUS_OFFSET );
	}

	public double getQuality()
	{
		return access.getDouble( QUALITY_OFFSET );
	}

	public void setQuality( final double quality )
	{
		access.putDouble( quality, QUALITY_OFFSET );
	}

	public int getFrame()
	{
		return access.getInt( FRAME_OFFSET );
	}

	public void setFrame( final int frame )
	{
		access.putInt( frame, FRAME_OFFSET );
	}

	public boolean getVisibility()
	{
		return access.getBoolean( VISIBILITY_OFFSET );
	}

	public void setVisibility( final boolean frame )
	{
		access.putBoolean( frame, VISIBILITY_OFFSET );
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

	public Spot getNewReference()
	{
		final Spot spot = pool.createRef();
		spot.refTo( this );
		return spot;
	}

	public Spot getNewReference( final Spot newReference )
	{
		newReference.refTo( this );
		return newReference;
	}

	public void referenceTo( final Spot spot )
	{
		refTo( spot );
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( ID=%d, X=%.2f, Y=%.2f, Z=%.2f )", getId(), getX(), getY(), getZ() );
	}

	Spot( final AbstractIdVertexPool< Spot, ?, ByteMappedElement > pool, final AdditionalFeatures additionalSpotFeatures )
	{
		super( pool );
		this.pool = pool;
		this.additionalFeatures = additionalSpotFeatures;
	}

	// === RealLocalizable ===

	@Override
	public int numDimensions()
	{
		return 3;
	}

	@Override
	public void localize( final float[] position )
	{
		position[ 0 ] = ( float ) getX();
		position[ 1 ] = ( float ) getY();
		position[ 2 ] = ( float ) getZ();
	}

	@Override
	public void localize( final double[] position )
	{
		position[ 0 ] = getX();
		position[ 1 ] = getY();
		position[ 2 ] = getZ();
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) getDoublePosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return ( d == 0 ) ? getX() : ( ( d == 1 ) ? getY() : getZ() );
	}
}
