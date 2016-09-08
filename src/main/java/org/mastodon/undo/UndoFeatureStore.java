package org.mastodon.undo;

import java.util.ArrayList;
import java.util.Map;

import org.mastodon.collection.UniqueHashcodeArrayMap;
import org.mastodon.features.Feature;
import org.mastodon.features.Features;
import org.mastodon.features.UndoFeatureMap;

/**
 * TODO javadoc
 *
 * @param <O>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
class UndoFeatureStore< O >
{
	private int idgen;

	private final ArrayList< UndoFeatureMap< O > > undoMapList;

	private final Map< Feature< ?, O, ? >, UndoFeatureMap< O > > undoMaps;

	public UndoFeatureStore( final Features< O > features )
	{
		idgen = 0;
		undoMapList = new ArrayList<>();
		undoMaps = new UniqueHashcodeArrayMap<>();
		features.addCreateFeatureMapListener( this::createFeatureMap );
	}

	public int createFeatureUndoId()
	{
		return idgen++;
	}

	/**
	 * Store all features of the specified {@code object} with key {@code undoId}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void storeAll( final int undoId, final O object )
	{
		undoMapList.forEach( m -> m.store( undoId, object ) );
	}

	/**
	 * Store the specified feature of the specified {@code object} with key
	 * {@code undoId}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void store( final int undoId, final Feature< ?, O, ? > feature, final O object )
	{
		undoMaps.get( feature ).store( undoId, object );
	}

	/**
	 * Retrieve all features stored with key {@code undoId} and set them in
	 * {@code object}. If there is no value for a feature associated with
	 * {@code undoId}, clear the feature in {@code object}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void retrieveAll( final int undoId, final O object )
	{
		undoMapList.forEach( m -> m.retrieve( undoId, object ) );
	}

	/**
	 * Retrieve the specified feature stored with key {@code undoId} and set
	 * them in {@code object}. If there is no value for the feature associated
	 * with {@code undoId}, clear the feature in {@code object}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void retrieve( final int undoId, final Feature< ?, O, ? > feature, final O object )
	{
		undoMaps.get( feature ).retrieve( undoId, object );
	}

	/**
	 * Store the specified feature of the specified {@code object} with key
	 * {@code undoId} and replace it with the feature value currently stored
	 * with key {@code undoId}. If there is no value currently associated with
	 * {@code undoId}, clear the feature in {@code object}.
	 *
	 * @param undoId
	 * @param object
	 */
	public void swap( final int undoId, final Feature< ?, O, ? > feature, final O object )
	{
		undoMaps.get( feature ).swap( undoId, object );
	}

	private < M > void createFeatureMap( final Feature< M, O, ? > feature, final M featureMap )
	{
		final UndoFeatureMap< O > undoMap = feature.createUndoFeatureMap( featureMap );
		undoMapList.add( undoMap );
		undoMaps.put( feature, undoMap );
	}
}
