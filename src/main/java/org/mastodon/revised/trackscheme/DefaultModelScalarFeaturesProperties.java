package org.mastodon.revised.trackscheme;

import org.mastodon.features.DoubleFeature;
import org.mastodon.features.Feature;
import org.mastodon.features.FeatureRegistry;
import org.mastodon.features.IntFeature;
import org.mastodon.graph.EdgeWithFeatures;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.VertexWithFeatures;

/**
 * A default implementation of {@link ModelScalarFeaturesProperties}, that
 * relies on the {@link VertexWithFeatures} and {@link EdgeWithFeatures}
 * interfaces for feature value access.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the vertex type of the model graph. Must implement
 *            {@link VertexWithFeatures}.
 * @param <E>
 *            the edge type of the model graph. Must implement
 *            {@link EdgeWithFeatures}.
 */
public class DefaultModelScalarFeaturesProperties< V extends VertexWithFeatures< V, E >, E extends EdgeWithFeatures< E, V > >
		implements ModelScalarFeaturesProperties
{

	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	public DefaultModelScalarFeaturesProperties(
			final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap )
	{
		this.graph = graph;
		this.idmap = idmap;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public FeatureProperties getVertexFeature( final String key )
	{
		final Feature< ?, ?, ? > feature = FeatureRegistry.getFeature( key );
		if ( feature instanceof DoubleFeature )
			return new VertexDoubleFeatureProperties( ( DoubleFeature< V > ) feature );
		else if ( feature instanceof IntFeature )
			return new VertexIntFeatureProperties( ( IntFeature< V > ) feature );
		return null;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public FeatureProperties getEdgeFeature( final String key )
	{
		final Feature< ?, ?, ? > feature = FeatureRegistry.getFeature( key );
		if ( feature instanceof DoubleFeature )
			return new EdgeDoubleFeatureProperties( ( DoubleFeature< E > ) feature );
		else if ( feature instanceof IntFeature )
			return new EdgeIntFeatureProperties( ( IntFeature< E > ) feature );
		return null;
	}

	/*
	 * INNER CLASSES.
	 */

	private final class VertexDoubleFeatureProperties implements FeatureProperties
	{

		private final DoubleFeature< V > feature;

		private final V ref;

		public VertexDoubleFeatureProperties( final DoubleFeature< V > feature )
		{
			this.feature = feature;
			this.ref = graph.vertexRef();
		}

		@Override
		public double get( final int id )
		{
			return idmap.getVertex( id, ref ).feature( feature ).getDouble();
		}

		@Override
		public boolean isSet( final int id )
		{
			return idmap.getVertex( id, ref ).feature( feature ).isSet();
		}

		@Override
		public double[] getMinMax()
		{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for ( final V v : graph.vertices() )
			{
				final double val = v.feature( feature ).getDouble();
				if ( val > max )
					max = val;
				if ( val < min )
					min = val;
			}
			return new double[] { min, max };
		}
	}

	private final class EdgeDoubleFeatureProperties implements FeatureProperties
	{

		private final DoubleFeature< E > feature;

		private final E ref;

		public EdgeDoubleFeatureProperties( final DoubleFeature< E > feature )
		{
			this.feature = feature;
			this.ref = graph.edgeRef();
		}

		@Override
		public double get( final int id )
		{
			return idmap.getEdge( id, ref ).feature( feature ).getDouble();
		}

		@Override
		public boolean isSet( final int id )
		{
			return idmap.getEdge( id, ref ).feature( feature ).isSet();
		}

		@Override
		public double[] getMinMax()
		{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for ( final E e : graph.edges() )
			{
				final double val = e.feature( feature ).getDouble();
				if ( val > max )
					max = val;
				if ( val < min )
					min = val;
			}
			return new double[] { min, max };
		}
	}

	private final class VertexIntFeatureProperties implements FeatureProperties
	{

		private final IntFeature< V > feature;

		private final V ref;

		public VertexIntFeatureProperties( final IntFeature< V > feature )
		{
			this.feature = feature;
			this.ref = graph.vertexRef();
		}

		@Override
		public double get( final int id )
		{
			return idmap.getVertex( id, ref ).feature( feature ).getInt();
		}

		@Override
		public boolean isSet( final int id )
		{
			return idmap.getVertex( id, ref ).feature( feature ).isSet();
		}

		@Override
		public double[] getMinMax()
		{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for ( final V v : graph.vertices() )
			{
				final double val = v.feature( feature ).getInt();
				if ( val > max )
					max = val;
				if ( val < min )
					min = val;
			}
			return new double[] { min, max };
		}

	}

	private final class EdgeIntFeatureProperties implements FeatureProperties
	{

		private final IntFeature< E > feature;

		private final E ref;

		public EdgeIntFeatureProperties( final IntFeature< E > feature )
		{
			this.feature = feature;
			this.ref = graph.edgeRef();
		}

		@Override
		public double get( final int id )
		{
			return idmap.getEdge( id, ref ).feature( feature ).getInt();
		}

		@Override
		public boolean isSet( final int id )
		{
			return idmap.getEdge( id, ref ).feature( feature ).isSet();
		}

		@Override
		public double[] getMinMax()
		{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for ( final E e : graph.edges() )
			{
				final double val = e.feature( feature ).getInt();
				if ( val > max )
					max = val;
				if ( val < min )
					min = val;
			}
			return new double[] { min, max };
		}
	}
}