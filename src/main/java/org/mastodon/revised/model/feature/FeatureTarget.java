package org.mastodon.revised.model.feature;

/**
 * Enum that specifies what is the object type a feature is defined for.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public enum FeatureTarget
{
	/**
	 * The feature is defined for individual vertices of a graph (example: the
	 * vertex number of incoming edges).
	 */
	VERTEX,
	/**
	 * The feature is defined for individual edges of a graph (example: the time
	 * difference between the source and target vertices of this link).
	 */
	EDGE,
	/**
	 * The feature is defined for a whole graph (example: the number of vertices
	 * in this graph).
	 */
	GRAPH,
	/**
	 * The feature is defined for a time-point (example: the number of vertices
	 * that belong to this time-point).
	 */
	TIMEPOINT,
	/**
	 * The feature target is not defined.
	 */
	UNDEFINED;
}