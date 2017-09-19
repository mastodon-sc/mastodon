package org.mastodon.revised.model.feature;

import java.util.Collection;
import java.util.Set;

import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.ui.ProgressListener;
import org.scijava.service.SciJavaService;

/**
 * Service that can discover feature computers and execute computation.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <AM>
 *            the type of the model on which features are computed.
 */
public interface FeatureComputerService< AM extends AbstractModel< ?, ?, ? > > extends SciJavaService
{

	/**
	 * Returns the collection of available computers for features defined on the
	 * objects with the specified class.
	 *
	 * @param target
	 *            the class of the objects the features are defined on.
	 * @return the available feature computers.
	 */
	public Collection< FeatureComputer< AM > > getFeatureComputers();

	/**
	 * Executes feature computation for the specified computers on the specified
	 * model.
	 *
	 * @param model
	 *            the model to compute features on.
	 * @param selectedComputers
	 *            what computers to run.
	 * @param progressListener
	 *            a progress listener, used to report calculation progress.
	 * @return <code>true</code> if computation terminated successfully.
	 */
	public boolean compute( AM model, Set< FeatureComputer< AM > > selectedComputers, ProgressListener progressListener );

}
