package org.mastodon.revised.trackscheme;

import org.mastodon.revised.trackscheme.ModelScalarFeaturesProperties.FeatureProperties;

/**
 * Bridges TrackScheme vertices and edges to the featue values of the
 * corresponding model vertex or edge.
 * 
 * @author Jean-Yves Tinevez
 */
public class TrackSchemeFeatures
{

	private final ModelScalarFeaturesProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeFeatures(
			final ModelScalarFeaturesProperties props,
			final TrackSchemeGraph< ?, ? > graph )
	{
		this.props = props;
		this.graph = graph;
	}

	public TrackSchemeVertexFeature getVertexFeature( final String key )
	{
		final FeatureProperties vertexFeature = props.getVertexFeature( key );
		if ( null == vertexFeature )
			throw new IllegalArgumentException( "Feature " + key + " is not registered as a scalar vertex feature." );
		return new TrackSchemeVertexFeature( vertexFeature );
	}

	public TrackSchemeEdgeFeature getEdgeFeature( final String key )
	{
		final FeatureProperties edgeFeature = props.getEdgeFeature( key );
		if ( null == edgeFeature )
			throw new IllegalArgumentException( "Feature " + key + " is not registered as a scalar edge feature." );
		return new TrackSchemeEdgeFeature( edgeFeature );
	}

	public class TrackSchemeVertexFeature
	{

		private final FeatureProperties feature;

		private final TrackSchemeVertex ref;

		public TrackSchemeVertexFeature( final FeatureProperties feature )
		{
			this.feature = feature;
			this.ref = graph.vertexRef();
		}

		public double get( final TrackSchemeVertex v )
		{
			return feature.get( v.getModelVertexId() );
		}

		public double get( final int trackSchemeVertexID )
		{
			return get( graph.getVertexPool().getObject( trackSchemeVertexID, ref ) );
		}

		public boolean isSet( final TrackSchemeVertex v )
		{
			return feature.isSet( v.getModelVertexId() );
		}

		public boolean isSet( final int trackSchemeVertexID )
		{
			return isSet( graph.getVertexPool().getObject( trackSchemeVertexID, ref ) );
		}

		public double[] getMinMax()
		{
			return feature.getMinMax();
		}
	}

	public class TrackSchemeEdgeFeature
	{

		private final FeatureProperties feature;

		private final TrackSchemeEdge ref;

		public TrackSchemeEdgeFeature( final FeatureProperties feature )
		{
			this.feature = feature;
			this.ref = graph.edgeRef();
		}

		public double get( final TrackSchemeEdge e )
		{
			return feature.get( e.getModelEdgeId() );
		}

		public double get( final int trackSchemeEdgeID )
		{
			return get( graph.getEdgePool().getObject( trackSchemeEdgeID, ref ) );
		}

		public boolean isSet( final TrackSchemeEdge e )
		{
			return feature.isSet( e.getModelEdgeId() );
		}

		public boolean isSet( final int trackSchemeEdgeID )
		{
			return isSet( graph.getEdgePool().getObject( trackSchemeEdgeID, ref ) );
		}

		public double[] getMinMax()
		{
			return feature.getMinMax();
		}
	}

}