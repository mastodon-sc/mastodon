package org.mastodon.views.bvv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttribute;

class ColorPool extends Pool< Color, BufferMappedElement >
{
	final Vector3fAttribute< Color > vec3fColor = new Vector3fAttribute<>( Color.layout.vec3fColor, this );

	public ColorPool( final int initialCapacity )
	{
		super( initialCapacity, Color.layout, Color.class,
				SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );
	}

	public ByteBuffer buffer()
	{
		final SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > memPool =
				( SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > ) getMemPool();
		final BufferMappedElementArray dataArray = memPool.getDataArray();
		final ByteBuffer buffer = dataArray.getBuffer();
		buffer.rewind();
		final ByteBuffer slice = buffer.slice().order( ByteOrder.nativeOrder() );
		slice.limit( this.size() * Color.layout.getSizeInBytes() );
		return slice;
	}

	@Override
	protected Color create( final Color obj )
	{
		return super.create( obj );
	}

	@Override
	protected void delete( final Color obj )
	{
		super.delete( obj );
	}

	@Override
	protected Color createEmptyRef()
	{
		return new Color( this );
	}
}
