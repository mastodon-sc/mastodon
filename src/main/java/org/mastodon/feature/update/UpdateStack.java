package org.mastodon.feature.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.DefaultFeatureComputerService;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;

public abstract class UpdateStack< O > implements Feature< O >
{

	/**
	 * Number of graph update commits we store before triggering a full
	 * recalculation.
	 */
	static final int BUFFER_SIZE = 10;

	private final SizedDeque< UpdateState< O > > stateStack;

	private final RefCollection< O > pool;

	protected UpdateStack( final RefCollection< O > pool )
	{
		this( pool, new SizedDeque<>( BUFFER_SIZE ) );
		clear();
	}

	/**
	 * For deserialization only.
	 *
	 * @param pool
	 *            the {@link RefCollection} used to create the object
	 *            collections in the stack.
	 * @param stateStack
	 *            the stack to use to store incremental changes.
	 */
	protected UpdateStack( final RefCollection< O > pool, final SizedDeque< UpdateState< O > > stateStack )
	{
		this.pool = pool;
		this.stateStack = stateStack;
	}

	/**
	 * This method should only be called by the
	 * {@link DefaultFeatureComputerService} after the computation step.
	 * <p>
	 * It stacks the current changes and mark them for the specified feature
	 * keys, then starts building a new one.
	 *
	 * @param featureKeys
	 *            the keys of the features that were computed before this
	 *            commit.
	 */
	public void commit( final Collection< FeatureSpec< ?, ? > > featureKeys )
	{
		stateStack.push( new UpdateState<>( new ArrayList<>( featureKeys ), new Update<>( pool ) ) );
	}

	/**
	 * Returns the incremental changes needed to update the feature with the
	 * specified specs.
	 * <p>
	 * A <code>null</code> value indicate that the feature should be re-computed
	 * for all the objects of the graph, without the possibility to use
	 * incremental updates. Otherwise, the objects to update can be retrieved
	 * with the {@link Update#get()} and {@link Update#getNeighbors()} methods.
	 *
	 * @param featureSpec
	 *            the feature specs of the feature to build an incremental
	 *            update for.
	 * @return an update object, or <code>null</code> if the full graph needs to
	 *         re-computed for this feature.
	 */
	public Update< O > changesFor( final FeatureSpec< ?, ? > featureSpec )
	{
		final Update< O > changes = new Update<>( pool );
		for ( final UpdateState< O > updateState : stateStack )
		{
			changes.concatenate( updateState.getChanges() );
			if ( updateState.contains( featureSpec ) )
				return changes;
		}
		return null;
	}

	public void clear()
	{
		stateStack.clear();
		commit( Collections.emptyList() );
	}

	public void addModified( final O obj )
	{
		stateStack.getFirst().getChanges().add( obj );
	}

	public void addNeighbor( final O obj )
	{
		stateStack.getFirst().getChanges().addAsNeighbor( obj );
	}

	public void remove( final O obj )
	{
		// Walk through the stack and remove trace of it.
		for ( final UpdateState< O > state : stateStack )
			state.getChanges().remove( obj );
	}

	/*
	 * Feature methods & fields.
	 */

	@Override
	public FeatureProjection< O > project( final FeatureProjectionKey key )
	{
		return null;
	}

	@Override
	public Set< FeatureProjection< O > > projections()
	{
		return Collections.emptySet();
	}

	/**
	 * Simple data class used to store graph updates in a stack along with the
	 * feature keys they are up-to-date for.
	 *
	 * @author Jean-Yves Tinevez
	 *
	 * @param <O>
	 *            the type of objects whose modification are tracked.
	 */
	public static final class UpdateState< O >
	{

		private final Collection< FeatureSpec< ?, ? > > featureKeys;

		private final Update< O > changes;

		public UpdateState( final Collection< FeatureSpec< ?, ? > > featureKeys, final Update< O > changes )
		{
			this.featureKeys = featureKeys;
			this.changes = changes;
		}

		public boolean contains( final FeatureSpec< ?, ? > featureKey )
		{
			return featureKeys.contains( featureKey );
		}

		@Override
		public String toString()
		{
			return super.toString() + " -> " + featureKeys.toString();
		}

		public Update< O > getChanges()
		{
			return changes;
		}

		Collection< FeatureSpec< ?, ? > > getFeatureKeys()
		{
			return featureKeys;
		}
	}

	static class SerialisationAccess< O >
	{

		private final UpdateStack< O > updateStack;

		SerialisationAccess( final UpdateStack< O > updateStack )
		{
			this.updateStack = updateStack;
		}

		SizedDeque< UpdateState< O > > getStateStack()
		{
			return updateStack.stateStack;
		}
	}
}
