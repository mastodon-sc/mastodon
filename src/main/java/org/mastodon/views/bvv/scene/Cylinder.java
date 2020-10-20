package org.mastodon.views.bvv.scene;

import org.mastodon.views.bvv.pool.attributes.Matrix3fAttributeValue;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttributeValue;

// bundle Shape and Color
public class Cylinder implements ModifiableRef< Cylinder >
{
	private final CylinderPool pool;

	final ShapeTransform shape;
	final Color color;

	/**
	 * Transform consisting of (anisotropic) scaling and rotation.
	 */
	public final Matrix3fAttributeValue e;

	/**
	 * Inverse transpose of {@link #e} (to transform normals).
	 */
	public final Matrix3fAttributeValue invte;

	/**
	 * Translation
	 */
	public final Vector3fAttributeValue t;

	/**
	 * RGB color
	 */
	public final Vector3fAttributeValue rgb;

	Cylinder( CylinderPool pool )
	{
		this.pool = pool;
		shape = pool.shapes.createRef();
		color = pool.colors.createRef();

		e = shape.e;
		invte = shape.invte;
		t = shape.t;
		rgb = color.color;
	}

	@Override
	public int getInternalPoolIndex()
	{
		return shape.getInternalPoolIndex();
	}

	@Override
	public Cylinder refTo( final Cylinder obj )
	{
		return pool.getObject( obj.getInternalPoolIndex(), this );
	}

	@Override
	public void set( final Cylinder obj )
	{
		shape.set( obj.shape );
		color.set( obj.color );
	}
}
