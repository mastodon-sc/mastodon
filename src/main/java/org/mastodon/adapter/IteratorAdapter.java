package org.mastodon.adapter;

import java.util.Iterator;

public class IteratorAdapter< O, WO >
		implements Iterator< WO >
{
	private final Iterator< O > iterator;

	private final RefBimap< O, WO > map;

	private final WO ref;

	public IteratorAdapter(
			final Iterator< O > iterator,
			final RefBimap< O, WO > map )
	{
		this.iterator = iterator;
		this.map = map;
		this.ref = map.reusableRightRef();
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public WO next()
	{
		return map.getRight( iterator.next(), ref );
	}

}
