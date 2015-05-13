package net.trackmate.graph;

import gnu.trove.TIntCollection;
import net.trackmate.graph.collection.RefCollection;

public interface PoolObjectCollection< O > extends RefCollection< O >
{
	public TIntCollection getIndexCollection();
}
