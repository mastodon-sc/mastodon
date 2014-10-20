package net.trackmate.kdtree;

import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.MappedElement;

/**
 * Implementation of {@link NearestNeighborSearch} search for kd-trees.
 *
 *
 * @author Tobias Pietzsch
 */
public class NearestNeighborSearchOnKDTree< O extends PoolObject< O, ? > & RealLocalizable, T extends MappedElement >
	implements NearestNeighborSearch< O >, Sampler< O >
{
	protected KDTree< O, T > tree;

	protected final int n;

	protected final double[] pos;

	private final KDTreeNode< O, T > node;

	protected int bestPointNodeIndex;

	protected double bestSquDistance;

	protected final O obj;

	public NearestNeighborSearchOnKDTree( final KDTree< O, T > tree )
	{
		n = tree.numDimensions();
		pos = new double[ n ];
		this.tree = tree;
		this.node = tree.createRef();
		this.obj = tree.getObjectPool().createRef();
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
		searchNode( tree.rootIndex, 0 );
	}

	private final void searchNode( final int currentNodeIndex, final int d )
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
		tree.getByInternalPoolIndex( bestPointNodeIndex, node );
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
		tree.getByInternalPoolIndex( bestPointNodeIndex, node );
		tree.getObjectPool().getByInternalPoolIndex( node.getDataIndex(), obj );
		return obj;
	}

	@Override
	public NearestNeighborSearchOnKDTree< O, T > copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
