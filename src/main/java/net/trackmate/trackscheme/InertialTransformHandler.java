package net.trackmate.trackscheme;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;

public class InertialTransformHandler implements MouseListener, MouseWheelListener, KeyListener, MouseMotionListener, TransformEventHandler< ScreenTransform >
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

	private ShowTrackScheme trackscheme;

	/**
	 * Current source to screen transform.
	 */
	final protected ScreenTransform transform = new ScreenTransform();

	/**
	 * Copy of {@link #affine current transform} when mouse dragging started.
	 */
	protected ScreenTransform transformDragStart = new ScreenTransform();

	/**
	 * Whom to notify when the current transform is changed.
	 */
	protected TransformListener< ScreenTransform > listener;

	/**
	 * Coordinates where mouse dragging started.
	 */
	protected int oX, oY;

	/**
	 * The screen size of the canvas (the component displaying the image and
	 * generating mouse events).
	 */
	protected int canvasW = 1, canvasH = 1;

	/**
	 * Screen coordinates to keep centered while zooming or rotating with the
	 * keyboard. For example set these to
	 * <em>(screen-width/2, screen-height/2)</em>
	 */
	protected int centerX = 0, centerY = 0;

	public InertialTransformHandler( final TransformListener< ScreenTransform > transformListener )
	{
		listener = transformListener;
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
		final int modifiers = e.getModifiersEx();
		if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
		{
			oX = e.getX();
			oY = e.getY();
		}
		synchronized ( transform )
		{
			transformDragStart.set( transform );
		}
	}

	@Override
	public synchronized void mouseReleased( final MouseEvent e )
	{
		final int modifiers = e.getModifiers();
		if ( ( modifiers & ( MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK ) ) != 0 ) // translate
		{
			if ( Math.abs( vx0 ) > 0 || Math.abs( vy0 ) > 0 )
			{
				trackscheme.transformAnimator = new InertialTranslationAnimator( transform, vx0, vy0, 500 );
				update();
			}
		}
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		final int modifiers = e.getModifiersEx();
		if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
		{
			final long t = System.currentTimeMillis();
			if ( t > t0 )
			{
				final double x = transformDragStart.screenToLayoutX( e.getX() );
				final double y = transformDragStart.screenToLayoutY( e.getY() );
				vx0 = ( x - x0 ) / ( ( double ) t - t0 );
				vy0 = ( y - y0 ) / ( ( double ) t - t0 );
				x0 = x;
				y0 = y;
				t0 = t;
			}

			synchronized ( transform )
			{
				final int dX = oX - e.getX();
				final int dY = oY - e.getY();
				transform.setScreenTranslated( dX, dY, transformDragStart );
			}
			update();
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

			trackscheme.transformAnimator = new InertialZoomAnimator( transform,
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
				vx0 = s * ( transform.maxX - transform.minX ) * MOUSEWHEEL_SCROLL_SPEED;
				vy0 = 0;
			}
			else
			{
				vx0 = 0;
				vy0 = s * ( transform.maxY - transform.minY ) * MOUSEWHEEL_SCROLL_SPEED;
			}
			trackscheme.transformAnimator = new InertialTranslationAnimator( transform, vx0, vy0, 500 );
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

	@Override
	public ScreenTransform getTransform()
	{
		synchronized ( transform )
		{
			return transform.copy();
		}
	}

	@Override
	public void setTransform( final ScreenTransform t )
	{
		synchronized ( transform )
		{
			transform.set( t );
		}
	}

	@Override
	public void setCanvasSize( final int width, final int height, final boolean updateTransform )
	{
		canvasW = width;
		canvasH = height;
		centerX = width / 2;
		centerY = height / 2;
		synchronized ( transform )
		{
			transform.screenWidth = canvasW;
			transform.screenHeight = canvasH;
			update();
		}
	}

	@Override
	public void setTransformListener( final TransformListener< ScreenTransform > transformListener )
	{
		listener = transformListener;
	}

	@Override
	public String getHelpString()
	{
		return null;
	}

	/**
	 * notifies {@link #listener} that the current transform changed.
	 */
	protected void update()
	{
		if ( listener != null )
			listener.transformChanged( transform );
	}

	public static TransformEventHandlerFactory< ScreenTransform > factory( final ShowTrackScheme trackscheme )
	{
		return new TransformEventHandlerFactory< ScreenTransform >()
		{
			@Override
			public TransformEventHandler< ScreenTransform > create( final TransformListener< ScreenTransform > transformListener )
			{
				final InertialTransformHandler handler = new InertialTransformHandler( transformListener );
				handler.trackscheme = trackscheme;
				return handler;
			}
		};
	}

}
