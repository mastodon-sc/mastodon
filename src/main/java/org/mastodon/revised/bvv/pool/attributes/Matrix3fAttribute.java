package org.mastodon.revised.bvv.pool.attributes;

import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.mastodon.pool.AbstractAttribute;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.PoolObjectLayout.FloatArrayField;
import org.mastodon.revised.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.revised.bvv.pool.PoolObjectLayoutJoml.Matrix3fField;

import static org.mastodon.pool.ByteUtils.FLOAT_SIZE;

public class Matrix3fAttribute< O extends PoolObject< O, ?, ? > >
	extends AbstractAttribute< O >
{
	private final int offset;

	public Matrix3fAttribute( final Matrix3fField layoutField, final Pool< O, ? > pool )
	{
		super( layoutField, pool );
		this.offset = layoutField.getOffset();
	}

	public void setQuiet( final O key, final Matrix3fc value )
	{
		access( key ).putFloat( value.m00(), offset );
		access( key ).putFloat( value.m01(), offset + 1 * FLOAT_SIZE );
		access( key ).putFloat( value.m02(), offset + 2 * FLOAT_SIZE );
		access( key ).putFloat( value.m10(), offset + 3 * FLOAT_SIZE );
		access( key ).putFloat( value.m11(), offset + 4 * FLOAT_SIZE );
		access( key ).putFloat( value.m12(), offset + 5 * FLOAT_SIZE );
		access( key ).putFloat( value.m20(), offset + 6 * FLOAT_SIZE );
		access( key ).putFloat( value.m21(), offset + 7 * FLOAT_SIZE );
		access( key ).putFloat( value.m22(), offset + 8 * FLOAT_SIZE );
	}

	public void set( final O key, final Matrix3fc value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public void setQuiet( final O key, final float[] value )
	{
		access( key ).putFloat( value[ 0 ], offset );
		access( key ).putFloat( value[ 1 ], offset + 1 * FLOAT_SIZE );
		access( key ).putFloat( value[ 2 ], offset + 2 * FLOAT_SIZE );
		access( key ).putFloat( value[ 3 ], offset + 3 * FLOAT_SIZE );
		access( key ).putFloat( value[ 4 ], offset + 4 * FLOAT_SIZE );
		access( key ).putFloat( value[ 5 ], offset + 5 * FLOAT_SIZE );
		access( key ).putFloat( value[ 6 ], offset + 6 * FLOAT_SIZE );
		access( key ).putFloat( value[ 7 ], offset + 7 * FLOAT_SIZE );
		access( key ).putFloat( value[ 8 ], offset + 8 * FLOAT_SIZE );
	}

	public void set( final O key, final float[] value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public Matrix3f get( final O key, Matrix3f dest )
	{
		dest.set(
				access( key ).getFloat( offset ),
				access( key ).getFloat( offset + 1 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 2 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 3 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 4 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 5 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 6 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 7 * FLOAT_SIZE ),
				access( key ).getFloat( offset + 8 * FLOAT_SIZE ) );
		return dest;
	}

	public void setIdentityQuiet( final O key )
	{
		access( key ).putFloat( 1, offset );
		access( key ).putFloat( 0, offset + 1 * FLOAT_SIZE );
		access( key ).putFloat( 0, offset + 2 * FLOAT_SIZE );
		access( key ).putFloat( 0, offset + 3 * FLOAT_SIZE );
		access( key ).putFloat( 1, offset + 4 * FLOAT_SIZE );
		access( key ).putFloat( 0, offset + 5 * FLOAT_SIZE );
		access( key ).putFloat( 0, offset + 6 * FLOAT_SIZE );
		access( key ).putFloat( 0, offset + 7 * FLOAT_SIZE );
		access( key ).putFloat( 1, offset + 8 * FLOAT_SIZE );
	}

	public void setIdentity( final O key )
	{
		notifyBeforePropertyChange( key );
		setIdentityQuiet( key );
		notifyPropertyChanged( key );
	}

	public Matrix3fAttributeValue createAttributeValue( final O key )
	{
		return new Matrix3fAttributeValue()
		{
			@Override
			public Matrix3f get( final Matrix3f dest )
			{
				return Matrix3fAttribute.this.get( key, dest );
			}

			@Override
			public void set( final Matrix3fc value )
			{
				Matrix3fAttribute.this.set( key, value );
			}

			@Override
			public void set( final float[] value )
			{
				Matrix3fAttribute.this.set( key, value );
			}

			@Override
			public void identity()
			{
				Matrix3fAttribute.this.setIdentity( key );
			}
		};
	}

	public Matrix3fAttributeValue createQuietAttributeValue( final O key )
	{
		return new Matrix3fAttributeValue()
		{
			@Override
			public Matrix3f get( final Matrix3f dest )
			{
				return Matrix3fAttribute.this.get( key, dest );
			}

			@Override
			public void set( final Matrix3fc value )
			{
				Matrix3fAttribute.this.setQuiet( key, value );
			}

			@Override
			public void set( final float[] value )
			{
				Matrix3fAttribute.this.setQuiet( key, value );
			}

			@Override
			public void identity()
			{
				Matrix3fAttribute.this.setIdentityQuiet( key );
			}
		};
	}
}
