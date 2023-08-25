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
package org.mastodon.ui.coloring;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Objects;
import java.util.function.Supplier;

import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.scijava.listeners.Listeners;

import bdv.viewer.OverlayRenderer;

/**
 * An {@link OverlayRenderer} that displays a color-bar for the
 * {@link FeatureColorMode} currently selected in the {@link ColoringModel}
 * provided at construction.
 *
 * @author Jean-Yves Tinevez
 */
public class ColorBarOverlay implements OverlayRenderer
{

	public static final Position DEFAULT_POSITION = Position.BOTTOM_RIGHT;

	public static final boolean DEFAULT_VISIBLE = false;

	private static final int DEFAULT_WIDTH = 70;

	private static final int COLORBARS_SPACING = 20;

	private static final String VERTEX_HEADER = "V";

	private static final String EDGE_HEADER = "E";

	private static final String BOTH_HEADER = "V/E";

	/**
	 * Inside horizontal space between the box border and the bar or text.
	 */
	private static final int HINSET = 10;

	/**
	 * Inside vertical space between the box border and the bar or text.
	 */
	private static final int VINSET = 5;

	/**
	 * Specifies the {@link ColorBarOverlay} position in the display.
	 *
	 * @author Jean-Yves Tinevez
	 *
	 */
	public static enum Position
	{
		TOP_LEFT( "Top-left" ),
		TOP_RIGHT( "Top-right" ),
		BOTTOM_LEFT( "Bottom-left" ),
		BOTTOM_RIGHT( "Bottom-right" );

		private final String str;

		private Position( final String str )
		{
			this.str = str;
		}

		@Override
		public String toString()
		{
			return str;
		}

		public int xOrigin( final int canvasWidth, final int barWidth, final int[] insets )
		{
			switch ( this )
			{
			default:
			case BOTTOM_LEFT:
			case TOP_LEFT:
				return insets[ 1 ];
			case BOTTOM_RIGHT:
			case TOP_RIGHT:
				return canvasWidth - barWidth - insets[ 3 ];
			}
		}

		public int yOrigin( final int canvasHeight, final int barHeight, final int[] insets )
		{
			switch ( this )
			{
			default:
			case TOP_LEFT:
			case TOP_RIGHT:
				return insets[ 0 ];
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
				return canvasHeight - barHeight - insets[ 2 ];
			}
		}
	}

	private final int minWidth = DEFAULT_WIDTH;

	private Position position = DEFAULT_POSITION;

	private final ColoringModel coloringModel;

	private int canvasWidth;

	private int canvasHeight;

	private boolean visible = DEFAULT_VISIBLE;

	private final Supplier< Color > bgColorSupplier;

	private final Listeners.List< ColorBarOverlayListener > listeners = new Listeners.List<>();

	/**
	 * Space between the panel border and the overlay border. 4 elements: top,
	 * left, bottom, right.
	 */
	private final int[] insets;

	public ColorBarOverlay( final ColoringModel coloringModel, final Supplier< Color > bgColorSupplier )
	{
		this.coloringModel = coloringModel;
		this.bgColorSupplier = bgColorSupplier;
		this.insets = new int[] { 15, 15, 15, 15 };
	}

	/**
	 * Returns the listeners objects, that will be notified when the this color
	 * bar overlay settings are changed.
	 * 
	 * @return the listeners list.
	 */
	public Listeners.List< ColorBarOverlayListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		listeners.list.forEach( l -> l.colorBarOverlayChanged() );
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( !visible || coloringModel.noColoring() )
			return;

		final FeatureColorMode featureColorMode = coloringModel.getFeatureColorMode();
		if ( null != featureColorMode )
		{
			draw( featureColorMode, g );
			return;
		}

		final TagSet tagSet = coloringModel.getTagSet();
		if ( null != tagSet )
		{
			draw( tagSet, g );
			return;
		}
	}

	private void draw( final TagSet tagSet, final Graphics g )
	{
		final int tw = totalWidth( tagSet, g );
		final int th = totalHeight( tagSet, g );
		int x = position.xOrigin( canvasWidth, tw, insets );
		int y = position.yOrigin( canvasHeight, th, insets );

		final Color panelBGColor = bgColorSupplier.get();
		final Color bgColor = new Color( panelBGColor.getRed(), panelBGColor.getGreen(), panelBGColor.getBlue(), 130 );
		final Color lineColor = panelBGColor.darker().darker();
		g.setColor( bgColor );

		final FontMetrics fm = g.getFontMetrics();
		final int height = fm.getHeight();
		final int ascent = fm.getAscent();

		g.setColor( bgColor );
		g.fillRect( x, y, tw, th );
		g.setColor( lineColor );
		g.drawRect( x, y, tw, th );

		y += VINSET;
		x += HINSET;

		y += ascent;
		g.setColor( Color.BLACK );
		g.drawString( tagSet.getName(), x, y );

		y += 2;
		for ( final Tag tag : tagSet.getTags() )
		{
			g.setColor( new Color( tag.color(), true ) );
			g.fillRect( x, y, height, height );
			x += height + 2;
			g.drawString( tag.label(), x, y + height );
			x += fm.stringWidth( tag.label() ) + 5;
		}
	}

	private int totalHeight( final TagSet tagSet, final Graphics g )
	{
		final FontMetrics fm = g.getFontMetrics();
		return 2 * fm.getHeight() + 2 + 2 * VINSET;
	}

	private int totalWidth( final TagSet tagSet, final Graphics g )
	{
		final FontMetrics fm = g.getFontMetrics();
		int totalWidth = 2 * HINSET;
		for ( final Tag tag : tagSet.getTags() )
			totalWidth += fm.stringWidth( tag.label() ) + 2 + fm.getHeight() + 5;
		return totalWidth;
	}

	private void draw( final FeatureColorMode featureColorMode, final Graphics g )
	{
		final int tw = totalWidth( featureColorMode, g );
		final int th = totalHeight( featureColorMode, g );
		final int x = position.xOrigin( canvasWidth, tw, insets );
		final int y = position.yOrigin( canvasHeight, th, insets );
		final String vertexColorMap = featureColorMode.getVertexColorMap();
		final String vertexProjectionKey = toString( featureColorMode.getVertexFeatureProjection() );
		final double vertexRangeMin = featureColorMode.getVertexRangeMin();
		final double vertexRangeMax = featureColorMode.getVertexRangeMax();
		final Color panelBGColor = bgColorSupplier.get();
		final Color bgColor = new Color( panelBGColor.getRed(), panelBGColor.getGreen(), panelBGColor.getBlue(), 130 );
		final Color lineColor = panelBGColor.darker().darker();

		g.setColor( bgColor );
		g.fillRect( x, y, tw, th );
		g.setColor( lineColor );
		g.drawRect( x, y, tw, th );
		if ( areVandEequal( featureColorMode ) )
		{
			draw( x, y, vertexColorMap, BOTH_HEADER, vertexProjectionKey, vertexRangeMin, vertexRangeMax, g );
		}
		else
		{
			final int xShift =
					draw( x, y, vertexColorMap, VERTEX_HEADER, vertexProjectionKey, vertexRangeMin, vertexRangeMax, g );
			final String edgeColorMap = featureColorMode.getEdgeColorMap();
			final String edgeProjectionKey = toString( featureColorMode.getEdgeFeatureProjection() );
			final double edgeRangeMin = featureColorMode.getEdgeRangeMin();
			final double edgeRangeMax = featureColorMode.getEdgeRangeMax();
			draw( x + xShift + COLORBARS_SPACING, y, edgeColorMap, EDGE_HEADER, edgeProjectionKey, edgeRangeMin,
					edgeRangeMax, g );
		}
	}

	private static boolean areVandEequal( final FeatureColorMode featureColorMode )
	{
		if ( !featureColorMode.getVertexColorMap().equals( featureColorMode.getEdgeColorMap() ) )
			return false;
		if ( !Objects.equals( featureColorMode.getVertexFeatureProjection(),
				featureColorMode.getEdgeFeatureProjection() ) )
			return false;
		if ( featureColorMode.getVertexRangeMin() != featureColorMode.getEdgeRangeMin() )
			return false;
		if ( featureColorMode.getVertexRangeMax() != featureColorMode.getEdgeRangeMax() )
			return false;

		return true;
	}

	private static String toString( final FeatureProjectionId featureProjectionId )
	{
		if ( featureProjectionId == null )
			return "null";
		final StringBuilder sb = new StringBuilder( featureProjectionId.getProjectionKey() );
		final int[] sourceIndices;
		switch ( featureProjectionId.getMultiplicity() )
		{
		case SINGLE:
		default:
			sourceIndices = new int[] {};
			break;
		case ON_SOURCES:
			sourceIndices = new int[] { featureProjectionId.getI0() };
			break;
		case ON_SOURCE_PAIRS:
			sourceIndices = new int[] { featureProjectionId.getI0(), featureProjectionId.getI1() };
			break;
		}

		for ( final int sourceIndex : sourceIndices )
		{
			sb.append( " ch" );
			sb.append( sourceIndex );
		}
		return sb.toString();
	}

	private int draw(
			final int xOrigin,
			final int yOrigin,
			final String strCmap,
			final String header,
			final String featureName,
			final double rangeMin,
			final double rangeMax,
			final Graphics g )
	{
		final FontMetrics fm = g.getFontMetrics();
		final int localWidth = Math.max( minWidth, fm.stringWidth( featureName ) );
		final int lw = ( int ) ( 0.85 * localWidth );
		final int height = fm.getHeight();
		final int ascent = fm.getAscent();

		int x = xOrigin;
		int y = yOrigin;

		x += HINSET;
		y += VINSET;

		// Colorbar header width.
		final int vWidth = fm.stringWidth( header );

		// Feature name.
		y += ascent;
		g.setColor( Color.BLACK );
		g.drawString( featureName, x + vWidth, y );

		// Colorbar header.
		y += height + 2;
		g.setColor( Color.BLACK );
		g.drawString( header, x, y );

		// Colorbar.
		x += vWidth + 2;
		final ColorMap vCmap = ColorMap.getColorMap( strCmap );
		for ( int i = 0; i < lw; i++ )
		{
			g.setColor( new Color( vCmap.get( ( double ) i / lw ), true ) );
			g.drawLine( x + i, y - height, x + i, y );
		}
		// No value patch.
		g.setColor( new Color( vCmap.get( Double.NaN ) ) );
		g.fillRect( ( int ) ( x + 0.9 * localWidth ), y - height, ( int ) ( 0.1 * localWidth ), height );

		// Ticks.
		g.drawLine( x, y, x, y + 2 );
		g.drawLine( x + lw / 2, y, x + lw / 2, y + 2 );
		g.drawLine( x + lw - 1, y, x + lw - 1, y + 2 );

		// Tick labels.
		y += 2;
		final String strVMin = String.format( "%.1f", rangeMin );
		final int stringWidthVMin = fm.stringWidth( strVMin );
		g.drawString( strVMin, x - stringWidthVMin / 2, y + ascent );

		final String strVMax = String.format( "%.1f", rangeMax );
		final int stringWidthVMax = fm.stringWidth( strVMax );
		g.drawString( strVMax, x + lw - 1 - stringWidthVMax / 2, y + ascent );

		return localWidth + vWidth;
	}

	private int totalHeight( final FeatureColorMode featureColorMode, final Graphics g )
	{
		final FontMetrics fm = g.getFontMetrics();
		// The bar has the same height that of a string.
		return fm.getAscent() + 2 * fm.getHeight() + 2 + 2 * VINSET;
	}

	private int totalWidth( final FeatureColorMode featureColorMode, final Graphics g )
	{
		final FontMetrics fm = g.getFontMetrics();
		if ( areVandEequal( featureColorMode ) )
		{
			int stringWidth = fm.stringWidth( BOTH_HEADER ) + 2;
			stringWidth +=
					Math.max( minWidth, fm.stringWidth( toString( featureColorMode.getVertexFeatureProjection() ) ) );
			return 2 * HINSET + stringWidth;
		}
		else
		{
			int stringWidth = fm.stringWidth( VERTEX_HEADER ) + 2;
			stringWidth +=
					Math.max( minWidth, fm.stringWidth( toString( featureColorMode.getVertexFeatureProjection() ) ) );
			stringWidth += COLORBARS_SPACING;
			stringWidth += fm.stringWidth( EDGE_HEADER ) + 2;
			stringWidth +=
					Math.max( minWidth, fm.stringWidth( toString( featureColorMode.getEdgeFeatureProjection() ) ) );
			return 2 * HINSET + stringWidth;
		}
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{
		this.canvasWidth = width;
		this.canvasHeight = height;
	}

	public void setVisible( final boolean visible )
	{
		if ( this.visible != visible )
		{
			this.visible = visible;
			notifyListeners();
		}
	}

	public void setPosition( final Position position )
	{
		if ( position != this.position )
		{
			this.position = position;
			notifyListeners();
		}
	}

	public void setInsets( final int top, final int left, final int bottom, final int right )
	{
		insets[ 0 ] = top;
		insets[ 1 ] = left;
		insets[ 2 ] = bottom;
		insets[ 3 ] = right;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public Position getPosition()
	{
		return position;
	}

	public interface ColorBarOverlayListener
	{
		void colorBarOverlayChanged();
	}
}
