package net.trackmate.graph.ref;

import java.util.Map;

import net.trackmate.collection.UniqueHashcodeArrayMap;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.VertexWithFeatures;
import net.trackmate.pool.MappedElement;

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
			fv = feature.createFeatureValue( ( V ) this, features );
			featureValues.put( feature, fv );
		}
		return fv;
	}
}
