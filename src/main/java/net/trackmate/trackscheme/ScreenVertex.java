package net.trackmate.trackscheme;

import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import static net.trackmate.graph.mempool.ByteUtils.*;

public class ScreenVertex extends PoolObject< ScreenVertex, ByteMappedElement >
{
	protected static final int ORIG_VERTEX_INDEX_OFFSET = 0;

	protected static final int X_OFFSET = ORIG_VERTEX_INDEX_OFFSET + INDEX_SIZE;

	protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;

	protected static final int VERTEX_DIST_OFFSET = Y_OFFSET + DOUBLE_SIZE;

	protected static final int SELECTED_OFFSET = VERTEX_DIST_OFFSET + DOUBLE_SIZE;

	protected static final int SIZE_IN_BYTES = SELECTED_OFFSET + BOOLEAN_SIZE;

	protected ScreenVertex( final Pool< ScreenVertex, ByteMappedElement > pool )
	{
		super( pool );
	}

	public ScreenVertex init(
			final int id,
			final double x,
			final double y,
			final boolean selected )
	{
		setId( id );
		setX( x );
		setY( y );
		setSelected( selected );
		return this;
	}

	public int getId()
	{
		return access.getIndex( ORIG_VERTEX_INDEX_OFFSET );
	}

	protected void setId( final int id )
	{
		access.putIndex( id, ORIG_VERTEX_INDEX_OFFSET );
	}

	public double getX()
	{
		return access.getDouble( X_OFFSET );
	}

	protected void setX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	public double getY()
	{
		return access.getDouble( Y_OFFSET );
	}

	protected void setY( final double y )
	{
		access.putDouble( y, Y_OFFSET );
	}

	public double getVertexDist()
	{
		return access.getDouble( VERTEX_DIST_OFFSET );
	}

	public void setVertexDist( final double minVertexScreenDist )
	{
		access.putDouble( minVertexScreenDist, VERTEX_DIST_OFFSET );
	}

	public String getLabel()
	{
		// TODO get label from original vertex
		return "";
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
		public ScreenVertexPool( final int initialCapacity )
		{
			this( initialCapacity, new VertexFactory( initialCapacity ) );
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

		public void release( final ScreenVertex vertex )
		{
			releaseByInternalPoolIndex( vertex.getInternalPoolIndex() );
		}

		private static class VertexFactory implements PoolObject.Factory< ScreenVertex, ByteMappedElement >
		{
			private ScreenVertexPool vertexPool;

			private final Labels labels;

			public VertexFactory( final int initialCapacity )
			{
				labels = new Labels( initialCapacity );
			}

			@Override
			public int getSizeInBytes()
			{
				return ScreenVertex.SIZE_IN_BYTES;
			}

			@Override
			public ScreenVertex createEmptyRef()
			{
				return new ScreenVertex( vertexPool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}
}
