package org.mastodon.revised.trackscheme;

import static org.mastodon.revised.trackscheme.ScreenVertex.Transition.NONE;

import org.mastodon.RefPool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.PoolObjectLayout;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.BooleanAttribute;
import org.mastodon.pool.attributes.ByteAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.revised.trackscheme.ScreenVertex.ScreenVertexPool;

/**
 * Layouted vertex.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenVertex extends PoolObject< ScreenVertex, ScreenVertexPool, ByteMappedElement >
{
	public static class ScreenVertexLayout extends PoolObjectLayout
	{
		final IndexField origVertex = indexField();
		final DoubleField xOffset = doubleField();
		final DoubleField yOffset = doubleField();
		final DoubleField vertexDist = doubleField();
		final BooleanField selected = booleanField();
		final BooleanField ghost = booleanField();
		final ByteField transition = byteField();
		final IndexField ipScreenVertex = indexField();
		final DoubleField ipRatio = doubleField();
	}

	public static ScreenVertexLayout layout = new ScreenVertexLayout();

	public static class ScreenVertexPool extends Pool< ScreenVertex, ByteMappedElement >
	{
		final RefPool< TrackSchemeVertex > trackSchemeVertexPool;

		final IndexAttribute< ScreenVertex > origVertex = new IndexAttribute<>( layout.origVertex );
		final DoubleAttribute< ScreenVertex > xOffset = new DoubleAttribute<>( layout.xOffset );
		final DoubleAttribute< ScreenVertex > yOffset = new DoubleAttribute<>( layout.yOffset );
		final DoubleAttribute< ScreenVertex > vertexDist = new DoubleAttribute<>( layout.vertexDist );
		final BooleanAttribute< ScreenVertex > selected = new BooleanAttribute<>( layout.selected );
		final BooleanAttribute< ScreenVertex > ghost = new BooleanAttribute<>( layout.ghost );
		final ByteAttribute< ScreenVertex > transition = new ByteAttribute<>( layout.transition );
		final IndexAttribute< ScreenVertex > ipScreenVertex = new IndexAttribute<>( layout.ipScreenVertex );
		final DoubleAttribute< ScreenVertex > ipRatio = new DoubleAttribute<>( layout.ipRatio );

		public ScreenVertexPool( final int initialCapacity, final RefPool< TrackSchemeVertex > trackSchemeVertexPool )
		{
			super( initialCapacity, layout, ScreenVertex.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
			this.trackSchemeVertexPool = trackSchemeVertexPool;
		}

		@Override
		protected ScreenVertex createEmptyRef()
		{
			return new ScreenVertex( this );
		}

		@Override
		public ScreenVertex create( final ScreenVertex vertex )
		{
			return super.create( vertex );
		}

		@Override
		public void delete( final ScreenVertex vertex )
		{
			super.delete( vertex );
		}
	}

	private final TrackSchemeVertex vref;

	private final RefPool< TrackSchemeVertex > trackSchemeVertexPool;

	public static enum Transition
	{
		NONE( 0 ),
		APPEAR( 1 ),
		DISAPPEAR( 2 ),
		SELECTING( 3 ),
		DESELECTING( 4 ), ;

		private final byte index;

		private Transition( final int index )
		{
			this.index = ( byte ) index;
		}

		public byte toByte()
		{
			return index;
		}
	}

	protected ScreenVertex( final ScreenVertexPool pool )
	{
		super( pool );
		this.trackSchemeVertexPool = pool.trackSchemeVertexPool;
		this.vref = trackSchemeVertexPool.createRef();
	}

	public ScreenVertex init(
			final int id,
			final double x,
			final double y,
			final boolean selected,
			final boolean ghost )
	{
		setTrackSchemeVertexId( id );
		setX( x );
		setY( y );
		setSelected( selected );
		setGhost( ghost );
		setTransition( NONE );
		return this;
	}

	/**
	 * Get the internal pool index of the associated {@link TrackSchemeVertex}.
	 *
	 * @return the internal pool index of the associated
	 *         {@link TrackSchemeVertex}.
	 */
	public int getTrackSchemeVertexId()
	{
		return pool.origVertex.get( this );
	}

	protected void setTrackSchemeVertexId( final int id )
	{
		pool.origVertex.setQuiet( this, id );
	}

	/**
	 * Get the X screen coordinate of the vertex center.
	 *
	 * @return X screen coordinate.
	 */
	public double getX()
	{
		return pool.xOffset.get( this );
	}

	protected void setX( final double x )
	{
		pool.xOffset.setQuiet( this, x );
	}

	/**
	 * Get the Y screen coordinate of the vertex center.
	 *
	 * @return Y screen coordinate.
	 */
	public double getY()
	{
		return pool.yOffset.get( this );
	}

	protected void setY( final double y )
	{
		pool.yOffset.setQuiet( this, y );
	}

	/**
	 * (Estimate of) the distance to the closest vertex on screen.
	 * Center-to-center distance. This is used to determine how large the vertex
	 * should be painted.
	 *
	 * @return distance to the closest vertex on screen.
	 */
	public double getVertexDist()
	{
		return pool.vertexDist.get( this );
	}

	protected void setVertexDist( final double minVertexScreenDist )
	{
		pool.vertexDist.setQuiet( this, minVertexScreenDist );
	}

	/**
	 * Get the label of the vertex. This calls
	 * {@link TrackSchemeVertex#getLabel()} of the associated
	 * {@link TrackSchemeVertex}.
	 *
	 * @return label of the vertex.
	 */
	public String getLabel()
	{
		final int idx = getTrackSchemeVertexId();
		if ( idx >= 0 )
		{
			return trackSchemeVertexPool.getObject( idx, vref ).getLabel();
		}
		else
			return "XXX";
	}

	/**
	 * Get the selected state of the vertex.
	 *
	 * @return true, if the vertex is selected.
	 */
	public boolean isSelected()
	{
		return pool.selected.get( this );
	}

	protected void setSelected( final boolean selected )
	{
		pool.selected.setQuiet( this, selected );
	}

	/**
	 * Get the ghost state of the vertex.
	 *
	 * @return true, if the vertex is ghosted.
	 */
	public boolean isGhost()
	{
		return pool.ghost.get( this );
	}

	protected void setGhost( final boolean ghost )
	{
		pool.ghost.setQuiet( this, ghost );
	}

	/**
	 * Returns the current transition state for this screen vertex.
	 *
	 * @return the transition state.
	 */
	public Transition getTransition()
	{
		return Transition.values()[ pool.transition.get( this ) ];
	}

	protected void setTransition( final Transition t )
	{
		pool.transition.setQuiet( this, t.toByte() );
	}

	/**
	 * Get the internal pool index of the interpolated {@link ScreenVertex} for
	 * which this {@link ScreenVertex} is the interpolation target.
	 *
	 * @return internal pool index of the interpolated {@link ScreenVertex}.
	 */
	protected int getInterpolatedScreenVertexIndex()
	{
		return pool.ipScreenVertex.get( this );
	}

	protected void setInterpolatedScreenVertexIndex( final int screenVertexIndex )
	{
		pool.ipScreenVertex.setQuiet( this, screenVertexIndex );
	}

	/**
	 * Returns the interpolation completion ratio of the current transition for
	 * this screen vertex.
	 *
	 * @return the interpolation completion ratio.
	 */
	public double getInterpolationCompletionRatio()
	{
		return pool.ipRatio.get( this );
	}

	/**
	 * Sets the interpolation completion ratio of the current transition for
	 * this screen vertex.
	 *
	 * @param ratio
	 *            the interpolation completion ratio.
	 */
	protected void setInterpolationCompletionRatio( final double ratio )
	{
		pool.ipRatio.setQuiet( this, ratio );
	}

	@Override
	protected void setToUninitializedState()
	{}

	/**
	 * Set all fields as in specified {@link ScreenVertex} (which is possibly
	 * from another pool).
	 * <p>
	 * ONLY USE THIS FOR {@link ScreenEntities#set(ScreenEntities)}!
	 *
	 * @param v
	 *            the vertex to clone parameters from.
	 * @return {@code this}.
	 */
	ScreenVertex cloneFrom( final ScreenVertex v )
	{
		setTrackSchemeVertexId( v.getTrackSchemeVertexId() );
		setX( v.getX() );
		setY( v.getY() );
		setVertexDist( v.getVertexDist() );
		setSelected( v.isSelected() );
		setGhost( v.isGhost() );
		setTransition( v.getTransition() );
		setInterpolatedScreenVertexIndex( v.getInterpolatedScreenVertexIndex() );
		setInterpolationCompletionRatio( v.getInterpolationCompletionRatio() );
		return this;
	}

// TODO REMOVE? should be covered by base class.
//	@Override
//	public boolean equals( final Object obj )
//	{
//		return obj instanceof ScreenVertex &&
//				access.equals( ( ( ScreenVertex ) obj ).access );
//	}
//
//	@Override
//	public int hashCode()
//	{
//		return access.hashCode();
//	}

	@Override
	public String toString()
	{
		return String.format( "ScreenVertex(%d, sv=%d, \"%s\", (%.2f, %.2f), %s, isv=%d%s)",
				getInternalPoolIndex(),
				getTrackSchemeVertexId(),
				getLabel(),
				getX(),
				getY(),
				getTransition().toString(),
				getInterpolatedScreenVertexIndex(),
				isSelected() ? ", selected" : "" );
	}
}
