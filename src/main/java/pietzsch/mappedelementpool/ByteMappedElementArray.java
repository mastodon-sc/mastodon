package pietzsch.mappedelementpool;

/**
 *
 * TODO: javadoc
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class ByteMappedElementArray implements MappedElementArray< ByteMappedElement >
{
	final byte[] data;

	final int bytesPerElement;

	private final long size;

	private ByteMappedElementArray( final long numElements, final int bytesPerElement )
	{
		this ( numElements, bytesPerElement, null );
	}

	private ByteMappedElementArray( final long numElements, final int bytesPerElement, final ByteMappedElementArray copyFrom )
	{
		final long numBytes = numElements * bytesPerElement;
		if ( numBytes > Integer.MAX_VALUE )
			throw new IllegalArgumentException(
					"trying to create a " + getClass().getName() + " with more than " + maxSize() + " elements of " + bytesPerElement + " bytes.");

		this.data = new byte[ ( int ) numBytes ];
		if ( copyFrom != null )
		{
			final int copyLength = Math.min( copyFrom.data.length, data.length );
			System.arraycopy( copyFrom.data, 0, data, 0, copyLength );
		}
		this.bytesPerElement = bytesPerElement;
		this.size = numElements;
	}

	@Override
	public long size()
	{
		return size;
	}

	@Override
	public long maxSize()
	{
		return Integer.MAX_VALUE / bytesPerElement;
	}

	@Override
	public ByteMappedElement createAccess()
	{
		return new ByteMappedElement( this, 0 );
	}

	@Override
	public void updateAccess( final ByteMappedElement access, final long index )
	{
		access.setDataArray( this );
		access.setElementIndex( ( int ) index );
	}

	public static final MappedElementArray.Factory< ByteMappedElementArray > factory = new MappedElementArray.Factory< ByteMappedElementArray >()
	{
		@Override
		public ByteMappedElementArray createArray( final long numElements, final int bytesPerElement )
		{
			return new ByteMappedElementArray( numElements, bytesPerElement );
		}

		@Override
		public ByteMappedElementArray createArrayAndCopy( final long numElements, final int bytesPerElement, final ByteMappedElementArray copyFrom )
		{
			return new ByteMappedElementArray( numElements, bytesPerElement, copyFrom );
		}
	};
}
