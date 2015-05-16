package net.trackmate.trackscheme;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INDEX_SIZE;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.trackscheme.TrackSchemeGraph.TrackSchemeVertexPool;

/**
 * Layouted vertex.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class ScreenVertex extends PoolObject< ScreenVertex, ByteMappedElement >
{
	protected static final int ORIG_VERTEX_INDEX_OFFSET = 0;

	protected static final int X_OFFSET = ORIG_VERTEX_INDEX_OFFSET + INDEX_SIZE;

	protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;

	protected static final int VERTEX_DIST_OFFSET = Y_OFFSET + DOUBLE_SIZE;

	protected static final int SELECTED_OFFSET = VERTEX_DIST_OFFSET + DOUBLE_SIZE;

	protected static final int SIZE_IN_BYTES = SELECTED_OFFSET + BOOLEAN_SIZE;

	private final TrackSchemeVertex vref;

	private final TrackSchemeVertexPool trackSchemeVertexPool;

	protected ScreenVertex( final Pool< ScreenVertex, ByteMappedElement > pool, final TrackSchemeVertexPool trackSchemeVertexPool )
	{
		super( pool );
		this.trackSchemeVertexPool = trackSchemeVertexPool;
		this.vref = trackSchemeVertexPool.createRef();
	}

	public ScreenVertex init(
			final int id,
			final double x,
			final double y,
			final boolean selected )
	{
		setTrackSchemeVertexId( id );
		setX( x );
		setY( y );
		setSelected( selected );
		return this;
	}

	/**
	 * Get the internal pool index of the associated {@link TrackSchemeVertex}.
	 *
	 * @return the internal pool index of the associated
	 *         {@link TrackSchemeVertex}.
	 */
	public int getTrackSchemeVertexId()
	{
		return access.getIndex( ORIG_VERTEX_INDEX_OFFSET );
	}

	protected void setTrackSchemeVertexId( final int id )
	{
		access.putIndex( id, ORIG_VERTEX_INDEX_OFFSET );
	}

	/**
	 * Get the X screen coordinate of the vertex center.
	 *
	 * @return X screen coordinate.
	 */
	public double getX()
	{
		return access.getDouble( X_OFFSET );
	}

	protected void setX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	/**
	 * Get the Y screen coordinate of the vertex center.
	 *
	 * @return Y screen coordinate.
	 */
	public double getY()
	{
		return access.getDouble( Y_OFFSET );
	}

	protected void setY( final double y )
	{
		access.putDouble( y, Y_OFFSET );
	}

	/**
	 * (Estimate of) the distance to the closest vertex on screen.
	 * Center-to-center distance.
	 *
	 * @return distance to the closest vertex on screen.
	 */
	public double getVertexDist()
	{
		return access.getDouble( VERTEX_DIST_OFFSET );
	}

	protected void setVertexDist( final double minVertexScreenDist )
	{
		access.putDouble( minVertexScreenDist, VERTEX_DIST_OFFSET );
	}

	/**
	 * Get the label of the vertex. This calls
	 * {@link TrackSchemeVertex#getLabel()} of the associated
	 * {@link TrackSchemeVertex}.
	 *
	 * @return label of the vertex.
	 */
	public String getLabel()
	{
		trackSchemeVertexPool.getByInternalPoolIndex( getTrackSchemeVertexId(), vref );
		return vref.getLabel();
	}

	/**
	 * Get the selected state of the vertex.
	 *
	 * @return true, if the vertex is selected.
	 */
	public boolean isSelected()
	{
		return access.getBoolean( SELECTED_OFFSET );
	}

	protected void setSelected( final boolean selected )
	{
		access.putBoolean( selected, SELECTED_OFFSET );
	}

	@Override
	protected void setToUninitializedState()
	{}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof ScreenVertex &&
				access.equals( ( ( ScreenVertex ) obj ).access );
	}

	@Override
	public int hashCode()
	{
		return access.hashCode();
	}

	public static class ScreenVertexPool extends Pool< ScreenVertex, ByteMappedElement >
	{
		public ScreenVertexPool( final int initialCapacity, final TrackSchemeVertexPool trackSchemeVertexPool )
		{
			this( initialCapacity, new VertexFactory( initialCapacity, trackSchemeVertexPool ) );
		}

		private ScreenVertexPool( final int initialCapacity, final VertexFactory f )
		{
			super( initialCapacity, f );
			f.vertexPool = this;
		}

		@Override
		public ScreenVertex create( final ScreenVertex vertex )
		{
			return super.create( vertex );
		}

		public void delete( final ScreenVertex vertex )
		{
			deleteByInternalPoolIndex( vertex.getInternalPoolIndex() );
		}

		private static class VertexFactory implements PoolObject.Factory< ScreenVertex, ByteMappedElement >
		{
			private ScreenVertexPool vertexPool;

			private final TrackSchemeVertexPool trackSchemeVertexPool;

			private final Labels labels;

			public VertexFactory( final int initialCapacity, final TrackSchemeVertexPool trackSchemeVertexPool )
			{
				labels = new Labels( initialCapacity );
				this.trackSchemeVertexPool = trackSchemeVertexPool;
			}

			@Override
			public int getSizeInBytes()
			{
				return ScreenVertex.SIZE_IN_BYTES;
			}

			@Override
			public ScreenVertex createEmptyRef()
			{
				return new ScreenVertex( vertexPool, trackSchemeVertexPool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}
}
