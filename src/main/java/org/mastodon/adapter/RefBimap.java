package org.mastodon.adapter;

public interface RefBimap< V, W >
{
	// TODO: don't need ref ???
	public V getLeft( W right /*, V ref*/ );

	public W getRight( V left, W ref );

	public V extractLeftRef( W ref );

//	public W extractRightRef( V ref );
}