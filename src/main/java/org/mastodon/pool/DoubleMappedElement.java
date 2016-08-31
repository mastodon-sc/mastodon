package org.mastodon.pool;

/**
 * A {@link MappedElement} that stores its data in a portion of a {@code double[]}
 * array.
 *
 * <p>
 * Contract: A {@link DoubleMappedElement} may be used on different
 * {@link DoubleMappedElementArray}s but they all must have the same
 * bytesPerElement.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class DoubleMappedElement implements MappedElement
{
	/**
	 * How many bytes are required to store one element.
	 */
	private final int bytesPerElement;

	/**
	 * The current base offset (in bytes) into the underlying
	 * {@link DoubleMappedElementArray#data storage array}.
	 */
	private int baseOffset;

	/**
	 * Contains the {@link DoubleMappedElementArray#data storage array}.
	 */
	private DoubleMappedElementArray dataArray;

	/**
	 * Create a new proxy for representing element is in the given
	 * {@link DoubleMappedElementArray}.
	 *
	 * @param dataArray
	 *            initial storage.
	 * @param index
	 *            initial element index in storage.
	 */
	public DoubleMappedElement( final DoubleMappedElementArray dataArray, final int index )
	{
		this.dataArray = dataArray;
		this.bytesPerElement = dataArray.bytesPerElement;
		this.baseOffset = index * bytesPerElement;
	}

	void setDataArray( final DoubleMappedElementArray dataArray )
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
	public void putByte( final byte value, final int offset )
	{
		DoubleUtils.putByte( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public byte getByte( final int offset )
	{
		return DoubleUtils.getByte( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putBoolean( final boolean value, final int offset )
	{
		DoubleUtils.putBoolean( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public boolean getBoolean( final int offset )
	{
		return DoubleUtils.getBoolean( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putInt( final int value, final int offset )
	{
		DoubleUtils.putInt( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public int getInt( final int offset )
	{
		return DoubleUtils.getInt( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putIndex( final int value, final int offset )
	{
		DoubleUtils.putIndex( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public int getIndex( final int offset )
	{
		return DoubleUtils.getIndex( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putLong( final long value, final int offset )
	{
		DoubleUtils.putLong( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public long getLong( final int offset )
	{
		return DoubleUtils.getLong( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putFloat( final float value, final int offset )
	{
		DoubleUtils.putFloat( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public float getFloat( final int offset )
	{
		return DoubleUtils.getFloat( dataArray.data, baseOffset + offset );
	}

	@Override
	public void putDouble( final double value, final int offset )
	{
		DoubleUtils.putDouble( value, dataArray.data, baseOffset + offset );
	}

	@Override
	public double getDouble( final int offset )
	{
		return DoubleUtils.getDouble( dataArray.data, baseOffset + offset );
	}

	/**
	 * Two {@link DoubleMappedElement} are equal if they refer to the same index
	 * in the same {@link DoubleMappedElementArray}.
	 */
	@Override
	public boolean equals( final Object obj )
	{
		if ( obj instanceof DoubleMappedElement )
		{
			final DoubleMappedElement e = ( DoubleMappedElement ) obj;
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
