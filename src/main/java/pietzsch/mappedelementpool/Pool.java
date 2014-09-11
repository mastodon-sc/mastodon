package pietzsch.mappedelementpool;


/**
 * Note that this class is not thread-safe!
 *
 * TODO: javadoc
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public abstract class Pool< T extends MappedElement >
{
	protected final int bytesPerElement;

	protected T dataAccess;

	protected long capacity;

	protected long size;

	protected long firstFreeIndex;

	public Pool( final long capacity, final int bytesPerElement )
	{
		this.bytesPerElement = Math.max( bytesPerElement, 12 );
		this.capacity = capacity;
		clear();
	}

	public void clear()
	{
		size = 0;
		firstFreeIndex = -1;
	}

	public long create()
	{
		if ( firstFreeIndex < 0 )
			return append();
		else
		{
			final long index = firstFreeIndex;
			updateAccess( dataAccess, firstFreeIndex );
			firstFreeIndex = dataAccess.getLong( 4 );
			return index;
		}
	}

	public void free( final long index )
	{
		if ( index >= 0 && index <  size )
		{
			updateAccess( dataAccess, index );
			final boolean isFree = dataAccess.getInt( 0 ) < 0;
			if ( ! isFree )
			{
				dataAccess.putLong( -1, 0 );
				dataAccess.putLong( firstFreeIndex, 4 );
				firstFreeIndex = index;
			}
		}
	}

	public abstract T createAccess();

	public abstract void updateAccess( final T access, final long index );

	protected abstract long append();

	public PoolIterator< T > iterator()
	{
		return new PoolIterator< T >( this );
	}

	public static class PoolIterator< T extends MappedElement >
	{
		private final Pool< T > pool;

		private long nextIndex;

		private long currentIndex;

		private final T element;

		private PoolIterator( final Pool< T > pool )
		{
			this.pool = pool;
			element = pool.createAccess();
			reset();
		}

		public void reset()
		{
			nextIndex = ( pool.size == 0 ) ? 1 : -1;
			currentIndex = -1;
			prepareNextElement();
		}

		private void prepareNextElement()
		{
			if ( hasNext() )
			{
				while( ++nextIndex < pool.size )
				{
					pool.updateAccess( element, nextIndex );
					final boolean isFree = element.getInt( 0 ) < 0;
					if ( ! isFree )
						break;
				}
			}
		}

		public boolean hasNext()
		{
			return nextIndex < pool.size;
		}

		public long next()
		{
			currentIndex = nextIndex;
			prepareNextElement();
			return currentIndex;
		}

		public void remove()
		{
			if ( currentIndex >= 0 )
				pool.free( currentIndex );
		}
	}

	public static interface Factory< T extends MappedElement >
	{
		public Pool< T > createPool( final long capacity, final int bytesPerElement );
	}
}
