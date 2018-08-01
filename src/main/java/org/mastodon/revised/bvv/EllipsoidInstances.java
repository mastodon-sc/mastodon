package org.mastodon.revised.bvv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.NoSuchElementException;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.revised.bvv.pool.attributes.Matrix3fAttribute;
import org.mastodon.revised.bvv.pool.attributes.Vector3fAttribute;

public class EllipsoidInstances< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > > extends Pool< EllipsoidInstance, BufferMappedElement >
{
	final Matrix3fAttribute< EllipsoidInstance > mat3fE = new Matrix3fAttribute<>( EllipsoidInstance.layout.mat3fE, this );
	final Matrix3fAttribute< EllipsoidInstance > mat3fInvE = new Matrix3fAttribute<>( EllipsoidInstance.layout.mat3fInvE, this );
	final Vector3fAttribute< EllipsoidInstance > vec3fT = new Vector3fAttribute<>( EllipsoidInstance.layout.vec3fT, this );

	private final RefRefMap< V, EllipsoidInstance > vertexToInstance;
	private final RefRefMap< EllipsoidInstance, V > instanceToVertex;

	private int modCount = 0;

	public EllipsoidInstances( BvvGraph< V, E > graph )
	{
		this( graph,100 );
	}

	public EllipsoidInstances( BvvGraph< V, E > graph, final int initialCapacity )
	{
		super( initialCapacity, EllipsoidInstance.layout, EllipsoidInstance.class, SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );
		vertexToInstance = RefMaps.createRefRefMap( graph.vertices(), this.asRefCollection(), initialCapacity );
		instanceToVertex = RefMaps.createRefRefMap( this.asRefCollection(), graph.vertices(), initialCapacity );
	}

	@Override
	protected EllipsoidInstance createEmptyRef()
	{
		return new EllipsoidInstance( this );
	}

	public int getModCount()
	{
		return modCount;
	}

	public ByteBuffer buffer()
	{
		final SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > memPool = ( SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > ) getMemPool();
		final BufferMappedElementArray dataArray = memPool.getDataArray();
		final ByteBuffer buffer = dataArray.getBuffer();
		buffer.rewind();
		buffer.limit( this.size() * EllipsoidInstance.layout.getSizeInBytes() );
		return buffer.slice().order( ByteOrder.nativeOrder() );
	}

	/**
	 * Add or update
	 *
	 * @param vertex
	 */
	public void addInstanceFor( final V vertex )
	{
		++modCount;

		final EllipsoidInstance ref = createRef();
		EllipsoidInstance instance = vertexToInstance.get( vertex, ref );
		if ( instance != null )
			instance.set( vertex );
		else
		{
			instance = create( ref ).init( vertex );
			vertexToInstance.put( vertex, instance );
			instanceToVertex.put( instance, vertex );
		}
		releaseRef( ref );
	}

	/**
	 * Swap with last instance and remove
	 *
	 * @param vertex
	 */
	public void removeInstanceFor( final V vertex )
	{
		++modCount;

		final EllipsoidInstance ref = createRef();
		final V vref = instanceToVertex.createValueRef();
		EllipsoidInstance instance = vertexToInstance.removeWithRef( vertex, ref );
		if ( instance == null )
			throw new NoSuchElementException();
		if ( instance.getInternalPoolIndex() == size() - 1 )
		{
			instanceToVertex.removeWithRef( instance, vref );
			delete( instance );
		}
		else
		{
			final EllipsoidInstance ref2 = createRef();
			final EllipsoidInstance last = getObject( size() - 1, ref2 );
			instance.set( last );
			instanceToVertex.put( instance, instanceToVertex.removeWithRef( last, vref ) );
			delete( last );
			releaseRef( ref2 );
		}
		instanceToVertex.releaseValueRef( vref );
		releaseRef( ref );
	}
}
