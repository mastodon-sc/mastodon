package org.mastodon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
	}

	@Test
	public void testScaleAlpha()
	{
		assertEquals( 0x00ffffff, ColorUtils.scaleAlpha( 0x11ffffff, -1 ) );
		assertEquals( 0x22aabbcc, ColorUtils.scaleAlpha( 0x44aabbcc, 0.5f ) );
		assertEquals( 0x11ffffff, ColorUtils.scaleAlpha( 0x11ffffff, 2 ) );

		Color input = Color.BLACK;
		float ratio = 0.5f;
		// generate color from result with valid alpha bits
		Color resultWithAlpha = new Color( ColorUtils.scaleAlpha( input.getRGB(), ratio ), true );
		assertEquals( 0, resultWithAlpha.getRed() );
		assertEquals( 0, resultWithAlpha.getGreen() );
		assertEquals( 0, resultWithAlpha.getBlue() );

		assertEquals( 255, input.getAlpha() );
		// alpha bits are set
		assertEquals( 127, resultWithAlpha.getAlpha() );

		// generate color from result without valid alpha bits
		Color resultWithoutAlpha = new Color( ColorUtils.scaleAlpha( input.getRGB(), ratio ) );
		assertEquals( 0, resultWithoutAlpha.getRed() );
		assertEquals( 0, resultWithoutAlpha.getGreen() );
		assertEquals( 0, resultWithoutAlpha.getBlue() );

		// alpha bits are not set -> alpha is still (but wrongly) 255
		assertEquals( 255, resultWithoutAlpha.getAlpha() );
	}
}
