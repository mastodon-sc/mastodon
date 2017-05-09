package org.mastodon.revised.model.feature;

import java.util.Set;

import org.mastodon.revised.model.AbstractModel;
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
	 * Returns the set of available feature computers for vertex features.
	 * 
	 * @return the available vertex feature computers.
	 */
	public Set< String > getAvailableVertexFeatureComputers();

	/**
	 * Returns the set of available feature computers for edge features.
	 * 
	 * @return the available edge feature computers.
	 */
	public Set< String > getAvailableEdgeFeatureComputers();

	/**
	 * Executes feature computation for the specified computers on the specified
	 * model.
	 * 
	 * @param model
	 *            the model to compute features on.
	 * @param computerNames
	 *            what feature computers to compute.
	 * @return <code>true</code> if computation terminated successfully.
	 */
	public boolean compute( AM model, Set< String > computerNames );

}