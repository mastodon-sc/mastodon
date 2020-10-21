package org.mastodon.views.bvv.scene;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class CylinderMath
{
	private final Spot vref;

	public CylinderMath()
	{
		vref = null;
	}

	public CylinderMath( final ModelGraph graph )
	{
		vref = graph.vertexRef();
	}

	public void set( final Vector3fc from, final Vector3fc to, final Cylinder cylinder )
	{
		// TODO: avoid object creation (reuse joml objects)
		final Vector3f k = to.sub( from, new Vector3f() );
		final float length = k.length();
		if ( length < 1e-12f )
		{
			cylinder.e.zero();
			cylinder.invte.zero();
			cylinder.t.zero();
			return;
		}
		k.mul( 1 / length );
		final Vector3f l = new Vector3f();
		final Vector3f m = new Vector3f();
		perpendicular(k, l, m);

		final Matrix3f E = new Matrix3f( l, m, k );
		final Matrix3f Einv = E.scale( 1, 1, 1 / length, new Matrix3f() );
		E.scale( 1, 1, length );

		cylinder.e.set( E );
		cylinder.invte.set( Einv );
		cylinder.t.set( from );
	}

	public void setFromEdge( final Link edge, final Cylinder cylinder )
	{
		final Spot source = edge.getSource( vref );
		final float sx = source.getFloatPosition( 0 );
		final float sy = source.getFloatPosition( 1 );
		final float sz = source.getFloatPosition( 2 );

		final Spot target = edge.getTarget( vref );
		final float tx = target.getFloatPosition( 0 );
		final float ty = target.getFloatPosition( 1 );
		final float tz = target.getFloatPosition( 2 );

		set( new Vector3f( sx, sy, sz ), new Vector3f( tx, ty, tz ), cylinder );
	}


	// TODO: This is a fixed version of JOML's GeometryUtils.perpendicular(). Replace
	//  once it has been fixed in a JOML release.
	static void perpendicular(float x, float y, float z, Vector3f dest1, Vector3f dest2) {
		float magX = z * z + y * y;
		float magY = z * z + x * x;
		float magZ = y * y + x * x;
		float mag;
		if (magX > magY && magX > magZ) {
			dest1.x = 0;
			dest1.y = z;
			dest1.z = -y;
			mag = magX;
		} else if (magY > magZ) {
			dest1.x = -z;
			dest1.y = 0;
			dest1.z = x;
			mag = magY;
		} else {
			dest1.x = y;
			dest1.y = -x;
			dest1.z = 0;
			mag = magZ;
		}
		float len = org.joml.Math.invsqrt(mag);
		dest1.x *= len;
		dest1.y *= len;
		dest1.z *= len;
		dest2.x = y * dest1.z - z * dest1.y;
		dest2.y = z * dest1.x - x * dest1.z;
		dest2.z = x * dest1.y - y * dest1.x;
	}

	static void perpendicular(Vector3fc v, Vector3f dest1, Vector3f dest2) {
		perpendicular(v.x(), v.y(), v.z(), dest1, dest2);
	}

}
