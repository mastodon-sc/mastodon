package net.trackmate.graph;

import java.util.Map;

import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.util.UniqueHashcodeArrayMap;

/**
 * TODO: javadoc
 *
 * @param <V>
 * @param <E>
 * @param <T>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AbstractVertexWithFeatures< V extends AbstractVertexWithFeatures< V, E, T >, E extends AbstractEdge< E, ?, ? >, T extends MappedElement >
		extends AbstractVertex< V, E, T >
		implements VertexWithFeatures< V, E >
{
	protected AbstractVertexWithFeatures( final AbstractVertexPool< V, ?, T > pool )
	{
		super( pool );
		featureValues = new UniqueHashcodeArrayMap<>();
	}

	GraphFeatures< V, ? > features;

	private final Map< VertexFeature< ?, V, ? >, FeatureValue< ? > > featureValues;

	@SuppressWarnings( "unchecked" )
	@Override
	public < F extends FeatureValue< ? >, M > F feature( final VertexFeature< M, V, F > feature )
	{
		F fv = ( F ) featureValues.get( feature );
		if ( fv == null )
		{
			fv = feature.createFeatureValue( features.getVertexFeature( feature ), ( V ) this );
			featureValues.put( feature, fv );
		}
		return fv;
	}
}
