/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.feature;

/**
 * The physical dimensions of a feature projection.
 *
 * @author Jean-Yves Tinevez
 */
public enum Dimension
{

	/**
	 * Dimensionless quantities, like frame position, number of things, ...
	 */
	NONE("None"),
	/**
	 * Dimension for detector quality.
	 */
	QUALITY("Quality"),
	/**
	 * Dimension for costs return by cost functions.
	 */
	COST( "Cost" ),
	/**
	 * In units of pixel values.
	 */
	INTENSITY("Intensity"),
	/**
	 * In units of pixel values squared.
	 */
	INTENSITY_SQUARED("Intensity²"),
	/**
	 * In units of position. Different from {@link #LENGTH} so that for objects
	 * with small lengths at large positions, quantitites are plotted
	 * separately.
	 */
	POSITION("Position"),
	/**
	 * In units of velocity, like speed of objects.
	 */
	VELOCITY("Velocity"),
	/**
	 * In units of length.
	 */
	LENGTH("Length"), // we separate length and position.
	/**
	 * Units of time.
	 */
	TIME("Time"),
	/**
	 * Units of angles. We alway pick radians for units.
	 */
	ANGLE("Angle"),
	/**
	 * Count per time units.
	 */
	RATE("Rate"), // count per frames.
	/**
	 * Angle per time units..
	 */
	ANGLE_RATE( "Angle rate" ),
	/**
	 * Non numerical quantities.
	 */
	STRING("NA"); // for non-numeric features

	public static final String COUNTS_UNITS = "Counts";

	public static final String QUALITY_UNITS = "";

	public static final String COST_UNITS = "";

	public static final String COUNTS_SQUARED_UNITS = "Counts²";

	public static final String RADIANS_UNITS = "Radians";

	public static final String NONE_UNITS = "";

	private final String str;

	private Dimension(final String str)
	{
		this.str = str;
	}

	/**
	 * Returns a String unit for this dimension. When suitable, the unit is
	 * taken from the settings field, which contains the spatial and time units.
	 * Otherwise, default units are used.
	 *
	 * @param spaceUnits
	 *            the space units.
	 * @param timeUnits
	 *            the time units.
	 * @return a unit string.
	 */
	public String getUnits( final String spaceUnits, final String timeUnits )
	{
		switch ( this )
		{
		case ANGLE:
			return RADIANS_UNITS;
		case INTENSITY:
			return COUNTS_UNITS;
		case INTENSITY_SQUARED:
			return COUNTS_SQUARED_UNITS;
		case NONE:
			return NONE_UNITS;
		case POSITION:
		case LENGTH:
			return spaceUnits;
		case QUALITY:
			return QUALITY_UNITS;
		case COST:
			return COST_UNITS;
		case TIME:
			return timeUnits;
		case VELOCITY:
			return spaceUnits + "/" + timeUnits;
		case RATE:
			return "/" + timeUnits;
		default:
		case STRING:
			return null;
		}
	}

	@Override
	public String toString()
	{
		return str;
	}

}
