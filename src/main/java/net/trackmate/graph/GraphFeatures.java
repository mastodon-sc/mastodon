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

	private final ArrayList< CreateFeatureMapListener > listeners;

	public GraphFeatures( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		vertexFeatureMaps = new UniqueHashcodeArrayMap<>();
		vertexFeatureCleanups = new ArrayList<>();
		listeners = new ArrayList<>();
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

	public interface CreateFeatureMapListener< V extends Vertex< E >, E extends Edge< V > >
	{
		public < M > void createFeatureMap( final VertexFeature< M, V, ? > feature, M featureMap );
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
	public boolean addCreateFeatureMapListener( final CreateFeatureMapListener< V, E > listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
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
	public synchronized boolean removeCreateFeatureMapListener( final CreateFeatureMapListener listener )
	{
		return listeners.remove( listener );
	}
}
