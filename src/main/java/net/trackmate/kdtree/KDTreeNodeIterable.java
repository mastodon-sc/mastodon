package net.trackmate.kdtree;

import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.util.TIntArrayDeque;

public class KDTreeNodeIterable< O extends PoolObject< O, ? > & RealLocalizable, T extends MappedElement > implements Iterable< KDTreeNode< O, T > >
{
	private final TIntArrayList nodes;

	private final TIntArrayList subtrees;

	private final KDTree< O, T > tree;

	public KDTreeNodeIterable( final TIntArrayList singleNodeIndices, final TIntArrayList subtreeIndices, final KDTree< O, T > tree )
	{
		this.nodes = singleNodeIndices;
		this.subtrees = subtreeIndices;
		this.tree = tree;
	}

	@Override
	public Iterator< KDTreeNode< O, T > > iterator()
	{
		return new Iter( tree );
	}

	private class Iter implements Iterator< KDTreeNode< O, T > >
	{
		private int nextNodeIndex;

		private int nextSubtreeIndex;

		private final TIntArrayDeque stack;

		private final KDTreeNode< O, T > current;

		private Iter( final KDTree< O, T > tree )
		{
			nextNodeIndex = 0;
			nextSubtreeIndex = 0;
			stack = new TIntArrayDeque();
			current = tree.createRef();
		}

		@Override
		public boolean hasNext()
		{
			return !stack.isEmpty() || nextSubtreeIndex < subtrees.size() || nextNodeIndex < nodes.size();
		}

		@Override
		public KDTreeNode< O, T > next()
		{
			if ( !stack.isEmpty() )
			{
				tree.getByInternalPoolIndex( stack.pop(), current );
				final int left = current.getLeftIndex();
				final int right = current.getRightIndex();
				if ( left >= 0 )
					stack.push( left );
				if ( right >= 0 )
					stack.push( right );
				return current;
			}
			else if ( nextSubtreeIndex < subtrees.size() )
			{
				tree.getByInternalPoolIndex( subtrees.get( nextSubtreeIndex++ ), current );
				final int left = current.getLeftIndex();
				final int right = current.getRightIndex();
				if ( left >= 0 )
					stack.push( left );
				if ( right >= 0 )
					stack.push( right );
				return current;
			}
			else if ( nextNodeIndex < nodes.size() )
			{
				tree.getByInternalPoolIndex( nodes.get( nextNodeIndex++ ), current );
				return current;
			}
			else
				return null;
		}

		@Override
		public void remove()
		{}
	}
}
