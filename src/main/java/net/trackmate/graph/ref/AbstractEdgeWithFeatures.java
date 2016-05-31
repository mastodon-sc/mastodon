package net.trackmate.graph.ref;

import java.util.Map;

import net.trackmate.collection.UniqueHashcodeArrayMap;
import net.trackmate.graph.EdgeWithFeatures;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.features.unify.Feature;
import net.trackmate.graph.features.unify.Features;
import net.trackmate.pool.MappedElement;

public class AbstractEdgeWithFeatures< E extends AbstractEdgeWithFeatures< E, V, T >, V extends AbstractVertex< V, ?, ? >, T extends MappedElement >
		extends AbstractEdge< E, V, T >
		implements EdgeWithFeatures< E, V >
{

	protected AbstractEdgeWithFeatures( final AbstractEdgePool< E, V, T > pool )
	{
		super( pool );
		featureValues = new UniqueHashcodeArrayMap< >();
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
