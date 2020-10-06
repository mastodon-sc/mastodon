package org.mastodon.views.bvv.pool.attributes;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.pool.AbstractAttribute;
import org.mastodon.pool.MappedElement;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.PoolObjectLayout.FloatArrayField;
import org.mastodon.views.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.views.bvv.pool.PoolObjectLayoutJoml.Vector3fField;

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
		final MappedElement access = access( key );
		access.putFloat( value.x(), offset );
		access.putFloat( value.y(), offset + 1 * FLOAT_SIZE );
		access.putFloat( value.z(), offset + 2 * FLOAT_SIZE );
	}

	public void set( final O key, final Vector3fc value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public void setQuiet( final O key, final Vector3fAttributeReadOnlyValue value )
	{
		final MappedElement access = access( key );
		access.putFloat( value.x(), offset );
		access.putFloat( value.y(), offset + 1 * FLOAT_SIZE );
		access.putFloat( value.z(), offset + 2 * FLOAT_SIZE );
	}

	public void set( final O key, final Vector3fAttributeReadOnlyValue value )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, value );
		notifyPropertyChanged( key );
	}

	public void setQuiet( final O key, final float x, final float y, final float z )
	{
		final MappedElement access = access( key );
		access.putFloat( x, offset );
		access.putFloat( y, offset + 1 * FLOAT_SIZE );
		access.putFloat( z, offset + 2 * FLOAT_SIZE );
	}

	public void set( final O key, final float x, final float y, final float z )
	{
		notifyBeforePropertyChange( key );
		setQuiet( key, x, y, z );
		notifyPropertyChanged( key );
	}

	public void setZeroQuiet( final O key )
	{
		final MappedElement access = access( key );
		access.putFloat( 0, offset );
		access.putFloat( 0, offset + 1 * FLOAT_SIZE );
		access.putFloat( 0, offset + 2 * FLOAT_SIZE );
	}

	public void setZero( final O key )
	{
		notifyBeforePropertyChange( key );
		setZeroQuiet( key );
		notifyPropertyChanged( key );
	}

	public Vector3f get( final O key, Vector3f dest )
	{
		final MappedElement access = access( key );
		dest.set(
				access.getFloat( offset ),
				access.getFloat( offset + 1 * FLOAT_SIZE ),
				access.getFloat( offset + 2 * FLOAT_SIZE ) );
		return dest;
	}

	public float x( final O key )
	{
		return access( key ).getFloat( offset );
	}

	public float y( final O key )
	{
		return access( key ).getFloat( offset + 1 * FLOAT_SIZE );
	}

	public float z( final O key )
	{
		return access( key ).getFloat( offset + 2 * FLOAT_SIZE );
	}

	public Vector3fAttributeValue createAttributeValue( final O key )
	{
		return new AbstractAttributeValue< O >( Vector3fAttribute.this, key )
		{
			@Override
			public void set( final Vector3fc value )
			{
				attribute.set( key, value );
			}

			@Override
			public void set( final float x, final float y, final float z )
			{
				attribute.set( key, x, y, z );
			}

			@Override
			public void set( final Vector3fAttributeReadOnlyValue value )
			{
				attribute.set( key, value );
			}

			@Override
			public void zero()
			{
				attribute.setZero( key );
			}
		};
	}

	public Vector3fAttributeValue createQuietAttributeValue( final O key )
	{
		return new AbstractAttributeValue< O >( Vector3fAttribute.this, key )
		{
			@Override
			public void set( final Vector3fc value )
			{
				attribute.setQuiet( key, value );
			}

			@Override
			public void set( final float x, final float y, final float z )
			{
				attribute.setQuiet( key, x, y, z );
			}

			@Override
			public void set( final Vector3fAttributeReadOnlyValue value )
			{
				attribute.setQuiet( key, value );
			}

			@Override
			public void zero()
			{
				attribute.setZeroQuiet( key );
			}
		};
	}

	private abstract static class AbstractAttributeValue< O extends PoolObject< O, ?, ? > > implements Vector3fAttributeValue
	{
		final Vector3fAttribute< O > attribute;

		final O key;

		AbstractAttributeValue( final Vector3fAttribute< O > attribute, final O key )
		{
			this.attribute = attribute;
			this.key = key;
		}

		@Override
		public Vector3f get( final Vector3f dest )
		{
			return attribute.get( key, dest );
		}

		@Override
		public float x()
		{
			return attribute.x( key );
		}

		@Override
		public float y()
		{
			return attribute.y( key );
		}

		@Override
		public float z()
		{
			return attribute.z( key );
		}
	}

}
