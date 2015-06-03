package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

public class ZoomBoxHandler extends MouseAdapter implements TransformListener< ScreenTransform >
{

	private final ZoomBoxOverlay zoomOverlay = new ZoomBoxOverlay();

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

	private boolean zoomStarted = false;

	private ScreenTransform transform;

	private final ShowTrackScheme trackscheme;

	public ZoomBoxHandler( final ShowTrackScheme trackscheme )
	{
		this.trackscheme = trackscheme;
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		if ( e.getButton() == MouseEvent.BUTTON1 && ( e.getModifiersEx() & InputEvent.ALT_DOWN_MASK ) == InputEvent.ALT_DOWN_MASK && !zoomStarted )
		{
			oX = e.getX();
			oY = e.getY();
			zoomStarted = true;
		}
		eX = e.getX();
		eY = e.getY();
		trackscheme.repaint();
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		if ( zoomStarted )
		{
			zoomStarted = false;

			final double minX = transform.screenToLayoutX( oX );
			final double minY = transform.screenToLayoutY( oY );
			final double maxX = transform.screenToLayoutX( eX );
			final double maxY = transform.screenToLayoutY( eY );

			transform.maxX = Math.max( maxX, minX ) + 0.1;
			transform.minX = Math.min( maxX, minX );
			transform.maxY = Math.max( maxY, minY ) + 0.1;
			transform.minY = Math.min( maxY, minY );

			trackscheme.transformChanged( transform );
		}
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		this.transform = transform;
	}

	public void setTransform( final ScreenTransform transform )
	{
		this.transform = transform;
	}

	public OverlayRenderer getZoomOverlay()
	{
		return zoomOverlay;
	}

	public class ZoomBoxOverlay implements OverlayRenderer
	{
		private final Color ZOOM_BOX_COLOR = Color.BLUE.brighter();

		@Override
		public void drawOverlays( final Graphics g )
		{
			if ( zoomStarted )
			{
				g.setColor( ZOOM_BOX_COLOR );
				final int x = Math.min( oX, eX );
				final int y = Math.min( oY, eY );
				final int width = Math.abs( eX - oX );
				final int height = Math.abs( eY - oY );
				g.drawRect( x, y, width, height );
			}
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}
}
