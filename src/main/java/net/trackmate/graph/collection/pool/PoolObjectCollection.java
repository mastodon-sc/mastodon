package net.trackmate.graph.collection.pool;

import gnu.trove.TIntCollection;
import net.trackmate.graph.collection.RefCollection;


// TODO rename. IntBackedRefCollection?
public interface PoolObjectCollection< O > extends RefCollection< O >
{
	public TIntCollection getIndexCollection();
}
