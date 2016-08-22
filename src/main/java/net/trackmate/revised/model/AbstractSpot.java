package net.trackmate.revised.model;

import static net.trackmate.pool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.pool.ByteUtils.INT_SIZE;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.graph.ref.AbstractListenableVertex;
import net.trackmate.graph.ref.AbstractVertex;
import net.trackmate.graph.ref.AbstractVertexPool;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.MappedElement;
import net.trackmate.pool.PoolObjectAttributeSerializer;
import net.trackmate.spatial.HasTimepoint;
import net.trackmate.undo.attributes.AttributeUndoSerializer;

/**
 * Base class for specialized vertices that are part of a graph, and are used to
 * store spatial and temporal location.
 * <p>
 * The class ships the minimal required features, that is coordinates and
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
public class AbstractSpot<
		V extends AbstractSpot< V, E, T, G >,
		E extends AbstractListenableEdge< E, V, T >,
		T extends MappedElement,
		G extends AbstractModelGraph< ?, ?, ?, V, E, T > >
	extends AbstractListenableVertex< V, E, T >
	implements RealLocalizable, RealPositionable, HasTimepoint
{
	protected static final int X_OFFSET = AbstractVertex.SIZE_IN_BYTES; // n * DOUBLE_SIZE
	private final int TP_OFFSET; // INT_SIZE

	protected final int n;

	protected G modelGraph;

	protected static int sizeInBytes( final int numDimensions )
	{
		return X_OFFSET + numDimensions * DOUBLE_SIZE + INT_SIZE;
	}

	static < V extends AbstractSpot< V, ?, ?, ? > > AttributeUndoSerializer< V > createPositionAttributeSerializer( final int numDimensions )
	{
		return new PoolObjectAttributeSerializer< V >( X_OFFSET, numDimensions * DOUBLE_SIZE)
		{
			@Override
			public void notifySet( final V obj )
			{
				obj.modelGraph.notifyVertexPositionChanged( obj );
			}
		};
	}

	private void setPosEntryInternal( final double value, final int d )
	{
		access.putDouble( value, X_OFFSET + d * DOUBLE_SIZE );
	}

	private double getPosEntryInternal( final int d )
	{
		return access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
	}

	private int getTimepointInternal()
	{
		return access.getInt( TP_OFFSET );
	}

	private void setTimepointInternal( final int tp )
	{
		access.putInt( tp, TP_OFFSET );
	}

	protected AbstractSpot( final AbstractVertexPool< V, E, T > pool, final int numDimensions )
	{
		super( pool );
		n = numDimensions;
		TP_OFFSET = X_OFFSET + n * DOUBLE_SIZE;
	}

	protected void partialInit( final int timepointId, final double[] pos )
	{
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( pos[ d ], d );
		setTimepointInternal( timepointId );
	}

	/*
	 * Public API
	 */

	@Override
	public int getTimepoint()
	{
		return getTimepointInternal();
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
			position[ d ] = ( float ) getPosEntryInternal( d );
	}

	@Override
	public void localize( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = getPosEntryInternal( d );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) getPosEntryInternal( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return getPosEntryInternal( d );
	}

	// === RealPositionable ===

	@SuppressWarnings( "unchecked" )
	@Override
	public void fwd( final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( getPosEntryInternal( d ) + 1, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void bck( final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( getPosEntryInternal( d ) - 1, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final int distance, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( getPosEntryInternal( d ) + distance, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final long distance, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( getPosEntryInternal( d ) + distance, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final Localizable localizable )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( getPosEntryInternal( d ) + localizable.getDoublePosition( d ), d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final int[] distance )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( getPosEntryInternal( d ) + distance[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final long[] distance )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( getPosEntryInternal( d ) + distance[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final Localizable localizable )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( localizable.getDoublePosition( d ), d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final int[] position )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( position[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final long[] position )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( position[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final int position, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( position, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final long position, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( position, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final float distance, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( getPosEntryInternal( d ) + distance, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final double distance, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( getPosEntryInternal( d ) + distance, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final RealLocalizable localizable )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( getPosEntryInternal( d ) + localizable.getDoublePosition( d ), d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final float[] distance )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( getPosEntryInternal( d ) + distance[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void move( final double[] distance )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( getPosEntryInternal( d ) + distance[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final RealLocalizable localizable )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( localizable.getDoublePosition( d ), d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final float[] position )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( position[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final double[] position )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		for ( int d = 0; d < n; ++d )
			setPosEntryInternal( position[ d ], d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final float position, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( position, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setPosition( final double position, final int d )
	{
		modelGraph.notifyBeforeVertexPositionChange( ( V ) this );
		setPosEntryInternal( position, d );
		modelGraph.notifyVertexPositionChanged( ( V ) this );
	}
}
