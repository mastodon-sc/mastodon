package org.mastodon.revised.bvv.pool.attributes;

import org.joml.Matrix3f;

public interface Matrix3fAttributeReadOnlyValue
{
	Matrix3f get( final Matrix3f dest );

	default Matrix3f get()
	{
		return get( new Matrix3f() );
	}
}
