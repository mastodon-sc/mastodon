package net.trackmate;


import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.Localizable;
import net.imglib2.Sampler;

public class ImgSpot extends AbstractEuclideanSpace implements Spot
{
	private long id;

	private SpotType data;

	protected ImgSpot( final int numDimensions )
	{
		super( numDimensions );
	}

	protected < T extends Sampler< SpotType > & Localizable > void update( final T access )
	{
		this.id = access.getLongPosition( 0 );
		this.data = access.get();
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public double getX()
	{
		return data.getX();
	}

	@Override
	public double getY()
	{
		return data.getY();
	}

	@Override
	public double getZ()
	{
		return data.getZ();
	}

	public void setX( final double x )
	{
		data.setX( x );
	}

	public void setY( final double y )
	{
		data.setY( y );
	}

	public void setZ( final double z )
	{
		data.setZ( z );
	}

	@Override
	public void localize( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = ( float ) data.getCoordinate( d );
	}

	@Override
	public void localize( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = data.getCoordinate( d );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) data.getCoordinate( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return data.getCoordinate( d );
	}

}
