package net.trackmate.graph.mempool;


/**
 * A {@link MemPool} that keeps data in a single {@link MappedElementArray}.
 *
 * @param <T>
 * @param <A>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class SingleArrayMemPool< T extends MappedElement, A extends MappedElementArray< T, A > > extends MemPool< T >
{
	private final A data;

	public SingleArrayMemPool( final MappedElementArray.Factory< A > arrayFactory, final int capacity, final int bytesPerElement )
	{
		super( capacity, bytesPerElement );
		data = arrayFactory.createArray( ( int ) capacity, this.bytesPerElement );
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
		// TODO: create data.updateAccessIndex() that does not update the dataArray of the access:
		// This can be used in SingleArrayPool (but not in MultiArrayPool)
		data.updateAccess( access, index );
	}

	@Override
	public void swap( final int index0, final int index1 )
	{
		data.swapElement( index0, data, index1 );
	}

	/**
	 * Create a factory for {@link SingleArrayMemPool}s that use the specified
	 * {@code arrayFactory} for creating their storage
	 * {@link MappedElementArray}.
	 */
	public static < T extends MappedElement, A extends MappedElementArray< T, A > >
			MemPool.Factory< T > factory( final MappedElementArray.Factory< A > arrayFactory )
	{
		return new MemPool.Factory< T >()
		{
			@Override
			public MemPool< T > createPool( final int capacity, final int bytesPerElement )
			{
				return new SingleArrayMemPool< T, A >( arrayFactory, capacity, bytesPerElement );
			}
		};
	}
}
