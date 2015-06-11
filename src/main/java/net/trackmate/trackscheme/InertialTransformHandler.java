package net.trackmate.trackscheme;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class InertialTransformHandler implements MouseListener, MouseWheelListener, KeyListener, MouseMotionListener
{

	/**
	 * Speed at which the screen scrolls when using the mouse wheel.
	 */
	private static final double MOUSEWHEEL_SCROLL_SPEED = -2e-4;

	/**
	 * Speed at which the zoom changes when using the mouse wheel.
	 */
	private static final double MOUSEWHEEL_ZOOM_SPEED = 1d;

	private double vx0;

	private double vy0;

	private double x0;

	private double y0;

	private long t0;

	private ScreenTransform transform;

	private final ShowTrackScheme trackscheme;

	public InertialTransformHandler( final ShowTrackScheme trackscheme )
	{
		this.trackscheme = trackscheme;
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
		this.transform = trackscheme.canvas.getTransformEventHandler().getTransform().copy();
		// TODO maybe this class should become the transformeventhandler.
		vx0 = 0;
		vy0 = 0;
	}

	@Override
	public synchronized void mouseReleased( final MouseEvent e )
	{
		final int modifiers = e.getModifiers();
		if ( ( modifiers & ( MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK ) ) != 0 ) // translate
		{
			if ( Math.abs( vx0 ) > 0 || Math.abs( vy0 ) > 0 )
			{
				trackscheme.transformAnimator = new InertialTranslationAnimator( trackscheme.canvas.getTransformEventHandler().getTransform(), vx0, vy0, 500 );
				trackscheme.refresh();
			}
		}
	};

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		final int modifiers = e.getModifiersEx();
		if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
		{
			final long t = System.currentTimeMillis();
			final double x = transform.screenToLayoutX( e.getX() );
			final double y = transform.screenToLayoutY( e.getY() );
			vx0 = ( x - x0 ) / ( ( double ) t - t0 );
			vy0 = ( y - y0 ) / ( ( double ) t - t0 );
			x0 = x;
			y0 = y;
			t0 = t;
		}
	}

	@Override
	public synchronized void mouseWheelMoved( final MouseWheelEvent e )
	{

		final int modifiers = e.getModifiersEx();
		final int s = e.getWheelRotation();
		final boolean ctrlPressed = ( modifiers & KeyEvent.CTRL_DOWN_MASK ) != 0;
		final boolean altPressed = ( modifiers & KeyEvent.ALT_DOWN_MASK ) != 0;
		final boolean metaPressed = ( ( modifiers & KeyEvent.META_DOWN_MASK ) != 0 ) || ( ctrlPressed && shiftPressed );

		final ScreenTransform currentTransform = trackscheme.canvas.getTransformEventHandler().getTransform();

		if ( metaPressed || shiftPressed || ctrlPressed || altPressed )
		{
			/*
			 * Zoom.
			 */

			final boolean zoomOut = s < 0;
			final int zoomSteps = ( int ) ( MOUSEWHEEL_ZOOM_SPEED * Math.abs( s ) );
			final boolean zoomX, zoomY;
			if ( metaPressed ) // zoom both axes
			{
				zoomX = true;
				zoomY = true;
			}
			else if ( shiftPressed ) // zoom X axis
			{
				zoomX = true;
				zoomY = false;
			}
			else if ( ctrlPressed || altPressed ) // zoom Y axis
			{
				zoomX = false;
				zoomY = true;
			}
			else
			{
				zoomX = false;
				zoomY = false;
			}

			trackscheme.transformAnimator = new InertialZoomAnimator( currentTransform,
					zoomSteps, zoomOut, zoomX, zoomY, e.getX(), e.getY(), 500 );
		}
		else
		{
			/*
			 * Scroll.
			 */

			final boolean dirX = ( modifiers & KeyEvent.SHIFT_DOWN_MASK ) != 0;
			if ( dirX )
			{
				vx0 = s * ( currentTransform.maxX - currentTransform.minX ) * MOUSEWHEEL_SCROLL_SPEED;
				vy0 = 0;
			}
			else
			{
				vx0 = 0;
				vy0 = s * ( currentTransform.maxY - currentTransform.minY ) * MOUSEWHEEL_SCROLL_SPEED;
			}
			trackscheme.transformAnimator = new InertialTranslationAnimator( currentTransform, vx0, vy0, 500 );
		}

		trackscheme.refresh();
	}

	private boolean shiftPressed = false;

	@Override
	public void keyPressed( final KeyEvent e )
	{
		if ( e.getKeyCode() == KeyEvent.VK_SHIFT )
			shiftPressed = true;
	}

	@Override
	public void keyReleased( final KeyEvent e )
	{
		if ( e.getKeyCode() == KeyEvent.VK_SHIFT )
			shiftPressed = false;
	}

	@Override
	public void keyTyped( final KeyEvent e )
	{
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mouseMoved( final MouseEvent arg0 )
	{}
}
