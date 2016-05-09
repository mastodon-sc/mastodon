package net.trackmate.undo;

import java.util.ArrayList;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.VertexFeature.UndoFeatureMap;

class UndoFeatureStore< V extends Vertex< E >, E extends Edge< V > >
{
	private int idgen;

	private final ArrayList< UndoFeatureMap< V > > undoMaps;

	public UndoFeatureStore( final GraphFeatures< V, E > features )
	{
		idgen = 0;
		undoMaps = new ArrayList<>();
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
		undoMaps.forEach( m -> m.store( undoId, vertex ) );
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
		undoMaps.forEach( m -> m.retrieve( undoId, vertex ) );
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

	private < M > void createFeatureMap( final VertexFeature< M, V, ? > feature, final M featureMap )
	{
		undoMaps.add( feature.createUndoFeatureMap( featureMap ) );
	}
}
