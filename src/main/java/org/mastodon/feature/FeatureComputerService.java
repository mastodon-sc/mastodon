/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.feature;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.scijava.Cancelable;
import org.scijava.service.SciJavaService;

public interface FeatureComputerService extends Cancelable, SciJavaService
{

	/**
	 * Get {@code FeatureSpec}s for all features computable by this service.
	 *
	 * @return {@code FeatureSpec}s for all features computable by this service.
	 */
	public Set< FeatureSpec< ?, ? > > getFeatureSpecs();

	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( boolean forceComputeAll,
			Collection< FeatureSpec< ?, ? > > featureKeys );

	public default Map< FeatureSpec< ?, ? >, Feature< ? > >
			compute( final Collection< FeatureSpec< ?, ? > > featureKeys )
	{
		return compute( false, featureKeys );
	}

	public default Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final FeatureSpec< ?, ? >... keys )
	{
		return compute( false, Arrays.asList( keys ) );
	}

	public default Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final boolean forceComputeAll,
			final FeatureSpec< ?, ? >... keys )
	{
		return compute( forceComputeAll, Arrays.asList( keys ) );
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
