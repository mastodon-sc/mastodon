package org.mastodon.views.bvv.scene;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class CylinderMath
{
	public void set( final Vector3fc from, final Vector3fc to, final Cylinder cylinder )
	{
		// TODO: avoid object creation (reuse joml objects)
		final Vector3f k = to.sub( from, new Vector3f() );
		final float length = k.length();
		if ( length < 0.0001 )
		{
			throw new RuntimeException( "TODO. not implemented yet" );
			// need to set some default parameters here
		}
		k.mul( 1.0f / length );
		final Vector3f ei = new Vector3f().setComponent( k.minComponent(), 1 );

		final Vector3f l = k.cross( ei, new Vector3f() );
		final Vector3f m = l.cross( k, new Vector3f() );

		final Matrix3f E = new Matrix3f( l, m, k );
		final Matrix3f Einv = E.scale( 1, 1, 1 / length, new Matrix3f() );
		E.scale( 1, 1, length );

		cylinder.e.set( E );
		cylinder.invte.set( Einv );
		cylinder.t.set( from );
	}
}
