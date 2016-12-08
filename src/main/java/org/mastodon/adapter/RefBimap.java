package org.mastodon.adapter;

/**
 * TODO DOCUMENT!!!!!!!!!!!!!!!!!!!!!!!
 *
 * @param <V>
 * @param <W>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface RefBimap< V, W >
{
	// TODO: don't need ref ???
	public V getLeft( W right /*, V ref */ );

	public W getRight( V left, W ref );

	public V reusableLeftRef( W ref );

	public W reusableRightRef();

	public void releaseRef( W ref );
}
