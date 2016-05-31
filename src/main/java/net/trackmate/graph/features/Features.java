package net.trackmate.graph.features;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.Map;

import net.trackmate.RefPool;
import net.trackmate.collection.RefCollection;
import net.trackmate.collection.UniqueHashcodeArrayMap;
import net.trackmate.graph.io.RawFeatureIO;

/**
 * Manage {@link Feature}s associated with a specific object type {@code O},
 * (and, if {@code O} is a {@link Ref} type, associated with a specific
 * {@link RefPool}).
 * <p>
 * For example, a graph would have one {@link Features} for its vertex type and
 * one for its edge type.
 *
 * @param <O>
 *            type of object to which feature should be attached.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class Features< O >
{
	private final RefCollection< O > pool;

	/**
	 * Maps {@link Feature} to feature-map objects, that are usually
	 * {@code Map<O,T>} with the type {@code T} of feature values. The reason
	 * this is not fixed to {@code Map<O,?>} is that for example primitive
	 * features might want to use Trove maps instead.
	 */
	private final Map< Feature< ?, O, ? >, Object > featureMaps;

	private final ArrayList< FeatureCleanup< O > > featureCleanups;

	private final ArrayList< CreateFeatureMapListener< O > > createFeatureMapListeners;

	private final ArrayList< FeatureChangeListener< O > > featureChangeListeners;

	private boolean emitEvents;

	public Features( final RefCollection< O > pool )
	{
		this.pool = pool;
		featureMaps = new UniqueHashcodeArrayMap<>();
		featureCleanups = new ArrayList<>();
		createFeatureMapListeners = new ArrayList<>();
		featureChangeListeners = new ArrayList<>();
		emitEvents = true;
	}

	/**
	 * For internal use only.
	 * <p>
	 * This is only public because it needs to be accessed from
	 * {@link RawFeatureIO}.
	 * <p>
	 * Returns the feature map for the given {@code feature}. If the feature map
	 * doesn't exist yet, it is created (
	 * {@link Feature#createFeatureMap(RefCollection)}) and
	 * {@link CreateFeatureMapListener}s are notified.
	 *
	 * @return feature map for the given {@code feature}.
	 */
	@SuppressWarnings( "unchecked" )
	public < M > M getFeatureMap( final Feature< M, O, ? > feature )
	{
		M fmap = ( M ) featureMaps.get( feature );
		if ( fmap == null )
		{
			fmap = feature.createFeatureMap( pool );
			featureMaps.put( feature, fmap );
			featureCleanups.add( feature.createFeatureCleanup( fmap ) );
			for ( final CreateFeatureMapListener< O > l : createFeatureMapListeners )
				l.createFeatureMap( feature, fmap );
		}
		return fmap;
	}

	public void clear()
	{
		featureMaps.clear();
		featureCleanups.clear();
	}

	/**
	 * For internal use only.
	 *
	 * @param object
	 */
	public void delete( final O object )
	{
		for ( final FeatureCleanup< O > cleanup : featureCleanups )
			cleanup.delete( object );
	}

	/**
	 * A listener that is notified when new feature maps are created (this
	 * happens once per newly occurring feature in a graph).
	 *
	 * @param <O>
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public interface CreateFeatureMapListener< O >
	{
		public < M > void createFeatureMap( final Feature< M, O, ? > feature, M featureMap );

	}

	/**
	 * Register a {@link CreateFeatureMapListener} that will be notified when
	 * new feature maps are created (this happens once per newly occurring
	 * feature in a graph).
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addCreateFeatureMapListener( final CreateFeatureMapListener< O > listener )
	{
		if ( ! createFeatureMapListeners.contains( listener ) )
		{
			createFeatureMapListeners.add( listener );
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified {@link CreateFeatureMapListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeCreateFeatureMapListener( final CreateFeatureMapListener< O > listener )
	{
		return createFeatureMapListeners.remove( listener );
	}

	/**
	 * Register a {@link FeatureChangeListener} that will be notified when
	 * feature values are changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addFeatureChangeListener( final FeatureChangeListener< O > listener )
	{
		if ( ! featureChangeListeners.contains( listener ) )
		{
			featureChangeListeners.add( listener );
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified {@link FeatureChangeListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeFeatureChangeListener( final FeatureChangeListener< O > listener )
	{
		return featureChangeListeners.remove( listener );
	}

	void notifyBeforeFeatureChange( final Feature< ?, O, ? > feature, final O object )
	{
		if ( emitEvents )
			for ( final FeatureChangeListener< O > l : featureChangeListeners )
				l.beforeFeatureChange( feature, object );
	}

	/**
	 * For internal use only.
	 * <p>
	 * Resume sending events to {@link FeatureChangeListener}s.
	 */
	public void pauseListeners()
	{
		emitEvents = false;
	}

	/**
	 * For internal use only.
	 * <p>
	 * Resume sending events to {@link FeatureChangeListener}s.
	 */
	public void resumeListeners()
	{
		emitEvents = true;
	}
}
