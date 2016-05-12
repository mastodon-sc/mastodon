package net.trackmate.kdtree;

import gnu.trove.deque.TIntArrayDeque;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.Ref;
import net.trackmate.graph.mempool.MappedElement;

public class KDTreeValueIterable< O extends Ref< O > & RealLocalizable, T extends MappedElement > implements Iterable< O >
{
	private final TIntArrayList nodes;

	private final TIntArrayList subtrees;

	private final KDTree< O, T > tree;

	private final boolean isDoubleIndices;

	public KDTreeValueIterable( final TIntArrayList singleNodes, final TIntArrayList subtrees, final KDTree< O, T > tree, final boolean isDoubleIndices )
	{
		this.nodes = singleNodes;
		this.subtrees = subtrees;
		this.tree = tree;
		this.isDoubleIndices = isDoubleIndices;
	}

	@Override
	public Iterator< O > iterator()
	{
		return isDoubleIndices ? new DoublesIter( tree ) : new Iter( tree );
	}

	private class Iter implements Iterator< O >
	{
		private int nextNodeIndex;

		private int nextSubtreeIndex;

		private final TIntArrayDeque stack;

		private final KDTreeNode< O, T > current;

		private final RefPool< O > pool;

		private final O obj;

		private Iter( final KDTree< O, T > tree )
		{
			nextNodeIndex = 0;
			nextSubtreeIndex = 0;
			stack = new TIntArrayDeque();
			current = tree.createRef();
			pool = tree.getObjectPool();
			obj = pool.createRef();
		}

		@Override
		public boolean hasNext()
		{
			return !stack.isEmpty() || nextSubtreeIndex < subtrees.size() || nextNodeIndex < nodes.size();
		}

		@Override
		public O next()
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
				pool.getByInternalPoolIndex( current.getDataIndex(), obj );
				return obj;
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
				pool.getByInternalPoolIndex( current.getDataIndex(), obj );
				return obj;
			}
			else if ( nextNodeIndex < nodes.size() )
			{
				tree.getByInternalPoolIndex( nodes.get( nextNodeIndex++ ), current );
				pool.getByInternalPoolIndex( current.getDataIndex(), obj );
				return obj;
			}
			else
				return null;
		}

		@Override
		public void remove()
		{}
	}

	private class DoublesIter implements Iterator< O >
	{
		private int nextNodeIndex;

		private int nextSubtreeIndex;

		private final TIntArrayDeque stack;

		private final double[] doubles;

		private final int n;

		private final RefPool< O > pool;

		private final O obj;

		private DoublesIter( final KDTree< O, T > tree )
		{
			nextNodeIndex = 0;
			nextSubtreeIndex = 0;
			stack = new TIntArrayDeque();
			doubles = tree.getDoubles();
			n = tree.numDimensions();
			pool = tree.getObjectPool();
			obj = pool.createRef();
		}

		@Override
		public boolean hasNext()
		{
			return !stack.isEmpty() || nextSubtreeIndex < subtrees.size() || nextNodeIndex < nodes.size();
		}

		@Override
		public O next()
		{
			if ( !stack.isEmpty() )
			{
				final int currentIndex = stack.pop();
				final long leftright = Double.doubleToRawLongBits( doubles[ currentIndex + n ] );
				final int objIndex = ( int ) ( Double.doubleToRawLongBits( doubles[ currentIndex + n + 1 ] ) & 0xffffffff );
				final int left = ( int ) ( leftright >> 32 );
				final int right = ( int ) ( leftright & 0xffffffff );
				if ( left >= 0 )
					stack.push( left );
				if ( right >= 0 )
					stack.push( right );
				pool.getByInternalPoolIndex( objIndex, obj );
				return obj;
			}
			else if ( nextSubtreeIndex < subtrees.size() )
			{
				final int currentIndex = subtrees.get( nextSubtreeIndex++ );
				final long leftright = Double.doubleToRawLongBits( doubles[ currentIndex + n ] );
				final int objIndex = ( int ) ( Double.doubleToRawLongBits( doubles[ currentIndex + n + 1 ] ) & 0xffffffff );
				final int left = ( int ) ( leftright >> 32 );
				final int right = ( int ) ( leftright & 0xffffffff );
				if ( left >= 0 )
					stack.push( left );
				if ( right >= 0 )
					stack.push( right );
				pool.getByInternalPoolIndex( objIndex, obj );
				return obj;
			}
			else if ( nextNodeIndex < nodes.size() )
			{
				final int currentIndex = nodes.get( nextNodeIndex++ );
				final int objIndex = ( int ) ( Double.doubleToRawLongBits( doubles[ currentIndex + n + 1 ] ) & 0xffffffff );
				pool.getByInternalPoolIndex( objIndex, obj );
				return obj;
			}
			else
				return null;
		}

		@Override
		public void remove()
		{}
	}
}
