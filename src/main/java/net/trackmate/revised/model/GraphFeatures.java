package net.trackmate.revised.model;

import java.util.Map;

import net.trackmate.graph.Edge;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;

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

	public GraphFeatures( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		vertexFeatureMaps = new UniqueHashcodeArrayMap<>();
	}

	/*
	 * TODO: It's not clear whether we should register features automatically
	 * when they are used for the first time. It seems easier for the user, but
	 * maybe also more confusing?
	 *
	 * However this requires more consideration for how to deal with new
	 * features appearing in a half-built model. Probably it would be good to
	 * call {@link VertexFeature#addVertex(net.trackmate.graph.Vertex, Object)}
	 * for all existing vertices?
	 */

	@SuppressWarnings( "unchecked" )
	public < M > M getVertexFeature( final VertexFeature< M, V, ? > feature )
	{
		M fmap = ( M ) vertexFeatureMaps.get( feature );
		if ( fmap == null )
		{
			fmap = feature.createFeatureMap( graph );
			vertexFeatureMaps.put( feature, fmap );
		}
		return fmap;
	}

	public void clear()
	{
		vertexFeatureMaps.clear();
	}
}
