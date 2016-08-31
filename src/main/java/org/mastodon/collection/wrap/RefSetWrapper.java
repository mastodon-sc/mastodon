package org.mastodon.collection.wrap;

import java.util.Set;

import org.mastodon.collection.RefSet;

/**
 * Wraps a {@link Set} as a {@link RefSet}.
 *
 * @param <O> the type of elements maintained by this set
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefSetWrapper< O > extends AbstractRefCollectionWrapper< O, Set< O > > implements RefSet< O >
{
	public RefSetWrapper( final Set< O > set )
	{
		super( set );
	}
}
