package org.mastodon.adapter;

/**
 * TODO DOCUMENT!!!!!!!!!!!!!!!!!!!!!!!
 *
 * @param <L>
 * @param <R>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface RefBimap< L, R >
{
	public L getLeft( R right );

	public R getRight( L left, R ref );

	public L reusableLeftRef( R ref );

	public R reusableRightRef();

	public void releaseRef( R ref );
}
