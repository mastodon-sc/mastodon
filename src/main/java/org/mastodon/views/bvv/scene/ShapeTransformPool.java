package org.mastodon.views.bvv.scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.views.bvv.pool.attributes.Matrix3fAttribute;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttribute;

class ShapeTransformPool extends Pool< ShapeTransform, BufferMappedElement >
{
	final Matrix3fAttribute< ShapeTransform > mat3fE = new Matrix3fAttribute<>( ShapeTransform.layout.mat3fE, this );
	final Matrix3fAttribute< ShapeTransform > mat3fInvTE = new Matrix3fAttribute<>( ShapeTransform.layout.mat3fInvTE, this );
	final Vector3fAttribute< ShapeTransform > vec3fT = new Vector3fAttribute<>( ShapeTransform.layout.vec3fT, this );

	private final AtomicBoolean modified = new AtomicBoolean( true );

	public ShapeTransformPool()
	{
		this( 100 );
	}

	public ShapeTransformPool( final int initialCapacity )
	{
		super( initialCapacity, ShapeTransform.layout, ShapeTransform.class,
				SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );

		mat3fE.propertyChangeListeners().add( o -> setModified() );
		mat3fInvTE.propertyChangeListeners().add( o -> setModified() );
		vec3fT.propertyChangeListeners().add( o -> setModified() );
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
		slice.limit( this.size() * ShapeTransform.layout.getSizeInBytes() );
		return slice;
	}

	@Override
	protected ShapeTransform createEmptyRef()
	{
		return new ShapeTransform( this );
	}

	@Override
	protected ShapeTransform create( final ShapeTransform obj )
	{
		setModified();
		return super.create( obj );
	}

	@Override
	protected void delete( final ShapeTransform obj )
	{
		setModified();
		super.delete( obj );
	}
}
