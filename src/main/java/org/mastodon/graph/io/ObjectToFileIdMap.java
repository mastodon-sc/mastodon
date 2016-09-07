package org.mastodon.graph.io;

import org.mastodon.RefPool;

import gnu.trove.map.TIntIntMap;

/**
 * Maps objects to IDs (used in a file).
 *
 * @param <O>
 *            the object type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class ObjectToFileIdMap< O >
{
	private final TIntIntMap objectIdToFileId;

	private final RefPool< O > pool;

	public ObjectToFileIdMap(
			final TIntIntMap objectIdToFileId,
			final RefPool< O > pool )
	{
		this.objectIdToFileId = objectIdToFileId;
		this.pool = pool;
	}

	public int getId( final O object )
	{
		return objectIdToFileId.get( pool.getId( object ) );
	}
}
