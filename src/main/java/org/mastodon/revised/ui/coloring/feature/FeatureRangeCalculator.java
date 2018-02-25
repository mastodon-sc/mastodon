package org.mastodon.revised.ui.coloring.feature;

import java.util.DoubleSummaryStatistics;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureProjection;

public class FeatureRangeCalculator< V extends Vertex< E >, E extends Edge< V > >
{

	private final ReadOnlyGraph< V, E > graph;

	private final FeatureModel featureModel;

	public FeatureRangeCalculator( final ReadOnlyGraph< V, E > graph, final FeatureModel featureModel )
	{
		this.graph = graph;
		this.featureModel = featureModel;
	}

	/**
	 * Returns the current min & max for the specified feature.
	 *
	 * @param clazz
	 *            the target class for which the feature is defined. Must be one of the graph vertex class or edge class.
	 * @param featureKey
	 *            the feature key.
	 * @param projectionKey
	 *            the projection key.
	 * @return the min & max as a new <code>double[]</code> 2-elements array, or
	 *         <code>null</code> if the calculation could not proceed because
	 *         the feature was not found the specified class, the projection
	 *         does not exist for the feature or there are no objects to compute
	 *         feature on.
	 */
	public double[] computeMinMax( final Class< ? > clazz, final String featureKey, final String projectionKey )
	{
		final Feature< ?, ? > feature = featureModel.getFeature( featureKey );
		if ( feature.getTargetClass() != clazz )
			return null;

		final FeatureProjection< ? > projection = feature.getProjections().get( projectionKey );
		if ( null == projection )
			return null;

		if ( clazz == graph.edgeRef().getClass() )
		{
			if ( graph.edges().isEmpty() )
				return null;

			@SuppressWarnings( "unchecked" )
			final FeatureProjection< E > fp = ( FeatureProjection< E > ) projection;
			final DoubleSummaryStatistics stats = graph.edges().stream()
					.filter( e -> fp.isSet( e ) )
					.mapToDouble( e -> fp.value( e ) )
					.summaryStatistics();
			return new double[] { stats.getMin(), stats.getMax() };
		}
		else if ( clazz == graph.vertexRef().getClass() )
		{
			if ( graph.vertices().isEmpty() )
				return null;

			@SuppressWarnings( "unchecked" )
			final FeatureProjection< V > fp = ( FeatureProjection< V > ) projection;
			final DoubleSummaryStatistics stats = graph.vertices().stream()
					.filter( e -> fp.isSet( e ) )
					.mapToDouble( e -> fp.value( e ) )
					.summaryStatistics();
			return new double[] { stats.getMin(), stats.getMax() };
		}

		return null;
	}
}
