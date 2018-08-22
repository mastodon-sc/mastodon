package org.mastodon.revised.model.feature;

import java.util.Collection;
import java.util.Set;

import org.mastodon.revised.ui.ProgressListener;
import org.scijava.service.SciJavaService;

/**
 * Service that can discover feature computers and execute computation.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <M>
 *            the type of the model on which features are computed.
 * @param <FC>
 *            the type of feature computers used in this service.
 */
public interface FeatureComputerService< M, FC extends FeatureComputer< M > > extends SciJavaService
{

	/**
	 * Returns the collection of available computers discovered by this service.
	 *
	 * @return the available feature computers.
	 */
	public Collection< FC > getFeatureComputers();

	/**
	 * Executes feature computation for the specified computers on the specified
	 * model. The dependencies of the computers will be managed automatically
	 * provided that all dependencies have been discovered.
	 *
	 * @param model
	 *            the model to compute features on.
	 * @param featureModel
	 *            the feature model to stores feature values in.
	 * @param selectedComputers
	 *            what computers to run. The computers in the specified set must
	 *            have been discovered by this instance.
	 * @param progressListener
	 *            a progress listener, used to report calculation progress.
	 * @return <code>true</code> if computation terminated successfully.
	 */
	public boolean compute( M model, FeatureModel featureModel, Set< FC > selectedComputers, ProgressListener progressListener );

}
