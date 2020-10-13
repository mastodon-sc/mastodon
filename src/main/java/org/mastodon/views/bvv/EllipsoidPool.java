package org.mastodon.views.bvv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.NoSuchElementException;
import java.util.function.Function;
import org.joml.Vector3f;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.views.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.views.bvv.pool.attributes.Matrix3fAttribute;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttribute;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttributeValue;

public class EllipsoidPool extends Pool< Ellipsoid, BufferMappedElement >
{
	final Matrix3fAttribute< Ellipsoid > mat3fE = new Matrix3fAttribute<>( Ellipsoid.layout.mat3fE, this );
	final Matrix3fAttribute< Ellipsoid > mat3fInvE = new Matrix3fAttribute<>( Ellipsoid.layout.mat3fInvE, this );
	final Vector3fAttribute< Ellipsoid > vec3fT = new Vector3fAttribute<>( Ellipsoid.layout.vec3fT, this );

	public EllipsoidPool()
	{
		this( 100 );
	}

	public EllipsoidPool( final int initialCapacity )
	{
		super( initialCapacity, Ellipsoid.layout, Ellipsoid.class,
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
		slice.limit( this.size() * Ellipsoid.layout.getSizeInBytes() );
		return slice;
	}

	@Override
	protected Ellipsoid createEmptyRef()
	{
		return new Ellipsoid( this );
	}

	@Override
	protected Ellipsoid create( final Ellipsoid obj )
	{
		return super.create( obj );
	}

	@Override
	protected void delete( final Ellipsoid obj )
	{
		super.delete( obj );
	}
}
