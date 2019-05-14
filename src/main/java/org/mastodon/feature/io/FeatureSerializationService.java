package org.mastodon.feature.io;

import org.mastodon.feature.FeatureSpec;
import org.scijava.service.SciJavaService;

public interface FeatureSerializationService extends SciJavaService
{

	/**
	 * Returns a {@link FeatureSerializer} that can de/serialize the feature
	 * with the specified specs.
	 *
	 * @param spec
	 *            the feature specification.
	 * @return a {@link FeatureSerializer}.
	 */
	public FeatureSerializer< ?, ? > getFeatureSerializerFor( FeatureSpec< ?, ? > spec );
}
