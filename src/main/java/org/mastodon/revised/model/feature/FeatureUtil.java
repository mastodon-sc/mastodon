package org.mastodon.revised.model.feature;

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

/**
 * Static utilities to simplify building features.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class FeatureUtil
{

	/**
	 * Enum of possible feature projection physical units dimensions.
	 */
	public static enum Dimension
	{
		QUALITY,
		INTENSITY,
		INTENSITY_SQUARED,
		POSITION,
		LENGTH,
		VELOCITY,
		TIME,
		ANGLE,
		RATE,
		STRING,
		NONE;
	}

	/**
	 * Returns a string representation of the physical units of a quantity of
	 * the specified dimension, using the specified spatial and time units.
	 *
	 * @param dimension
	 *            the dimension.
	 * @param spaceUnits
	 *            the spatial units string.
	 * @param timeUnits
	 *            the time units strings.
	 * @return the physical units of the quantity.
	 */
	public static final String dimensionToUnits( final Dimension dimension, final String spaceUnits, final String timeUnits )
	{
		switch ( dimension )
		{
		case QUALITY:
			return "Quality";
		case INTENSITY:
			return "Counts";
		case INTENSITY_SQUARED:
			return "Counts²";
		case POSITION:
		case LENGTH:
			return spaceUnits;
		case VELOCITY:
			return spaceUnits + "/" + timeUnits;
		case TIME:
			return timeUnits;
		case ANGLE:
			return "Radians";
		case RATE:
			return  "/" + timeUnits;
		case STRING:
		case NONE:
		default:
			return "";
		}
	}

	/**
	 * Returns a view of the specified scalar <code>double</code> map as a
	 * projection.
	 *
	 * @param map
	 *            the property map.
	 * @param units
	 *            the projection physical units.
	 * @return a feature projection for the specified map.
	 */
	public static final < O > DoubleFeatureProjection< O > project( final DoublePropertyMap< O > map, final String units )
	{
		return new DoubleFeatureProjection<>( map, units );
	}

	/**
	 * Returns a view of the specified scalar <code>int</code> map as a
	 * projection.
	 *
	 * @param map
	 *            the property map.
	 * @param units
	 *            the projection physical units.
	 * @return a feature projection for the specified map.
	 */
	public static final < O > IntFeatureProjection< O > project( final IntPropertyMap< O > map, final String units )
	{
		return new IntFeatureProjectionImp<>( map, units );
	}

	public static final class DoubleFeatureProjection< O > implements FeatureProjection< O >
	{

		private final DoublePropertyMap< O > pm;

		private final String units;

		public DoubleFeatureProjection( final DoublePropertyMap< O > pm, final String units )
		{
			this.pm = pm;
			this.units = units;
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

		@Override
		public String units()
		{
			return units;
		}
	}

	public static final class IntFeatureProjectionImp< O > implements IntFeatureProjection< O >
	{

		private final IntPropertyMap< O > pm;

		private final String units;

		public IntFeatureProjectionImp( final IntPropertyMap< O > pm, final String units )
		{
			this.pm = pm;
			this.units = units;
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

		@Override
		public String units()
		{
			return units;
		}
	}

	private FeatureUtil()
	{}
}
