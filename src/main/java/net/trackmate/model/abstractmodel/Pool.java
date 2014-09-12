package net.trackmate.model.abstractmodel;

import net.trackmate.util.mempool.MappedElement;
import net.trackmate.util.mempool.MemPool;


public abstract class Pool< O extends PoolObject< T >, T extends MappedElement >
{
	public abstract O createEmptyRef();

	abstract MemPool< T > getMemPool();
}
