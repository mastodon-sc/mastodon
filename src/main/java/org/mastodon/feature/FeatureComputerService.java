package org.mastodon.feature;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.scijava.Cancelable;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

public interface FeatureComputerService extends Cancelable, SciJavaService
{

	/**
	 * Get {@code FeatureSpec}s for all features computable by this service.
	 *
	 * @return {@code FeatureSpec}s for all features computable by this service.
	 */
	public Set< FeatureSpec< ?, ? > > getFeatureSpecs();

	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( Collection< FeatureSpec< ?, ? > > featureKeys );

	public default Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final FeatureSpec< ?, ? >... keys )
	{
		return compute( Arrays.asList( keys ) );
	}

	/**
	 * Returns a {@link FeatureComputer} discovered by this service that can
	 * compute the feature with the given specifications. Returns
	 * <code>null</code> if a feature computer was not discovered by this
	 * service.
	 *
	 * @param spec
	 *            the specification of the feature to compute.
	 * @return a feature computer.
	 */
	public FeatureComputer getFeatureComputerFor( FeatureSpec< ?, ? > spec );

	/**
	 * Returns the list of dependencies identified for the feature with the
	 * given specification.
	 * 
	 * @param spec
	 *            the specification of the feature to query.
	 * @return the dependencies.
	 */
	public Collection< FeatureSpec< ?, ? > > getDependencies( FeatureSpec< ?, ? > spec );
}
