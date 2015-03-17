package net.trackmate.kdtree;

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.MappedElement;

/**
 * Partition nodes in a {@link KDTree} into disjoint sets of nodes that are
 * above and below a given hyperplane, respectively.
 *
 * <p>
 * Construct with the {@link KDTree}. Call {@link #split(HyperPlane)} to
 * partition with respect to a {@link HyperPlane}. Then call
 * {@link #getAboveNodes()} and {@link #getBelowNodes()} to get the sets of
 * nodes above and below the hyperplane, respectively.
 *
 * <p>
 * The algorithm is described in <a
 * href="http://fly.mpi-cbg.de/~pietzsch/polytope.pdf">this note</a>.
 *
 * @param <T>
 *            type of values stored in the tree.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class SplitHyperPlaneKDTree< O extends PoolObject< O, ? > & RealLocalizable, T extends MappedElement >
{
	private final KDTree< O, T > tree;

	private final int n;

	private final double[] normal;

	private double m;

	private final double[] xmin;

	private final double[] xmax;

	private final TIntArrayList aboveNodeIndices;

	private final TIntArrayList aboveSubtreeIndices;

	private final TIntArrayList belowNodeIndices;

	private final TIntArrayList belowSubtreeIndices;

	private final KDTreeNode< O, T > current;

	private final FastDoubleSearch fastDoubleSearch;

	public SplitHyperPlaneKDTree( final KDTree< O, T > tree )
	{
		n = tree.numDimensions();
		xmin = new double[ n ];
		xmax = new double[ n ];
		normal = new double[ n ];
		this.tree = tree;
		aboveNodeIndices = new TIntArrayList();
		aboveSubtreeIndices = new TIntArrayList();
		belowNodeIndices = new TIntArrayList();
		belowSubtreeIndices = new TIntArrayList();
		current = tree.createRef();
		fastDoubleSearch = ( tree.getDoubles() != null ) ? new FastDoubleSearch() : null;
	}

	public int numDimensions()
	{
		return n;
	}

	public void split( final HyperPlane plane )
	{
		initNewSearch();
		System.arraycopy( plane.getNormal(), 0, normal, 0, n );
		m = plane.getDistance();
		if ( fastDoubleSearch != null )
			fastDoubleSearch.split();
		else
			split( tree.rootIndex, 0 );
	}

	public void split( final double[] plane )
	{
		initNewSearch();
		System.arraycopy( plane, 0, normal, 0, n );
		m = plane[ n ];
		if ( fastDoubleSearch != null )
			fastDoubleSearch.split();
		else
			split( tree.rootIndex, 0 );
	}

	private void initNewSearch()
	{
		aboveNodeIndices.clear();
		aboveSubtreeIndices.clear();
		belowNodeIndices.clear();
		belowSubtreeIndices.clear();
// TODO
//		tree.realMin( xmin );
//		tree.realMax( xmax );
		Arrays.fill( xmin, Double.NEGATIVE_INFINITY );
		Arrays.fill( xmax, Double.POSITIVE_INFINITY );
	}

	public Iterable< KDTreeNode< O, T > > getAboveNodes()
	{
		return new KDTreeNodeIterable< O, T >( aboveNodeIndices, aboveSubtreeIndices, tree );
	}

	public Iterable< KDTreeNode< O, T > > getBelowNodes()
	{
		return new KDTreeNodeIterable< O, T >( belowNodeIndices, belowSubtreeIndices, tree );
	}

	private boolean allAbove()
	{
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] >= 0 ? xmin[ d ] : xmax[ d ] );
		return dot >= m;
	}

	private boolean allBelow()
	{
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] < 0 ? xmin[ d ] : xmax[ d ] );
		return dot < m;
	}

	private void splitSubtree( final int currentNodeIndex, final int parentsd, final boolean p, final boolean q )
	{
		if ( p && q && allAbove() )
			aboveSubtreeIndices.add( currentNodeIndex );
		else if ( !p && !q && allBelow() )
			belowSubtreeIndices.add( currentNodeIndex );
		else
			split( currentNodeIndex, parentsd + 1 == n ? 0 : parentsd + 1 );
	}

	private void split( final int currentNodeIndex, final int sd )
	{
		// consider the current node
		tree.getByInternalPoolIndex( currentNodeIndex, current );
		final double sc = current.getDoublePosition( sd );
		final int left = current.getLeftIndex();
		final int right = current.getRightIndex();

		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += current.getDoublePosition( d ) * normal[ d ];
		final boolean p = dot >= m;

		// current
		if ( p )
			aboveNodeIndices.add( currentNodeIndex );
		else
			belowNodeIndices.add( currentNodeIndex );

		// left
		if ( left >= 0 )
		{
			final double max = xmax[ sd ];
			xmax[ sd ] = sc;
			splitSubtree( left, sd, p, normal[ sd ] < 0 );
			xmax[ sd ] = max;
		}

		// right
		if ( right >= 0 )
		{
			final double min = xmin[ sd ];
			xmin[ sd ] = sc;
			splitSubtree( right, sd, p, normal[ sd ] >= 0 );
			xmin[ sd ] = min;
		}
	}

	// TODO:
	// Make FastDouble variants of KDTreeNodeIterable (respectively something that returns the objects stored in the nodes)
	private final class FastDoubleSearch
	{
		private final int nodeSizeInDoubles;

		private final double[] doubles;

		private final int doublesRootIndex;

		private FastDoubleSearch()
		{
			nodeSizeInDoubles = n + 2;
			doubles = tree.getDoubles();
			doublesRootIndex = tree.rootIndex * nodeSizeInDoubles;
		}

		private void split()
		{
			split( doublesRootIndex, 0 );
		}

		private void splitSubtree( final int currentIndex, final int parentsd, final boolean p, final boolean q )
		{
			if ( p && q && allAbove() )
				aboveSubtreeIndices.add( currentIndex / nodeSizeInDoubles );
			else if ( !p && !q && allBelow() )
				belowSubtreeIndices.add( currentIndex / nodeSizeInDoubles );
			else
				split( currentIndex, parentsd + 1 == n ? 0 : parentsd + 1 );
		}

		private void split( final int currentIndex, final int sd )
		{
			// consider the current node
			final double sc = doubles[ currentIndex + sd ];
			final long leftright = Double.doubleToRawLongBits( doubles[ currentIndex + n ] );
			final int left = ( int ) ( leftright >> 32 );
			final int right = ( int ) ( leftright & 0xffffffff );

			double dot = 0;
			for ( int d = 0; d < n; ++d )
				dot += doubles[ currentIndex + d ] * normal[ d ];
			final boolean p = dot >= m;

			// current
			if ( p )
				aboveNodeIndices.add( currentIndex / nodeSizeInDoubles );
			else
				belowNodeIndices.add( currentIndex / nodeSizeInDoubles );

			// left
			if ( left >= 0 )
			{
				final double max = xmax[ sd ];
				xmax[ sd ] = sc;
				splitSubtree( left, sd, p, normal[ sd ] < 0 );
				xmax[ sd ] = max;
			}

			// right
			if ( right >= 0 )
			{
				final double min = xmin[ sd ];
				xmin[ sd ] = sc;
				splitSubtree( right, sd, p, normal[ sd ] >= 0 );
				xmin[ sd ] = min;
			}
		}
	}
}
