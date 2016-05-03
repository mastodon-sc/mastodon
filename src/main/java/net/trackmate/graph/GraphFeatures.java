package net.trackmate.graph;

import java.util.ArrayList;
import java.util.Map;

import net.trackmate.graph.VertexFeature.FeatureCleanup;
import net.trackmate.graph.util.UniqueHashcodeArrayMap;

public class GraphFeatures< V extends Vertex< E >, E extends Edge< V > >
{
	private final ReadOnlyGraph< V, E > graph;

	/**
	 * Maps {@link VertexFeature} to feature-map objects, that are usually
	 * {@code Map<V,T>} with the type {@code T} of feature values. The reason
	 * this is not fixed to {@code Map<V,?>} is that for example primitive
	 * features might want to use Trove maps instead.
	 */
	private final Map< VertexFeature< ?, V, ? >, Object > vertexFeatureMaps;

	private final ArrayList< FeatureCleanup< V > > vertexFeatureCleanups;

	public GraphFeatures( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		vertexFeatureMaps = new UniqueHashcodeArrayMap<>();
		vertexFeatureCleanups = new ArrayList<>();
	}

	@SuppressWarnings( "unchecked" )
	public < M > M getVertexFeature( final VertexFeature< M, V, ? > feature )
	{
		M fmap = ( M ) vertexFeatureMaps.get( feature );
		if ( fmap == null )
		{
			fmap = feature.createFeatureMap( graph );
			vertexFeatureMaps.put( feature, fmap );
			vertexFeatureCleanups.add( feature.createFeatureCleanup( fmap ) );
		}
		return fmap;
	}

	// TODO
	// TODO
	// TODO
	// TODO
	// TODO
	// TODO
	// TODO
	// TODO
	// TODO: someone needs to call this!!!
	public void clear()
	{
		vertexFeatureMaps.clear();
		vertexFeatureCleanups.clear();
	}

	void delete( final V vertex )
	{
		for ( final FeatureCleanup< V > cleanup : vertexFeatureCleanups )
			cleanup.delete( vertex );
	}
}
