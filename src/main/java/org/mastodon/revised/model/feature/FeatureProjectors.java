package org.mastodon.revised.model.feature;

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

/**
 * Static utilities to build feature projections for numerical features.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class FeatureProjectors
{

	/**
	 * Returns a view of the specified scalar <code>double</code> feature as a
	 * projection.
	 *
	 * @param feature
	 *            the feature.
	 * @return a feature projection for the specified feature.
	 */
	public static final < O > DoubleFeatureProjection< O > project( final DoublePropertyMap< O > feature )
	{
		return new DoubleFeatureProjection<>( feature );
	}

	/**
	 * Returns a view of the specified scalar <code>int</code> feature as a
	 * projection.
	 *
	 * @param feature
	 *            the feature.
	 * @return a feature projection for the specified feature.
	 */
	public static final < O > IntFeatureProjection< O > project( final IntPropertyMap< O > feature )
	{
		return new IntFeatureProjectionImp<>( feature );
	}

	public static final class DoubleFeatureProjection< O > implements FeatureProjection< O >
	{

		private final DoublePropertyMap< O > pm;

		public DoubleFeatureProjection( final DoublePropertyMap< O > pm )
		{
			this.pm = pm;
		}

		@Override
		public boolean isSet( final O obj )
		{
			return pm.isSet( obj );
		}

		@Override
		public double value( final O obj )
		{
			return pm.getDouble( obj );
		}
	}

	public static final class IntFeatureProjectionImp< O > implements IntFeatureProjection< O >
	{

		private final IntPropertyMap< O > pm;

		public IntFeatureProjectionImp( final IntPropertyMap< O > pm )
		{
			this.pm = pm;
		}

		@Override
		public boolean isSet( final O obj )
		{
			return pm.isSet( obj );
		}

		@Override
		public double value( final O obj )
		{
			return pm.getInt( obj );
		}
	}

	private FeatureProjectors()
	{}
}
