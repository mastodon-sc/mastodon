package net.trackmate.model.abstractmodel;

import gnu.trove.TIntCollection;

import java.util.Collection;

import net.trackmate.util.mempool.MappedElement;

public interface PoolObjectCollection< O extends PoolObject< T >, T extends MappedElement > extends Collection< O >
{
	public TIntCollection getIndexCollection();
}
