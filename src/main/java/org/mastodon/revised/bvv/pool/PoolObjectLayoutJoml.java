package org.mastodon.revised.bvv.pool;

import org.mastodon.pool.PoolObjectLayout;

import static org.mastodon.pool.ByteUtils.*;

public abstract class PoolObjectLayoutJoml extends PoolObjectLayout
{

	// TODO extend PoolPbjectLayout as JomlPoolPbjectLayout and add vector3fField etc.
//	final FloatArrayField mat3fE = floatArrayField( 9 );

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
