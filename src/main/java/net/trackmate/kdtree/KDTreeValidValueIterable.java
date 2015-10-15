package net.trackmate.kdtree;

import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.Ref;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.util.TIntArrayDeque;

public class KDTreeValidValueIterable< O extends Ref< O > & RealLocalizable, T extends MappedElement > implements Iterable< O >
{
	private final TIntArrayList nodes;

	private final TIntArrayList subtrees;

	private final KDTree< O, T > tree;

	private final boolean isDoubleIndices;

	public KDTreeValidValueIterable( final TIntArrayList singleNodes, final TIntArrayList subtrees, final KDTree< O, T > tree, final boolean isDoubleIndices )
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

		private final O ref;

		private final O nextref;

		private boolean hasNext;

		private Iter( final KDTree< O, T > tree )
		{
			nextNodeIndex = 0;
			nextSubtreeIndex = 0;
			stack = new TIntArrayDeque();
			current = tree.createRef();
			pool = tree.getObjectPool();
			ref = pool.createRef();
			nextref = pool.createRef();
			hasNext = prepareNext();
		}

		private boolean prepareNext()
		{
			while ( true )
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
				}
				else if ( nextNodeIndex < nodes.size() )
				{
					tree.getByInternalPoolIndex( nodes.get( nextNodeIndex++ ), current );
				}
				else
					return false;

				if ( current.isValid() )
				{
					pool.getByInternalPoolIndex( current.getDataIndex(), nextref );
					return true;
				}
			}
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public O next()
		{
			if ( hasNext )
			{
				ref.refTo( nextref );
				hasNext = prepareNext();
			}
			return ref;
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

		private final O ref;

		private final O nextref;

		private boolean hasNext;

		private DoublesIter( final KDTree< O, T > tree )
		{
			nextNodeIndex = 0;
			nextSubtreeIndex = 0;
			stack = new TIntArrayDeque();
			doubles = tree.getDoubles();
			n = tree.numDimensions();
			pool = tree.getObjectPool();
			ref = pool.createRef();
			nextref = pool.createRef();
			hasNext = prepareNext();
		}

		private boolean prepareNext()
		{
			while ( true )
			{
				int currentIndex;
				if ( !stack.isEmpty() )
				{
					currentIndex = stack.pop();
					final long leftright = Double.doubleToRawLongBits( doubles[ currentIndex + n ] );
					final int left = ( int ) ( leftright >> 32 );
					final int right = ( int ) ( leftright & 0xffffffff );
					if ( left >= 0 )
						stack.push( left );
					if ( right >= 0 )
						stack.push( right );
				}
				else if ( nextSubtreeIndex < subtrees.size() )
				{
					currentIndex = subtrees.get( nextSubtreeIndex++ );
					final long leftright = Double.doubleToRawLongBits( doubles[ currentIndex + n ] );
					final int left = ( int ) ( leftright >> 32 );
					final int right = ( int ) ( leftright & 0xffffffff );
					if ( left >= 0 )
						stack.push( left );
					if ( right >= 0 )
						stack.push( right );
				}
				else if ( nextNodeIndex < nodes.size() )
				{
					currentIndex = nodes.get( nextNodeIndex++ );
				}
				else
					return false;

				final int flags = ( int ) ( Double.doubleToRawLongBits( doubles[ currentIndex + n + 1 ] ) >> 32 );
				if ( flags == 0 ) // if node is valid
				{
					final int objIndex = ( int ) ( Double.doubleToRawLongBits( doubles[ currentIndex + n + 1 ] ) & 0xffffffff );
					pool.getByInternalPoolIndex( objIndex, nextref );
					return true;
				}
			}
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public O next()
		{
			if ( hasNext )
			{
				ref.refTo( nextref );
				hasNext = prepareNext();
			}
			return ref;
		}

		@Override
		public void remove()
		{}
	}
}
