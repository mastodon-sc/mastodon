package org.mastodon.revised.model;

import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.MappedElement;
import org.mastodon.pool.attributes.IntAttributeValue;
import org.mastodon.pool.attributes.RealPointAttributeValue;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.util.DelegateRealLocalizable;
import org.mastodon.util.DelegateRealPositionable;

/**
 * Base class for specialized vertices that are part of a graph, and are used to
 * store spatial and temporal location.
 * <p>
 * The class ships the minimal required features, that is coordinates and
 * time-point.
 *
 * @param <V>
 *            the recursive type of the concrete implementation.
 * @param <E>
 *            associated edge type.
 * @param <VP>
 *            the type of the vertex pool.
 * @param <T>
 *            the MappedElement type, for example {@link ByteMappedElement}.
 * @param <G>
 *            the type of the model graph using this class as vertex class.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class AbstractSpot<
		V extends AbstractSpot< V, E, VP, T, G >,
		E extends AbstractListenableEdge< E, V, ?, T >,
		VP extends AbstractSpotPool< V, ?, T, G >,
		T extends MappedElement,
		G extends AbstractModelGraph< ?, ?, ?, V, E, T > >
	extends AbstractListenableVertex< V, E, VP, T >
	implements DelegateRealLocalizable, DelegateRealPositionable, HasTimepoint
{
	protected final int n;

	private final IntAttributeValue timepoint;

	private final RealPointAttributeValue position;

	protected AbstractSpot( final VP pool )
	{
		super( pool );
		n = pool.numDimensions();

		@SuppressWarnings( "unchecked" ) final V self = ( V ) this;
		position = pool.position.createAttributeValue( self );
		timepoint = pool.timepoint.createQuietAttributeValue( self );
	}

	protected void partialInit( final int timepointId, final double[] pos )
	{
		@SuppressWarnings( "unchecked" ) final V self = ( V ) this;
		pool.position.setPositionQuiet( self, pos );
		pool.timepoint.setQuiet( self, timepointId );
	}

	@Override
	public RealPointAttributeValue delegate()
	{
		return position;
	}

	/*
	 * Public API
	 */

	@Override
	public int getTimepoint()
	{
		return timepoint.get();
	}

	public G getModelGraph()
	{
		return pool.modelGraph;
	}
}
