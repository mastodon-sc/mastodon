package org.mastodon.revised.bvv.pool.attributes;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.pool.AbstractAttribute;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.PoolObjectLayout.FloatArrayField;
import org.mastodon.revised.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.revised.bvv.pool.PoolObjectLayoutJoml.Vector3fField;

import static org.mastodon.pool.ByteUtils.FLOAT_SIZE;

public class Vector3fAttribute< O extends PoolObject< O, ?, ? > >
	extends AbstractAttribute< O >
{
	private final int offset;

	public Vector3fAttribute( final Vector3fField layoutField, final Pool< O, ? > pool )
	{
		super( layoutField, pool );
		this.offset = layoutField.getOffset();
	}

	public void setQuiet( final O key, final Vector3fc value )
	{
		access( key ).putFloat( value.x(), offset );
		access( key ).putFloat( value.y(), offset + 1 * FLOAT_SIZE );
		access( key ).putFloat( value.z(), offset + 2 * FLOAT_SIZE );
	}

	public void set( final O key, final Vector3fc value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public void setQuiet( final O key, final float x, final float y, final float z )
	{
		access( key ).putFloat( x, offset );
		access( key ).putFloat( y, offset + 1 * FLOAT_SIZE );
		access( key ).putFloat( z, offset + 2 * FLOAT_SIZE );
	}

	public void set( final O key, final float x, final float y, final float z )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, x, y, z );
		notifyPropertyChanged( key );
	}

	public Vector3f get( final O key, Vector3f dest )
	{
		dest.set(
				access( key ).getFloat( offset ),
				access( key ).getFloat( offset + 1 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 2 * FLOAT_SIZE ) );
		return dest;
	}

	public void setZeroQuiet( final O key )
	{
		access( key ).putFloat( 0, offset );
		access( key ).putFloat( 0, offset + 1 * FLOAT_SIZE );
		access( key ).putFloat( 0, offset + 2 * FLOAT_SIZE );
	}

	public void setZero( final O key )
	{
		notifyBeforePropertyChange( key );
		setZeroQuiet( key );
		notifyPropertyChanged( key );
	}

	public Vector3fAttributeValue createAttributeValue( final O key )
	{
		return new Vector3fAttributeValue()
		{
			@Override
			public Vector3f get( final Vector3f dest )
			{
				return Vector3fAttribute.this.get( key, dest );
			}

			@Override
			public void set( final Vector3fc value )
			{
				Vector3fAttribute.this.set( key, value );
			}

			@Override
			public void set( final float x, final float y, final float z )
			{
				Vector3fAttribute.this.set( key, x, y, z );
			}

			@Override
			public void zero()
			{
				Vector3fAttribute.this.setZero( key );
			}
		};
	}

	public Vector3fAttributeValue createQuietAttributeValue( final O key )
	{
		return new Vector3fAttributeValue()
		{
			@Override
			public Vector3f get( final Vector3f dest )
			{
				return Vector3fAttribute.this.get( key, dest );
			}

			@Override
			public void set( final Vector3fc value )
			{
				Vector3fAttribute.this.setQuiet( key, value );
			}

			@Override
			public void set( final float x, final float y, final float z )
			{
				Vector3fAttribute.this.setQuiet( key, x, y, z );
			}

			@Override
			public void zero()
			{
				Vector3fAttribute.this.setZeroQuiet( key );
			}
		};
	}
}
