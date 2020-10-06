package org.mastodon.views.bvv.pool;

import org.mastodon.pool.PoolObjectLayout;

import static org.mastodon.pool.ByteUtils.FLOAT_SIZE;

public abstract class PoolObjectLayoutJoml extends PoolObjectLayout
{
	public static class Matrix3fField extends PrimitiveField
	{
		Matrix3fField( final CurrentSizeInBytes sib )
		{
			super( sib, 9 * FLOAT_SIZE );
		}
	}

	/**
	 * Append a {@link Matrix3fField} to this {@link PoolObjectLayout}.
	 *
	 * @return the {@link Matrix3fField} specification
	 */
	protected Matrix3fField matrix3fField()
	{
		return new Matrix3fField( currentSizeInBytes );
	}

	public static class Vector3fField extends PrimitiveField
	{
		Vector3fField( final CurrentSizeInBytes sib )
		{
			super( sib, 3 * FLOAT_SIZE );
		}
	}

	/**
	 * Append a {@link Vector3fField} to this {@link PoolObjectLayout}.
	 *
	 * @return the {@link Vector3fField} specification
	 */
	protected Vector3fField vector3fField()
	{
		return new Vector3fField( currentSizeInBytes );
	}
}
