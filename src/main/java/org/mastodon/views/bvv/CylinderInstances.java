package org.mastodon.views.bvv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.joml.Vector3fc;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.views.bvv.pool.attributes.Matrix3fAttribute;
import org.mastodon.views.bvv.pool.attributes.Vector3fAttribute;

public class CylinderInstances extends Pool< CylinderInstance, BufferMappedElement >
{
	final Matrix3fAttribute< CylinderInstance > mat3fE = new Matrix3fAttribute<>( CylinderInstance.layout.mat3fE, this );
	final Matrix3fAttribute< CylinderInstance > mat3fInvE = new Matrix3fAttribute<>( CylinderInstance.layout.mat3fInvE, this );
	final Vector3fAttribute< CylinderInstance > vec3fT = new Vector3fAttribute<>( CylinderInstance.layout.vec3fT, this );

	/**
	 * Tracks modifications to ellipsoid number and shape (for one timepoint).
	 * The {@link BvvRenderer} uses this to decide when to update the vertex array.
	 */
	private int modCount = 0;

	public CylinderInstances()
	{
		this( 100 );
	}

	public CylinderInstances( int initialCapacity )
	{
		super( initialCapacity, CylinderInstance.layout, CylinderInstance.class,
				SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );
	}

	@Override
	protected CylinderInstance createEmptyRef()
	{
		return new CylinderInstance( this );
	}

	public int getModCount()
	{
		return modCount;
	}

	public ByteBuffer buffer()
	{
		final SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > memPool =
				( SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > ) getMemPool();
		final BufferMappedElementArray dataArray = memPool.getDataArray();
		final ByteBuffer buffer = dataArray.getBuffer();
		buffer.rewind();
		final ByteBuffer slice = buffer.slice().order( ByteOrder.nativeOrder() );
		slice.limit( this.size() * CylinderInstance.layout.getSizeInBytes() );
		return slice;
	}

	public void addInstanceFor(  final Vector3fc from, final Vector3fc to )
	{
		++modCount;
		final CylinderInstance ref = createRef();
		CylinderInstance instance = create( ref ).init( from, to );
		releaseRef( ref );
	}
}
