package net.trackmate.graph.mempool;

/**
 * An array of {@link MappedElement MappedElements}. The array can grow, see
 * {@link #resize(int)}, which involves reallocating and copying the underlying
 * primitive array.
 *
 * @param <T>
 *            the {@link MappedElement} type stored in this array.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface MappedElementArray< A extends MappedElementArray< A, T >, T extends MappedElement >
{
	/**
	 * Get the number of {@link MappedElement elements} in this array.
	 *
	 * @return number of {@link MappedElement elements} in this array.
	 */
	public int size();

	/**
	 * The maximum number of {@link MappedElement elements} that could be
	 * maximally contained in a {@link MappedElementArray} of this type.
	 *
	 * This depends on the size of a single element and the strategy used to
	 * store their data. For example, a {@link ByteMappedElementArray} stores
	 * data in a {@code byte[]} array, which means that at most
	 * 2GB/size(element) can be stored. If data would be mapped into a
	 * {@code long[]} it would be 8 times more, etc.
	 *
	 * @return maximum number of {@link MappedElement elements} storable in a
	 *         {@link MappedElementArray} of the same type.
	 */
	public int maxSize();

	/**
	 * Create a new proxy referring to the element at index 0.
	 *
	 * @return new access (proxy).
	 */
	public T createAccess();

	/**
	 * Update the given {@link MappedElement} to refer the element at
	 * {@code index} in this array.
	 */
	public void updateAccess( final T access, final int index );

	/**
	 * Set the size of this array to contain {@code numElements}
	 * {@link MappedElement elements}.
	 *
	 * @param numElements
	 *            new number of {@link MappedElement elements} in this array.
	 */
	public void resize( final int numElements );

	/**
	 * Swap the {@link MappedElement} data at {@code index} in this
	 * {@link MappedElementArray} with the element at {@code arrayIndex} in the
	 * {@link MappedElementArray} {@code array}.
	 *
	 * @param index
	 *            index of element to swap in this array.
	 * @param array
	 *            other array
	 * @param arrayIndex
	 *            index of element to swap in other array.
	 */
	public void swapElement( final int index, final A array, final int arrayIndex );

	/**
	 * A factory for {@link MappedElementArray}.
	 *
	 * @param <A>
	 *            the type of {@link MappedElementArray} created by this
	 *            factory.
	 */
	public static interface Factory< A > // A extends MappedElementArray< T >
	{
		/**
		 * Create an array containing {@code numElements} elements of
		 * {@code bytesPerElement} bytes each.
		 */
		public A createArray( final int numElements, final int bytesPerElement );
	}
}
