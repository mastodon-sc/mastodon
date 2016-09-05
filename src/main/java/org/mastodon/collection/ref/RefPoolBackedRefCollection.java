package org.mastodon.collection.ref;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;

public interface RefPoolBackedRefCollection< O > extends RefCollection< O >
{
	public RefPool< O > getRefPool();
}
