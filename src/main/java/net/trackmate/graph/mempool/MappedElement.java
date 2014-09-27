package net.trackmate.graph.mempool;

/**
 * Maps into region of underlying memory area (a primitive array or similar).
 * The memory is split into same sized regions. By
 * {@link MappedElementArray#updateAccess(MappedElement, int)}, the index of the
 * region that this {@link MappedElement} represents can be set. This translates
 * into computing a base offset in the memory area. Then values of different
 * types can be read or written at (byte) offsets relative to the current base
 * offset. For example {@code putLong( 42l, 2 )} would put write the
 * {@code long} value 42 into the bytes 2 ... 10 relative to the current base
 * offset.
 *
 * This is used to build imglib2-like proxy objects that map into primitive
 * arrays.
 *
 * Note: The method for updating the base offset
 * {@link MappedElementArray#updateAccess(MappedElement, int)} needs to be in
 * the {@link MappedElementArray}, not here. This is because data might be split
 * up into several {@link MappedElementArray MappedElementArrays}, in which case
 * the reference to the memory area must be updated in addition to the base
 * offset.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface MappedElement
{
	public void putByte( final byte value, final int offset );

	public byte getByte( final int offset );

	public void putBoolean( final boolean value, final int offset );

	public boolean getBoolean( final int offset );

	public void putInt( final int value, final int offset );

	public int getInt( final int offset );

	public void putIndex( final int value, final int offset );

	public int getIndex( final int offset );

	public void putLong( final long value, final int offset );

	public long getLong( final int offset );

	public void putFloat( final float value, final int offset );

	public float getFloat( final int offset );

	public void putDouble( final double value, final int offset );

	public double getDouble( final int offset );
}
