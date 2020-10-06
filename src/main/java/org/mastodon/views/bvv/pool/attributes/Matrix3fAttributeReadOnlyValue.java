package org.mastodon.views.bvv.pool.attributes;

import org.joml.Matrix3f;

public interface Matrix3fAttributeReadOnlyValue
{
	Matrix3f get( final Matrix3f dest );

	default Matrix3f get()
	{
		return get( new Matrix3f() );
	}

	float m00();
	float m01();
	float m02();
	float m10();
	float m11();
	float m12();
	float m20();
	float m21();
	float m22();
}
