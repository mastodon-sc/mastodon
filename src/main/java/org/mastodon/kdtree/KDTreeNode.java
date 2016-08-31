package org.mastodon.kdtree;

import static org.mastodon.pool.ByteUtils.DOUBLE_SIZE;
import static org.mastodon.pool.ByteUtils.INDEX_SIZE;
import static org.mastodon.pool.ByteUtils.INT_SIZE;

import java.nio.ByteOrder;

import org.mastodon.RefPool;
import org.mastodon.pool.MappedElement;
import org.mastodon.pool.PoolObject;

import net.imglib2.RealLocalizable;

public class KDTreeNode<
			O extends RealLocalizable,
			T extends MappedElement >
		extends PoolObject< KDTreeNode< O, T >, T >
		implements RealLocalizable
{
	private final int n;

	private final RefPool< O > objPool;

	private final int POS_INDEX_OFFSET;
	private final int LEFT_INDEX_OFFSET;
	private final int RIGHT_INDEX_OFFSET;
	private final int DATA_INDEX_OFFSET;
	private final int FLAGS_OFFSET;

	private final int sizeInDoubles;

	public KDTreeNode( final KDTree< O, T > kdtree, final int numDimensions )
	{
		super( kdtree );
		this.objPool = kdtree.getObjectPool();
		n = numDimensions;
		POS_INDEX_OFFSET = 0;
		if ( java.nio.ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN )
		{
			RIGHT_INDEX_OFFSET = POS_INDEX_OFFSET + n * DOUBLE_SIZE;
			LEFT_INDEX_OFFSET = RIGHT_INDEX_OFFSET + INDEX_SIZE;
			DATA_INDEX_OFFSET = LEFT_INDEX_OFFSET + INDEX_SIZE;
			FLAGS_OFFSET = DATA_INDEX_OFFSET + INDEX_SIZE;
			sizeInDoubles = ( FLAGS_OFFSET + INT_SIZE ) / DOUBLE_SIZE;
		}
		else
		{
			LEFT_INDEX_OFFSET = POS_INDEX_OFFSET + n * DOUBLE_SIZE;
			RIGHT_INDEX_OFFSET = LEFT_INDEX_OFFSET + INDEX_SIZE;
			FLAGS_OFFSET = RIGHT_INDEX_OFFSET + INDEX_SIZE;
			DATA_INDEX_OFFSET = FLAGS_OFFSET + INT_SIZE;
			sizeInDoubles = ( DATA_INDEX_OFFSET + INDEX_SIZE ) / DOUBLE_SIZE;
		}
	}

	protected int getLeftIndex()
	{
		return access.getIndex( LEFT_INDEX_OFFSET ) / sizeInDoubles;
	}

	protected void setLeftIndex( final int index )
	{
		access.putIndex( index * sizeInDoubles, LEFT_INDEX_OFFSET );
	}

	protected int getRightIndex()
	{
		return access.getIndex( RIGHT_INDEX_OFFSET ) / sizeInDoubles;
	}

	protected void setRightIndex( final int index )
	{
		access.putIndex( index * sizeInDoubles, RIGHT_INDEX_OFFSET );
	}

	protected int getDataIndex()
	{
		return access.getIndex( DATA_INDEX_OFFSET );
	}

	protected void setDataIndex( final int index )
	{
		access.putIndex( index, DATA_INDEX_OFFSET );
	}

	protected int getFlags()
	{
		return access.getInt( FLAGS_OFFSET );
	}

	protected void setFlags( final int flags )
	{
		access.putInt( flags, FLAGS_OFFSET );
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
		setDataIndex( objPool.getId( o ) );
		setFlags( 0 );
		for ( int d = 0; d < o.numDimensions(); ++d )
			setPosition( o.getDoublePosition( d ), d );
	}

	protected static int sizeInBytes( final int n )
	{
		return 4 * INDEX_SIZE + n * DOUBLE_SIZE;
	}

	/**
	 * Compute the squared distance from p to this node.
	 */
	public final float squDistanceTo( final float[] p )
	{
		float sum = 0;
		for ( int d = 0; d < n; ++d )
		{
			final double diff = getPosition( d ) - p[ d ];
			sum += diff * diff;
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
			final double diff = getPosition( d ) - p[ d ];
			sum += diff * diff;
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
			final double diff = getPosition( d ) - p.getDoublePosition( d );
			sum += diff * diff;
		}
		return sum;
	}

	public boolean isValid()
	{
		return getFlags() == 0;
	}

	public void setValid( final boolean valid )
	{
		setFlags( valid ? 0 : 1 );
	}

	@Override
	public int numDimensions()
	{
		return n;
	}

	@Override
	public void localize( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = ( float ) getPosition( d );
	}

	@Override
	public void localize( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = getPosition( d );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) getPosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return getPosition( d );
	}
}
