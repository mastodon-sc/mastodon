package org.mastodon.views.bvv;

import org.joml.Vector3fc;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.views.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttributeValue;

class Color extends PoolObject< Color, ColorPool, BufferMappedElement >
{
	public static class ColorLayout extends PoolObjectLayoutJoml
	{
		final Vector3fField vec3fColor = vector3fField();
	}

	public static ColorLayout layout = new ColorLayout();

	public final Vector3fAttributeValue color;

	Color( final ColorPool pool )
	{
		super( pool );
		color = pool.vec3fColor.createAttributeValue( this );
	}

	public void set( Color other )
	{
		this.color.set( other.color );
	}

	public void set( Vector3fc value )
	{
		color.set( value );
	}

	public void set( float r, float g, float b )
	{
		color.set( r, g, b );
	}

	@Override
	protected void setToUninitializedState()
	{}

	/*

	// -- implements Vector3fAttributeValue --

	@Override
	public Vector3f get( final Vector3f dest )
	{
		return color.get( dest );
	}

	@Override
	public float x()
	{
		return color.x();
	}

	@Override
	public float y()
	{
		return color.y();
	}

	@Override
	public float z()
	{
		return color.z();
	}

	@Override
	public void set( final Vector3fAttributeReadOnlyValue value )
	{
		color.set( value );
	}

	@Override
	public void zero()
	{
		color.zero();
	}

	*/
}
