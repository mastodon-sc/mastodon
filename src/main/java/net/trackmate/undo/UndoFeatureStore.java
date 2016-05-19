package net.trackmate.undo;

import java.util.ArrayList;
import java.util.Map;

import net.trackmate.collection.UniqueHashcodeArrayMap;
import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.VertexFeature.UndoFeatureMap;

class UndoFeatureStore< V extends Vertex< E >, E extends Edge< V > >
{
	private int idgen;

	private final ArrayList< UndoFeatureMap< V > > vertexUndoMapList;

	private final Map< VertexFeature< ?, V, ? >, UndoFeatureMap< V > > vertexUndoMaps;

	public UndoFeatureStore( final GraphFeatures< V, E > features )
	{
		idgen = 0;
		vertexUndoMapList = new ArrayList<>();
		vertexUndoMaps = new UniqueHashcodeArrayMap<>();
		features.addCreateFeatureMapListener( this::createFeatureMap );
	}

	public int createFeatureUndoId()
	{
		return idgen++;
	}

	/**
	 * Store all features of the specified {@code vertex} with key {@code undoId}.
	 *
	 * @param undoId
	 * @param vertex
	 */
	public void storeAll( final int undoId, final V vertex )
	{
		vertexUndoMapList.forEach( m -> m.store( undoId, vertex ) );
	}

	/**
	 * Store the specified feature of the specified {@code vertex} with key
	 * {@code undoId}.
	 *
	 * @param undoId
	 * @param vertex
	 */
	public void store( final int undoId, final VertexFeature< ?, V, ? > feature, final V vertex )
	{
		vertexUndoMaps.get( feature ).store( undoId, vertex );
	}

	/**
	 * Store all features of the specified {@code edge} with key {@code undoId}.
	 *
	 * @param undoId
	 * @param edge
	 */
	public void storeAll( final int undoId, final E edge )
	{
		// TODO
	}

	/**
	 * Retrieve all features stored with key {@code undoId} and set them in
	 * {@code vertex}. If there is no value for a feature associated with
	 * {@code undoId}, clear the feature in {@code vertex}.
	 *
	 * @param undoId
	 * @param vertex
	 */
	public void retrieveAll( final int undoId, final V vertex )
	{
		vertexUndoMapList.forEach( m -> m.retrieve( undoId, vertex ) );
	}

	/**
	 * Retrieve the specified feature stored with key {@code undoId} and set
	 * them in {@code vertex}. If there is no value for the feature associated
	 * with {@code undoId}, clear the feature in {@code vertex}.
	 *
	 * @param undoId
	 * @param vertex
	 */
	public void retrieve( final int undoId, final VertexFeature< ?, V, ? > feature, final V vertex )
	{
		vertexUndoMaps.get( feature ).retrieve( undoId, vertex );
	}

	/**
	 * Retrieve all features stored with key {@code undoId} and set them in
	 * {@code edge}. If there is no value for a feature associated with
	 * {@code undoId}, clear the feature in {@code edge}.
	 *
	 * @param undoId
	 * @param edge
	 */
	public void retrieveAll( final int undoId, final E edge )
	{
		// TODO
	}

	/**
	 * Store the specified feature of the specified {@code vertex} with key
	 * {@code undoId} and replace it with the feature value currently stored
	 * with key {@code undoId}. If there is no value currently associated with
	 * {@code undoId}, clear the feature in {@code vertex}.
	 *
	 * @param undoId
	 * @param vertex
	 */
	public void swap( final int undoId, final VertexFeature< ?, V, ? > feature, final V vertex )
	{
		vertexUndoMaps.get( feature ).swap( undoId, vertex );
	}

	private < M > void createFeatureMap( final VertexFeature< M, V, ? > feature, final M featureMap )
	{
		final UndoFeatureMap< V > undoMap = feature.createUndoFeatureMap( featureMap );
		vertexUndoMapList.add( undoMap );
		vertexUndoMaps.put( feature, undoMap );
	}
}
