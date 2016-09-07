package org.mastodon.graph.io;

import org.mastodon.RefPool;

import gnu.trove.map.TIntIntMap;

/**
 * Maps IDs (used in a file) to objects.
 *
 * @param <O>
 *            the object type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class FileIdToObjectMap< O >
{
	private final TIntIntMap fileIdToObjectId;

	private final RefPool< O > pool;

	public FileIdToObjectMap(
			final TIntIntMap fileIdToObjectId,
			final RefPool< O > pool )
	{
		this.fileIdToObjectId = fileIdToObjectId;
		this.pool = pool;
	}

	public O getObject( final int id, final O ref )
	{
		return pool.getObject( fileIdToObjectId.get( id ), ref );
	}

	public O createRef()
	{
		return pool.createRef();
	}

	public void releaseRef( final O ref )
	{
		pool.releaseRef( ref );
	}
}
