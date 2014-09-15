package net.trackmate.graph;

import gnu.trove.TIntCollection;

import java.util.Collection;

import net.trackmate.graph.mempool.MappedElement;

public interface PoolObjectCollection< O extends PoolObject< T >, T extends MappedElement > extends Collection< O >
{
	public TIntCollection getIndexCollection();
}
