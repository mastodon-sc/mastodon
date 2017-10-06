package org.mastodon.revised.model.feature;

import java.util.Set;

import org.mastodon.revised.model.AbstractModel;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Interface for classes that can compute a feature on a model.
 * <p>
 * Concrete implementations must be stateless, without side effects. A computer
 * must generate a single feature, however a feature does not have to be scalar.
 *
 * @param <AM>
 *            the type of the model the feature is calculated on and stored in.
 */
public interface FeatureComputer< AM extends AbstractModel< ?, ?, ? > > extends SciJavaPlugin
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
	public Feature< ?, ? > compute( final AM model );

	/**
	 * Returns the string key of the feature calculated by this computer.
	 *
	 * @return the feature key.
	 */
	public String getKey();
}
