/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
	 * @see <a href="https://imagej.net/plugins/glasbey">Implementation of Glasbey LUT</a>
	 */
	public static final Color[] GLASBEY = new Color[] {
			new Color( 255, 255, 255 ),
			new Color( 20, 20, 255 ),
			new Color( 255, 20, 20 ),
			new Color( 20, 255, 20 ),
			new Color( 50, 50, 101 ),
			new Color( 255, 0, 182 ),
			new Color( 50, 133, 50 ),
			new Color( 255, 211, 0 ),
			new Color( 0, 159, 255 ),
			new Color( 154, 77, 66 ),
			new Color( 0, 255, 190 ),
			new Color( 120, 63, 193 ),
			new Color( 31, 150, 152 ),
			new Color( 255, 172, 253 ),
			new Color( 177, 204, 113 ),
			new Color( 241, 8, 92 ),
			new Color( 254, 143, 66 ),
			new Color( 221, 0, 255 ),
			new Color( 132, 126, 1 ),
			new Color( 114, 0, 85 ),
			new Color( 118, 108, 149 ),
			new Color( 52, 173, 36 ),
			new Color( 200, 255, 0 ),
			new Color( 136, 108, 0 ),
			new Color( 255, 183, 159 ),
			new Color( 133, 133, 103 ),
			new Color( 161, 3, 0 ),
			new Color( 20, 249, 255 ),
			new Color( 100, 71, 158 ),
			new Color( 220, 94, 147 ),
			new Color( 147, 212, 255 ),
			new Color( 0, 76, 255 ),
			new Color( 0, 166, 180 ),
			new Color( 57, 167, 106 ),
			new Color( 238, 112, 254 ),
			new Color( 0, 0, 100 ),
			new Color( 171, 245, 204 ),
			new Color( 161, 146, 255 ),
			new Color( 164, 255, 115 ),
			new Color( 255, 206, 113 ),
			new Color( 171, 0, 121 ),
			new Color( 212, 173, 197 ),
			new Color( 251, 118, 111 ),
			new Color( 171, 188, 0 ),
			new Color( 117, 0, 215 ),
			new Color( 166, 0, 154 ),
			new Color( 0, 115, 254 ),
			new Color( 165, 93, 174 ),
			new Color( 98, 132, 2 ),
			new Color( 0, 121, 168 ),
			new Color( 0, 255, 131 ),
			new Color( 186, 53, 10 ),
			new Color( 159, 0, 63 ),
			new Color( 166, 145, 166 ),
			new Color( 255, 242, 187 ),
			new Color( 0, 93, 67 ),
			new Color( 252, 255, 124 ),
			new Color( 159, 191, 186 ),
			new Color( 167, 84, 19 ),
			new Color( 74, 39, 108 ),
			new Color( 0, 16, 166 ),
			new Color( 145, 78, 109 ),
			new Color( 207, 149, 0 ),
			new Color( 195, 187, 255 ),
			new Color( 253, 68, 64 ),
			new Color( 66, 78, 32 ),
			new Color( 106, 1, 0 ),
			new Color( 181, 131, 84 ),
			new Color( 132, 233, 147 ),
			new Color( 96, 217, 0 ),
			new Color( 255, 111, 211 ),
			new Color( 102, 75, 63 ),
			new Color( 254, 100, 0 ),
			new Color( 228, 3, 127 ),
			new Color( 17, 199, 174 ),
			new Color( 210, 129, 139 ),
			new Color( 91, 118, 124 ),
			new Color( 32, 59, 106 ),
			new Color( 180, 84, 255 ),
			new Color( 226, 8, 210 ),
			new Color( 0, 101, 120 ),
			new Color( 93, 132, 68 ),
			new Color( 166, 250, 255 ),
			new Color( 97, 123, 201 ),
			new Color( 98, 0, 122 ),
			new Color( 126, 190, 58 ),
			new Color( 0, 60, 183 ),
			new Color( 255, 253, 0 ),
			new Color( 7, 197, 226 ),
			new Color( 180, 167, 57 ),
			new Color( 148, 186, 138 ),
			new Color( 204, 187, 160 ),
			new Color( 105, 0, 99 ),
			new Color( 0, 140, 101 ),
			new Color( 150, 122, 129 ),
			new Color( 39, 136, 38 ),
			new Color( 206, 130, 180 ),
			new Color( 150, 164, 196 ),
			new Color( 180, 32, 128 ),
			new Color( 110, 86, 180 ),
			new Color( 147, 0, 185 ),
			new Color( 199, 48, 61 ),
			new Color( 115, 102, 255 ),
			new Color( 15, 187, 253 ),
			new Color( 172, 164, 100 ),
			new Color( 182, 117, 250 ),
			new Color( 216, 220, 254 ),
			new Color( 87, 141, 113 ),
			new Color( 216, 85, 34 ),
			new Color( 0, 196, 103 ),
			new Color( 243, 165, 105 ),
			new Color( 216, 255, 182 ),
			new Color( 1, 24, 219 ),
			new Color( 52, 66, 54 ),
			new Color( 255, 154, 0 ),
			new Color( 87, 95, 1 ),
			new Color( 198, 241, 79 ),
			new Color( 255, 95, 133 ),
			new Color( 123, 172, 240 ),
			new Color( 120, 100, 49 ),
			new Color( 162, 133, 204 ),
			new Color( 105, 255, 220 ),
			new Color( 198, 82, 100 ),
			new Color( 121, 26, 64 ),
			new Color( 0, 238, 70 ),
			new Color( 231, 207, 69 ),
			new Color( 217, 128, 233 ),
			new Color( 255, 211, 209 ),
			new Color( 209, 255, 141 ),
			new Color( 200, 10, 163 ),
			new Color( 87, 163, 193 ),
			new Color( 211, 231, 201 ),
			new Color( 203, 111, 79 ),
			new Color( 162, 124, 0 ),
			new Color( 0, 117, 223 ),
			new Color( 112, 176, 88 ),
			new Color( 209, 24, 0 ),
			new Color( 10, 130, 107 ),
			new Color( 105, 200, 197 ),
			new Color( 255, 203, 255 ),
			new Color( 233, 194, 137 ),
			new Color( 191, 129, 46 ),
			new Color( 69, 42, 145 ),
			new Color( 171, 76, 194 ),
			new Color( 14, 117, 61 ),
			new Color( 0, 130, 125 ),
			new Color( 118, 73, 127 ),
			new Color( 255, 169, 200 ),
			new Color( 94, 55, 217 ),
			new Color( 238, 230, 138 ),
			new Color( 159, 54, 33 ),
			new Color( 80, 0, 148 ),
			new Color( 189, 144, 128 ),
			new Color( 0, 109, 126 ),
			new Color( 88, 223, 96 ),
			new Color( 71, 80, 103 ),
			new Color( 1, 93, 159 ),
			new Color( 99, 48, 60 ),
			new Color( 2, 206, 148 ),
			new Color( 139, 83, 37 ),
			new Color( 171, 0, 255 ),
			new Color( 141, 42, 135 ),
			new Color( 85, 83, 148 ),
			new Color( 150, 255, 0 ),
			new Color( 0, 152, 123 ),
			new Color( 255, 138, 203 ),
			new Color( 222, 69, 200 ),
			new Color( 107, 109, 230 ),
			new Color( 30, 0, 68 ),
			new Color( 173, 76, 138 ),
			new Color( 255, 134, 161 ),
			new Color( 0, 35, 60 ),
			new Color( 138, 205, 0 ),
			new Color( 111, 202, 157 ),
			new Color( 225, 75, 253 ),
			new Color( 255, 176, 77 ),
			new Color( 229, 232, 57 ),
			new Color( 114, 16, 255 ),
			new Color( 111, 82, 101 ),
			new Color( 134, 137, 48 ),
			new Color( 99, 38, 80 ),
			new Color( 105, 38, 32 ),
			new Color( 200, 110, 0 ),
			new Color( 209, 164, 255 ),
			new Color( 198, 210, 86 ),
			new Color( 79, 103, 77 ),
			new Color( 174, 165, 166 ),
			new Color( 170, 45, 101 ),
			new Color( 199, 81, 175 ),
			new Color( 255, 89, 172 ),
			new Color( 146, 102, 78 ),
			new Color( 102, 134, 184 ),
			new Color( 111, 152, 255 ),
			new Color( 92, 255, 159 ),
			new Color( 172, 137, 178 ),
			new Color( 210, 34, 98 ),
			new Color( 199, 207, 147 ),
			new Color( 255, 185, 30 ),
			new Color( 250, 148, 141 ),
			new Color( 49, 34, 78 ),
			new Color( 254, 81, 97 ),
			new Color( 254, 141, 100 ),
			new Color( 68, 54, 23 ),
			new Color( 201, 162, 84 ),
			new Color( 199, 232, 240 ),
			new Color( 68, 152, 0 ),
			new Color( 147, 172, 58 ),
			new Color( 22, 75, 28 ),
			new Color( 8, 84, 121 ),
			new Color( 116, 45, 0 ),
			new Color( 104, 60, 255 ),
			new Color( 64, 41, 38 ),
			new Color( 164, 113, 215 ),
			new Color( 207, 0, 155 ),
			new Color( 118, 1, 35 ),
			new Color( 83, 0, 88 ),
			new Color( 0, 82, 232 ),
			new Color( 43, 92, 87 ),
			new Color( 160, 217, 146 ),
			new Color( 176, 26, 229 ),
			new Color( 29, 3, 36 ),
			new Color( 122, 58, 159 ),
			new Color( 214, 209, 207 ),
			new Color( 160, 100, 105 ),
			new Color( 106, 157, 160 ),
			new Color( 153, 219, 113 ),
			new Color( 192, 56, 207 ),
			new Color( 125, 255, 89 ),
			new Color( 149, 0, 34 ),
			new Color( 213, 162, 223 ),
			new Color( 22, 131, 204 ),
			new Color( 166, 249, 69 ),
			new Color( 109, 105, 97 ),
			new Color( 86, 188, 78 ),
			new Color( 255, 109, 81 ),
			new Color( 255, 3, 248 ),
			new Color( 255, 0, 73 ),
			new Color( 202, 0, 35 ),
			new Color( 67, 109, 18 ),
			new Color( 234, 170, 173 ),
			new Color( 191, 165, 0 ),
			new Color( 38, 44, 51 ),
			new Color( 85, 185, 2 ),
			new Color( 121, 182, 158 ),
			new Color( 254, 236, 212 ),
			new Color( 139, 165, 89 ),
			new Color( 141, 254, 193 ),
			new Color( 0, 60, 43 ),
			new Color( 63, 17, 40 ),
			new Color( 255, 221, 246 ),
			new Color( 17, 26, 146 ),
			new Color( 154, 66, 84 ),
			new Color( 149, 157, 238 ),
			new Color( 126, 130, 72 ),
			new Color( 58, 6, 101 ),
			new Color( 189, 117, 101 )
	};

	private ColorUtils()
	{
		// prevent from instantiation
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
	@Nonnull
	public static Color mixColors( final Color color0, final Color color1, final float ratio )
	{
		return new Color( mixColors( color0.getRGB(), color1.getRGB(), ratio ), true );
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
	public static int mixColors( final int color0, final int color1, float ratio )
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
	 * Scales the alpha value of the given color by the given factor.<p>
	 * When using the resulting value to create a {@link Color} object,
	 * the {@link Color} object should be created with the {@code hasAlpha}
	 * parameter set to {@code true} to indicate that the alpha value is valid.
	 * @see Color#Color(int, boolean)
	 *
	 * @param color color encoded as int, see {@link Color#getRGB()}.
	 * @param factor the factor that is used to scale the alpha value.
	 * @return the resulting color.
	 */
	public static int scaleAlpha( final int color, float factor )
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
