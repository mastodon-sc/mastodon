package net.trackmate.util;

import net.imglib2.img.NativeImg;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.basictypeaccess.LongAccess;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.type.NativeType;

public abstract class LongMappedType< T extends LongMappedType< T > > implements NativeType< T >
{
	private int i = 0;

	private int startIndex = 0;

	protected NativeImg< ?, ? extends LongAccess > img;

	// the DataAccess that holds the information
	protected LongAccess dataAccess;

	@Override
	public T createVariable()
	{
		final T t = createUnitialized();
		t.img = null;
		t.dataAccess = new LongArray( getEntitiesPerPixel() );
		return t;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public T copy()
	{
		final T t = createVariable();
		t.img = null;
		t.dataAccess = new LongArray( getEntitiesPerPixel() );
		t.set( ( T ) this );
		return t;
	}

	@Override
	public T duplicateTypeOnSameNativeImg()
	{
		final T t = createUnitialized();
		t.img = img;
		return t;
	}

	@Override
	public void updateContainer( final Object c )
	{
		dataAccess = img.update( c );
	}

	@Override
	public NativeImg< T, ? > createSuitableNativeImg( final NativeImgFactory< T > imgFactory, final long[] dim )
	{
		final NativeImg< T, ? extends LongAccess > img = imgFactory.createLongInstance( dim, getEntitiesPerPixel() );
		final T linkedType = createUnitialized();
		linkedType.img = img;
		img.setLinkedType( linkedType );
		return img;
	}

	@Override
	public void updateIndex( final int index )
	{
		i = index;
		startIndex = index * getEntitiesPerPixel();
	}

	@Override
	public int getIndex()
	{
		return i;
	}

	@Override
	public void incIndex()
	{
		++i;
		startIndex += getEntitiesPerPixel();
	}

	@Override
	public void incIndex( final int increment )
	{
		i += increment;
		startIndex = i * getEntitiesPerPixel();
	}

	@Override
	public void decIndex()
	{
		--i;
		startIndex -= getEntitiesPerPixel();
	}

	@Override
	public void decIndex( final int decrement )
	{
		i -= decrement;
		startIndex = i * getEntitiesPerPixel();
	}

	protected long getLong( final int offset )
	{
		return dataAccess.getValue( startIndex + offset );
	}

	protected void setLong( final int offset, final long value )
	{
		dataAccess.setValue( startIndex + offset, value );
	}

	protected double getDouble( final int offset )
	{
		return Double.longBitsToDouble( dataAccess.getValue( startIndex + offset ) );
	}

	protected void setDouble( final int offset, final double value )
	{
		dataAccess.setValue( startIndex + offset, Double.doubleToRawLongBits( value ) );
	}

	protected abstract T createUnitialized();
}
