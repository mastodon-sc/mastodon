package org.mastodon.util;

import java.awt.Color;

import javax.annotation.Nonnull;

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
	 * {@code ratio} param.
	 * 
	 * @param color1
	 *            the first {@link Color}
	 * @param color2
	 *            the second {@link Color}
	 * @param ratio
	 *            the mixing ratio.
	 *            <ul>
	 *            <li>{@code ratio} {@code = 0} results in {@code color1} will
	 *            be returned.</li>
	 *            <li>{@code ratio} {@code = 1} results in {@code color2} will
	 *            be returned.</li>
	 *            <li>{@code ratio} {@code = 0.5} results in an equal mixture of
	 *            both colors will be returned.</li>
	 *            <li>{@code = 0.5 <} {@code ratio} {@code < 1} means more
	 *            {@code color2} will be used</li>
	 *            <li>{@code = 0.0 <} {@code ratio} {@code = <} 0.5 means more
	 *            {@code color1} will be used during the mixing.</li>
	 *            </ul>
	 * @return the resulting {@link Color}
	 */
	@Nonnull
	public static Color getMixedColor( Color color1, Color color2, float ratio )
	{
		if ( ratio > 1f )
			ratio = 1f;
		else if ( ratio < 0f )
			ratio = 0f;
		float iRatio = 1.0f - ratio;

		int i1 = color1.getRGB();
		int i2 = color2.getRGB();

		int a1 = ( i1 >> 24 & 0xff );
		int r1 = ( ( i1 & 0xff0000 ) >> 16 );
		int g1 = ( ( i1 & 0xff00 ) >> 8 );
		int b1 = ( i1 & 0xff );

		int a2 = ( i2 >> 24 & 0xff );
		int r2 = ( ( i2 & 0xff0000 ) >> 16 );
		int g2 = ( ( i2 & 0xff00 ) >> 8 );
		int b2 = ( i2 & 0xff );

		int a = ( int ) ( ( a1 * iRatio ) + ( a2 * ratio ) );
		int r = ( int ) ( ( r1 * iRatio ) + ( r2 * ratio ) );
		int g = ( int ) ( ( g1 * iRatio ) + ( g2 * ratio ) );
		int b = ( int ) ( ( b1 * iRatio ) + ( b2 * ratio ) );

		return new Color( a << 24 | r << 16 | g << 8 | b );
	}
}
