package net.trackmate.revised.trackscheme;

import static net.trackmate.pool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.pool.ByteUtils.BYTE_SIZE;
import static net.trackmate.pool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.pool.ByteUtils.INDEX_SIZE;
import static net.trackmate.revised.trackscheme.ScreenVertex.Transition.NONE;

import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.ByteMappedElementArray;
import net.trackmate.pool.MemPool;
import net.trackmate.pool.Pool;
import net.trackmate.pool.PoolObject;
import net.trackmate.pool.SingleArrayMemPool;
import net.trackmate.revised.trackscheme.ScreenVertex.Transition;

/**
 * Layouted edge.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenEdge extends PoolObject< ScreenEdge, ByteMappedElement >
{
	protected static final int ORIG_EDGE_INDEX_OFFSET = 0;
	protected static final int SOURCE_SCREEN_VERTEX_INDEX_OFFSET = ORIG_EDGE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int TARGET_SCREEN_VERTEX_INDEX_OFFSET = SOURCE_SCREEN_VERTEX_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SELECTED_OFFSET = TARGET_SCREEN_VERTEX_INDEX_OFFSET + INDEX_SIZE;
	protected static final int TRANSITION_OFFSET = SELECTED_OFFSET + BOOLEAN_SIZE;
	protected static final int IP_RATIO_OFFSET = TRANSITION_OFFSET + BYTE_SIZE;
	protected static final int SIZE_IN_BYTES = IP_RATIO_OFFSET + DOUBLE_SIZE;

	protected ScreenEdge( final Pool< ScreenEdge, ByteMappedElement > pool )
	{
		super( pool );
	}

	public ScreenEdge init(
			final int id,
			final int sourceScreenVertexIndex,
			final int targetScreenVertexIndex,
			final boolean selected )
	{
		setTrackSchemeEdgeId( id );
		setSourceScreenVertexIndex( sourceScreenVertexIndex );
		setTargetScreenVertexIndex( targetScreenVertexIndex );
		setSelected( selected );
		setTransition( NONE );
		return this;
	}

	public Transition getTransition()
	{
		return Transition.values()[ access.getByte( TRANSITION_OFFSET ) ];
	}

	protected void setTransition( final Transition t )
	{
		access.putByte( t.toByte(), TRANSITION_OFFSET );
	}

	public double getInterpolationCompletionRatio()
	{
		return access.getDouble( IP_RATIO_OFFSET );
	}

	protected void setInterpolationCompletionRatio( final double ratio )
	{
		access.putDouble( ratio, IP_RATIO_OFFSET );
	}

	/**
	 * Get the internal pool index of the associated {@link TrackSchemeEdge}.
	 *
	 * @return the internal pool index of the associated
	 *         {@link TrackSchemeEdge}.
	 */
	public int getTrackSchemeEdgeId()
	{
		return access.getIndex( ORIG_EDGE_INDEX_OFFSET );
	}

	protected void setTrackSchemeEdgeId( final int id )
	{
		access.putIndex( id, ORIG_EDGE_INDEX_OFFSET );
	}

	/**
	 * Get the index of the source ("from") {@link ScreenVertex} in the screen
	 * vertex list {@link ScreenEntities#getVertices()}. This is at the same
	 * time the internal pool index of the source {@link ScreenVertex}.
	 *
	 * @return internal pool index of the source {@link ScreenVertex}.
	 */
	public int getSourceScreenVertexIndex()
	{
		return access.getIndex( SOURCE_SCREEN_VERTEX_INDEX_OFFSET );
	}

	protected void setSourceScreenVertexIndex( final int index )
	{
		access.putIndex( index, SOURCE_SCREEN_VERTEX_INDEX_OFFSET );
	}

	/**
	 * Get the index of the target ("to") {@link ScreenVertex} in the screen
	 * vertex list {@link ScreenEntities#getVertices()}. This is at the same
	 * time the internal pool index of the target {@link ScreenVertex}.
	 *
	 * @return internal pool index of the target {@link ScreenVertex}.
	 */
	public int getTargetScreenVertexIndex()
	{
		return access.getIndex( TARGET_SCREEN_VERTEX_INDEX_OFFSET );
	}

	protected void setTargetScreenVertexIndex( final int index )
	{
		access.putIndex( index, TARGET_SCREEN_VERTEX_INDEX_OFFSET );
	}

	/**
	 * Get the selected state of the edge.
	 *
	 * @return true, if the edge is selected.
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

	/**
	 * Set all fields as in specified {@link ScreenEdge} (which is possibly
	 * from another pool).
	 * <p>
	 * ONLY USE THIS FOR {@link ScreenEntities#set(ScreenEntities)}!
	 *
	 * @param e
	 * @return {@code this}.
	 */
	ScreenEdge cloneFrom( final ScreenEdge e )
	{
		setTrackSchemeEdgeId( e.getTrackSchemeEdgeId() );
		setSourceScreenVertexIndex( e.getSourceScreenVertexIndex() );
		setTargetScreenVertexIndex( e.getTargetScreenVertexIndex() );
		setSelected( e.isSelected() );
		setTransition( e.getTransition() );
		setInterpolationCompletionRatio( e.getInterpolationCompletionRatio() );
		return this;
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof ScreenEdge &&
				access.equals( ( ( ScreenEdge ) obj ).access );
	}

	@Override
	public int hashCode()
	{
		return access.hashCode();
	}

	public static class ScreenEdgePool extends Pool< ScreenEdge, ByteMappedElement >
	{
		public ScreenEdgePool( final int initialCapacity )
		{
			this( initialCapacity, new EdgeFactory() );
		}

		private ScreenEdgePool( final int initialCapacity, final EdgeFactory f )
		{
			super( initialCapacity, f );
			f.edgePool = this;
		}

		@Override
		public ScreenEdge create( final ScreenEdge edge )
		{
			return super.create( edge );
		}

		public void delete( final ScreenEdge edge )
		{
			deleteByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		private static class EdgeFactory implements PoolObject.Factory< ScreenEdge, ByteMappedElement >
		{
			private ScreenEdgePool edgePool;

			@Override
			public int getSizeInBytes()
			{
				return ScreenEdge.SIZE_IN_BYTES;
			}

			@Override
			public ScreenEdge createEmptyRef()
			{
				return new ScreenEdge( edgePool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}
}
