package org.mastodon.revised.trackscheme;

/**
 * Interface for classes that access model graph features.
 * <p>
 * To make {@link TrackSchemeGraph} adaptable to various model graph type
 * without requiring the graph to implement specific interfaces, we access
 * features of model vertices and edges through
 * {@link ModelScalarFeaturesProperties}.
 * <p>
 * This interface is limited to access numerical scalar features, for which the
 * value can be represented as {@code double}.
 *
 * @author Jean-Yves Tinevez
 */
public interface ModelScalarFeaturesProperties
{

	/**
	 * Interface a feature value.
	 * 
	 * @author Jean-Yves Tinevez
	 *
	 */
	public interface FeatureProperties
	{
		
		/**
		 * Returns the value of this feature for the vertex or edge wit the
		 * specified ID.
		 * 
		 * @param id
		 *            the vertex or edge id.
		 * @return the feature value.
		 */
		public double get( int id );

		/**
		 * Returns whether the value of this feature is set for the vertex or
		 * edge with the specified ID.
		 * 
		 * @param id
		 *            the vertex or edge id.
		 * @return whether the feature is set.
		 */
		public boolean isSet( int id );

		/**
		 * Returns the range of this feature, that is its minimal and maximal
		 * value across the whole graph.
		 * 
		 * @return a new {@code double[]} array, made of two elements:
		 *         <ol start="0">
		 *         <li>the min feature value.
		 *         <li>the max feature value.
		 *         <ol>
		 */
		public double[] getMinMax();
	}

	/**
	 * Returns the vertex feature associated to the specified key. Returns
	 * {@code null} if there are no vertex feature with such key.
	 * 
	 * @param key
	 *            the feature key.
	 * @return the feature.
	 */
	public FeatureProperties getVertexFeature( String key );

	/**
	 * Returns the edge feature associated to the specified key. Returns
	 * {@code null} if there are no edge feature with such key.
	 * 
	 * @param key
	 *            the feature key.
	 * @return the feature.
	 */
	public FeatureProperties getEdgeFeature( String key );
}