package org.mastodon.collection;

import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.collection.wrap.RefDequeWrapper;
import org.mastodon.collection.wrap.RefListWrapper;
import org.mastodon.collection.wrap.RefMapWrapper;
import org.mastodon.collection.wrap.RefSetWrapper;
import org.mastodon.collection.wrap.RefStackWrapper;


/**
 * Static utility methods, for example wrapping standard {@code java.util}
 * {@link Collections} as {@link RefCollection}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefCollections
{
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < O > Iterator< O > safeIterator( final Iterator< O > iterator, final RefCollection< O > collection )
	{
		if ( iterator instanceof MaybeRefIterator )
			if ( ( ( MaybeRefIterator ) iterator ).isRefIterator() )
				return new SafeRefIteratorWrapper( iterator, collection );
		return iterator;
	}

	public static < O > RefSet< O > wrap( final Set< O > set )
	{
		return new RefSetWrapper<>( set );
	}

	public static < O > RefList< O > wrap( final List< O > set )
	{
		return new RefListWrapper<>( set );
	}

	public static < O > RefDeque< O > wrap( final Deque< O > set )
	{
		return new RefDequeWrapper<>( set );
	}

	public static < O > RefStack< O > wrapAsStack( final Deque< O > set )
	{
		return new RefStackWrapper<>( set );
	}

	public static < K, O > RefRefMap< K, O > wrap( final Map< K, O > map )
	{
		return new RefMapWrapper<>( map );
	}
}
