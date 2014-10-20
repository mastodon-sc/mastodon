package net.trackmate.kdtree;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.Sampler;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.MappedElement;

public class NNDoubles< O extends PoolObject< O, ? > & RealLocalizable, T extends MappedElement >
		implements NearestNeighborSearch< O >, Sampler< O >
{
	protected KDTree< O, T > tree;

	protected final int n;

	protected final double[] pos;

	protected int bestPointIndex;

	protected double bestSquDistance;

	protected final O obj;

	private final double[] doubles;

	private final RealPoint posPoint;

	private final double[] axisDiffs;

	private final int[] awayChildNodeIndices;

	private final int[] ds;

	public NNDoubles( final KDTree< O, T > tree )
	{
		n = tree.numDimensions();
		pos = new double[ n ];
		posPoint = RealPoint.wrap( pos );
		this.tree = tree;
		this.doubles = tree.doubles;
		this.obj = tree.getObjectPool().createRef();
		final int depth = 18; // TODO: get this from the KDTree
		axisDiffs = new double[ depth ];
		awayChildNodeIndices = new int[ depth ];
		ds = new int[ depth ];
		for ( int i = 0; i < depth; ++i )
			ds[ i ] = i % n;
	}

	@Override
	public int numDimensions()
	{
		return n;
	}

	@Override
	public void search( final RealLocalizable p )
	{
		p.localize( pos );
		int currentIndex = tree.doublesRootIndex;
		int depth = 0;
		double bestSquDistanceL = Double.MAX_VALUE;
		int bestPointIndexL = 0;
		while ( true )
		{
			final double distance = squDistance( currentIndex );
			if ( distance < bestSquDistanceL )
			{
				bestSquDistanceL = distance;
				bestPointIndexL = currentIndex;
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
						bestPointIndex = bestPointIndexL;
						return;
					}
				currentIndex = awayChildNodeIndices[ depth ];
				awayChildNodeIndices[ depth ] = -1;
			}
			else
				currentIndex = nearChildNodeIndex;
		}
	}

	private final double squDistance( final int currentIndex )
	{
		double sum = 0;
		for ( int d = 0; d < n; ++d )
		{
			final double diff = ( pos[ d ] - doubles[ currentIndex + d ] );
			sum += diff * diff;
		}
		return sum;
	}

	@Override
	public Sampler< O > getSampler()
	{
		return this;
	}

	@Override
	public RealLocalizable getPosition()
	{
		for ( int d = 0; d < n; ++d )
			pos[ d ] = doubles[ bestPointIndex + d ];
		return posPoint;
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
		final int dataIndex = ( int ) Double.doubleToRawLongBits( doubles[ bestPointIndex + n + 1 ] );
		tree.getObjectPool().getByInternalPoolIndex( dataIndex, obj );
		return obj;
	}

	@Override
	public NNDoubles< O, T > copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
