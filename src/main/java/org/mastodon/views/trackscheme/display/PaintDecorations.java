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
package org.mastodon.views.trackscheme.display;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.mastodon.views.trackscheme.ScreenColumn;
import org.mastodon.views.trackscheme.ScreenEntities;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyle;

/**
 * Painting background and headers of the TrackScheme display.
 *
 * @author Tobias Pietzsch
 */
public class PaintDecorations
{
	/**
	 * If the time rows are smaller than this size in pixels, they won't be
	 * drawn.
	 */
	private static final int MIN_TIMELINE_SPACING = 20;

	/**
	 * Increasingly transparent blacks for painting header shadows.
	 */
	private static final Color[] shadowColors;

	static
	{
		final int[] shadowAlphas = new int[] { 28, 22, 17, 12, 8, 6, 3 };
		shadowColors = new Color[ shadowAlphas.length ];
		for ( int i = 0; i < shadowAlphas.length; ++i )
			shadowColors[ i ] = new Color( 0, 0, 0, shadowAlphas[ i ] );
	}

	/**
	 * Paint background of the TrackScheme display. Specifics depend on the
	 * {@link TrackSchemeStyle} settings:
	 * <ul>
	 * <li>whether row and column separators are painted,</li>
	 * <li>whether the row with the current timepoint is highlighted,</li>
	 * <li>colors and strokes.</li>
	 * </ul>
	 *
	 * @param g2
	 *            {@code Graphics2D} context to paint to.
	 * @param width
	 *            width of the overlay (the area to paint).
	 * @param height
	 *            height of the overlay (the area to paint).
	 * @param headerWidth
	 *            width of the header decorations to the left of the graph
	 *            (timepoint numbers).
	 * @param headerHeight
	 *            width of the header decorations to the top of the graph
	 *            (column numbers).
	 * @param screenEntities
	 *            the entities to paint. Specifically, {@code paintBackground()}
	 *            uses the {@code ScreenTransform} and {@link ScreenColumn}s.
	 * @param currentTimepoint
	 *            the current timepoint. (The corresponding line in the
	 *            TrackScheme will be highlighted.)
	 * @param style
	 *            style with which to paint (background color, etc.)
	 */
	public void paintBackground(
			final Graphics2D g2,
			final int width,
			final int height,
			final int headerWidth,
			final int headerHeight,
			final ScreenEntities screenEntities,
			final int currentTimepoint,
			final TrackSchemeStyle style )
	{
		final ScreenTransform screenTransform = new ScreenTransform();
		screenEntities.getScreenTransform( screenTransform );
		final double yScale = screenTransform.getScaleY();
		final double minY = screenTransform.getMinY();
		final double maxY = screenTransform.getMaxY();

		g2.setColor( style.getBackgroundColor() );
		g2.fillRect( 0, 0, width, height );

		if ( style.isHighlightCurrentTimepoint() )
		{
			final double t = currentTimepoint;
			final int y = ( int ) Math.round( yScale * ( t - minY - 0.5 ) ) + headerHeight;
			final int h = Math.max( 1, ( int ) Math.round( yScale ) );
			g2.setColor( style.getCurrentTimepointColor() );
			g2.fillRect( 0, y, width, h );
		}

		if ( style.isPaintRows() )
		{
			g2.setColor( style.getDecorationColor() );
			g2.setStroke( style.getDecorationStroke() );

			final int stepT = 1 + MIN_TIMELINE_SPACING / ( int ) ( 1 + yScale );

			int tstart = Math.max( 0, ( int ) minY - 1 );
			tstart = ( tstart / stepT ) * stepT;
			int tend = Math.max( 0, 1 + ( int ) maxY );
			tend = ( 1 + tend / stepT ) * stepT;

			for ( int t = tstart; t < tend; t = t + stepT )
			{
				final int yline = ( int ) ( ( t - minY - 0.5 ) * yScale ) + headerHeight;
				g2.drawLine( 0, yline, width, yline );
			}

			// Last line
			final int yline = ( int ) ( ( tend - minY - 0.5 ) * yScale ) + headerHeight;
			g2.drawLine( 0, yline, width, yline );
		}

		if ( style.isPaintColumns() )
		{
			g2.setColor( style.getDecorationColor() );
			g2.setStroke( style.getDecorationStroke() );

			for ( final ScreenColumn column : screenEntities.getColumns() )
			{
				g2.drawLine( column.xLeft, 0, column.xLeft, height );
				g2.drawLine( column.xLeft + column.width, 0, column.xLeft + column.width, height );
			}
		}
	}

	/**
	 * Paint headers of the TrackScheme display. Specifics depend on the
	 * {@link TrackSchemeStyle} settings:
	 * <ul>
	 * <li>whether the row with the current timepoint is highlighted,</li>
	 * <li>colors, fonts, and strokes.</li>
	 * </ul>
	 *
	 * @param g2
	 *            {@code Graphics2D} context to paint to.
	 * @param width
	 *            width of the overlay (the area to paint).
	 * @param height
	 *            height of the overlay (the area to paint).
	 * @param headerWidth
	 *            width of the header decorations to the left of the graph
	 *            (timepoint numbers).
	 * @param headerHeight
	 *            width of the header decorations to the top of the graph
	 *            (column numbers).
	 * @param screenEntities
	 *            the entities to paint. Specifically, {@code paintBackground()}
	 *            uses the {@code ScreenTransform} and {@link ScreenColumn}s.
	 * @param currentTimepoint
	 *            the current timepoint. (The corresponding line in the
	 *            TrackScheme will be highlighted.)
	 * @param style
	 *            style with which to paint (background color, etc.)
	 */
	public void paintHeaders(
			final Graphics2D g2,
			final int width,
			final int height,
			final int headerWidth,
			final int headerHeight,
			final ScreenEntities screenEntities,
			final int currentTimepoint,
			final TrackSchemeStyle style )
	{
		final ScreenTransform screenTransform = new ScreenTransform();
		screenEntities.getScreenTransform( screenTransform );
		final double yScale = screenTransform.getScaleY();
		final double minY = screenTransform.getMinY();
		final double maxY = screenTransform.getMaxY();

		final boolean isHeaderVisibleX = headerWidth > 0;
		final boolean isHeaderVisibleY = headerHeight > 0;

		if ( isHeaderVisibleX )
		{
			g2.setColor( style.getHeaderBackgroundColor() );
			g2.fillRect( 0, headerHeight, headerWidth, height - headerHeight );

			if ( style.isPaintHeaderShadow() )
			{
				for ( int i = 0; i < shadowColors.length; ++i )
				{
					g2.setColor( shadowColors[ i ] );
					g2.fillRect( headerWidth + i, headerHeight + i, 1, height - headerHeight - i );
				}
			}

			if ( style.isHighlightCurrentTimepoint() )
			{
				final double t = currentTimepoint;
				final int y = ( int ) Math.round( yScale * ( t - minY - 0.5 ) ) + headerHeight;
				final int h = Math.max( 1, ( int ) Math.round( yScale ) );
				g2.setColor( style.getHeaderCurrentTimepointColor() );
				g2.fillRect( 0, y, headerWidth, h );
			}

			g2.setColor( style.getHeaderDecorationColor() );
			final FontMetrics fm = g2.getFontMetrics( style.getHeaderFont() );
			g2.setFont( style.getHeaderFont() );

			final int fontHeight = fm.getHeight();
			final int fontInc = fontHeight / 2;
			final int stepT = 1 + MIN_TIMELINE_SPACING / ( int ) ( 1 + yScale );

			int tstart = Math.max( 0, ( int ) minY - 1 );
			tstart = ( tstart / stepT ) * stepT;
			int tend = Math.max( 0, 1 + ( int ) maxY );
			tend = ( 1 + tend / stepT ) * stepT;

			g2.setStroke( style.getDecorationStroke() );
			for ( int t = tstart; t <= tend; t = t + stepT )
			{
				final int yline = ( int ) ( ( t - minY - 0.5 ) * yScale ) + headerHeight;
				g2.drawLine( 0, yline, headerWidth, yline );

				final int ytext = Math.max(
						( int ) ( ( t - minY ) * yScale ) + fontInc + headerHeight,
						yline + fontHeight );
				g2.drawString( "" + t, 5, ytext );
			}
		}

		if ( isHeaderVisibleY )
		{
			g2.setColor( style.getHeaderBackgroundColor() );
			g2.fillRect( headerWidth, 0, width - headerWidth, headerHeight );

			if ( style.isPaintHeaderShadow() )
			{
				for ( int i = 0; i < shadowColors.length; ++i )
				{
					g2.setColor( shadowColors[ i ] );
					g2.fillRect( headerWidth + i, headerHeight + i, width - headerWidth - i, 1 );
				}
			}

			g2.setColor( style.getHeaderDecorationColor() );
			final FontMetrics fm = g2.getFontMetrics( style.getHeaderFont() );
			g2.setFont( style.getHeaderFont() );

			g2.setStroke( style.getDecorationStroke() );
			for ( final ScreenColumn column : screenEntities.getColumns() )
			{
				g2.drawLine( column.xLeft, 0, column.xLeft, headerHeight );
				g2.drawLine( column.xLeft + column.width, 0, column.xLeft + column.width, headerHeight );

				final String str = column.label;
				final int stringWidth = fm.stringWidth( str );

				final int boundedMin = Math.max( headerWidth, column.xLeft );
				final int boundedMax = Math.min( column.xLeft + column.width, width );
				final int boundedWidth = boundedMax - boundedMin;
				if ( boundedWidth >= stringWidth + 5  )
				{
					final int xtext = ( boundedMin + boundedMax - stringWidth ) / 2;
					g2.drawString( str, xtext, headerHeight / 2 );
				}
			}
		}

		if ( isHeaderVisibleX && isHeaderVisibleY )
		{
			g2.setColor( style.getHeaderBackgroundColor() );
			g2.fillRect( 0, 0, headerWidth, headerHeight );
		}
	}
}
