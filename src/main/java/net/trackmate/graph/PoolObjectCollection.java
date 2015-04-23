package net.trackmate.graph;

import gnu.trove.TIntCollection;
import net.trackmate.graph.collection.RefCollection;
import net.trackmate.graph.mempool.MappedElement;

public interface PoolObjectCollection< O extends PoolObject< O, T >, T extends MappedElement > extends RefCollection< O >
{
	public TIntCollection getIndexCollection();
}
