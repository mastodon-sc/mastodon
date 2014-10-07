package net.trackmate.kdtree;

import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;

/**
 * Implementation of {@link NearestNeighborSearch} search for kd-trees.
 *
 *
 * @author Tobias Pietzsch
 */
public class NearestNeighborSearchOnKDTree< N extends KDTreeNode< ?, O >, O extends PoolObject< ? > & RealLocalizable >
	implements NearestNeighborSearch< O >, Sampler< O >
{
	protected KDTree< N, O, ? > tree;

	protected final int n;

	protected final double[] pos;

	protected N bestPoint;

	protected int bestPointNodeIndex;

	protected double bestSquDistance;

	public NearestNeighborSearchOnKDTree( final KDTree< N, O, ? > tree )
	{
		n = tree.numDimensions();
		pos = new double[ n ];
		this.tree = tree;
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
		bestSquDistance = Double.MAX_VALUE;
		searchNode( tree.createRef(), tree.rootIndex );
	}

	protected void searchNode( final N node, final int currentNodeIndex, final int d )
	{
		// consider the current node
		tree.getByInternalPoolIndex( currentNodeIndex, node );
		final double distance = node.squDistanceTo( pos );
		if ( distance < bestSquDistance )
		{
			bestSquDistance = distance;
			bestPointNodeIndex = currentNodeIndex;
		}

		final double axisDiff = pos[ d ] - node.getPosition( d );
		final double axisSquDistance = axisDiff * axisDiff;
		final boolean leftIsNearBranch = axisDiff < 0;

		final int dChild = ( d + 1 == n ) ? 0 : d + 1;

		// search the near branch
		final int left = node.getLeftIndex();
		final int right = node.getRightIndex();
		final int nearChildNodeIndex = leftIsNearBranch ? left : right;
		final int awayChildNodeIndex = leftIsNearBranch ? right : left;
		if ( nearChildNodeIndex != -1 )
			searchNode( node, nearChildNodeIndex, dChild );

		// search the away branch - maybe
		if ( ( axisSquDistance <= bestSquDistance ) && ( awayChildNodeIndex != -1 ) )
			searchNode( node, awayChildNodeIndex, dChild );
	}

	@Override
	public Sampler< O > getSampler()
	{
		return this;
	}

	@Override
	public RealLocalizable getPosition()
	{
		return bestPoint;
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

	Pool< O, ? > pool;

	@Override
	public O get()
	{
		final N node = tree.createRef();
		tree.getByInternalPoolIndex( bestPointNodeIndex, node );
		final O obj = pool.createRef();

		return null;
	}

	@Override
	public NearestNeighborSearchOnKDTree< N, O > copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
