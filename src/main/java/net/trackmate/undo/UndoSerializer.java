package net.trackmate.undo;

/**
 * Provides serialization of (parts of) an object of type {@code O} to a byte
 * array.
 *
 * @param <O>
 *            type of object.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface UndoSerializer< O >
{
	/**
	 * How many bytes are needed for storage. (This is the expected size of the
	 * {@code bytes} array passed to {@link #getBytes(Object, byte[])},
	 * {@link #setBytes(Object, byte[])}.
	 *
	 * @return number of bytes are needed for storage.
	 */
	public int getNumBytes();

	/**
	 * Store data from {@code obj} into {@code bytes}.
	 */
	public void getBytes( final O obj, final byte[] bytes );

	/**
	 * Restore data from {@code bytes} into {@code obj}.
	 */
	public void setBytes( final O obj, final byte[] bytes );

	/**
	 * Notify that bytes have been written ({@link #setBytes(Object, byte[])})
	 * to {@code obj}.
	 * <p>
	 * Note: Currently nothing is ever done in between {@code setBytes()} and
	 * {@code notifySet()}, so maybe this will be removed later and
	 * notifications directly linked to {@codes setBytes}. For now, we keep it
	 * explicit.
	 */
	public void notifySet( final O obj );
}
