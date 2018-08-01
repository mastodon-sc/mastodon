package org.mastodon.revised.bvv.pool.attributes;

import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.mastodon.pool.AbstractAttribute;
import org.mastodon.pool.MappedElement;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
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
		final MappedElement access = access( key );
		access.putFloat( value.m00(), offset );
		access.putFloat( value.m01(), offset + 1 * FLOAT_SIZE );
		access.putFloat( value.m02(), offset + 2 * FLOAT_SIZE );
		access.putFloat( value.m10(), offset + 3 * FLOAT_SIZE );
		access.putFloat( value.m11(), offset + 4 * FLOAT_SIZE );
		access.putFloat( value.m12(), offset + 5 * FLOAT_SIZE );
		access.putFloat( value.m20(), offset + 6 * FLOAT_SIZE );
		access.putFloat( value.m21(), offset + 7 * FLOAT_SIZE );
		access.putFloat( value.m22(), offset + 8 * FLOAT_SIZE );
	}

	public void set( final O key, final Matrix3fc value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public void setQuiet( final O key, final float[] value )
	{
		final MappedElement access = access( key );
		access.putFloat( value[ 0 ], offset );
		access.putFloat( value[ 1 ], offset + 1 * FLOAT_SIZE );
		access.putFloat( value[ 2 ], offset + 2 * FLOAT_SIZE );
		access.putFloat( value[ 3 ], offset + 3 * FLOAT_SIZE );
		access.putFloat( value[ 4 ], offset + 4 * FLOAT_SIZE );
		access.putFloat( value[ 5 ], offset + 5 * FLOAT_SIZE );
		access.putFloat( value[ 6 ], offset + 6 * FLOAT_SIZE );
		access.putFloat( value[ 7 ], offset + 7 * FLOAT_SIZE );
		access.putFloat( value[ 8 ], offset + 8 * FLOAT_SIZE );
	}

	public void set( final O key, final float[] value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public void setQuiet( final O key, final Matrix3fAttributeReadOnlyValue value )
	{
		final MappedElement access = access( key );
		access.putFloat( value.m00(), offset );
		access.putFloat( value.m01(), offset + 1 * FLOAT_SIZE );
		access.putFloat( value.m02(), offset + 2 * FLOAT_SIZE );
		access.putFloat( value.m10(), offset + 3 * FLOAT_SIZE );
		access.putFloat( value.m11(), offset + 4 * FLOAT_SIZE );
		access.putFloat( value.m12(), offset + 5 * FLOAT_SIZE );
		access.putFloat( value.m20(), offset + 6 * FLOAT_SIZE );
		access.putFloat( value.m21(), offset + 7 * FLOAT_SIZE );
		access.putFloat( value.m22(), offset + 8 * FLOAT_SIZE );
	}

	public void set( final O key, final Matrix3fAttributeReadOnlyValue value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public void setIdentityQuiet( final O key )
	{
		final MappedElement access = access( key );
		access.putFloat( 1, offset );
		access.putFloat( 0, offset + 1 * FLOAT_SIZE );
		access.putFloat( 0, offset + 2 * FLOAT_SIZE );
		access.putFloat( 0, offset + 3 * FLOAT_SIZE );
		access.putFloat( 1, offset + 4 * FLOAT_SIZE );
		access.putFloat( 0, offset + 5 * FLOAT_SIZE );
		access.putFloat( 0, offset + 6 * FLOAT_SIZE );
		access.putFloat( 0, offset + 7 * FLOAT_SIZE );
		access.putFloat( 1, offset + 8 * FLOAT_SIZE );
	}

	public void setIdentity( final O key )
	{
		notifyBeforePropertyChange( key );
		setIdentityQuiet( key );
		notifyPropertyChanged( key );
	}

	public Matrix3f get( final O key, Matrix3f dest )
	{
		final MappedElement access = access( key );
		dest.set(
				access.getFloat( offset ),
				access.getFloat( offset + 1 * FLOAT_SIZE ),
				access.getFloat( offset + 2 * FLOAT_SIZE ),
				access.getFloat( offset + 3 * FLOAT_SIZE ),
				access.getFloat( offset + 4 * FLOAT_SIZE ),
				access.getFloat( offset + 5 * FLOAT_SIZE ),
				access.getFloat( offset + 6 * FLOAT_SIZE ),
				access.getFloat( offset + 7 * FLOAT_SIZE ),
				access.getFloat( offset + 8 * FLOAT_SIZE ) );
		return dest;
	}

	public float m00( final O key )
	{
		return access( key ).getFloat( offset );
	}

	public float m01( final O key )
	{
		return access( key ).getFloat( offset + 1 * FLOAT_SIZE );
	}

	public float m02( final O key )
	{
		return access( key ).getFloat( offset + 2 * FLOAT_SIZE );
	}

	public float m10( final O key )
	{
		return access( key ).getFloat( offset + 3 * FLOAT_SIZE );
	}

	public float m11( final O key )
	{
		return access( key ).getFloat( offset + 4 * FLOAT_SIZE );
	}

	public float m12( final O key )
	{
		return access( key ).getFloat( offset + 5 * FLOAT_SIZE );
	}

	public float m20( final O key )
	{
		return access( key ).getFloat( offset + 6 * FLOAT_SIZE );
	}

	public float m21( final O key )
	{
		return access( key ).getFloat( offset + 7 * FLOAT_SIZE );
	}

	public float m22( final O key )
	{
		return access( key ).getFloat( offset + 8 * FLOAT_SIZE );
	}

	public Matrix3fAttributeValue createAttributeValue( final O key )
	{
		return new AbstractAttributeValue< O >( Matrix3fAttribute.this, key )
		{
			@Override
			public void set( final Matrix3fc value )
			{
				attribute.set( key, value );
			}

			@Override
			public void set( final float[] value )
			{
				attribute.set( key, value );
			}

			@Override
			public void set( final Matrix3fAttributeReadOnlyValue value )
			{
				attribute.set( key, value );
			}

			@Override
			public void identity()
			{
				attribute.setIdentity( key );
			}
		};
	}

	public Matrix3fAttributeValue createQuietAttributeValue( final O key )
	{
		return new AbstractAttributeValue< O >( Matrix3fAttribute.this, key )
		{
			@Override
			public void set( final Matrix3fc value )
			{
				attribute.setQuiet( key, value );
			}

			@Override
			public void set( final float[] value )
			{
				attribute.setQuiet( key, value );
			}

			@Override
			public void set( final Matrix3fAttributeReadOnlyValue value )
			{
				attribute.setQuiet( key, value );
			}

			@Override
			public void identity()
			{
				attribute.setIdentityQuiet( key );
			}
		};
	}

	private abstract static class AbstractAttributeValue< O extends PoolObject< O, ?, ? > > implements Matrix3fAttributeValue
	{
		final Matrix3fAttribute< O > attribute;

		final O key;

		AbstractAttributeValue( final Matrix3fAttribute< O > attribute, final O key )
		{
			this.attribute = attribute;
			this.key = key;
		}

		@Override
		public Matrix3f get( final Matrix3f dest )
		{
			return attribute.get( key, dest );
		}

		@Override
		public float m00()
		{
			return attribute.m00( key );
		}

		@Override
		public float m01()
		{
			return attribute.m01( key );
		}

		@Override
		public float m02()
		{
			return attribute.m02( key );
		}

		@Override
		public float m10()
		{
			return attribute.m10( key );
		}

		@Override
		public float m11()
		{
			return attribute.m11( key );
		}

		@Override
		public float m12()
		{
			return attribute.m12( key );
		}

		@Override
		public float m20()
		{
			return attribute.m20( key );
		}

		@Override
		public float m21()
		{
			return attribute.m21( key );
		}

		@Override
		public float m22()
		{
			return attribute.m22( key );
		}
	}
}
