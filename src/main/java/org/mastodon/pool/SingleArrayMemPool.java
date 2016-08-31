package org.mastodon.pool;


/**
 * A {@link MemPool} that keeps data in a single {@link MappedElementArray}.
 *
 * @param <T>
 * @param <A>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class SingleArrayMemPool< A extends MappedElementArray< A, T >, T extends MappedElement > extends MemPool< T >
{
	private final A data;

	public SingleArrayMemPool( final MappedElementArray.Factory< A > arrayFactory, final int capacity, final int bytesPerElement )
	{
		super( capacity, bytesPerElement );
		data = arrayFactory.createArray( capacity, this.bytesPerElement );
		dataAccess = data.createAccess();
	}

	@Override
	protected int append()
	{
		final int index = allocatedSize++;
		if ( allocatedSize > capacity )
		{
			capacity = Math.min( capacity << 1, data.maxSize() );
			if ( allocatedSize > capacity )
				throw new IllegalArgumentException( "cannot store more than " + data.maxSize() + " elements" );
			data.resize( capacity );
		}
		return index;
	}

	@Override
	public T createAccess()
	{
		return data.createAccess();
	}

	@Override
	public void updateAccess( final T access, final int index )
	{
		data.updateAccess( access, index );
	}

	@Override
	public void swap( final int index0, final int index1 )
	{
		data.swapElement( index0, data, index1 );
	}

	/**
	 * For internal use only!
	 */
	public A getDataArray()
	{
		return data;
	}

	/**
	 * Create a factory for {@link SingleArrayMemPool}s that use the specified
	 * {@code arrayFactory} for creating their storage
	 * {@link MappedElementArray}.
	 */
	public static < A extends MappedElementArray< A, T >, T extends MappedElement >
			MemPool.Factory< T > factory( final MappedElementArray.Factory< A > arrayFactory )
	{
		return new MemPool.Factory< T >()
		{
			@Override
			public MemPool< T > createPool( final int capacity, final int bytesPerElement )
			{
				return new SingleArrayMemPool< A, T >( arrayFactory, capacity, bytesPerElement );
			}
		};
	}
}
