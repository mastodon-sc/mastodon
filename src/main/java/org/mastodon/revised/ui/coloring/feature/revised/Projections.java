package org.mastodon.revised.ui.coloring.feature.revised;

import org.mastodon.feature.FeatureProjection;

public interface Projections< T >
{
	public FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id );
}
