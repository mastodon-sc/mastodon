/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.display;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.mastodon.views.grapher.datagraph.ScreenEntities;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.style.DataDisplayStyle;

/**
 * Painting background and headers of the TrackScheme display.
 *
 * @author Tobias Pietzsch
 */
public class PaintDecorations
{

	/**
	 * Paint background of the DataGraph display. Specifics depend on the
	 * {@link DataDisplayStyle} settings.
	 *
	 * @param g2
	 *            {@code Graphics2D} context to paint to.
	 * @param width
	 *            width of the overlay (the area to paint).
	 * @param height
	 *            height of the overlay (the area to paint).
	 * @param axesWidth
	 *            width of the decorations for the ordinate axis to the left of
	 *            the plot (vertical axis).
	 * @param axesHeight
	 *            width of the decorations for the abscissa axis at the bottom
	 *            plot (horizontal axis).
	 * @param screenEntities
	 *            the entities to paint. Specifically, {@code paintBackground()}
	 *            uses the {@code ScreenTransform}s.
	 * @param style
	 *            style with which to paint (background color, etc.)
	 */
	public void paintBackground(
			final Graphics2D g2,
			final int width,
			final int height,
			final int axesWidth,
			final int axesHeight,
			final ScreenEntities screenEntities,
			final DataDisplayStyle style )
	{
		g2.setColor( style.getBackgroundColor() );
		g2.fillRect( 0, 0, width, height );
	}

	/**
	 * Paint axes of the data graph plot. Specifics depend on the
	 * {@link DataDisplayStyle} settings.
	 * 
	 * @param g2
	 *            {@code Graphics2D} context to paint to.
	 * @param width
	 *            width of the overlay (the area to paint).
	 * @param height
	 *            height of the overlay (the area to paint).
	 * @param axesWidth
	 *            width of the decorations for the ordinate axis to the left of
	 *            the plot (vertical axis).
	 * @param axesHeight
	 *            width of the decorations for the abscissa axis at the bottom
	 *            plot (horizontal axis).
	 * @param screenEntities
	 *            the entities to paint. Specifically, {@code paintBackground()}
	 *            uses the {@code ScreenTransform}.
	 * @param style
	 *            style with which to paint (background color, etc.)
	 */
	public void paintHeaders(
			final Graphics2D g2,
			final int width,
			final int height,
			final int axesWidth,
			final int axesHeight,
			final ScreenEntities screenEntities,
			final DataDisplayStyle style )
	{
		final ScreenTransform screenTransform = new ScreenTransform();
		screenEntities.getScreenTransform( screenTransform );
		final double xScale = screenTransform.getScaleX();
		final double minX = screenTransform.getMinX();
		final double maxX = screenTransform.getMaxX();
		final double yScale = screenTransform.getScaleY();
		final double minY = screenTransform.getMinY();
		final double maxY = screenTransform.getMaxY();

		final boolean isVisibleYAxis = axesWidth > 0;
		final boolean isVisibleXAxis = axesHeight > 0;

		final int maxTickSpacing = 200;
		final int minTickSpacing = 50;

		final FontMetrics fm = g2.getFontMetrics( style.getHeaderFont() );
		g2.setFont( style.getHeaderFont() );
		g2.setStroke( style.getDecorationStroke() );

		// Width of the ticks.
		final int tickWidth = 2;

		// How to center Y ticks on the ticks themselves.
		final int fontAscent = fm.getAscent();
		final int fontInc = fontAscent / 2;

		if ( isVisibleYAxis )
		{
			// Erase.
			g2.setColor( style.getHeaderBackgroundColor() );
			g2.fillRect( 0, axesHeight, axesWidth, height - axesHeight );

			// Paint axis.
			g2.setColor( style.getHeaderDecorationColor() );

			// Steps.
			final int stepY = Math.max( 1, maxTickSpacing / ( int ) ( 1 + yScale ) );
			int ystart = Math.max( 0, ( int ) minY - 1 );
			ystart = ( ystart / stepY ) * stepY;
			int yend = Math.max( 0, 1 + ( int ) maxY );
			yend = ( 1 + yend / stepY ) * stepY;

			// From right to left.

			// 0. Vertical line.
			g2.drawLine( axesWidth, 0, axesWidth, height );

			int maxStringWidth = -1;
			for ( int y = ystart; y <= yend; y = y + stepY )
			{
				// 1. Ticks.
				final int yline = ( int ) ( ( y - minY ) * yScale ) + axesHeight;
				g2.drawLine( axesWidth - tickWidth, yline, axesWidth - 1, yline );

				// 2. Tick labels.
				final int ytext = yline + fontInc;
				final String tickLabel = "" + y;
				final int stringWidth = fm.stringWidth( tickLabel );
				g2.drawString( tickLabel, axesWidth - tickWidth - 2 - stringWidth, ytext );
				if ( stringWidth > maxStringWidth )
					maxStringWidth = stringWidth;
			}

			// 3. Y label
			// TODO specific fonts for labels
			final String yLabel = "The Y feature projection"; // TODO
			final int yLabelWidth = fm.stringWidth( yLabel );
			drawStringRotated( g2,
					axesWidth - tickWidth - 2 - maxStringWidth - 5,
					height / 2 + yLabelWidth / 2,
					-90.,
					yLabel );
		}

		if ( isVisibleXAxis )
		{
			// Erase.
			g2.setColor( style.getHeaderBackgroundColor() );
			g2.fillRect( axesWidth, 0, width - axesWidth, axesHeight );

			// Paint axis.
			g2.setColor( style.getHeaderDecorationColor() );

			// Steps.
			final int stepX = Math.max( 1, maxTickSpacing / ( int ) ( 1 + xScale ) );
			int xstart = Math.max( 0, ( int ) minX - 1 );
			xstart = ( xstart / stepX ) * stepX;
			int xend = Math.max( 0, 1 + ( int ) maxX );
			xend = ( 1 + xend / stepX ) * stepX;

			// From top to bottom.

			// 0. Horizontal line.
			g2.drawLine( axesWidth, axesHeight, width, axesHeight );

			int maxStringWidth = -1;
			for ( int x = xstart; x <= xend; x = x + stepX )
			{
				// 1. Ticks.
				final int xline = ( int ) ( ( x - minX ) * xScale ) + axesWidth;
				g2.drawLine( xline, axesHeight - tickWidth, xline, axesHeight - 1 );

				// 2. Tick labels.
				final String tickLabel = "" + x;
				final int stringWidth = fm.stringWidth( tickLabel );
				final int xtext = xline - stringWidth / 2;
				g2.drawString( tickLabel, xtext, axesHeight - tickWidth - 2 );
				if ( stringWidth > maxStringWidth )
					maxStringWidth = stringWidth;
			}

			// 3. X label
			// TODO specific fonts for labels
			final String xLabel = "The X feature projection"; // TODO
			final int xLabelWidth = fm.stringWidth( xLabel );
			g2.drawString( xLabel,
					axesWidth + ( width - axesWidth ) / 2 - xLabelWidth / 2,
					axesHeight - tickWidth - 2 - fontAscent - 5 );
		}

		if ( isVisibleYAxis && isVisibleXAxis )
		{
			g2.setColor( style.getHeaderBackgroundColor() );
			g2.fillRect( 0, 0, axesWidth, axesHeight );
		}
	}

	private static void drawStringRotated( final Graphics2D g2d, final int x, final int y, final double angle, final String text )
	{
		g2d.translate( x, y );
		g2d.rotate( Math.toRadians( angle ) );
		g2d.drawString( text, 0, 0 );
		g2d.rotate( -Math.toRadians( angle ) );
		g2d.translate( -x, -y );
	}
}
