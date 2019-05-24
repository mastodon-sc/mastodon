package org.mastodon.feature;

/**
 * The Multiplicity of a feature indicates whether it is dependent of the number
 * of sources (e.g., channels, etc) present in the model.
 */
public enum Multiplicity
{
	/**
	 * For features that do not have multiplicity.
	 */
	SINGLE,
	/**
	 * For features that aggregates multiple scalar projections.
	 */
	MULTI,
	/**
	 * For features that have one value per source present in the model.
	 */
	ON_SOURCES,
	/**
	 * For features that have one value per source pair.
	 */
	ON_SOURCE_PAIRS,
}
