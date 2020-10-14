package org.mastodon.views.bvv;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.function.Function;
import org.joml.Vector3f;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.views.bvv.scene.Color;
import org.mastodon.views.bvv.scene.ColorPool;
import org.mastodon.views.bvv.scene.EllipsoidMath;
import org.mastodon.views.bvv.scene.EllipsoidShape;
import org.mastodon.views.bvv.scene.EllipsoidShapePool;

public class EllipsoidInstances< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > >
{
	private final EllipsoidShapePool ellipsoids;
	private final ColorPool colors;

	private final RefRefMap< V, EllipsoidShape > vertexToInstance;
	private final RefRefMap< EllipsoidShape, V > instanceToVertex;

	/**
	 * Tracks modifications to ellipsoid number and shape (for one timepoint).
	 * The {@link BvvRenderer} uses this to decide when to update the vertex array.
	 */
	private int modCount = 0;

	/**
	 * Tracks potential modifications to ellipsoid colors because of {@code selectionChanged()} events, etc.
	 * These events do not immediately trigger a color update, because it would need to go through all timepoints whether or not they are currently painted.
	 * Rather, the {@link BvvRenderer} actively does the update when required.
	 * {@code colorModCount} is only read and written by {@link BvvRenderer}.
	 */
	private int colorModCount = 0;

	private final EllipsoidMath util = new EllipsoidMath();

	public EllipsoidInstances( BvvGraph< V, E > graph )
	{
		this( graph,100 );
	}

	public EllipsoidInstances( BvvGraph< V, E > graph, final int initialCapacity )
	{
		ellipsoids = new EllipsoidShapePool( initialCapacity );
		colors = new ColorPool( initialCapacity );
		vertexToInstance = RefMaps.createRefRefMap( graph.vertices(), ellipsoids.asRefCollection(), initialCapacity );
		instanceToVertex = RefMaps.createRefRefMap( ellipsoids.asRefCollection(), graph.vertices(), initialCapacity );
	}

	public int getModCount()
	{
		return modCount;
	}

	public int getColorModCount()
	{
		return colorModCount;
	}

	public void setColorModCount( final int colorModCount )
	{
		this.colorModCount = colorModCount;
	}

	public ByteBuffer ellipsoidBuffer()
	{
		return ellipsoids.buffer();
	}

	public ByteBuffer colorBuffer()
	{
		return colors.buffer();
	}

	public int size()
	{
		return ellipsoids.size();
	}

	public void updateColors( Function< V, Vector3f > coloring )
	{
		final EllipsoidShape ref = ellipsoids.createRef();
		final Color cref = colors.createRef();
		final V vref = instanceToVertex.createValueRef();
		for ( int i = 0; i < size(); ++i )
		{
			final Vector3f color = coloring.apply( instanceToVertex.get( ellipsoids.getObject( i, ref ), vref ) );
			colors.getObject( i, cref ).set( color );
		}
		instanceToVertex.releaseValueRef( vref );
		colors.releaseRef( cref );
		ellipsoids.releaseRef( ref );
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

		final EllipsoidShape ref = ellipsoids.createRef();
		final Color cref = colors.createRef();
		EllipsoidShape instance = vertexToInstance.get( vertex, ref );
		if ( instance != null )
			util.setFromVertex( vertex, instance );
		else
		{
			instance = ellipsoids.create( ref );
			util.setFromVertex( vertex, instance );
			colors.create( cref );
			vertexToInstance.put( vertex, instance );
			instanceToVertex.put( instance, vertex );
		}
		colors.releaseRef( cref );
		ellipsoids.releaseRef( ref );
	}

	/**
	 * Swap with last instance and remove
	 *
	 * @param vertex
	 */
	public void removeInstanceFor( final V vertex )
	{
		++modCount;

		final EllipsoidShape ref = ellipsoids.createRef();
		final Color cref = colors.createRef();
		final V vref = instanceToVertex.createValueRef();
		final EllipsoidShape instance = vertexToInstance.removeWithRef( vertex, ref );
		if ( instance == null )
			throw new NoSuchElementException();
		final Color color = colors.getObject( instance.getInternalPoolIndex(), cref );
		if ( instance.getInternalPoolIndex() == size() - 1 )
		{
			instanceToVertex.removeWithRef( instance, vref );
			ellipsoids.delete( instance );
			colors.delete( color );
		}
		else
		{
			final EllipsoidShape ref2 = ellipsoids.createRef();
			final Color cref2 = colors.createRef();
			final EllipsoidShape last = ellipsoids.getObject( size() - 1, ref2 );
			final Color colorLast = colors.getObject( size() - 1, cref2 );
			instance.set( last );
			color.set( colorLast );
			final V lastVertex = instanceToVertex.removeWithRef( last, vref );
			instanceToVertex.put( instance, lastVertex );
			vertexToInstance.put( lastVertex, instance );
			ellipsoids.delete( last );
			colors.delete( colorLast );
			colors.releaseRef( cref2 );
			ellipsoids.releaseRef( ref2 );
		}
		instanceToVertex.releaseValueRef( vref );
		colors.releaseRef( cref );
		ellipsoids.releaseRef( ref );
	}

	public int indexOf( final V vertex )
	{
		if ( vertex == null )
			return -1;

		final EllipsoidShape ref = ellipsoids.createRef();
		try
		{
			final EllipsoidShape instance = vertexToInstance.get( vertex, ref );
			return ( instance == null ) ? -1 : instance.getInternalPoolIndex();
		}
		finally
		{
			ellipsoids.releaseRef( ref );
		}
	}
}
