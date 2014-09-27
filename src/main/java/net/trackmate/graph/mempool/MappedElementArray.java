package net.trackmate.graph.mempool;

/**
 * A fixed-size array of {@link MappedElement MappedElements}.
 *
 * @param <T>
 *            the {@link MappedElement} type stored in this array.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface MappedElementArray< T extends MappedElement >
{
	/**
	 * Get the number of {@link MappedElement elements} in this array.
	 *
	 * @return number of {@link MappedElement elements} in this array.
	 */
	public int size();

	/**
	 * The maximum number of {@link MappedElement elements} that could be
	 * maximally contained in a {@link MappedElementArray} of the same type as
	 * this one.
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
	 * Create a new proxy referring element 0.
	 *
	 * @return new access (proxy).
	 */
	public T createAccess();

	/**
	 * Update the given {@code access} to refer the element at {@code index} in
	 * this array.
	 */
	public void updateAccess( final T access, final int index );

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

		/**
		 * Create an array containing {@code numElements} elements of
		 * {@code bytesPerElement} bytes each. Copy data from the array
		 * {@code copyFrom}. {@code copyFrom} may be larger or smaller than the
		 * created array in which case as much data as possible is copied.
		 */
		public A createArrayAndCopy( final int numElements, final int bytesPerElement, final A copyFrom );
	}
}
