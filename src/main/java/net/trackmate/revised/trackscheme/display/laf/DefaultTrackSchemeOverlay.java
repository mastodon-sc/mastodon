package net.trackmate.revised.trackscheme.display.laf;

import static net.trackmate.revised.trackscheme.ScreenVertex.Transition.APPEAR;
import static net.trackmate.revised.trackscheme.ScreenVertex.Transition.DISAPPEAR;
import static net.trackmate.revised.trackscheme.ScreenVertex.Transition.NONE;
import gnu.trove.list.TDoubleList;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.Util;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenEdge;
import net.trackmate.revised.trackscheme.ScreenEntities;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.ScreenVertex;
import net.trackmate.revised.trackscheme.ScreenVertex.Transition;
import net.trackmate.revised.trackscheme.ScreenVertexRange;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.AbstractTrackSchemeOverlay;
import net.trackmate.revised.trackscheme.display.TrackSchemeOptions;

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


	/**
	 * Y position for columns labels.
	 */
	private static final int YTEXT = 20;

	/**
	 * X position for time labels.
	 */
	private static final int XTEXT = 20;

	/**
	 * Size in pixel below which we do not draw column bars.
	 */
	private static final int MIN_DRAWING_COLUMN_WIDTH = 30;

	public static final double simplifiedVertexRadius = 3.0;

	public static final double minDisplayVertexDist = 20.0;

	public static final double maxDisplayVertexSize = 100.0;

	public static final double minDisplaySimplifiedVertexDist = 5.0;

	public static final double avgLabelLetterWidth = 5.0;

	/*
	 * FIELDS
	 */

	private final boolean highlightCurrentTimepoint = true;

	private final boolean paintRows = true;

	private final boolean paintColumns = true;

	protected TrackSchemeStyle style = TrackSchemeStyle.defaultStyle();

	private final LineageTreeLayout layout;

	public DefaultTrackSchemeOverlay(
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout,
			final TrackSchemeHighlight highlight,
			final TrackSchemeOptions options )
	{
		super( graph, highlight, options );
		this.layout = layout;
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

		g2.setColor( style.backgroundColor );
		g2.fillRect( 0, 0, width, height );

		if ( highlightCurrentTimepoint )
		{
			final double t = getCurrentTimepoint();
			final int y = ( int ) Math.round( yScale * ( t - minY - 0.5 ) );
			final int h = Math.max( 1, ( int ) Math.round( yScale ) );
			g2.setColor( style.currentTimepointColor );
			g2.fillRect( 0, y, width, h );
		}
	}

	@Override
	protected void paintDecoration( final Graphics2D g2, final ScreenEntities screenEntities )
	{
		final int width = getWidth();
		final int height = getHeight();

		final ScreenTransform screenTransform = new ScreenTransform();
		screenEntities.getScreenTransform( screenTransform );
		final double yScale = screenTransform.getScaleY();
		final double minX = screenTransform.getMinX();
		final double maxX = screenTransform.getMaxX();
		final double minY = screenTransform.getMinY();
		final double maxY = screenTransform.getMaxY();

		/*
		 * DECORATIONS
		 */

		final FontMetrics fm = g2.getFontMetrics( style.font );
		g2.setFont( style.font );

		final int stepT = 1 + MIN_TIMELINE_SPACING / ( int ) ( 1 + yScale );
		int tstart = Math.max( getMinTimepoint(), ( int ) minY - 1 );
		tstart = ( tstart / stepT ) * stepT;
		int tend = Math.min( getMaxTimepoint(), 1 + ( int ) maxY );
		tend = ( 1 + tend / stepT ) * stepT;

		if ( paintRows )
		{
			final int eraseWidth = ( int ) ( 2.5 * XTEXT );

			g2.setColor( style.backgroundColor );
			g2.fillRect( 0, 0, eraseWidth, height );

			if ( highlightCurrentTimepoint )
			{
				final double t = getCurrentTimepoint();
				final int y = ( int ) Math.round( yScale * ( t - minY - 0.5 ) );
				final int h = Math.max( 1, ( int ) Math.round( yScale ) );
				g2.setColor( style.currentTimepointColor );
				g2.fillRect( 0, y, eraseWidth, h );
			}

			final int fontInc = fm.getHeight() / 2;
			g2.setColor( style.decorationColor );
			for ( int t = tstart; t < tend; t = t + stepT )
			{
				final int yline = ( int ) ( ( t - minY - 0.5 ) * yScale );
				g2.drawLine( 0, yline, width, yline );

				final int ytext = ( int ) ( ( t - minY + stepT / 2 ) * yScale ) + fontInc;
				if ( ytext < 2 * YTEXT )
				{
					continue;
				}
				g2.drawString( "" + t, XTEXT, ytext );
			}

			// Last line
			final int yline = ( int ) ( ( tend - minY - 0.5 ) * yScale );
			g2.drawLine( 0, yline, width, yline );
		}

		if ( paintColumns )
		{
			g2.setColor( style.backgroundColor );
			g2.fillRect( 0, 0, width, 2 * YTEXT );
			g2.setColor( style.decorationColor );

			final double xScale = screenTransform.getScaleX();
			final int minLineY = ( int ) ( ( tstart - minY - 0.5 ) * yScale );
			final int maxLineY = ( int ) ( ( tend - minY - 0.5 ) * yScale );

			final TDoubleList columnX = layout.getCurrentLayoutColumnX();
			final RefList< TrackSchemeVertex > columnRoots = layout.getCurrentLayoutColumnRoot();

			int minC = columnX.binarySearch( minX );
			if ( minC < 0 )
			{
				minC = -1 - minC;
			}
			minC = Math.max( 0, minC - 1 ); // at least 1 column out

			int maxC = columnX.binarySearch( maxX + 0.5, minC, columnX.size() );
			if ( maxC < 0 )
			{
				maxC = -1 - maxC;
			}
			maxC = Math.min( columnX.size(), maxC + 1 );

			double lastX = Double.NEGATIVE_INFINITY;
			for ( int c = minC; c < maxC; c++ )
			{
				final double col = columnX.get( c );
				final int xline = ( int ) ( ( col - minX - 0.5 ) * xScale );
				if ( xline < 2 * XTEXT || ( xline - lastX ) < MIN_DRAWING_COLUMN_WIDTH )
				{
					continue;
				}
				g2.drawLine( xline, minLineY, xline, maxLineY );
				lastX = xline;

				if ( c < 1 )
				{
					continue;
				}
				final int xprevline = ( int ) ( ( columnX.get( c - 1 ) - minX - 0.5 ) * xScale );
				final String str = columnRoots.get( c - 1 ).getLabel();
				final int stringWidth = fm.stringWidth( str );

				final int columnWidth = xline - xprevline;
				if ( columnWidth < stringWidth + 5 )
				{
					continue;
				}

				final int xtext = ( Math.min( width, xline ) + Math.max( 0, xprevline ) - stringWidth ) / 2;
				if ( xtext < 2 * XTEXT )
				{
					continue;
				}
				g2.drawString( str, xtext, YTEXT );
			}

			// Last column?
			final int nCols = columnX.size();
			if ( maxC == nCols && nCols > 0 )
			{
				final int xline = ( int ) ( ( layout.getCurrentLayoutMaxX() - minX + 0.5 ) * xScale );
				g2.drawLine( xline, minLineY, xline, maxLineY );

				final int xprevline = ( int ) ( ( columnX.get( nCols - 1 ) - minX - 0.5 ) * xScale );
				final String str = columnRoots.get( nCols - 1 ).getLabel();
				final int stringWidth = fm.stringWidth( str );

				final int columnWidth = xline - xprevline;
				if ( columnWidth >= stringWidth + 5 )
				{
					final int xtext = ( Math.min( width, xline ) + Math.max( 0, xprevline ) - stringWidth ) / 2;
					if ( xtext >= 2 * XTEXT )
					{
						g2.drawString( str, xtext, YTEXT );
					}
				}
			}
		}
	}

	@Override
	protected void beforeDrawVertex( final Graphics2D g2 )
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawVertex( final Graphics2D g2, final ScreenVertex vertex )
	{
		final double d = vertex.getVertexDist();
		if ( d >= minDisplayVertexDist )
		{
			drawVertexFull( g2, vertex );
		}
		else if ( d >= minDisplaySimplifiedVertexDist )
		{
			drawVertexSimplified( g2, vertex );
		}
	}

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
			radius = simplifiedVertexRadius;
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
		Transition transition = vs.getTransition();
		double ratio = vs.getInterpolationCompletionRatio();
		if ( vt.getTransition() == APPEAR )
		{
			transition = APPEAR;
			ratio = vt.getInterpolationCompletionRatio();
		}
		final boolean selected = edge.isSelected();
		final Color drawColor = getColor( selected, transition, ratio, style.edgeColor, style.selectedEdgeColor );
		g2.setColor( drawColor );
		g2.drawLine( ( int ) vs.getX(), ( int ) vs.getY(), ( int ) vt.getX(), ( int ) vt.getY() );
	}

	protected void drawVertexSimplified( final Graphics2D g2, final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final double ratio = vertex.getInterpolationCompletionRatio();
		final boolean disappear = ( transition == DISAPPEAR );
		final boolean selected = vertex.isSelected();

		double spotradius = simplifiedVertexRadius;
		if ( disappear )
			spotradius *= ( 1 + 3 * ratio );

		final Color fillColor = getColor( selected, transition, ratio,
				disappear ? style.selectedSimplifiedVertexFillColor : style.simplifiedVertexFillColor,
				style.selectedSimplifiedVertexFillColor );

		final double x = vertex.getX();
		final double y = vertex.getY();
		g2.setColor( fillColor );
		final int ox = ( int ) x - ( int ) spotradius;
		final int oy = ( int ) y - ( int ) spotradius;
		final int ow = 2 * ( int ) spotradius;
		g2.fillOval( ox, oy, ow, ow );
	}

	protected void drawVertexFull( final Graphics2D g2, final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();


		// TODO: FIX!!!
		// TODO: FIX!!!
		// TODO: FIX!!!
		// TODO: FIX!!!
		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean selected = vertex.isSelected() || highlighted; // TODO: FIX!!!
		// TODO: FIX!!!
		// TODO: FIX!!!
		// TODO: FIX!!!
		// TODO: FIX!!!

		double spotdiameter = Math.min( vertex.getVertexDist() - 10.0, maxDisplayVertexSize );
		if ( highlighted )
			spotdiameter += 10.0;
		if ( disappear )
			spotdiameter *= ( 1 + ratio );
		final double spotradius = spotdiameter / 2;

		final Color fillColor = getColor( selected, transition, ratio, style.vertexFillColor, style.selectedVertexFillColor );
		final Color drawColor = getColor( selected, transition, ratio, style.vertexDrawColor, style.selectedVertexDrawColor );

		final double x = vertex.getX();
		final double y = vertex.getY();
		final int ox = ( int ) x - ( int ) spotradius;
		final int oy = ( int ) y - ( int ) spotradius;
		final int sd = 2 * ( int ) spotradius;
		g2.setColor( fillColor );
		g2.fillOval( ox, oy, sd, sd );
		g2.setColor( drawColor );
		g2.drawOval( ox, oy, sd, sd );

		final int maxLabelLength = ( int ) ( spotdiameter / avgLabelLetterWidth );
		if ( maxLabelLength > 2 && !disappear )
		{
			String label = vertex.getLabel();
			if ( label.length() > maxLabelLength )
				label = label.substring( 0, maxLabelLength - 2 ) + "...";

			final FontRenderContext frc = g2.getFontRenderContext();
			final TextLayout layout = new TextLayout( label, style.font, frc );
			final Rectangle2D bounds = layout.getBounds();
			final float tx = ( float ) ( x - bounds.getCenterX() );
			final float ty = ( float ) ( y - bounds.getCenterY() );
			layout.draw( g2, tx, ty );
		}
	}

	protected Color getColor(
			final boolean isSelected,
			final Transition transition,
			final double completionRatio,
			final Color normalColor,
			final Color selectedColor )
	{
		if ( transition == NONE )
			return isSelected ? selectedColor : normalColor;
		else
		{
			int r = normalColor.getRed();
			int g = normalColor.getGreen();
			int b = normalColor.getBlue();
			final double ratio = transition == DISAPPEAR ? completionRatio : 1 - completionRatio;
			if ( isSelected )
			{
				r = ( int ) ( ratio * r + ( 1 - ratio ) * selectedColor.getRed() );
				g = ( int ) ( ratio * g + ( 1 - ratio ) * selectedColor.getGreen() );
				b = ( int ) ( ratio * b + ( 1 - ratio ) * selectedColor.getBlue() );
			}
			return new Color( r, g, b, ( int ) ( 255 * ( 1 - ratio ) ) );
		}
	}
}
