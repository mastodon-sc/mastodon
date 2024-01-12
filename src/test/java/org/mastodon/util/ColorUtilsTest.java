/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.Test;

public class ColorUtilsTest
{

	@Test
	public void testGetMixedColor()
	{
		Color mixedColor = ColorUtils.mixColors( Color.BLACK, Color.WHITE, 0.5f );
		assertEquals( new Color( 127, 127, 127 ), mixedColor );
		mixedColor = ColorUtils.mixColors( Color.BLACK, Color.WHITE, 0.75f );
		assertEquals( new Color( 191, 191, 191 ), mixedColor );
		mixedColor = ColorUtils.mixColors( Color.BLACK, Color.WHITE, 0.25f );
		assertEquals( new Color( 63, 63, 63 ), mixedColor );
		mixedColor = ColorUtils.mixColors( Color.BLACK, Color.WHITE, 1.0f );
		assertEquals( Color.WHITE, mixedColor );
		mixedColor = ColorUtils.mixColors( Color.BLACK, Color.WHITE, 123456789.0f );
		assertEquals( Color.WHITE, mixedColor );
		mixedColor = ColorUtils.mixColors( Color.BLACK, Color.WHITE, 0.0f );
		assertEquals( Color.BLACK, mixedColor );
		mixedColor = ColorUtils.mixColors( Color.BLACK, Color.WHITE, -123456789.0f );
		assertEquals( Color.BLACK, mixedColor );
		mixedColor = ColorUtils.mixColors( new Color( 0, 0, 0, 0), Color.WHITE, 0.5f );
		assertEquals( 127, mixedColor.getAlpha() );
	}

	@Test
	public void testScaleAlpha()
	{
		assertEquals( 0x00ffffff, ColorUtils.scaleAlpha( 0x11ffffff, -1 ) );
		assertEquals( 0x22aabbcc, ColorUtils.scaleAlpha( 0x44aabbcc, 0.5f ) );
		assertEquals( 0x11ffffff, ColorUtils.scaleAlpha( 0x11ffffff, 2 ) );
	}
}
