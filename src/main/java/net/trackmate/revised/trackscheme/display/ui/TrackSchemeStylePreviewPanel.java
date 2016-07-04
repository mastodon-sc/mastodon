package net.trackmate.revised.trackscheme.display.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import net.trackmate.revised.trackscheme.ScreenColumn;
import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;

public class TrackSchemeStylePreviewPanel extends JComponent
{

	private final TrackSchemeStyle style;

	public TrackSchemeStylePreviewPanel()
	{
		style = TrackSchemeStyle.defaultStyle();
	}

	@Override
	protected void paintComponent( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;
		paintBackground( g2 );
	}

	protected void paintBackground( final Graphics2D g2 )
	{
		final int width = getWidth();
		final int height = getHeight();

		g2.setColor( style.backgroundColor );
		g2.fillRect( 0, 0, width, height );

		final int yScale = 30;
		final double minY = 0;
		final int headerHeight = 25;

		if ( style.highlightCurrentTimepoint )
		{
			final double t = 3;
			final int y = ( int ) Math.round( yScale * ( t - minY - 0.5 ) ) + headerHeight;
			final int h = Math.max( 1, Math.round( yScale ) );
			g2.setColor( style.currentTimepointColor );
			g2.fillRect( 0, y, width, h );
		}

		if ( style.paintRows )
		{
			g2.setColor( style.decorationColor );

			final int tstart = 0;
			final int tend = 5;

			for ( int t = tstart; t < tend; t++ )
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

			final ScreenColumn[] columns = new ScreenColumn[] {
					new ScreenColumn( "A", 0, 10 ),
					new ScreenColumn( "B", 1, 30 ),
					new ScreenColumn( "C", 4, 20 )
			};
			for ( final ScreenColumn column : columns )
			{
				g2.drawLine( column.xLeft, 0, column.xLeft, height );
				g2.drawLine( column.xLeft + column.width, 0, column.xLeft + column.width, height );
			}
		}
	}

}
