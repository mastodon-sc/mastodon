package net.trackmate.util.mempool;

/**
 * TODO: javadoc
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface MappedElementArray< T extends MappedElement >
{
	public int size();

	public int maxSize();

	public T createAccess();

	public void updateAccess( final T access, final int index );

	public static interface Factory< A > // A extends MappedElementArray< T >
	{
		public A createArray( final int numElements, final int bytesPerElement );

		public A createArrayAndCopy( final int numElements, final int bytesPerElement, final A copyFrom );
	}
}
