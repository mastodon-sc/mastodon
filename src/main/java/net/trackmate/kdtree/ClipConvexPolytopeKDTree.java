package net.trackmate.kdtree;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.trackmate.Ref;
import net.trackmate.pool.MappedElement;

/**
 * Partition nodes in a {@link KDTree} into disjoint sets of nodes that are
 * inside and outside a given convex polytope, respectively.
 *
 * <p>
 * Construct with the {@link KDTree}. Call {@link #clip(ConvexPolytope)} to
 * partition with respect to a {@link ConvexPolytope}. Then call
 * {@link #getInsideValues()} and {@link #getOutsideValues()} to get the sets of
 * node values inside and outside the polytope, respectively.
 *
 * <p>
 * The algorithm is described in <a
 * href="http://fly.mpi-cbg.de/~pietzsch/polytope.pdf">this note</a>.
 *
 * @param <O>
 *            type of objects stored in the tree.
 * @param <T>
 *            the MappedElement type of the {@link KDTreeNode tree nodes}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ClipConvexPolytopeKDTree< O extends Ref< O > & RealLocalizable, T extends MappedElement >
{
	private final KDTree< O, T > tree;

	private final int n;

	private int nPlanes;

	private double[][] normals;

	private double[] ms;

	private final int[] sds;

	private final double[] xmin;

	private final double[] xmax;

	private boolean[] qR;

	private boolean[] qL;

	private final ArrayList< boolean[] > activeStack;

	private final ArrayList< boolean[] > psStack;

	private final TIntArrayList inNodes;

	private final TIntArrayList inSubtrees;

	private final TIntArrayList outNodes;

	private final TIntArrayList outSubtrees;

	private final KDTreeNode< O, T > current;

	private final FastDoubleSearch fastDoubleSearch;

	public ClipConvexPolytopeKDTree( final KDTree< O, T > tree )
	{
		this.tree = tree;
		n = tree.numDimensions();
		xmin = new double[ n ];
		xmax = new double[ n ];
		final int depth = ( tree.size() <= 0 ) ? 0 :
			( int ) ( Math.log( tree.size() ) / Math.log( 2 ) ) + 2;
		activeStack = new ArrayList< boolean[] >( depth );
		psStack = new ArrayList< boolean[] >( depth );
		inNodes = new TIntArrayList();
		inSubtrees = new TIntArrayList();
		outNodes = new TIntArrayList();
		outSubtrees = new TIntArrayList();
		current = tree.createRef();
		fastDoubleSearch = ( tree.getDoubles() != null ) ? new FastDoubleSearch() : null;
		sds = new int[ depth ];
		for ( int i = 0; i < depth; ++i )
			sds[ i ] = i % n;
	}

	public int numDimensions()
	{
		return n;
	}

	public void clip( final ConvexPolytope polytope )
	{
		if ( tree.size() <= 0 )
			return;

		final Collection< ? extends HyperPlane > planes = polytope.getHyperplanes();
		initNewSearch( planes.size() );
		int i = 0;
		for ( final HyperPlane plane : planes )
		{
			final double[] normal = normals[ i ];
			System.arraycopy( plane.getNormal(), 0, normal, 0, n );
			ms[ i ] = plane.getDistance();
			for ( int d = 0; d < n; ++d )
			{
				qL[ d * nPlanes + i ] = normal[ d ] < 0;
				qR[ d * nPlanes + i ] = normal[ d ] >= 0;
			}
			++i;
		}
		if ( fastDoubleSearch != null )
			fastDoubleSearch.clip();
		else
			clip( tree.rootIndex, 0 );
	}

	public void clip( final double[][] planes )
	{
		if ( tree.size() <= 0 )
			return;

		initNewSearch( planes.length );
		for ( int i = 0; i < nPlanes; ++i )
		{
			final double[] normal = normals[ i ];
			System.arraycopy( planes[ i ], 0, normal, 0, n );
			ms[ i ] = planes[ i ][ n ];
			for ( int d = 0; d < n; ++d )
			{
				qL[ d * nPlanes + i ] = normal[ d ] < 0;
				qR[ d * nPlanes + i ] = normal[ d ] >= 0;
			}
		}
		if ( fastDoubleSearch != null )
			fastDoubleSearch.clip();
		else
			clip( tree.rootIndex, 0 );
	}

	public Iterable< O > getInsideValues()
	{
		return new KDTreeValueIterable< O, T >( inNodes, inSubtrees, tree, fastDoubleSearch != null );
	}

	public Iterable< O > getOutsideValues()
	{
		return new KDTreeValueIterable< O, T >( outNodes, outSubtrees, tree, fastDoubleSearch != null );
	}

	public Iterable< O > getValidInsideValues()
	{
		return new KDTreeValidValueIterable< O, T >( inNodes, inSubtrees, tree, fastDoubleSearch != null );
	}

	public Iterable< O > getValidOutsideValues()
	{
		return new KDTreeValidValueIterable< O, T >( outNodes, outSubtrees, tree, fastDoubleSearch != null );
	}

	private void initNewSearch( final int nPlanes )
	{
		this.nPlanes = nPlanes;
		normals = new double[ nPlanes ][];
		for ( int i = 0; i < nPlanes; ++i )
			normals[ i ] = new double[ n ];
		ms = new double[ nPlanes ];
		qR = new boolean[ n * nPlanes ];
		qL = new boolean[ n * nPlanes ];
		inNodes.clear();
		inSubtrees.clear();
		outNodes.clear();
		outSubtrees.clear();
		activeStack.clear();
		psStack.clear();
		tree.realMin( xmin );
		tree.realMax( xmax );
		Arrays.fill( getActiveArray( 0 ), true );
	}

	private boolean[] getActiveArray( final int i )
	{
		if ( i >= activeStack.size() )
		{
			activeStack.add( new boolean[ nPlanes ] );
			psStack.add( new boolean[ nPlanes ] );
		}
		return activeStack.get( i );
	}

	private boolean[] getPsArray( final int i )
	{
		return psStack.get( i );
	}

	private boolean allAbove( final int i )
	{
		final double[] normal = normals[ i ];
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] >= 0 ? xmin[ d ] : xmax[ d ] );
		return dot >= ms[ i ];
	}

	private boolean allBelow( final int i )
	{
		final double[] normal = normals[ i ];
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] < 0 ? xmin[ d ] : xmax[ d ] );
		return dot < ms[ i ];
	}

	private void clipSubtree( final int currentIndex, final boolean[] ps, final boolean[] qs, final int qoff, final int recursionDepth )
	{
		final boolean[] active = getActiveArray( recursionDepth );
		final boolean[] stillActive = getActiveArray( recursionDepth + 1 );
		System.arraycopy( active, 0, stillActive, 0, nPlanes );
		boolean noneActive = true;
		for ( int i = 0; i < nPlanes; ++i )
		{
			if ( active[ i ] )
			{
				if ( ps[ i ] && qs[ qoff + i ] && allAbove( i ) )
					stillActive[ i ] = false;
				else
				{
					noneActive = false;
					if ( !ps[ i ] && !qs[ qoff + i ] && allBelow( i ) )
					{
						outSubtrees.add( currentIndex );
						return;
					}
				}
			}
		}
		if ( noneActive )
			inSubtrees.add( currentIndex );
		else
			clip( currentIndex, recursionDepth + 1 );
	}

	private void clip( final int currentIndex, final int recursionDepth )
	{
		tree.getObject( currentIndex, current );
		final int sd = sds[ recursionDepth ];
		final double sc = current.getDoublePosition( sd );
		final int left = current.getLeftIndex();
		final int right = current.getRightIndex();

		final boolean[] active = getActiveArray( recursionDepth );
		final boolean[] ps = getPsArray( recursionDepth );

		boolean p = true;
		for ( int i = 0; i < nPlanes; ++i )
		{
			if ( active[ i ] )
			{
				final double[] normal = normals[ i ];
				double dot = 0;
				for ( int d = 0; d < n; ++d )
					dot += current.getDoublePosition( d ) * normal[ d ];
				ps[ i ] = dot >= ms[ i ];
				p &= ps[ i ];
			}
		}

		if ( p )
			inNodes.add( currentIndex );
		else
			outNodes.add( currentIndex );

		final int qoff = sd * nPlanes;
		if ( left >= 0 )
		{
			final double max = xmax[ sd ];
			xmax[ sd ] = sc;
			clipSubtree( left, ps, qL, qoff, recursionDepth );
			xmax[ sd ] = max;
		}

		if ( right >= 0 )
		{
			final double min = xmin[ sd ];
			xmin[ sd ] = sc;
			clipSubtree( right, ps, qR, qoff, recursionDepth );
			xmin[ sd ] = min;
		}
	}

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

		private void clip()
		{
			clip( doublesRootIndex, 0 );
		}

		private void clipSubtree( final int currentIndex, final boolean[] ps, final boolean[] qs, final int qoff, final int recursionDepth )
		{
			final boolean[] active = getActiveArray( recursionDepth );
			final boolean[] stillActive = getActiveArray( recursionDepth + 1 );
			System.arraycopy( active, 0, stillActive, 0, nPlanes );
			boolean noneActive = true;
			for ( int i = 0; i < nPlanes; ++i )
			{
				if ( active[ i ] )
				{
					if ( ps[ i ] && qs[ qoff + i ] && allAbove( i ) )
						stillActive[ i ] = false;
					else
					{
						noneActive = false;
						if ( !ps[ i ] && !qs[ qoff + i ] && allBelow( i ) )
						{
							outSubtrees.add( currentIndex );
							return;
						}
					}
				}
			}
			if ( noneActive )
				inSubtrees.add( currentIndex );
			else
				clip( currentIndex, recursionDepth + 1 );
		}

		private void clip( final int currentIndex, final int recursionDepth )
		{
			// consider the current node
			final int sd = sds[ recursionDepth ];
			final double sc = doubles[ currentIndex + sd ];
			final long leftright = Double.doubleToRawLongBits( doubles[ currentIndex + n ] );
			final int left = ( int ) ( leftright >> 32 );
			final int right = ( int ) ( leftright & 0xffffffff );

			final boolean[] active = getActiveArray( recursionDepth );
			final boolean[] ps = getPsArray( recursionDepth );

			boolean p = true;
			for ( int i = 0; i < nPlanes; ++i )
			{
				if ( active[ i ] )
				{
					final double[] normal = normals[ i ];
					double dot = 0;
					for ( int d = 0; d < n; ++d )
						dot += doubles[ currentIndex + d ] * normal[ d ];
					ps[ i ] = dot >= ms[ i ];
					p &= ps[ i ];
				}
			}

			if ( p )
				inNodes.add( currentIndex );
			else
				outNodes.add( currentIndex );

			final int qoff = sd * nPlanes;
			if ( left >= 0 )
			{
				final double max = xmax[ sd ];
				xmax[ sd ] = sc;
				clipSubtree( left, ps, qL, qoff, recursionDepth );
				xmax[ sd ] = max;
			}

			if ( right >= 0 )
			{
				final double min = xmin[ sd ];
				xmin[ sd ] = sc;
				clipSubtree( right, ps, qR, qoff, recursionDepth );
				xmin[ sd ] = min;
			}
		}
	}
}
