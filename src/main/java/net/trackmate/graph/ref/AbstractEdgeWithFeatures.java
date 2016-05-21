package net.trackmate.graph.ref;

import java.util.Map;

import net.trackmate.collection.UniqueHashcodeArrayMap;
import net.trackmate.graph.EdgeFeature;
import net.trackmate.graph.EdgeWithFeatures;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.GraphFeatures;
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

	GraphFeatures< ?, E > features;

	private final Map< EdgeFeature< ?, E, ? >, FeatureValue< ? > > featureValues;

	@SuppressWarnings( "unchecked" )
	@Override
	public < F extends FeatureValue< ? >, M > F feature( final EdgeFeature< M, E, F > feature )
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
