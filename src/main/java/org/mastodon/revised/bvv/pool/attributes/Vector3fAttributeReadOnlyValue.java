package org.mastodon.revised.bvv.pool.attributes;

import org.joml.Vector3f;

public interface Vector3fAttributeReadOnlyValue
{
	Vector3f get( final Vector3f dest );

	default Vector3f get()
	{
		return get( new Vector3f() );
	}

	float x();
	float y();
	float z();
}
