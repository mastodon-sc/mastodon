package org.mastodon.revised.bvv;

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
import org.mastodon.revised.bvv.pool.PoolObjectLayoutJoml;
import org.mastodon.revised.bvv.pool.attributes.Matrix3fAttribute;
import org.mastodon.revised.bvv.pool.attributes.Vector3fAttribute;
import org.mastodon.revised.bvv.pool.attributes.Vector3fAttributeValue;

public class EllipsoidInstances< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > > extends Pool< EllipsoidInstance, BufferMappedElement >
{
	final Matrix3fAttribute< EllipsoidInstance > mat3fE = new Matrix3fAttribute<>( EllipsoidInstance.layout.mat3fE, this );
	final Matrix3fAttribute< EllipsoidInstance > mat3fInvE = new Matrix3fAttribute<>( EllipsoidInstance.layout.mat3fInvE, this );
	final Vector3fAttribute< EllipsoidInstance > vec3fT = new Vector3fAttribute<>( EllipsoidInstance.layout.vec3fT, this );

	private final RefRefMap< V, EllipsoidInstance > vertexToInstance;
	private final RefRefMap< EllipsoidInstance, V > instanceToVertex;

	private final ColorInstances colors;

	/**
	 * Tracks modifications to ellipsoid number and shape (for one timepoint).
	 * The {@link BvvScene} uses this to decide when to update the vertex array.
	 */
	private int modCount = 0;

	/**
	 * Tracks potential modifications to ellipsoid colors because of {@code selectionChanged()} events, etc.
	 * These events do not immediately trigger a color update, because it would need to go through all timepoints whether or not they are currently painted.
	 * Rather, the {@link BvvScene} actively does the update when required.
	 * {@code colorModCount} is only read and written by {@link BvvScene}.
	 */
	private int colorModCount = 0;

	public EllipsoidInstances( BvvGraph< V, E > graph )
	{
		this( graph,100 );
	}

	public EllipsoidInstances( BvvGraph< V, E > graph, final int initialCapacity )
	{
		super( initialCapacity, EllipsoidInstance.layout, EllipsoidInstance.class,
				SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );
		vertexToInstance = RefMaps.createRefRefMap( graph.vertices(), this.asRefCollection(), initialCapacity );
		instanceToVertex = RefMaps.createRefRefMap( this.asRefCollection(), graph.vertices(), initialCapacity );
		colors = new ColorInstances( initialCapacity );
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
		final SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > memPool =
				( SingleArrayMemPool< BufferMappedElementArray, BufferMappedElement > ) getMemPool();
		final BufferMappedElementArray dataArray = memPool.getDataArray();
		final ByteBuffer buffer = dataArray.getBuffer();
		buffer.rewind();
		final ByteBuffer slice = buffer.slice().order( ByteOrder.nativeOrder() );
		slice.limit( this.size() * EllipsoidInstance.layout.getSizeInBytes() );
		return slice;
	}

	public ByteBuffer colorBuffer()
	{
		return colors.buffer();
	}

	public int getColorModCount()
	{
		return colorModCount;
	}

	public void setColorModCount( final int colorModCount )
	{
		this.colorModCount = colorModCount;
	}

	public void updateColors( Function< V, Vector3f > coloring )
	{
		final EllipsoidInstance ref = createRef();
		final ColorInstance cref = colors.createRef();
		final V vref = instanceToVertex.createValueRef();
		for ( int i = 0; i < size(); ++i )
		{
			final Vector3f color = coloring.apply( instanceToVertex.get( getObject( i, ref ), vref ) );
			colors.getObject( i, cref ).set( color );
		}
		instanceToVertex.releaseValueRef( vref );
		colors.releaseRef( cref );
		releaseRef( ref );
	}

	/**
	 * Add or update
	 *
	 * @param vertex
	 */
	public void addInstanceFor( final V vertex )
	{
		++modCount;
		colorModCount = 0;

		final EllipsoidInstance ref = createRef();
		final ColorInstance cref = colors.createRef();
		EllipsoidInstance instance = vertexToInstance.get( vertex, ref );
		if ( instance != null )
			instance.set( vertex );
		else
		{
			instance = create( ref ).init( vertex );
			colors.create( cref );
			vertexToInstance.put( vertex, instance );
			instanceToVertex.put( instance, vertex );
		}
		colors.releaseRef( cref );
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
		final ColorInstance cref = colors.createRef();
		final V vref = instanceToVertex.createValueRef();
		final EllipsoidInstance instance = vertexToInstance.removeWithRef( vertex, ref );
		final ColorInstance colorInstance = colors.getObject( instance.getInternalPoolIndex(), cref );
		if ( instance == null )
			throw new NoSuchElementException();
		if ( instance.getInternalPoolIndex() == size() - 1 )
		{
			instanceToVertex.removeWithRef( instance, vref );
			delete( instance );
			colors.delete( colorInstance );
		}
		else
		{
			final EllipsoidInstance ref2 = createRef();
			final ColorInstance cref2 = colors.createRef();
			final EllipsoidInstance last = getObject( size() - 1, ref2 );
			final ColorInstance colorLast = colors.getObject( size() - 1, cref2 );
			instance.set( last );
			colorInstance.set( colorLast );
			final V lastVertex = instanceToVertex.removeWithRef( last, vref );
			instanceToVertex.put( instance, lastVertex );
			vertexToInstance.put( lastVertex, instance );
			delete( last );
			colors.delete( colorLast );
			colors.releaseRef( cref2 );
			releaseRef( ref2 );
		}
		instanceToVertex.releaseValueRef( vref );
		colors.releaseRef( cref );
		releaseRef( ref );
	}

	public int indexOf( final V vertex )
	{
		if ( vertex == null )
			return -1;

		final EllipsoidInstance ref = createRef();
		try
		{
			final EllipsoidInstance instance = vertexToInstance.get( vertex, ref );
			return ( instance == null ) ? -1 : instance.getInternalPoolIndex();
		}
		finally
		{
			releaseRef( ref );
		}
	}

	/*
	 * Colors
	 */

	static class ColorInstance extends PoolObject< ColorInstance, ColorInstances, BufferMappedElement >
	{
		public static class ColorInstanceLayout extends PoolObjectLayoutJoml
		{
			final Vector3fField vec3fColor = vector3fField();
		}

		public static ColorInstanceLayout layout = new ColorInstanceLayout();

		public final Vector3fAttributeValue color;

		ColorInstance( final ColorInstances pool )
		{
			super( pool );
			color = pool.vec3fColor.createQuietAttributeValue( this );
		}

		public void set( ColorInstance other )
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
		{
			access.putIndex( -1, 0 );
		}
	}

	static class ColorInstances extends Pool< ColorInstance, BufferMappedElement >
	{
		final Vector3fAttribute< ColorInstance > vec3fColor = new Vector3fAttribute<>( ColorInstance.layout.vec3fColor, this );

		public ColorInstances( final int initialCapacity )
		{
			super( initialCapacity, ColorInstance.layout, ColorInstance.class,
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
			slice.limit( this.size() * ColorInstance.layout.getSizeInBytes() );
			return slice;
		}

		@Override
		protected ColorInstance create( final ColorInstance obj )
		{
			return super.create( obj );
		}

		@Override
		protected void delete( final ColorInstance obj )
		{
			super.delete( obj );
		}

		@Override
		protected ColorInstance createEmptyRef()
		{
			return new ColorInstance( this );
		}
	}
}
