package net.trackmate.kdtree;

import java.util.Collection;

import net.imglib2.EuclideanSpace;
import net.imglib2.RealLocalizable;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.DoubleMappedElement;
import net.trackmate.graph.mempool.DoubleMappedElementArray;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.MemPool.Factory;
import net.trackmate.graph.mempool.SingleArrayMemPool;

public class KDTree<
			O extends PoolObject< O, ? > & RealLocalizable,
			T extends MappedElement >
		extends Pool< KDTreeNode< O, T >, T >
		implements EuclideanSpace
{
	private static final MemPool.Factory< DoubleMappedElement > defaultPoolFactory = SingleArrayMemPool.factory( DoubleMappedElementArray.factory );

	public static < O extends PoolObject< O, ? > & RealLocalizable >
			KDTree< O, DoubleMappedElement > kdtree( final Collection< O > objects, final Pool< O, ? > objectPool )
	{
		return kdtree( objects, objectPool, defaultPoolFactory );
	}

	public static < O extends PoolObject< O, ? > & RealLocalizable, T extends MappedElement >
			KDTree< O, T > kdtree( final Collection< O > objects, final Pool< O, ? > objectPool, final MemPool.Factory< T > poolFactory )
	{
		if ( objects.isEmpty() )
			return null;
		final KDTree< O, T > kdtree = new NodeFactory< O, T >( objects, objectPool, poolFactory ).kdtree;
		kdtree.build( objects );
		return kdtree;
	}

	private static final class NodeFactory<
				O extends PoolObject< O, ? > & RealLocalizable,
				T extends MappedElement >
			implements PoolObject.Factory< KDTreeNode< O, T >, T >
	{
		private final KDTree< O, T > kdtree;

		private final int sizeInBytes;

		private final int numDimensions;

		private final MemPool.Factory< T > poolFactory;

		public NodeFactory( final Collection< O > objects, final Pool< O, ? > objectPool, final MemPool.Factory< T > poolFactory )
		{
			this.poolFactory = poolFactory;
			this.numDimensions = objects.iterator().next().numDimensions();
			this.sizeInBytes = KDTreeNode.sizeInBytes( numDimensions );
			final int capacity = objects.size();
			kdtree = new KDTree< O, T >( capacity, this, numDimensions, objects, objectPool );
		}

		@Override
		public int getSizeInBytes()
		{
			return sizeInBytes;
		}

		@Override
		public KDTreeNode< O, T > createEmptyRef()
		{
			return new KDTreeNode< O, T >( kdtree, numDimensions );
		}

		@Override
		public Factory< T > getMemPoolFactory()
		{
			return poolFactory;
		}
	};

	private KDTree(
			final int initialCapacity,
			final NodeFactory< O, T > nodeFactory,
			final int numDimensions,
			final Collection< O > objects,
			final Pool< O, ? > objectPool )
	{
		super( initialCapacity, nodeFactory );
		this.n = numDimensions;
		this.objectPool = objectPool;
	}

	private void build( final Collection< O > objects )
	{
		final KDTreeNode< O, T > n1 = createRef();
		final KDTreeNode< O, T > n2 = createRef();
		final KDTreeNode< O, T > n3 = createRef();
		for ( final O obj : objects )
			create( n1 ).init( obj );
		final int max = objects.size() - 1;
		final int r = makeNode( 0, max, 0, n1, n2, n3 );
		releaseRef( n1 );
		releaseRef( n2 );
		releaseRef( n3 );
		rootIndex = r;
	}

	private final Pool< O, ? > objectPool;

	private final int n;

	int rootIndex;

	double[] getDoubles()
	{
		if ( this.getMemPool() instanceof SingleArrayMemPool )
		{
			final SingleArrayMemPool< ?, ? > mempool = ( SingleArrayMemPool< ?, ? > ) this.getMemPool();
			if ( mempool.getDataArray() instanceof DoubleMappedElementArray )
			{
				final DoubleMappedElementArray doublearray = ( DoubleMappedElementArray ) mempool.getDataArray();
				return doublearray.getCurrentDataArray();
			}
		}
		return null;
	}

	/**
	 * Construct the tree by recursively adding nodes. The sublist of positions
	 * between indices i and j (inclusive) is split at the median element with
	 * respect to coordinates in the given dimension d. The median becomes the
	 * new node which is returned. The left and right partitions of the sublist
	 * are processed recursively and form the left and right subtrees of the
	 * node.
	 *
	 * @param positions
	 *            list of positions
	 * @param i
	 *            start index of sublist to process
	 * @param j
	 *            end index of sublist to process
	 * @param d
	 *            dimension along which to split the sublist
	 * @param values
	 *            list of values corresponding to permuted positions
	 * @param permutation
	 *            the index of the values element at index k is permutation[k]
	 * @return a new node containing the subtree of the given sublist of
	 *         positions.
	 */
	private int makeNode( final int i, final int j, final int d, final KDTreeNode< O, T > n1, final KDTreeNode< O, T > n2, final KDTreeNode< O, T > n3 )
	{
		if ( j > i )
		{
			final int k = i + ( j - i ) / 2;
			kthElement( i, j, k, d, n1, n2, n3 );

			final int dChild = ( d + 1 == n ) ? 0 : d + 1;
			final int left = makeNode( i, k - 1, dChild, n1, n2, n3 );
			final int right = makeNode( k + 1, j, dChild, n1, n2, n3 );

			getByInternalPoolIndex( k, n1 );
			n1.setLeftIndex( left );
			n1.setRightIndex( right );
			return k;
		}
		else if ( j == i )
		{
			// no left/right children
			getByInternalPoolIndex( i, n1 );
			n1.setLeftIndex( -1 );
			n1.setRightIndex( -1 );
			return i;
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Partition a sublist of {@code values} such that the k-th smallest value
	 * is at position {@code k}, elements before the k-th are smaller or equal
	 * and elements after the k-th are larger or equal.
	 *
	 * @param i
	 *            index of first element of subarray
	 * @param j
	 *            index of last element of subarray
	 * @param k
	 *            index for k-th smallest value. i <= k <= j.
	 * @param values
	 *            array
	 * @param compare
	 *            ordering function on T
	 */
	private void kthElement( int i, int j, final int k, final int compare_d, final KDTreeNode< O, T > pivot, final KDTreeNode< O, T > ti, final KDTreeNode< O, T > tj )
	{
		while ( true )
		{
			final int pivotpos = partitionSubList( i, j, compare_d, pivot, ti, tj );
			if ( pivotpos > k )
			{
				// partition lower half
				j = pivotpos - 1;
			}
			else if ( pivotpos < k )
			{
				// partition upper half
				i = pivotpos + 1;
			}
			else
				break;
		}
	}

	/**
	 * Partition a sublist of {@code values}.
	 *
	 * The element at index {@code j} is taken as the pivot value. The elements
	 * {@code [i,j]} are reordered, such that all elements before the pivot are
	 * smaller and all elements after the pivot are equal or larger than the
	 * pivot. The index of the pivot element is returned.
	 *
	 * @param i
	 *            index of first element of the sublist
	 * @param j
	 *            index of last element of the sublist
	 * @param values
	 *            the list
	 * @param compare
	 *            ordering function on T
	 * @return index of pivot element
	 */
	private int partitionSubList( int i, int j, final int compare_d, final KDTreeNode< O, T > pivot, final KDTreeNode< O, T > ti, final KDTreeNode< O, T > tj )
	{
		final int pivotIndex = j;
		getByInternalPoolIndex( j--, pivot );
		final double pivotPosition = pivot.getPosition( compare_d );

		A: while ( true )
		{
			// move i forward while < pivot (and not at j)
			while ( i <= j )
			{
				getByInternalPoolIndex( i, ti );
				if ( ti.getPosition( compare_d ) >= pivotPosition )
					break;
				++i;
			}
			// now [i] is the place where the next value < pivot is to be
			// inserted

			if ( i > j )
				break;

			// move j backward while >= pivot (and not at i)
			while ( true )
			{
				getByInternalPoolIndex( j, tj );
				if ( tj.getPosition( compare_d ) < pivotPosition )
				{
					// swap [j] with [i]
					getMemPool().swap( i++, j-- );
					break;
				}
				else if ( j == i )
				{
					break A;
				}
				--j;
			}
		}

		// we are done. put the pivot element here.
		// check whether the element at iLastIndex is <
		if ( i != pivotIndex )
		{
			getMemPool().swap( i, pivotIndex );
		}
		return i;
	}

	@Override
	public int numDimensions()
	{
		return n;
	}

	public Pool< O, ? > getObjectPool()
	{
		return objectPool;
	}
}
