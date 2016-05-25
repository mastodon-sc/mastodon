package net.trackmate.undo;

import java.util.ArrayList;
import java.util.Map;

import net.trackmate.collection.UniqueHashcodeArrayMap;
import net.trackmate.graph.Edge;
import net.trackmate.graph.EdgeFeature;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.GraphFeatures.CreateFeatureMapListener;
import net.trackmate.graph.features.UndoFeatureMap;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;

class UndoFeatureStore< V extends Vertex< E >, E extends Edge< V > >
{
	private int idgen;

	private final ArrayList< UndoFeatureMap< V > > vertexUndoMapList;

	private final ArrayList< UndoFeatureMap< E > > edgeUndoMapList;

	private final Map< VertexFeature< ?, V, ? >, UndoFeatureMap< V > > vertexUndoMaps;

	private final Map< EdgeFeature< ?, E, ? >, UndoFeatureMap< E > > edgeUndoMaps;


	public UndoFeatureStore( final GraphFeatures< V, E > features )
	{
		idgen = 0;
		vertexUndoMapList = new ArrayList<>();
		vertexUndoMaps = new UniqueHashcodeArrayMap<>();
		edgeUndoMapList = new ArrayList< >();
		edgeUndoMaps = new UniqueHashcodeArrayMap< >();
		features.addCreateFeatureMapListener( new CreateFeatureMapListener< V, E >()
		{
			@Override
			public < M > void createFeatureMap( final VertexFeature< M, V, ? > feature, final M featureMap )
			{
				final UndoFeatureMap< V > undoMap = feature.createUndoFeatureMap( featureMap );
				vertexUndoMapList.add( undoMap );
				vertexUndoMaps.put( feature, undoMap );
			}

			@Override
			public < M > void createFeatureMap( final EdgeFeature< M, E, ? > feature, final M featureMap )
			{
				final UndoFeatureMap< E > undoMap = feature.createUndoFeatureMap( featureMap );
				edgeUndoMapList.add( undoMap );
				edgeUndoMaps.put( feature, undoMap );
			}
		} );
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
	 * Store the specified feature of the specified {@code edge} with key
	 * {@code undoId}.
	 *
	 * @param undoId
	 * @param edge
	 */
	public void store( final int undoId, final EdgeFeature< ?, E, ? > feature, final E edge )
	{
		edgeUndoMaps.get( feature ).store( undoId, edge );
	}

	/**
	 * Store all features of the specified {@code edge} with key {@code undoId}.
	 *
	 * @param undoId
	 * @param edge
	 */
	public void storeAll( final int undoId, final E edge )
	{
		edgeUndoMapList.forEach( m -> m.store( undoId, edge ) );
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
		edgeUndoMapList.forEach( m -> m.retrieve( undoId, edge ) );
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
}
