package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

public class CanvasOverlay implements OverlayRenderer, TransformListener< ScreenTransform >
{

	/**
	 * The font used to draw text.
	 */
	private final Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	/**
	 * If the time rows are smaller than this size in pixels, they won't be
	 * drawn.
	 */
	private static final int MIN_TIMELINE_SPACING = 20;

	private static final Color WIDGET_COLOR = Color.YELLOW.darker().darker();

	/**
	 * Y position for columns labels.
	 */
	private static final int YTEXT = 20;

	/**
	 * X position for time labels.
	 */
	private static final int XTEXT = 20;

	private double minX;

	private double maxX;

	private double minY;

	private double maxY;

	private int screenWidth;

	private int screenHeight;

	private double yScale;

	private double xScale;

	private final boolean paintRows = true;

	private final boolean paintColumns = false;

	private final LineageTreeLayout layout;

	private final VertexOrder order;

	public CanvasOverlay( final LineageTreeLayout layout, final VertexOrder order )
	{
		this.layout = layout;
		this.order = order;
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		g.setColor( WIDGET_COLOR );
		final FontMetrics fm = g.getFontMetrics( font );
		g.setFont( font );

		int minLineY = 0;
		int maxLineY = screenHeight;

		if ( paintRows )
		{
			final int fontInc = fm.getHeight() / 2;
			final int stepT = 1 + MIN_TIMELINE_SPACING / ( int ) ( 1 + yScale );
			final int maxTimePoint = order.getMaxTimepoint();

			int tstart = Math.max( 0, ( int ) minY - 1 );
			tstart = ( tstart / stepT ) * stepT;
			int tend = 1 + Math.min( maxTimePoint, ( int ) maxY );
			tend = ( 1 + tend / stepT ) * stepT;

			final int maxLineX = screenWidth;
//			final int maxLineX = Math.min( screenWidth,
//					( int ) ( ( layout.columns.get( layout.columns.size() - 1 ) - minX - 0.5 ) * xScale ) );

			minLineY = ( int ) ( ( tstart - minY - 0.5 ) * yScale );
			maxLineY = ( int ) ( ( tend - minY - 0.5 ) * yScale );

			for ( int t = tstart; t < tend; t = t + stepT )
			{
				final int yline = ( int ) ( ( t - minY - 0.5 ) * yScale );
				g.drawLine( 0, yline, maxLineX, yline );

				final int ytext = ( int ) ( ( t - minY + stepT / 2 ) * yScale ) + fontInc;
				if ( ytext < 2 * YTEXT )
				{
					continue;
				}
				g.drawString( "" + t, XTEXT, ytext );
			}

			// Last line
			final int yline = ( int ) ( ( tend - minY - 0.5 ) * yScale );
			g.drawLine( 0, yline, maxLineX, yline );

		}

		if ( paintColumns )
		{
			int minC = layout.columns.binarySearch( minX );
			if ( minC < 0 )
			{
				minC = -1 - minC;
			}
			minC = Math.max( 0, minC - 1 ); // at least 1 column out

			int maxC = layout.columns.binarySearch( maxX + 0.5, minC, layout.columns.size() );
			if ( maxC < 0 )
			{
				maxC = -1 - maxC;
			}
			maxC = Math.min( layout.columns.size(), maxC + 1 );

			for ( int c = minC; c < maxC; c++ )
			{
				final double col = layout.columns.get( c );
				final int xline = ( int ) ( ( col - minX - 0.5 ) * xScale );
				if ( xline < 2 * XTEXT )
				{
					continue;
				}
				g.drawLine( xline, minLineY, xline, maxLineY );

				if ( c < 1 )
				{
					continue;
				}
				final int xprevline = ( int ) ( ( layout.columns.get( c - 1 ) - minX - 0.5 ) * xScale );
				final String str = layout.columnNames.get( c - 1 );
				final int stringWidth = fm.stringWidth( str );

				final int columnWidth = xline - xprevline;
				if ( columnWidth < stringWidth + 5 )
				{
					continue;
				}

				final int xtext = ( Math.min( screenWidth, xline ) + Math.max( 0, xprevline ) - stringWidth ) / 2;
				if ( xtext < 2 * XTEXT )
				{
					continue;
				}
				g.drawString( str, xtext, YTEXT );
			}

		}
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		minX = transform.minX;
		maxX = transform.maxX;
		minY = transform.minY;
		maxY = transform.maxY;
		screenWidth = transform.screenWidth;
		screenHeight = transform.screenHeight;
		yScale = ( screenHeight - 1 ) / ( maxY - minY );
		xScale = ( screenWidth - 1 ) / ( maxX - minX );
	}

}
