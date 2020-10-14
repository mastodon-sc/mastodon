package org.mastodon.views.bvv.scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttribute;

public class ColorPool extends Pool< Color, BufferMappedElement >
{
	final Vector3fAttribute< Color > vec3fColor = new Vector3fAttribute<>( Color.layout.vec3fColor, this );

	private final AtomicBoolean modified = new AtomicBoolean( true );

	public ColorPool( final int initialCapacity )
	{
		super( initialCapacity, Color.layout, Color.class,
				SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );
		vec3fColor.addPropertyChangeListener( o -> setModified() );
	}

	private void setModified()
	{
		modified.set( true );
	}

	/**
	 * Get the modified flag, and reset it to {@code false}.
	 * @return
	 */
	public boolean getAndClearModified()
	{
		return modified.getAndSet( false );
	}

	/**
	 * Get the {@code ByteBuffer} slice with the TODO
	 * @return
	 */
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
	protected Color createEmptyRef()
	{
		return new Color( this );
	}

	@Override
	protected Color create( final Color obj )
	{
		setModified();
		return super.create( obj );
	}

	@Override
	protected void delete( final Color obj )
	{
		setModified();
		super.delete( obj );
	}
}
