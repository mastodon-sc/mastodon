package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;

public class ZoomBoxHandler extends MouseAdapter implements TransformListener< ScreenTransform >
{

	private static final int MOUSE_MASK = InputEvent.BUTTON1_DOWN_MASK +  InputEvent.ALT_DOWN_MASK;
	
	private final ZoomBoxOverlay zoomOverlay = new ZoomBoxOverlay();

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

	private final TransformEventHandler<ScreenTransform> transformEventHandler;

	private boolean zoomStarted = false;

	private ScreenTransform transform;

	private final ShowTrackScheme trackscheme;

	public ZoomBoxHandler( TransformEventHandler<ScreenTransform> transformEventHandler, ShowTrackScheme selectionListener )
	{
		this.transformEventHandler = transformEventHandler;
		this.trackscheme = selectionListener;
	}

	@Override
	public void mouseDragged( MouseEvent e )
	{
		if ( (e.getModifiersEx() == MOUSE_MASK ) && !zoomStarted )
		{
			oX = e.getX();
			oY = e.getY();
			zoomStarted = true;
		}
		eX = e.getX();
		eY = e.getY();
		trackscheme.refresh();
	}

	@Override
	public void mouseReleased( MouseEvent e )
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

			transformEventHandler.setTransform( transform );
			trackscheme.refresh();
		}
	}

	@Override
	public void transformChanged( ScreenTransform transform )
	{
		this.transform = transform;
	}

	public void setTransform( ScreenTransform transform )
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
		public void drawOverlays( Graphics g )
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
		public void setCanvasSize( int width, int height )
		{}

	}
}
