package org.mastodon.revised.ui.coloring.feature;

import gnu.trove.list.TIntList;
import java.util.Collection;
import org.mastodon.feature.Multiplicity;

public interface AvailableFeatureProjections
{
	public TIntList getSourceIndices();

	public Collection< String > featureKeys( TargetType targetType );

	public Collection< String > projectionKeys( TargetType targetType, String featureKey );

	public Multiplicity multiplicity( TargetType targetType, String featureKey );
}
