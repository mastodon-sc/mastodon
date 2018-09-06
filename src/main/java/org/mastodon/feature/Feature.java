package org.mastodon.feature;

/**
 * Interface for Mastodon features.
 * <p>
 * A feature is a read-only quantity calculated for a model at runtime. They
 * store values that are defined for a model but are not required for the model
 * consistency. They are typically defined for a specific application by the
 * user; they are optional and their selection and calculation are triggered at
 * runtime.
 * <p>
 * A Feature is defined for a specific target in the model (vertex, edge, graph,
 * etc.) specified as a type parameter of this class.
 * <p>
 *
 * @param <T>
 *            target the type of the object this feature is defined for.
 */
public interface Feature< T >
{

	public FeatureProjection< T > project( String projectionKey );

	public String[] projectionKeys();

}
