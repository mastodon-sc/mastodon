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
