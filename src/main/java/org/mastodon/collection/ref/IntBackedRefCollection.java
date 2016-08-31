package org.mastodon.collection.ref;

import org.mastodon.Ref;
import org.mastodon.collection.RefCollection;

import gnu.trove.TIntCollection;

/**
 * A {@link RefCollection} that is backed by a {@link TIntCollection} storing
 * {@link Ref#getInternalPoolIndex() pool indices}.
 *
 * @param <O>
 *            the type of object stored in the collection.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface IntBackedRefCollection< O > extends RefCollection< O >
{
	public TIntCollection getIndexCollection();
}
