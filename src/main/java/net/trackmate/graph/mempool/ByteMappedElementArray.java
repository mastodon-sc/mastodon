package net.trackmate.graph.mempool;

/**
 * A {@link MappedElementArray} that stores {@link ByteMappedElement
 * ByteMappedElements} in a {@code byte[]} array.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class ByteMappedElementArray implements MappedElementArray< ByteMappedElement, ByteMappedElementArray >
{
	/**
	 * The current data storage. This is changed when the array is
	 * {@link #resize(int) resized}.
	 */
	byte[] data;

	final private byte[] swapTmp;

	/**
	 * How many bytes on element in this array accupies.
	 */
	final int bytesPerElement;

	/**
	 * How many elements are stored in this array.
	 */
	private int size;

	/**
	 * Create a new array containing {@code numElements} elements of
	 * {@code bytesPerElement} bytes each.
	 */
	private ByteMappedElementArray( final int numElements, final int bytesPerElement )
	{
		final long numBytes = ( long ) numElements * bytesPerElement;
		if ( numBytes > Integer.MAX_VALUE )
			throw new IllegalArgumentException(
					"trying to create a " + getClass().getName() + " with more than " + maxSize() + " elements of " + bytesPerElement + " bytes.");

		this.data = new byte[ ( int ) numBytes ];
		this.swapTmp = new byte[ bytesPerElement ];
		this.bytesPerElement = bytesPerElement;
		this.size = numElements;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public int maxSize()
	{
		return Integer.MAX_VALUE / bytesPerElement;
	}

	@Override
	public ByteMappedElement createAccess()
	{
		return new ByteMappedElement( this, 0 );
	}

	@Override
	public void updateAccess( final ByteMappedElement access, final int index )
	{
		access.setDataArray( this );
		access.setElementIndex( ( int ) index );
	}

	/**
	 * {@inheritDoc} Moves the data using
	 * {@link System#arraycopy(Object, int, Object, int, int)}, using
	 * {@link #swapTmp} as a temporary.
	 */
	@Override
	public void swapElement( final int index, final ByteMappedElementArray array, final int arrayIndex )
	{
		final int baseOffset = index * bytesPerElement;
		final int arrayBaseOffset = arrayIndex * bytesPerElement;
		System.arraycopy( data, baseOffset, swapTmp, 0, bytesPerElement );
		System.arraycopy( array.data, arrayBaseOffset, data, baseOffset, bytesPerElement );
		System.arraycopy( swapTmp, 0, array.data, arrayBaseOffset, bytesPerElement );
	}

	/**
	 * {@inheritDoc} The storage array is reallocated and the old contents
	 * copied over.
	 */
	@Override
	public void resize( final int numElements )
	{
		final long numBytes = ( long ) numElements * bytesPerElement;
		if ( numBytes > Integer.MAX_VALUE )
			throw new IllegalArgumentException(
					"trying to resize a " + getClass().getName() + " to more than " + maxSize() + " elements of " + bytesPerElement + " bytes.");

		final byte[] datacopy = new byte[ ( int ) numBytes ];
			final int copyLength = Math.min( data.length, datacopy.length );
			System.arraycopy( data, 0, datacopy, 0, copyLength );
		this.data = datacopy;
		this.size = numElements;
	}

	/**
	 * A factory for {@link ByteMappedElementArray}s.
	 */
	public static final MappedElementArray.Factory< ByteMappedElementArray > factory = new MappedElementArray.Factory< ByteMappedElementArray >()
	{
		@Override
		public ByteMappedElementArray createArray( final int numElements, final int bytesPerElement )
		{
			return new ByteMappedElementArray( numElements, bytesPerElement );
		}
	};
}
