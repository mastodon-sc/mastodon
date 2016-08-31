package org.mastodon.graph.ref;

import java.util.Map;

import org.mastodon.collection.UniqueHashcodeArrayMap;
import org.mastodon.graph.EdgeWithFeatures;
import org.mastodon.graph.FeatureValue;
import org.mastodon.graph.features.Feature;
import org.mastodon.graph.features.Features;
import org.mastodon.pool.MappedElement;

public class AbstractEdgeWithFeatures< E extends AbstractEdgeWithFeatures< E, V, T >, V extends AbstractVertex< V, ?, ? >, T extends MappedElement >
		extends AbstractEdge< E, V, T >
		implements EdgeWithFeatures< E, V >
{

	protected AbstractEdgeWithFeatures( final AbstractEdgePool< E, V, T > pool )
	{
		super( pool );
		featureValues = new UniqueHashcodeArrayMap<>();
	}

	Features< E > features;

	private final Map< Feature< ?, E, ? >, FeatureValue< ? > > featureValues;

	@SuppressWarnings( "unchecked" )
	@Override
	public < F extends FeatureValue< ? >, M > F feature( final Feature< M, E, F > feature )
	{
		F fv = ( F ) featureValues.get( feature );
		if ( fv == null )
		{
			fv = feature.createFeatureValue( ( E ) this, features );
			featureValues.put( feature, fv );
		}
		return fv;
	}
}
