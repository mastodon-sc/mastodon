package org.mastodon.revised.trackscheme.display.laf;

import static org.mastodon.revised.trackscheme.ScreenVertex.Transition.APPEAR;
import static org.mastodon.revised.trackscheme.ScreenVertex.Transition.DISAPPEAR;
import static org.mastodon.revised.trackscheme.ScreenVertex.Transition.NONE;
import static org.mastodon.revised.trackscheme.ScreenVertex.Transition.SELECTING;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import org.mastodon.revised.Util;
import org.mastodon.revised.trackscheme.ScreenColumn;
import org.mastodon.revised.trackscheme.ScreenEdge;
import org.mastodon.revised.trackscheme.ScreenEntities;
import org.mastodon.revised.trackscheme.ScreenTransform;
import org.mastodon.revised.trackscheme.ScreenVertex;
import org.mastodon.revised.trackscheme.ScreenVertexRange;
import org.mastodon.revised.trackscheme.TrackSchemeFocus;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeHighlight;
import org.mastodon.revised.trackscheme.ScreenVertex.Transition;
import org.mastodon.revised.trackscheme.display.AbstractTrackSchemeOverlay;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;

import net.imglib2.RealLocalizable;

/**
 * An AbstractTrackSchemeOverlay implementation that:
 * <ul>
 * <li>draws vertex as circles with the label inside.
 * <li>offers two sizes of vertices (full and simplified).
 * <li>draws edges as lines.
 * </ul>
 * <p>
 * Colors and strokes can be configured separately, using a
 * {@link TrackSchemeStyle}.
 */
public class DefaultTrackSchemeOverlay extends AbstractTrackSchemeOverlay
{
	/*
	 * CONSTANTS
	 */

	/**
	 * If the time rows are smaller than this size in pixels, they won't be
	 * drawn.
	 */
	private static final int MIN_TIMELINE_SPACING = 20;

	public static final double simplifiedVertexRadius = 2.5;

	public static final double simplifiedVertexSelectTolerance = 3.5;

	public static final double minDisplayVertexDist = 17.0;

	public static final double maxDisplayVertexSize = 100.0;

	public static final double minDisplaySimplifiedVertexDist = 5.0;

	public static final double avgLabelLetterWidth = 5.0;

	/*
	 * FIELDS
	 */

	private final Color[] shadowColors;

	private final TrackSchemeStyle style;

	public DefaultTrackSchemeOverlay(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeFocus focus,
			final TrackSchemeOptions options,
			final TrackSchemeStyle style )
	{
		super( graph, highlight, focus, options );
		this.style = style;

		final int[] shadowAlphas = new int[] { 28, 22, 17, 12, 8, 6, 3 };
//		final int[] shadowAlphas = new int[] { 44, 35, 28, 22, 17, 12, 8, 6, 3 };
//		final int[] shadowAlphas = new int[] { 78, 44, 35, 28, 22, 17, 12, 8, 6, 3, 1 };
		shadowColors = new Color[ shadowAlphas.length ];
		for ( int i = 0; i < shadowAlphas.length; ++i )
			shadowColors[ i ] = new Color( 0, 0, 0, shadowAlphas[ i ] );
	}

	@Override
	protected void paintBackground( final Graphics2D g2, final ScreenEntities screenEntities )
	{
		final int width = getWidth();
		final int height = getHeight();

		final ScreenTransform screenTransform = new ScreenTransform();
		screenEntities.getScreenTransform( screenTransform );
		final double yScale = screenTransform.getScaleY();
		final double minY = screenTransform.getMinY();
		final double maxY = screenTransform.getMaxY();

		g2.setColor( style.backgroundColor );
		g2.fillRect( 0, 0, width, height );

		if ( style.highlightCurrentTimepoint )
		{
			final double t = getCurrentTimepoint();
			final int y = ( int ) Math.round( yScale * ( t - minY - 0.5 ) ) + headerHeight;
			final int h = Math.max( 1, ( int ) Math.round( yScale ) );
			g2.setColor( style.currentTimepointColor );
			g2.fillRect( 0, y, width, h );
		}

		if ( style.paintRows )
		{
			g2.setColor( style.decorationColor );

			final int stepT = 1 + MIN_TIMELINE_SPACING / ( int ) ( 1 + yScale );

			int tstart = Math.max( getMinTimepoint(), ( int ) minY - 1 );
			tstart = ( tstart / stepT ) * stepT;
			int tend = Math.min( getMaxTimepoint(), 1 + ( int ) maxY );
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

		if ( style.paintColumns )
		{
			g2.setColor( style.decorationColor );

			for ( final ScreenColumn column : screenEntities.getColumns() )
			{
				g2.drawLine( column.xLeft, 0, column.xLeft, height );
				g2.drawLine( column.xLeft + column.width, 0, column.xLeft + column.width, height );
			}
		}
	}

	@Override
	protected void paintHeaders( final Graphics2D g2, final ScreenEntities screenEntities )
	{
		final int width = getWidth();
		final int height = getHeight();

		final ScreenTransform screenTransform = new ScreenTransform();
		screenEntities.getScreenTransform( screenTransform );
		final double yScale = screenTransform.getScaleY();
		final double minY = screenTransform.getMinY();
		final double maxY = screenTransform.getMaxY();

		if ( isHeaderVisibleX )
		{
			g2.setColor( style.headerBackgroundColor );
			g2.fillRect( 0, headerHeight, headerWidth, height - headerHeight );

			if ( style.paintHeaderShadow )
			{
				for ( int i = 0; i < shadowColors.length; ++i )
				{
					g2.setColor( shadowColors[ i ] );
					g2.fillRect( headerWidth + i, headerHeight + i, 1, height - headerHeight - i );
				}
			}

			if ( style.highlightCurrentTimepoint )
			{
				final double t = getCurrentTimepoint();
				final int y = ( int ) Math.round( yScale * ( t - minY - 0.5 ) ) + headerHeight;
				final int h = Math.max( 1, ( int ) Math.round( yScale ) );
				g2.setColor( style.headerCurrentTimepointColor );
				g2.fillRect( 0, y, headerWidth, h );
			}

			g2.setColor( style.headerDecorationColor );
			final FontMetrics fm = g2.getFontMetrics( style.headerFont );
			g2.setFont( style.headerFont );

			final int fontHeight = fm.getHeight();
			final int fontInc = fontHeight / 2;
			final int stepT = 1 + MIN_TIMELINE_SPACING / ( int ) ( 1 + yScale );

			int tstart = Math.max( getMinTimepoint(), ( int ) minY - 1 );
			tstart = ( tstart / stepT ) * stepT;
			int tend = Math.min( getMaxTimepoint(), 1 + ( int ) maxY );
			tend = ( 1 + tend / stepT ) * stepT;

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
			g2.setColor( style.headerBackgroundColor );
			g2.fillRect( headerWidth, 0, width - headerWidth, headerHeight );

			if ( style.paintHeaderShadow )
			{
				for ( int i = 0; i < shadowColors.length; ++i )
				{
					g2.setColor( shadowColors[ i ] );
					g2.fillRect( headerWidth + i, headerHeight + i, width - headerWidth - i, 1 );
				}
			}

			g2.setColor( style.headerDecorationColor );
			final FontMetrics fm = g2.getFontMetrics( style.headerFont );
			g2.setFont( style.headerFont );

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
			g2.setColor( style.headerBackgroundColor );
			g2.fillRect( 0, 0, headerWidth, headerHeight );
		}
	}

	@Override
	protected void beforeDrawVertex( final Graphics2D g2 )
	{
		g2.setStroke( style.vertexStroke );
	}

	@Override
	protected void drawVertex( final Graphics2D g2, final ScreenVertex vertex )
	{
		final double d = vertex.getVertexDist();
		if ( d >= minDisplayVertexDist )
			drawVertexFull( g2, vertex );
		else if ( d >= minDisplaySimplifiedVertexDist )
			drawVertexSimplified( g2, vertex );
		else
			drawVertexSimplifiedIfHighlighted( g2, vertex );
	}

	// TODO: take double x, y instead of RealLocalizable parameter
	@Override
	protected double distanceToPaintedEdge( final RealLocalizable pos, final ScreenEdge edge, final ScreenVertex source, final ScreenVertex target )
	{
		final double x0 = pos.getDoublePosition( 0 );
		final double y0 = pos.getDoublePosition( 1 );
		final double x1 = source.getX();
		final double y1 = source.getY();
		final double x2 = target.getX();
		final double y2 = target.getY();
		final double d = Util.segmentDist( x0, y0, x1, y1, x2, y2 );
		return d;
	}

	// TODO: take double x, y instead of RealLocalizable parameter
	@Override
	protected boolean isInsidePaintedVertex( final RealLocalizable pos, final ScreenVertex vertex )
	{
		final double d = vertex.getVertexDist();
		double radius = 0;
		if ( d >= minDisplayVertexDist )
		{
			final double spotdiameter = Math.min( vertex.getVertexDist() - 10.0, maxDisplayVertexSize );
			radius = spotdiameter / 2;
		}
		else if ( d >= minDisplaySimplifiedVertexDist )
		{
			radius = simplifiedVertexRadius + simplifiedVertexSelectTolerance;
		}
		final double x = pos.getDoublePosition( 0 ) - vertex.getX();
		final double y = pos.getDoublePosition( 1 ) - vertex.getY();
		return ( x * x + y * y < radius * radius );
	}

	@Override
	protected void beforeDrawVertexRange( final Graphics2D g2 )
	{
		g2.setColor( style.vertexRangeColor );
	}

	@Override
	protected void drawVertexRange( final Graphics2D g2, final ScreenVertexRange range )
	{
		final int x = ( int ) range.getMinX();
		final int y = ( int ) range.getMinY();
		final int w = ( int ) range.getMaxX() - x;
		final int h = ( int ) range.getMaxY() - y;
		g2.fillRect( x, y, w, h );
	}

	@Override
	public void beforeDrawEdge( final Graphics2D g2 )
	{
		g2.setStroke( style.edgeStroke );
	}

	@Override
	public void drawEdge( final Graphics2D g2, final ScreenEdge edge, final ScreenVertex vs, final ScreenVertex vt )
	{
		Transition transition = edge.getTransition();
		double ratio = edge.getInterpolationCompletionRatio();
		if ( vt.getTransition() == APPEAR )
		{
			transition = APPEAR;
			ratio = vt.getInterpolationCompletionRatio();
		}
		if ( vs.getTransition() == APPEAR || vs.getTransition() == DISAPPEAR )
		{
			transition = vs.getTransition();
			ratio = vs.getInterpolationCompletionRatio();
		}
		final boolean highlighted = ( highlightedEdgeId >= 0 ) && ( edge.getTrackSchemeEdgeId() == highlightedEdgeId );
		final boolean selected = edge.isSelected();
		final boolean ghost = vs.isGhost() && vt.isGhost();
		final Color drawColor = getColor( selected, ghost, transition, ratio,
				style.edgeColor, style.selectedEdgeColor,
				style.ghostEdgeColor, style.ghostSelectedEdgeColor );
		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( style.edgeHighlightStroke );
		else if ( ghost )
			g2.setStroke( style.edgeGhostStroke );
		g2.drawLine( ( int ) vs.getX(), ( int ) vs.getY(), ( int ) vt.getX(), ( int ) vt.getY() );
		if ( highlighted || ghost )
			g2.setStroke( style.edgeStroke );
	}

	protected void drawVertexSimplified( final Graphics2D g2, final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		final boolean selected = vertex.isSelected();
		final boolean ghost = vertex.isGhost();

		double spotradius = simplifiedVertexRadius;
		if ( disappear )
			spotradius *= ( 1 + 3 * ratio );

		if ( highlighted || focused )
			spotradius *= 1.5;

		final Color fillColor = getColor( selected, ghost, transition, ratio,
				disappear ? style.selectedSimplifiedVertexFillColor : style.simplifiedVertexFillColor,
				style.selectedSimplifiedVertexFillColor,
				disappear ? style.ghostSelectedSimplifiedVertexFillColor : style.ghostSimplifiedVertexFillColor,
				style.ghostSelectedSimplifiedVertexFillColor );

		final double x = vertex.getX();
		final double y = vertex.getY();
		g2.setColor( fillColor );
		final int ox = ( int ) x - ( int ) spotradius;
		final int oy = ( int ) y - ( int ) spotradius;
		final int ow = 2 * ( int ) spotradius;

		if ( focused )
			g2.fillRect( ox, oy, ow, ow );
		else
			g2.fillOval( ox, oy, ow, ow );
	}

	protected void drawVertexSimplifiedIfHighlighted( final Graphics2D g2, final ScreenVertex vertex )
	{
		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		if ( highlighted || focused )
		{
			final Transition transition = vertex.getTransition();
			final boolean disappear = ( transition == DISAPPEAR );
			final double ratio = vertex.getInterpolationCompletionRatio();

			final boolean selected = vertex.isSelected();
			final boolean ghost = vertex.isGhost();

			double spotradius = simplifiedVertexRadius;
			if ( disappear )
				spotradius *= ( 1 + 3 * ratio );

			final Color fillColor = getColor( selected, ghost, transition, ratio,
					disappear ? style.selectedSimplifiedVertexFillColor : style.simplifiedVertexFillColor,
					style.selectedSimplifiedVertexFillColor,
					disappear ? style.ghostSelectedSimplifiedVertexFillColor : style.ghostSimplifiedVertexFillColor,
					style.ghostSelectedSimplifiedVertexFillColor );

			final double x = vertex.getX();
			final double y = vertex.getY();
			g2.setColor( fillColor );
			final int ox = ( int ) x - ( int ) spotradius;
			final int oy = ( int ) y - ( int ) spotradius;
			final int ow = 2 * ( int ) spotradius;

			if ( focused )
				g2.fillRect( ox, oy, ow, ow );
			else
				g2.fillOval( ox, oy, ow, ow );
		}
	}

	protected void drawVertexFull( final Graphics2D g2, final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		final boolean selected = vertex.isSelected();
		final boolean ghost = vertex.isGhost();

		double spotdiameter = Math.min( vertex.getVertexDist() - 10.0, maxDisplayVertexSize );
		if ( highlighted )
			spotdiameter += 10.0;
		if ( disappear )
			spotdiameter *= ( 1 + ratio );
		final double spotradius = spotdiameter / 2;

		final Color fillColor = getColor( selected, ghost, transition, ratio,
				style.vertexFillColor, style.selectedVertexFillColor,
				style.ghostVertexFillColor, style.ghostSelectedVertexFillColor );
		final Color drawColor = getColor( selected, ghost, transition, ratio,
				style.vertexDrawColor, style.selectedVertexDrawColor,
				style.ghostVertexDrawColor, style.ghostSelectedVertexDrawColor );

		final double x = vertex.getX();
		final double y = vertex.getY();
		final int ox = ( int ) x - ( int ) spotradius;
		final int oy = ( int ) y - ( int ) spotradius;
		final int sd = 2 * ( int ) spotradius;
		g2.setColor( fillColor );
		g2.fillOval( ox, oy, sd, sd );

		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( style.vertexHighlightStroke );
		else if ( focused )
			// An animation might be better for the focus, but for now this is it.
			g2.setStroke( style.focusStroke );
		else if ( ghost )
			g2.setStroke( style.vertexGhostStroke );
		g2.drawOval( ox, oy, sd, sd );
		if ( highlighted || focused || ghost )
			g2.setStroke( style.vertexStroke );

		final int maxLabelLength = ( int ) ( spotdiameter / avgLabelLetterWidth );
		if ( maxLabelLength > 2 && !disappear )
		{
			String label = vertex.getLabel();
			if ( label.length() > maxLabelLength )
				label = label.substring( 0, maxLabelLength - 2 ) + "...";

			if ( ! label.isEmpty() )
			{
				final FontRenderContext frc = g2.getFontRenderContext();
				final TextLayout layout = new TextLayout( label, style.font, frc );
				final Rectangle2D bounds = layout.getBounds();
				final float tx = ( float ) ( x - bounds.getCenterX() );
				final float ty = ( float ) ( y - bounds.getCenterY() );
				layout.draw( g2, tx, ty );
			}
		}
	}

	protected Color getColor(
			final boolean isSelected,
			final boolean isGhost,
			final Transition transition,
			final double completionRatio,
			final Color normalColor,
			final Color selectedColor,
			final Color ghostNormalColor,
			final Color ghostSelectedColor )
	{
		if ( transition == NONE )
			return isGhost
					? ( isSelected ? ghostSelectedColor : ghostNormalColor )
					: ( isSelected ? selectedColor : normalColor );
		else
		{
			final double ratio = ( transition == APPEAR || transition == SELECTING )
					? 1 - completionRatio
					: completionRatio;
			final boolean fade = ( transition == APPEAR || transition == DISAPPEAR );
			int r = normalColor.getRed();
			int g = normalColor.getGreen();
			int b = normalColor.getBlue();
			int a = normalColor.getAlpha();
			if ( isSelected || !fade )
			{
				r = ( int ) ( ratio * r + ( 1 - ratio ) * selectedColor.getRed() );
				g = ( int ) ( ratio * g + ( 1 - ratio ) * selectedColor.getGreen() );
				b = ( int ) ( ratio * b + ( 1 - ratio ) * selectedColor.getBlue() );
				a = ( int ) ( ratio * a + ( 1 - ratio ) * selectedColor.getAlpha() );
			}
			if ( fade )
				a = ( int ) ( a * ( 1 - ratio ) );
			final Color color = new Color( r, g, b, a );
			return isGhost
					? TrackSchemeStyle.mixGhostColor( color, style.backgroundColor )
					: color;
		}
	}
}
