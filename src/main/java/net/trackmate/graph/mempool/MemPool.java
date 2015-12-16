package net.trackmate.graph.mempool;

import java.util.Iterator;

/**
 * A pool of {@link MappedElement MappedElements}. This is realized on top of
 * one or more {@link MappedElementArray}. It has a current size() and capacity,
 * and provides the possibility to {@link #create()} and {@link #free(int)}
 * elements.
 *
 * <p>
 * If elements are {@link #create() added} beyond the current capacity, the pool
 * grows the underlying storage. If elements are {@link #free(int) removed}, the
 * capacity is not decreased. Instead, free elements are added to a linked list
 * and reused for creating new elements. This ensures the crucial property, that
 * the internal index of any existing element remains fixed.
 *
 * <p>
 * <em>Note that this class is not thread-safe!</em>
 *
 * @param <T>
 *            the {@link MappedElement} type stored in this pool.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class MemPool< T extends MappedElement >
{
	/**
	 * Magic number used to indicate a free element slot. Allocated elements
	 * must never use this number as the first 4 bytes of their data.
	 */
	public static final int FREE_ELEMENT_MAGIC_NUMBER = -2;

	/**
	 * How many bytes each T occupies.
	 */
	protected final int bytesPerElement;

	/**
	 * One proxy access into the underlying {@link MappedElementArray}. This is
	 * used to manipulate the linked list of free elements
	 */
	protected T dataAccess;

	/**
	 * Current capacity of the pool. The underlying storage can hold this many
	 * elements before it must be resized.
	 */
	protected int capacity;

	/**
	 * The number of elements currently allocated in this pool.
	 */
	protected int size;

	/**
	 * The max size this pool ever had. This equals {@link #size} the number of
	 * elements in the free-element linked list.
	 */
	protected int allocatedSize;

	/**
	 * The element index of the first free element, that is, the start of the
	 * free-element linekd list.
	 */
	protected int firstFreeIndex;

	/**
	 * Create an empty pool which can hold {@code capacity} elements of
	 * {@code ByteMappedElement} bytes each.
	 *
	 * @param capacity
	 *            how many elements this pool should hold.
	 * @param bytesPerElement
	 *            how many bytes each element occupies.
	 */
	public MemPool( final int capacity, final int bytesPerElement )
	{
		this.bytesPerElement = Math.max( bytesPerElement, 8 );
		this.capacity = capacity;
		clear();
	}

	/**
	 * Free all allocated elements.
	 */
	public void clear()
	{
		size = 0;
		allocatedSize = 0;
		firstFreeIndex = -1;
	}

	/**
	 * Get the number of elements currently allocated in this pool.
	 *
	 * @return number of elements.
	 */
	public int size()
	{
		return size;
	}

	/**
	 * Allocate a new element. This is either taken from the free-element list
	 * or appended to the end of the pool.
	 *
	 * @return element index of the new element.
	 */
	public int create()
	{
		++size;
		if ( firstFreeIndex < 0 )
			return append();
		else
		{
			final int index = firstFreeIndex;
			updateAccess( dataAccess, firstFreeIndex );
			firstFreeIndex = dataAccess.getIndex( 4 );
			return index;
		}
	}

	/**
	 * Free the element at the given element index.
	 *
	 * @param index
	 *            element index.
	 */
	public void free( final int index )
	{
		if ( index >= 0 && index < allocatedSize )
		{
			updateAccess( dataAccess, index );
			final boolean isFree = dataAccess.getInt( 0 ) == FREE_ELEMENT_MAGIC_NUMBER;
			if ( !isFree )
			{
				--size;
				dataAccess.putIndex( FREE_ELEMENT_MAGIC_NUMBER, 0 );
				dataAccess.putIndex( firstFreeIndex, 4 );
				firstFreeIndex = index;
			}
		}
	}

	/**
	 * Create a new proxy access. This can be made to refer the element at a
	 * given index in this pool by {@link #updateAccess(MappedElement, int)}.
	 *
	 * @return a new proxy access.
	 */
	public abstract T createAccess();

	/**
	 * Make {@code access} refer to the element at {@code index}.
	 */
	public abstract void updateAccess( final T access, final int index );

	/**
	 * Swap the element at {@code index0} with the element at {@code index1}.
	 */
	public abstract void swap( final int index0, final int index1 );

	/**
	 * Append a new element at the end of the list. Must be implemented in
	 * subclasses. It is called when allocating an element and the free-element
	 * list is empty.
	 */
	protected abstract int append();

	/**
	 * Get a {@link PoolIterator} of this pool.
	 *
	 * <p>
	 * A {@link PoolIterator} is not an {@link Iterator Iterator&lt;T&gt;} of the
	 * allocated elements themselves, but rather an iterator of their element
	 * indices.
	 */
	public PoolIterator< T > iterator()
	{
		return new PoolIterator< T >( this );
	}

	/**
	 * Iterator of the indices of allocated elements.
	 */
	public static class PoolIterator< T extends MappedElement >
	{
		private final MemPool< T > pool;

		private int nextIndex;

		private int currentIndex;

		private final T element;

		private PoolIterator( final MemPool< T > pool )
		{
			this.pool = pool;
			element = pool.createAccess();
			reset();
		}

		public void reset()
		{
			nextIndex = ( pool.allocatedSize == 0 ) ? 1 : -1;
			currentIndex = -1;
			prepareNextElement();
		}

		private void prepareNextElement()
		{
			if ( hasNext() )
			{
				while ( ++nextIndex < pool.allocatedSize )
				{
					pool.updateAccess( element, nextIndex );
					final boolean isFree = element.getInt( 0 ) == FREE_ELEMENT_MAGIC_NUMBER;
					if ( !isFree )
						break;
				}
			}
		}

		public boolean hasNext()
		{
			return nextIndex < pool.allocatedSize;
		}

		public int next()
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

	/**
	 * A factory for {@link MemPool}.
	 *
	 * @param <T>
	 *            the {@link MappedElement} type of the created pool.
	 */
	public static interface Factory< T extends MappedElement >
	{
		public MemPool< T > createPool( final int capacity, final int bytesPerElement );
	}
}
