package net.trackmate.util.mempool;

/**
 *
 * TODO: javadoc
 *
 *
 * Contract: may be used on different {@link ByteMappedElementArray}s but they all must have the same bytesPerElement.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class ByteMappedElement implements MappedElement
{
	private final int bytesPerElement;

	private int baseOffset;

	private ByteMappedElementArray dataArray;

	private final byte[] data;

	public ByteMappedElement( final ByteMappedElementArray dataArray, final int index )
	{
		this.dataArray = dataArray;
		this.data = dataArray.data;
		this.bytesPerElement = dataArray.bytesPerElement;
		this.baseOffset = index * bytesPerElement;
	}

	void setDataArray( final ByteMappedElementArray dataArray )
	{
		this.dataArray = dataArray;
	}

	/**
	 * Set the index of the element that this {@link MappedElement} represents.
	 * Computes the base offset in the underlying memory area as
	 * <em>baseOffset = index * bytesPerElement</em>.
	 *
	 * @param index
	 *            index of the element that this {@link MappedElement} should
	 *            point to.
	 */
	void setElementIndex( final int index )
	{
		this.baseOffset = index * bytesPerElement;
	}

	@Override
	public void putInt( final int value, final int offset )
	{
		ByteUtils.putInt( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public int getInt( final int offset )
	{
		return ByteUtils.getInt( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putIndex( final int value, final int offset )
	{
		ByteUtils.putIndex( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public int getIndex( final int offset )
	{
		return ByteUtils.getIndex( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putLong( final long value, final int offset )
	{
		ByteUtils.putLong( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public long getLong( final int offset )
	{
		return ByteUtils.getLong( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putFloat( final float value, final int offset )
	{
		ByteUtils.putFloat( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public float getFloat( final int offset )
	{
		return ByteUtils.getFloat( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putDouble( final double value, final int offset )
	{
		ByteUtils.putDouble( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public double getDouble( final int offset )
	{
		return ByteUtils.getDouble( dataArray.data, baseOffset + offset );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if ( obj instanceof ByteMappedElement )
		{
			final ByteMappedElement e = ( ByteMappedElement ) obj;
			return e.dataArray == dataArray && e.baseOffset == baseOffset;
		}
		else
			return false;
	}

	@Override
	public int hashCode()
	{
		return dataArray.hashCode() + baseOffset;
	}
}
