package org.mastodon.views.bvv;

import org.joml.Vector3f;
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
		color = pool.vec3fColor.createQuietAttributeValue( this );
	}

	public void set( Color other )
	{
		this.color.set( other.color );
	}

	public void set( Vector3f color )
	{
		this.color.set( color );
	}

	public void set( float r, float g, float b )
	{
		this.color.set( r, g, b );
	}

	@Override
	protected void setToUninitializedState()
	{}
}
