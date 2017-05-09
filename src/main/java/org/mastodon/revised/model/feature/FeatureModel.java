package org.mastodon.revised.model.feature;

/**
 * Interface for feature models, classes that manage a collection of features in
 * a model graph.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices in the model.
 * @param <E>
 *            the type of edges in the model.
 */
public interface FeatureModel< V, E > extends FeatureKeys
{
	/**
	 * Returns the edge feature projection with the specified key.
	 * 
	 * @param projectionKey
	 *            the projection key.
	 * @return the feature projection of <code>null</code> if they projection
	 *         key is unknown or defined for another target than an edge.
	 */
	public FeatureProjection< E > getEdgeProjection( final String projectionKey );

	/**
	 * Returns the vertex feature projection with the specified key.
	 * 
	 * @param projectionKey
	 *            the projection key.
	 * @return the feature projection of <code>null</code> if they projection
	 *         key is unknown or defined for another target than a vertex.
	 */
	public FeatureProjection< V > getVertexProjection( final String projectionKey );

	/**
	 * Returns the feature with the specified key.
	 * 
	 * @param featureKey
	 *            the feature key.
	 * @return the feature, or <code>null</code> if the feature is unknown.
	 */
	public Feature< ?, ?, ? > getFeature( final String featureKey );

	/**
	 * Clears this feature and projection model.
	 */
	public void clear();

	/**
	 * Registers the feature and the feature projections provided by the
	 * specified feature computer.
	 * 
	 * @param fc
	 *            the feature computer.
	 */
	public void declareFeature( final FeatureComputer< ?, ?, ? > fc );
}