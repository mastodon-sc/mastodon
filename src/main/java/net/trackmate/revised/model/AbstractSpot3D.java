package net.trackmate.revised.model;

import static net.trackmate.pool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.pool.ByteUtils.INT_SIZE;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.trackmate.graph.ref.AbstractEdge;
import net.trackmate.graph.ref.AbstractListenableVertex;
import net.trackmate.graph.ref.AbstractVertex;
import net.trackmate.graph.ref.AbstractVertexPool;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.MappedElement;
import net.trackmate.spatial.HasTimepoint;

/**
 * Base class for specialized vertices that are part of a graph, and are used to
 * store spatial and temporal location.
 * <p>
 * The class ships the minimal required feature, that is X, Y, Z, and
 * time-point.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 *
 * @param <V>
 *            the recursive type of the concrete implementation.
 * @param <E>
 *            associated edge type
 * @param <T>
 *            the MappedElement type, for example {@link ByteMappedElement}.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AbstractSpot3D<
		V extends AbstractSpot3D< V, E, T >,
		E extends AbstractEdge< E, ?, ? >,
		T extends MappedElement >
	extends AbstractListenableVertex< V, E, T >
	implements RealLocalizable, RealPositionable, HasTimepoint
{
	protected static final int X_OFFSET = AbstractVertex.SIZE_IN_BYTES;
	protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;
	protected static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;
	protected static final int TP_OFFSET = Z_OFFSET + DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = TP_OFFSET + INT_SIZE;

	private static final int n = 3;

	protected void setCoord( final double value, final int d )
	{
		access.putDouble( value, X_OFFSET + d * DOUBLE_SIZE );
	}

	protected double getCoord( final int d )
	{
		return access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
	}

	protected int getTimepointId()
	{
		return access.getInt( TP_OFFSET );
	}

	protected void setTimepointId( final int tp )
	{
		access.putInt( tp, TP_OFFSET );
	}

	@Override
	public int getTimepoint()
	{
		return getTimepointId();
	}

	protected AbstractSpot3D( final AbstractVertexPool< V, E, T > pool )
	{
		super( pool );
	}

	// === RealLocalizable ===

	@Override
	public int numDimensions()
	{
		return n;
	}

	@Override
	public void localize( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = ( float ) getCoord( d );
	}

	@Override
	public void localize( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = getCoord( d );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) getCoord( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return getCoord( d );
	}

	// === RealPositionable ===

	@Override
	public void fwd( final int d )
	{
		setCoord( getCoord( d ) + 1, d );
	}

	@Override
	public void bck( final int d )
	{
		setCoord( getCoord( d ) - 1, d );
	}

	@Override
	public void move( final int distance, final int d )
	{
		setCoord( getCoord( d ) + distance, d );
	}

	@Override
	public void move( final long distance, final int d )
	{
		setCoord( getCoord( d ) + distance, d );
	}

	@Override
	public void move( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( getCoord( d ) + localizable.getDoublePosition( d ), d );
	}

	@Override
	public void move( final int[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( getCoord( d ) + distance[ d ], d );
	}

	@Override
	public void move( final long[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( getCoord( d ) + distance[ d ], d );
	}

	@Override
	public void setPosition( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( localizable.getDoublePosition( d ), d );
	}

	@Override
	public void setPosition( final int[] position )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( position[ d ], d );
	}

	@Override
	public void setPosition( final long[] position )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( position[ d ], d );
	}

	@Override
	public void setPosition( final int position, final int d )
	{
		setCoord( position, d );
	}

	@Override
	public void setPosition( final long position, final int d )
	{
		setCoord( position, d );
	}

	@Override
	public void move( final float distance, final int d )
	{
		setCoord( getCoord( d ) + distance, d );
	}

	@Override
	public void move( final double distance, final int d )
	{
		setCoord( getCoord( d ) + distance, d );
	}

	@Override
	public void move( final RealLocalizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( getCoord( d ) + localizable.getDoublePosition( d ), d );
	}

	@Override
	public void move( final float[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( getCoord( d ) + distance[ d ], d );
	}

	@Override
	public void move( final double[] distance )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( getCoord( d ) + distance[ d ], d );
	}

	@Override
	public void setPosition( final RealLocalizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( localizable.getDoublePosition( d ), d );
	}

	@Override
	public void setPosition( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( position[ d ], d );
	}

	@Override
	public void setPosition( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			setCoord( position[ d ], d );
	}

	@Override
	public void setPosition( final float position, final int d )
	{
		setCoord( position, d );
	}

	@Override
	public void setPosition( final double position, final int d )
	{
		setCoord( position, d );
	}
}
