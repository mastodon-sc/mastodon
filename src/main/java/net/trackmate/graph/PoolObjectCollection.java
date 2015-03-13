package net.trackmate.graph;

import gnu.trove.TIntCollection;

import java.util.Collection;

import net.trackmate.graph.collection.RefCollection;
import net.trackmate.graph.mempool.MappedElement;

public interface PoolObjectCollection< O extends PoolObject< O, T >, T extends MappedElement > extends RefCollection< O >
{
	public TIntCollection getIndexCollection();
}
