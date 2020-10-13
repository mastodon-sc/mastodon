package org.mastodon.views.bvv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.views.bvv.pool.attributes.Matrix3fAttribute;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttribute;

public class EllipsoidShapePool extends Pool< EllipsoidShape, BufferMappedElement >
{
	final Matrix3fAttribute< EllipsoidShape > mat3fE = new Matrix3fAttribute<>( EllipsoidShape.layout.mat3fE, this );
	final Matrix3fAttribute< EllipsoidShape > mat3fInvE = new Matrix3fAttribute<>( EllipsoidShape.layout.mat3fInvE, this );
	final Vector3fAttribute< EllipsoidShape > vec3fT = new Vector3fAttribute<>( EllipsoidShape.layout.vec3fT, this );

	public EllipsoidShapePool()
	{
		this( 100 );
	}

	public EllipsoidShapePool( final int initialCapacity )
	{
		super( initialCapacity, EllipsoidShape.layout, EllipsoidShape.class,
				SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );
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
		slice.limit( this.size() * EllipsoidShape.layout.getSizeInBytes() );
		return slice;
	}

	@Override
	protected EllipsoidShape createEmptyRef()
	{
		return new EllipsoidShape( this );
	}

	@Override
	protected EllipsoidShape create( final EllipsoidShape obj )
	{
		return super.create( obj );
	}

	@Override
	protected void delete( final EllipsoidShape obj )
	{
		super.delete( obj );
	}
}
