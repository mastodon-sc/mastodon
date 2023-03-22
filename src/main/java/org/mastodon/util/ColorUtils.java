package org.mastodon.util;

import java.awt.Color;

import javax.annotation.Nonnull;

import net.imglib2.type.numeric.ARGBType;

/**
 * Class that contains utitility function to deal with {@link Color} objects
 *
 * @author Stefan Hahmann
 */
public class ColorUtils
{

	/**
	 * This method mixes the colors and returns a new color. How much of each
	 * color will be contained in the resulting color can be controlled via the
	 * {@code ratio} parameter.
	 * 
	 * @param color0
	 *            the color that is returned if {@code ratio = 0}.
	 * @param color1
	 *            the color that is returned if {@code ratio = 1}.
	 * @param ratio
	 *            the mixing ratio.
	 *            <ul>
	 *            <li>{@code ratio = 0} results in {@code color0} will
	 *            be returned.</li>
	 *            <li>{@code ratio = 1} results in {@code color1} will
	 *            be returned.</li>
	 *            <li>{@code ratio = 0.5} results in an equal mixture of
	 *            both colors will be returned.</li>
	 *            <li>{@code 0.0 < ratio < 0.5} means more
	 *            {@code color0} will be used during the mixing.</li>
	 *            <li>{@code 0.5 < ratio < 1} means more
	 *            {@code color1} will be used</li>
	 *            </ul>
	 * @return the resulting {@link Color}
	 */
	@Nonnull
	public static Color mixColors( Color color0, Color color1, float ratio )
	{
		return new Color( mixColors( color0.getRGB(), color1.getRGB(), ratio ) );
	}

	/**
	 * This method mixes the colors and returns a new color. How much of each
	 * color will be contained in the resulting color can be controlled via the
	 * {@code ratio} parameter.
	 *
	 * @param color0
	 *            the color that is returned if {@code ratio = 0}.
	 * @param color1
	 *            the color that is returned if {@code ratio = 1}.
	 * @param ratio
	 *            the mixing ratio.
	 *            <ul>
	 *            <li>{@code ratio = 0} results in {@code color0} will
	 *            be returned.</li>
	 *            <li>{@code ratio = 1} results in {@code color1} will
	 *            be returned.</li>
	 *            <li>{@code ratio = 0.5} results in an equal mixture of
	 *            both colors will be returned.</li>
	 *            <li>{@code 0.0 < ratio < 0.5} means more
	 *            {@code color0} will be used during the mixing.</li>
	 *            <li>{@code 0.5 < ratio < 1} means more
	 *            {@code color1} will be used</li>
	 *            </ul>
	 * @return the resulting {@link Color}
	 */
	public static int mixColors( int color0, int color1, float ratio )
	{
		if ( ratio > 1f )
			ratio = 1f;
		else if ( ratio < 0f )
			ratio = 0f;
		float iRatio = 1.0f - ratio;

		int a0 = ARGBType.alpha( color0 );
		int r0 = ARGBType.red( color0 );
		int g0 = ARGBType.green( color0 );
		int b0 = ARGBType.blue( color0 );

		int a1 = ARGBType.alpha( color1 );
		int r1 = ARGBType.red( color1 );
		int g1 = ARGBType.green( color1 );
		int b1 = ARGBType.blue( color1 );

		int a = ( int ) ( ( a0 * iRatio ) + ( a1 * ratio ) );
		int r = ( int ) ( ( r0 * iRatio ) + ( r1 * ratio ) );
		int g = ( int ) ( ( g0 * iRatio ) + ( g1 * ratio ) );
		int b = ( int ) ( ( b0 * iRatio ) + ( b1 * ratio ) );

		return ARGBType.rgba( r, g, b, a );
	}

	/**
	 * Scales the alpha value of the given color by the given factor.
	 *
	 * @param color color encoded as int, see {@link Color#getRGB()}.
	 * @param factor the factor that is used to scale the alpha value.
	 * @return the resulting color.
	 */
	public static int scaleAlpha( int color, float factor )
	{
		if (factor > 1)
			factor = 1;
		else if (factor < 0)
			factor = 0;

		int a = ARGBType.alpha( color );
		a = ( int ) ( a * factor );
		return a << 24 | color & 0x00ffffff;
	}
}
