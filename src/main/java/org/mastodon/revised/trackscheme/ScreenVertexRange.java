package org.mastodon.revised.trackscheme;

import static org.mastodon.pool.ByteUtils.DOUBLE_SIZE;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.SingleArrayMemPool;

/**
 * Layouted dense vertex area.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenVertexRange extends PoolObject< ScreenVertexRange, ByteMappedElement >
{
	protected static final int MIN_X_OFFSET = 0;
	protected static final int MAX_X_OFFSET = MIN_X_OFFSET + DOUBLE_SIZE;
	protected static final int MIN_Y_OFFSET = MAX_X_OFFSET + DOUBLE_SIZE;
	protected static final int MAX_Y_OFFSET = MIN_Y_OFFSET + DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = MAX_Y_OFFSET + DOUBLE_SIZE;

	protected ScreenVertexRange( final Pool< ScreenVertexRange, ByteMappedElement > pool )
	{
		super( pool );
	}

	public ScreenVertexRange init( final double minX, final double maxX, final double minY, final double maxY )
	{
		setMinX( minX );
		setMaxX( maxX );
		setMinY( minY );
		setMaxY( maxY );
		return this;
	}

	public double getMinX()
	{
		return access.getDouble( MIN_X_OFFSET );
	}

	protected void setMinX( final double minX )
	{
		access.putDouble( minX, MIN_X_OFFSET );
	}

	public double getMaxX()
	{
		return access.getDouble( MAX_X_OFFSET );
	}

	protected void setMaxX( final double maxX )
	{
		access.putDouble( maxX, MAX_X_OFFSET );
	}

	public double getMinY()
	{
		return access.getDouble( MIN_Y_OFFSET );
	}

	protected void setMinY( final double minY )
	{
		access.putDouble( minY, MIN_Y_OFFSET );
	}

	public double getMaxY()
	{
		return access.getDouble( MAX_Y_OFFSET );
	}

	protected void setMaxY( final double maxY )
	{
		access.putDouble( maxY, MAX_Y_OFFSET );
	}

	@Override
	protected void setToUninitializedState()
	{}

	/**
	 * Set all fields as in specified {@link ScreenVertexRange} (which is
	 * possibly from another pool).
	 *
	 * @param r
	 * @return {@code this}.
	 */
	ScreenVertexRange cloneFrom( final ScreenVertexRange r )
	{
		setMinX( r.getMinX() );
		setMaxX( r.getMaxX() );
		setMinY( r.getMinY() );
		setMaxY( r.getMaxY() );
		return this;
	}

	public static class ScreenVertexRangePool extends Pool< ScreenVertexRange, ByteMappedElement >
	{
		public ScreenVertexRangePool( final int initialCapacity )
		{
			this( initialCapacity, new ScreenVertexRangeFactory() );
		}

		private ScreenVertexRangePool( final int initialCapacity, final ScreenVertexRangeFactory f )
		{
			super( initialCapacity, f );
			f.pool = this;
		}

		@Override
		public ScreenVertexRange create( final ScreenVertexRange range )
		{
			return super.create( range );
		}

		public void delete( final ScreenVertexRange range )
		{
			deleteByInternalPoolIndex( range.getInternalPoolIndex() );
		}

		private static class ScreenVertexRangeFactory implements PoolObject.Factory< ScreenVertexRange, ByteMappedElement >
		{
			private ScreenVertexRangePool pool;

			@Override
			public int getSizeInBytes()
			{
				return ScreenVertexRange.SIZE_IN_BYTES;
			}

			@Override
			public ScreenVertexRange createEmptyRef()
			{
				return new ScreenVertexRange( pool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}

			@Override
			public Class< ScreenVertexRange > getRefClass()
			{
				return ScreenVertexRange.class;
			}
		};
	}
}
