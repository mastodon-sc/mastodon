package net.trackmate.kdtree;

import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.trackmate.Ref;
import net.trackmate.pool.MappedElement;

/**
 * Implementation of {@link NearestNeighborSearch} search for kd-trees.
 *
 *
 * @author Tobias Pietzsch
 */
public final class NearestNeighborSearchOnKDTree< O extends Ref< O > & RealLocalizable, T extends MappedElement >
	implements NearestNeighborSearch< O >, Sampler< O >
{
	private final KDTree< O, T > tree;

	private final int n;

	private final double[] pos;

	private final KDTreeNode< O, T > node;

	private int bestPointNodeIndex;

	private double bestSquDistance;

	private final O obj;

	private final FastDoubleSearch fastDoubleSearch;

	public NearestNeighborSearchOnKDTree( final KDTree< O, T > tree )
	{
		n = tree.numDimensions();
		pos = new double[ n ];
		bestPointNodeIndex = -1;
		bestSquDistance = Double.MAX_VALUE;
		this.tree = tree;
		this.node = tree.createRef();
		this.obj = tree.getObjectPool().createRef();
		this.fastDoubleSearch = ( tree.getDoubles() != null ) ? new FastDoubleSearch( tree ) : null;
	}

	@Override
	public int numDimensions()
	{
		return n;
	}

	@Override
	public void search( final RealLocalizable p )
	{
		if ( tree.size() <= 0 )
			return;

		if ( fastDoubleSearch != null )
		{
			fastDoubleSearch.search( p );
			bestPointNodeIndex = fastDoubleSearch.getBestPointNodeIndex();
			bestSquDistance = fastDoubleSearch.getBestSquDistance();
		}
		else
		{
			p.localize( pos );
			bestSquDistance = Double.MAX_VALUE;
			searchNode( tree.rootIndex, 0 );
		}
	}

	private final void searchNode( final int currentNodeIndex, final int d )
	{
		// consider the current node
		tree.getObject( currentNodeIndex, node );
		final double distance = node.squDistanceTo( pos );
		if ( distance < bestSquDistance )
		{
			bestSquDistance = distance;
			bestPointNodeIndex = currentNodeIndex;
		}

		final double axisDiff = pos[ d ] - node.getPosition( d );
		final boolean leftIsNearBranch = axisDiff < 0;

		// search the near branch
		final int nearChildNodeIndex = leftIsNearBranch ? node.getLeftIndex() : node.getRightIndex();
		final int awayChildNodeIndex = leftIsNearBranch ? node.getRightIndex() : node.getLeftIndex();
		if ( nearChildNodeIndex != -1 )
			searchNode( nearChildNodeIndex, d + 1 == n ? 0 : d + 1 );

		// search the away branch - maybe
		if ( ( awayChildNodeIndex != -1 ) && ( axisDiff * axisDiff <= bestSquDistance ) )
			searchNode( awayChildNodeIndex, d + 1 == n ? 0 : d + 1 );
	}

	@Override
	public Sampler< O > getSampler()
	{
		return this;
	}

	@Override
	public RealLocalizable getPosition()
	{
		if ( bestPointNodeIndex == -1 )
			return null;

		tree.getObject( bestPointNodeIndex, node );
		return node;
	}

	@Override
	public double getSquareDistance()
	{
		return bestSquDistance;
	}

	@Override
	public double getDistance()
	{
		return Math.sqrt( bestSquDistance );
	}

	@Override
	public O get()
	{
		if ( bestPointNodeIndex == -1 )
			return null;

		tree.getObject( bestPointNodeIndex, node );
		tree.getObjectPool().getObject( node.getDataIndex(), obj );
		return obj;
	}

	@Override
	public NearestNeighborSearchOnKDTree< O, T > copy()
	{
		final NearestNeighborSearchOnKDTree< O, T > copy = new NearestNeighborSearchOnKDTree< O, T >( tree );
		System.arraycopy( pos, 0, copy.pos, 0, pos.length );
		copy.bestPointNodeIndex = bestPointNodeIndex;
		copy.bestSquDistance = bestSquDistance;
		return copy;
	}

	private static final class FastDoubleSearch
	{
		private final int n;

		private final int nodeSizeInDoubles;

		private final double[] pos;

		private int bestIndex;

		private double bestSquDistance;

		private final double[] doubles;

		private final int doublesRootIndex;

		private final double[] axisDiffs;

		private final int[] awayChildNodeIndices;

		private final int[] ds;

		FastDoubleSearch( final KDTree< ?, ? > tree )
		{
			n = tree.numDimensions();
			nodeSizeInDoubles = n + 2;
			final int depth = ( tree.size() <= 0 ) ? 0 :
				( int ) ( Math.log( tree.size() ) / Math.log( 2 ) ) + 2;
			pos = new double[ n ];
			doubles = tree.getDoubles();
			doublesRootIndex = tree.rootIndex * nodeSizeInDoubles;
			axisDiffs = new double[ depth ];
			awayChildNodeIndices = new int[ depth ];
			ds = new int[ depth ];
			for ( int i = 0; i < depth; ++i )
				ds[ i ] = i % n;
		}

		void search( final RealLocalizable p )
		{
			p.localize( pos );
			int currentIndex = doublesRootIndex;
			int depth = 0;
			double bestSquDistanceL = Double.MAX_VALUE;
			int bestIndexL = 0;
			while ( true )
			{
				final double distance = squDistance( currentIndex );
				if ( distance < bestSquDistanceL )
				{
					bestSquDistanceL = distance;
					bestIndexL = currentIndex;
				}

				final int d = ds[ depth ];
				final double axisDiff = pos[ d ] - doubles[ currentIndex + d ];
				final boolean leftIsNearBranch = axisDiff < 0;

				final long leftright = Double.doubleToRawLongBits( doubles[ currentIndex + n ] );
				final int left = ( int ) ( leftright >> 32 );
				final int right = ( int ) ( leftright & 0xffffffff );

				// search the near branch
				final int nearChildNodeIndex = leftIsNearBranch ? left : right;
				final int awayChildNodeIndex = leftIsNearBranch ? right : left;
				++depth;
				awayChildNodeIndices[ depth ] = awayChildNodeIndex;
				axisDiffs[ depth ] = axisDiff * axisDiff;
				if ( nearChildNodeIndex < 0 )
				{
					while ( awayChildNodeIndices[ depth ] < 0 || axisDiffs[ depth ] > bestSquDistanceL )
						if ( --depth == 0 )
						{
							bestSquDistance = bestSquDistanceL;
							bestIndex = bestIndexL;
							return;
						}
					currentIndex = awayChildNodeIndices[ depth ];
					awayChildNodeIndices[ depth ] = -1;
				}
				else
					currentIndex = nearChildNodeIndex;
			}
		}

		int getBestPointNodeIndex()
		{
			return bestIndex / nodeSizeInDoubles;
		}

		double getBestSquDistance()
		{
			return bestSquDistance;
		}

		private final double squDistance( final int index )
		{
			double sum = 0;
			for ( int d = 0; d < n; ++d )
			{
				final double diff = ( pos[ d ] - doubles[ index + d ] );
				sum += diff * diff;
			}
			return sum;
		}
	}
}
