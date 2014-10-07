package net.trackmate.kdtree;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INDEX_SIZE;
import net.imglib2.RealLocalizable;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;

public class KDTreeNode< T extends MappedElement, O extends PoolObject< ? > & RealLocalizable > extends PoolObject< T >
{
	protected static final int LEFT_INDEX_OFFSET = 0;
	protected static final int RIGHT_INDEX_OFFSET = LEFT_INDEX_OFFSET + INDEX_SIZE;
	protected static final int DATA_INDEX_OFFSET = RIGHT_INDEX_OFFSET + INDEX_SIZE;
	protected static final int POS_INDEX_OFFSET = DATA_INDEX_OFFSET + INDEX_SIZE;

	private final int n;

	public KDTreeNode( final MemPool< T > pool, final int numDimensions )
	{
		super( pool );
		this.n = numDimensions;
	}

	protected int getLeftIndex()
	{
		return access.getIndex( LEFT_INDEX_OFFSET );
	}

	protected void setLeftIndex( final int index )
	{
		access.putIndex( index, LEFT_INDEX_OFFSET );
	}

	protected int getRightIndex()
	{
		return access.getIndex( RIGHT_INDEX_OFFSET );
	}

	protected void setRightIndex( final int index )
	{
		access.putIndex( index, RIGHT_INDEX_OFFSET );
	}

	protected int getDataIndex()
	{
		return access.getIndex( DATA_INDEX_OFFSET );
	}

	protected void setDataIndex( final int index )
	{
		access.putIndex( index, DATA_INDEX_OFFSET );
	}

	protected double getPosition( final int d )
	{
		return access.getDouble( POS_INDEX_OFFSET + d * DOUBLE_SIZE );
	}

	protected void setPosition( final double position, final int d )
	{
		access.putDouble( position, POS_INDEX_OFFSET + d * DOUBLE_SIZE );
	}

	@Override
	protected void setToUninitializedState()
	{}

	protected void init( final O o )
	{
		setDataIndex( o.getInternalPoolIndex() );
		for ( int d = 0; d < o.numDimensions(); ++d )
			setPosition( o.getDoublePosition( d ), d );
	}

	protected static int sizeInBytes( final int n )
	{
		return POS_INDEX_OFFSET + n * DOUBLE_SIZE;
	}

	/**
	 * Compute the squared distance from p to this node.
	 */
	public final float squDistanceTo( final float[] p )
	{
		float sum = 0;
		for ( int d = 0; d < n; ++d )
		{
			final double posd = getPosition( d );
			sum += ( posd - p[ d ] ) * ( posd - p[ d ] );
		}
		return sum;
	}

	/**
	 * Compute the squared distance from p to this node.
	 */
	public final double squDistanceTo( final double[] p )
	{
		double sum = 0;
		for ( int d = 0; d < n; ++d )
		{
			final double posd = getPosition( d );
			sum += ( posd - p[ d ] ) * ( posd - p[ d ] );
		}
		return sum;
	}

	/**
	 * Compute the squared distance from p to this node.
	 */
	public final double squDistanceTo( final RealLocalizable p )
	{
		double sum = 0;
		for ( int d = 0; d < n; ++d )
		{
			final double posd = getPosition( d );
			final double pd = p.getDoublePosition( d );
			sum += ( posd - pd ) * ( posd - pd );
		}
		return sum;
	}
}
