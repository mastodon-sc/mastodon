package org.mastodon.revised.ui.coloring.feature;

import org.mastodon.feature.FeatureProjection;

public interface Projections
{
	public FeatureProjection< ? > getFeatureProjection( final FeatureProjectionId id );

	public < T > FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id, Class< T > target );
}
