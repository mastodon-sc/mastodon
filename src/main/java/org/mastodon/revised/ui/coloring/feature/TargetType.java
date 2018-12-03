package org.mastodon.revised.ui.coloring.feature;

/**
 * The kind of target, a feature/projection applies to.
 * <p>
 * This is used for FeatureColorModes, which need to be serialized/edited
 * without mentioning/knowing an explicit target {@link Class}.
 */
public enum TargetType
{
	VERTEX,
	EDGE;
}
