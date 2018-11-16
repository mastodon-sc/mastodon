package org.mastodon.revised.ui.coloring.feature;

import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;

/**
 * Identifies a particular projection of a particular feature.
 */
public class FeatureAndProjection
{
	private final FeatureSpec< ?, ? > feature;

	private final FeatureProjectionKey projection;

	public FeatureAndProjection( final FeatureSpec< ?, ? > feature, final FeatureProjectionKey projection )
	{
		this.feature = feature;
		this.projection = projection;
	}

	@Override
	public String toString()
	{
		return "FeatureAndProjection{" +
				"feature=" + feature +
				", projection=" + projection +
				'}';
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		final FeatureAndProjection that = ( FeatureAndProjection ) o;

		if ( !feature.equals( that.feature ) )
			return false;
		return projection.equals( that.projection );
	}

	@Override
	public int hashCode()
	{
		int result = feature.hashCode();
		result = 31 * result + projection.hashCode();
		return result;
	}
}
