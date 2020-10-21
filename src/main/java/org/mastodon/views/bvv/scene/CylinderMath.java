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
}
