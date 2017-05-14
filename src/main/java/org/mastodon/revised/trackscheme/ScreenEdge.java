package org.mastodon.revised.trackscheme;

import static org.mastodon.revised.trackscheme.ScreenVertex.Transition.NONE;

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
import org.mastodon.pool.attributes.IntAttribute;
import org.mastodon.revised.trackscheme.ScreenEdge.ScreenEdgePool;
import org.mastodon.revised.trackscheme.ScreenVertex.Transition;

/**
 * Layouted edge.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenEdge extends PoolObject< ScreenEdge, ScreenEdgePool, ByteMappedElement >
{
	public static class ScreenEdgeLayout extends PoolObjectLayout
	{
		final IndexField origEdge = indexField();
		final IndexField sourceScreenVertex = indexField();
		final IndexField targetScreenVertex = indexField();
		final BooleanField selected = booleanField();
		final ByteField transition = byteField();
		final DoubleField ipRatio = doubleField();
		final IntField color = intField();
	}

	public static ScreenEdgeLayout layout = new ScreenEdgeLayout();

	public static class ScreenEdgePool extends Pool< ScreenEdge, ByteMappedElement >
	{
		final IndexAttribute< ScreenEdge > origEdge = new IndexAttribute<>( layout.origEdge, this );
		final IndexAttribute< ScreenEdge > sourceScreenVertex = new IndexAttribute<>( layout.sourceScreenVertex, this );
		final IndexAttribute< ScreenEdge > targetScreenVertex = new IndexAttribute<>( layout.targetScreenVertex, this );
		final BooleanAttribute< ScreenEdge > selected = new BooleanAttribute<>( layout.selected, this );
		final ByteAttribute< ScreenEdge > transition = new ByteAttribute<>( layout.transition, this );
		final DoubleAttribute< ScreenEdge > ipRatio = new DoubleAttribute<>( layout.ipRatio, this );
		final IntAttribute< ScreenEdge > color = new IntAttribute<>( layout.color, this );

		public ScreenEdgePool( final int initialCapacity )
		{
			super( initialCapacity, layout, ScreenEdge.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		}

		@Override
		protected ScreenEdge createEmptyRef()
		{
			return new ScreenEdge( this );
		}

		@Override
		public ScreenEdge create( final ScreenEdge edge )
		{
			return super.create( edge );
		}

		@Override
		public void delete( final ScreenEdge edge )
		{
			super.delete( edge );
		}
	}

	protected ScreenEdge( final ScreenEdgePool pool )
	{
		super( pool );
	}

	public ScreenEdge init(
			final int id,
			final int sourceScreenVertexIndex,
			final int targetScreenVertexIndex,
			final boolean selected,
			final int color )
	{
		setTrackSchemeEdgeId( id );
		setSourceScreenVertexIndex( sourceScreenVertexIndex );
		setTargetScreenVertexIndex( targetScreenVertexIndex );
		setSelected( selected );
		setTransition( NONE );
		setColor( color );
		return this;
	}

	/**
	 * Returns the current transition state for this screen edge.
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
	 * Returns the interpolation completion ratio of the current transition for
	 * this screen edge.
	 *
	 * @return the interpolation completion ratio.
	 */
	public double getInterpolationCompletionRatio()
	{
		return pool.ipRatio.get( this );
	}

	/**
	 * Sets the interpolation completion ratio of the current transition for
	 * this screen edge.
	 *
	 * @param ratio
	 *            the interpolation completion ratio.
	 */
	protected void setInterpolationCompletionRatio( final double ratio )
	{
		pool.ipRatio.setQuiet( this, ratio );
	}

	/**
	 * Get the internal pool index of the associated {@link TrackSchemeEdge}.
	 *
	 * @return the internal pool index of the associated
	 *         {@link TrackSchemeEdge}.
	 */
	public int getTrackSchemeEdgeId()
	{
		return pool.origEdge.get( this );
	}

	protected void setTrackSchemeEdgeId( final int id )
	{
		pool.origEdge.setQuiet( this, id );
	}

	/**
	 * Get the index of the source ("from") {@link ScreenVertex} in the screen
	 * vertex list {@link ScreenEntities#getVertices()}. This is at the same
	 * time the internal pool index of the source {@link ScreenVertex}.
	 *
	 * @return internal pool index of the source {@link ScreenVertex}.
	 */
	public int getSourceScreenVertexIndex()
	{
		return pool.sourceScreenVertex.get( this );
	}

	protected void setSourceScreenVertexIndex( final int index )
	{
		pool.sourceScreenVertex.setQuiet( this, index );
	}

	/**
	 * Get the index of the target ("to") {@link ScreenVertex} in the screen
	 * vertex list {@link ScreenEntities#getVertices()}. This is at the same
	 * time the internal pool index of the target {@link ScreenVertex}.
	 *
	 * @return internal pool index of the target {@link ScreenVertex}.
	 */
	public int getTargetScreenVertexIndex()
	{
		return pool.targetScreenVertex.get( this );
	}

	protected void setTargetScreenVertexIndex( final int index )
	{
		pool.targetScreenVertex.setQuiet( this, index );
	}

	/**
	 * Get the selected state of the edge.
	 *
	 * @return true, if the edge is selected.
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
	 * Returns the color of this edge (ARGB bytes packed into {@code int}).
	 *
	 * @return the color.
	 */
	public int getColor()
	{
		return pool.color.get( this );
	}

	/**
	 * Set the color of this edge (ARGB bytes packed into {@code int}).
	 *
	 * @param color
	 *            the color as ARGB bytes packed into {@code int}
	 */
	protected void setColor( final int color )
	{
		pool.color.setQuiet( this, color );
	}

	@Override
	protected void setToUninitializedState()
	{}

	/**
	 * Set all fields as in specified {@link ScreenEdge} (which is possibly
	 * from another pool).
	 * <p>
	 * ONLY USE THIS FOR {@link ScreenEntities#set(ScreenEntities)}!
	 *
	 * @param e
	 * @return {@code this}.
	 */
	ScreenEdge cloneFrom( final ScreenEdge e )
	{
		setTrackSchemeEdgeId( e.getTrackSchemeEdgeId() );
		setSourceScreenVertexIndex( e.getSourceScreenVertexIndex() );
		setTargetScreenVertexIndex( e.getTargetScreenVertexIndex() );
		setSelected( e.isSelected() );
		setTransition( e.getTransition() );
		setInterpolationCompletionRatio( e.getInterpolationCompletionRatio() );
		setColor( e.getColor() );
		return this;
	}

// TODO REMOVE? should be covered by base class.
//	@Override
//	public boolean equals( final Object obj )
//	{
//		return obj instanceof ScreenEdge &&
//				access.equals( ( ( ScreenEdge ) obj ).access );
//	}
//
//	@Override
//	public int hashCode()
//	{
//		return access.hashCode();
//	}
}
