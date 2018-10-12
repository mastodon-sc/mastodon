package org.mastodon.feature;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.scijava.Cancelable;
import org.scijava.service.Service;

public interface FeatureComputerService extends Cancelable, Service
{

	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( Collection< String > featureKeys );

	public default Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final String... keys )
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
}
