package org.mastodon.views.bvv.scene;

import org.joml.Matrix3fc;
import org.joml.Vector3fc;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.views.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.views.bvv.pool.attributes.Matrix3fAttributeValue;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttributeValue;

/**
 * Transformation of a shape (ellipsoid, cylinder) consisting of scaling, rotation, and translation.
 *
 * @author Tobias Pietzsch
 */
class ShapeTransform extends PoolObject< ShapeTransform, ShapeTransformPool, BufferMappedElement >
{
	public static class EllipsoidShapeLayout extends PoolObjectLayoutJoml
	{
		final Matrix3fField mat3fE = matrix3fField();
		final Matrix3fField mat3fInvE = matrix3fField();
		final Vector3fField vec3fT = vector3fField();
	}

	public static EllipsoidShapeLayout layout = new EllipsoidShapeLayout();

	public final Matrix3fAttributeValue e;
	public final Matrix3fAttributeValue inve;
	public final Vector3fAttributeValue t;

	ShapeTransform( final ShapeTransformPool pool )
	{
		super( pool );
		e = pool.mat3fE.createAttributeValue( this );
		inve = pool.mat3fInvE.createAttributeValue( this );
		t = pool.vec3fT.createAttributeValue( this );
	}

	public ShapeTransform init()
	{
		e.identity();
		inve.identity();
		t.zero();
		return this;
	}

	public void set( ShapeTransform other )
	{
		this.e.set( other.e );
		this.inve.set( other.inve );
		this.t.set( other.t );
	}

	public void set(
			final Matrix3fc e,
			final Matrix3fc inve,
			final Vector3fc t )
	{
		this.e.set( e );
		this.inve.set( inve );
		this.t.set( t );
	}

	@Override
	protected void setToUninitializedState()
	{}

	@Override
	public String toString()
	{
		return String.format( "Ellipsoid(%d, pos=%s, e=%s, e^-1=%s)",
				getInternalPoolIndex(),
				t.get().toString(),
				e.get().toString(),
				inve.get().toString() );
	}
}
