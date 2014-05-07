package net.trackmate;

import net.trackmate.util.LongMappedType;

public class SpotType extends LongMappedType< SpotType >
{
	private static final int OFFSET_X = 0;

	private static final int OFFSET_Y = 1;

	private static final int OFFSET_Z = 2;

	private static final int entitiesPerPixel = 3;

	public SpotType()
	{}

	public double getCoordinate( final int index )
	{
		return getDouble( OFFSET_X + index );
	}

	public void setCoordinate( final int index, final double value )
	{
		setDouble( OFFSET_X + index, value );
	}

	public double getX()
	{
		return getDouble( OFFSET_X );
	}

	public void setX( final double x )
	{
		setDouble( OFFSET_X, x );
	}

	public double getY()
	{
		return getDouble( OFFSET_Y );
	}

	public void setY( final double y )
	{
		setDouble( OFFSET_Y, y );
	}

	public double getZ()
	{
		return getDouble( OFFSET_Z );
	}

	public void setZ( final double z )
	{
		setDouble( OFFSET_Z, z );
	}

	@Override
	public int getEntitiesPerPixel()
	{
		return entitiesPerPixel;
	}

	@Override
	public void set( final SpotType c )
	{}

	@Override
	protected SpotType createUnitialized()
	{
		return new SpotType();
	}
}
