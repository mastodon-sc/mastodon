package org.mastodon.revised.model.feature;

import java.util.Set;

import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.model.AbstractModel;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Interface for classes that can compute a feature on a model.
 * <p>
 * Concrete implementations must be stateless, without side effects. A computer
 * must generate a single feature, however a feature does not have to be scalar.
 *
 * @param <O>
 *            the type of the objects this feature is defined for.
 * @param <M>
 *            the type of the property map this feature relies on.
 * @param <AM>
 *            the type of the model the feature is calculated on and stored in.
 */
public interface FeatureComputer< O, M extends PropertyMap< O, ? >, AM extends AbstractModel< ?, ?, ? > > extends SciJavaPlugin
{

	/**
	 * Returns the set of dependencies of this feature computer.
	 * <p>
	 * Dependencies are expressed as the set of feature computer names.
	 *
	 * @return the set of dependencies.
	 */
	public Set< String > getDependencies();

	/**
	 * Performs feature calculation.
	 * <p>
	 * Objects for which the feature is defined are taken from the specified
	 * model. The model is also used to store the feature within one of its
	 * feature model components.
	 *
	 * @param model
	 *            the model to retrieve objects from.
	 */
	public Feature< O, M > compute( final AM model );

	/**
	 * Returns the string key of the feature calculated by this computer.
	 *
	 * @return the feature key.
	 */
	public String getKey();

	/**
	 * Returns a feature serializer that can de/serialize <b>this specific
	 * feature</b> from/to a raw file.
	 *
	 * @return a feature serializer.
	 */
	public FeatureSerializer< O, M, AM > getSerializer();

}
