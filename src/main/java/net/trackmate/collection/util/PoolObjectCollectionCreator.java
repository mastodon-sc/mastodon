package net.trackmate.collection.util;

import java.util.Iterator;

import net.trackmate.pool.Pool;
import net.trackmate.pool.PoolObject;

public class PoolObjectCollectionCreator< O extends PoolObject< O, ? > > extends AbstractRefPoolCollectionCreator< O, Pool< O, ? > >
{
	public PoolObjectCollectionCreator( final Pool< O, ? > pool )
	{
		super( pool );
	}

	@Override
	public int size()
	{
		return pool.size();
	}

	@Override
	public Iterator< O > iterator()
	{
		return pool.iterator();
	}
}
