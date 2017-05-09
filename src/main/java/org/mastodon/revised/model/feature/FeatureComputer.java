package org.mastodon.revised.model.feature;

import java.util.Map;
import java.util.Set;

import org.mastodon.revised.model.AbstractModel;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Interface for classes that can compute a feature on a model.
 * <p>
 * Concrete implementations must be stateless, without side effects. A computer
 * must generate a single feature, however a feature does not have to be scalar.
 * <p>
 * Since many visualization tools in Mastodon deal with scalar, real feature
 * values, the computer must also be able to return a set of feature
 * projections, that translate or project the feature on several real scalar
 * values. Since the computer defines what the feature it compute is, it also
 * must be able to define the meaningful projections for the feature.
 *
 * @param <K>
 *            the feature type.
 * @param <O>
 *            the object on which the feature is defined (vertex, edge, ...).
 * @param <AM>
 *            the type of the model the feature is calculated on and stored in.
 */
public interface FeatureComputer< K extends Feature< O, ?, ? >, O, AM extends AbstractModel< ?, ?, ? > > extends SciJavaPlugin
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
	public void compute( final AM model );

	/**
	 * Returns the feature this computer calculate.
	 * 
	 * @return the feature.
	 */
	public K getFeature();

	/**
	 * Returns the feature projections for the caluculated feature.
	 * <p>
	 * They are returned as a map from projection keys to actual projections.
	 * 
	 * @return the map of feature projections.
	 */
	public Map< String, FeatureProjection< O > > getProjections();

	/**
	 * Returns the object the calculated feature is defined on, as a
	 * {@link FeatureTarget}.
	 * 
	 * @return the feature target.
	 */
	public FeatureTarget getTarget();

}