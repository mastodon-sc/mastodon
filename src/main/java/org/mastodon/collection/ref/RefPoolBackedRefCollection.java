package org.mastodon.collection.ref;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;

/**
 * A {@link RefCollection} that contains objects stored in a {@link RefPool}.
 *
 * @param <O>
 *            the type of object stored in the collection.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface RefPoolBackedRefCollection< O > extends RefCollection< O >
{
	public RefPool< O > getRefPool();
}
