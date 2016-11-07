/**
 *
 */
package org.mastodon.revised.trackscheme.display.style.dummygraph;

import org.mastodon.revised.trackscheme.ModelScalarFeaturesProperties;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle;

/**
 * A dummy implementation of {@link ModelScalarFeaturesProperties} that returns
 * random values between a min and max given by a {@link TrackSchemeStyle}.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class DummyScalarFeaturesProperties implements ModelScalarFeaturesProperties
{

	public class DummyEdgeProperties implements FeatureProperties
	{

		@Override
		public double get( final int id )
		{
			final double r = ( id % 20d ) / 20d;
			final double min = style.minEdgeColorRange;
			final double max = style.maxEdgeColorRange;
			return min + r * ( max - min );
		}

		@Override
		public boolean isSet( final int id )
		{
			return ( ( id % 20d ) / 20d ) > 0.05;
		}

		@Override
		public double[] getMinMax()
		{
			return null;
		}
	}

	public class DummyVertexProperties implements FeatureProperties
	{

		@Override
		public double get( final int id )
		{
			final double r = ( id % 20d ) / 20d;
			final double min = style.minVertexColorRange;
			final double max = style.maxVertexColorRange;
			return min + r * ( max - min );
		}

		@Override
		public boolean isSet( final int id )
		{
			return ( ( id % 20d ) / 20d ) > 0.05;
		}

		@Override
		public double[] getMinMax()
		{
			return null;
		}

	}

	private TrackSchemeStyle style;

	public void setStyle( final TrackSchemeStyle style )
	{
		this.style = style;
	}

	@Override
	public FeatureProperties getVertexFeature( final String key )
	{
		return new DummyVertexProperties();
	}

	@Override
	public FeatureProperties getEdgeFeature( final String key )
	{
		return new DummyEdgeProperties();
	}

}