package net.trackmate.trackscheme;

import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import static net.trackmate.graph.mempool.ByteUtils.*;

public class ScreenEdge extends PoolObject< ScreenEdge, ByteMappedElement >
{
	protected static final int ORIG_EDGE_INDEX_OFFSET = 0;

	protected static final int SOURCE_SCREEN_VERTEX_INDEX_OFFSET = ORIG_EDGE_INDEX_OFFSET + INDEX_SIZE;

	protected static final int TARGET_SCREEN_VERTEX_INDEX_OFFSET = SOURCE_SCREEN_VERTEX_INDEX_OFFSET + INDEX_SIZE;

	protected static final int SELECTED_OFFSET = TARGET_SCREEN_VERTEX_INDEX_OFFSET + INDEX_SIZE;

	protected static final int SIZE_IN_BYTES = SELECTED_OFFSET + BOOLEAN_SIZE;

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
		setId( id );
		setSourceScreenVertexIndex( sourceScreenVertexIndex );
		setTargetScreenVertexIndex( targetScreenVertexIndex );
		setSelected( selected );
		return this;
	}

	public int getId()
	{
		return access.getIndex( ORIG_EDGE_INDEX_OFFSET );
	}

	protected void setId( final int id )
	{
		access.putIndex( id, ORIG_EDGE_INDEX_OFFSET );
	}

	public int getSourceScreenVertexIndex()
	{
		return access.getIndex( SOURCE_SCREEN_VERTEX_INDEX_OFFSET );
	}

	protected void setSourceScreenVertexIndex( final int index )
	{
		access.putIndex( index, SOURCE_SCREEN_VERTEX_INDEX_OFFSET );
	}

	public int getTargetScreenVertexIndex()
	{
		return access.getIndex( TARGET_SCREEN_VERTEX_INDEX_OFFSET );
	}

	protected void setTargetScreenVertexIndex( final int index )
	{
		access.putIndex( index, TARGET_SCREEN_VERTEX_INDEX_OFFSET );
	}

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
			this( initialCapacity, new EdgeFactory( initialCapacity ) );
		}

		private ScreenEdgePool( final int initialCapacity, final EdgeFactory f )
		{
			super( initialCapacity, f );
			f.EdgePool = this;
		}

		@Override
		public ScreenEdge create( final ScreenEdge edge )
		{
			return super.create( edge );
		}

		public void release( final ScreenEdge edge )
		{
			releaseByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		private static class EdgeFactory implements PoolObject.Factory< ScreenEdge, ByteMappedElement >
		{
			private ScreenEdgePool EdgePool;

			private final Labels labels;

			public EdgeFactory( final int initialCapacity )
			{
				labels = new Labels( initialCapacity );
			}

			@Override
			public int getSizeInBytes()
			{
				return ScreenEdge.SIZE_IN_BYTES;
			}

			@Override
			public ScreenEdge createEmptyRef()
			{
				return new ScreenEdge( EdgePool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}
}
