package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread.Paintable;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;
import net.trackmate.trackscheme.animate.AbstractTransformAnimator;

public class DefaultTransformHandler implements MouseListener, MouseWheelListener, KeyListener, MouseMotionListener, TransformEventHandler< ScreenTransform >, Paintable
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

	private boolean zoomStarted = false;

	/**
	 * Current source to screen transform.
	 */
	final protected ScreenTransform transform = new ScreenTransform();

	/**
	 * Copy of current transform when mouse dragging started.
	 */
	protected ScreenTransform transformDragStart = new ScreenTransform();

	/**
	 * Whom to notify when the current transform is changed.
	 */
	protected TransformListener< ScreenTransform > listener;

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

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

	private AbstractTransformAnimator< ScreenTransform > transformAnimator;

	private final ZoomBoxHandler zoomBoxHandler;

	private InertiaHandler inertiaHandler;

	public DefaultTransformHandler( final TransformListener< ScreenTransform > transformListener )
	{
		listener = transformListener;
		zoomBoxHandler = new ZoomBoxHandler();
		inertiaHandler = new InertiaHandler();
		overlay = new Overlay();
	}

	public void setInertiaEnabled( final boolean inertiaEnabled )
	{
		if ( !inertiaEnabled )
		{
			inertiaHandler = null;
			if ( null != transformAnimator )
			{
				synchronized ( transformAnimator )
				{
					transformAnimator = null;
				}
			}
		}
		else
		{
			inertiaHandler = new InertiaHandler();
		}
	}

	public void moveTo( final double x, final int y )
	{
		if ( inertiaHandler != null )
		{
			inertiaHandler.moveTo( x, y );
		}
		else
		{
			synchronized ( transform )
			{
				final double deltaX = transform.maxX - transform.minX;
				transform.minX = x - deltaX / 2;
				transform.maxX = x + deltaX / 2;
				final double deltaY = transform.maxY - transform.minY;
				transform.minY = y - deltaY / 2;
				transform.maxY = y + deltaY / 2;
				update();
			}
		}
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
		final int modifiers = e.getModifiersEx();
		if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
		{
			oX = e.getX();
			oY = e.getY();
			vx0 = 0.;
			vy0 = 0.;
			if ( null != transformAnimator )
			{
				synchronized ( transformAnimator )
				{
					transformAnimator = null;
				}
			}
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

		if ( ( modifiers == MouseEvent.BUTTON2_MASK ) || ( modifiers == MouseEvent.BUTTON3_MASK ) ) // translate
		{
			if ( inertiaHandler != null )
			{
				inertiaHandler.drift();
			}
		}
		else if ( zoomBoxHandler != null )
		{
			zoomBoxHandler.mouseReleased( e );
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
		else if ( zoomBoxHandler != null )
		{
			zoomBoxHandler.mouseDragged( e );
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

			if ( inertiaHandler != null )
			{
				inertiaHandler.zoom( zoomSteps, zoomOut, zoomX, zoomY, e.getX(), e.getY() );
			}
			else
			{
				final double dScale = 1.1;
				if ( zoomX && zoomY )
				{
					if ( zoomOut )
						transform.scale( 1.0 / dScale, e.getX(), e.getY() );
					else
						transform.scale( dScale, e.getX(), e.getY() );
				}
				else if ( zoomX && !zoomY ) // zoom X axis
				{
					if ( zoomOut )
						transform.scaleX( 1.0 / dScale, e.getX(), e.getY() );
					else
						transform.scaleX( dScale, e.getX(), e.getY() );
				}
				else if ( !zoomX && zoomY ) // zoom Y axis
				{
					if ( zoomOut )
						transform.scaleY( 1.0 / dScale, e.getX(), e.getY() );
					else
						transform.scaleY( dScale, e.getX(), e.getY() );
				}
				update();
			}
		}
		else
		{
			/*
			 * Scroll.
			 */

			final boolean dirX = ( modifiers & KeyEvent.SHIFT_DOWN_MASK ) != 0;

			if ( inertiaHandler != null )
			{
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
				inertiaHandler.drift();
			}
			else
			{
				final int d = s * 15;
				synchronized ( transform )
				{
					transform.setScreenTranslated( dirX ? d : 0, dirX ? 0 : d, transform );
				}
			}
		}
		update();
	}

	private boolean shiftPressed = false;

	private final OverlayRenderer overlay;

	@Override
	public void keyPressed( final KeyEvent e )
	{
		if ( e.getKeyCode() == KeyEvent.VK_SHIFT )
			shiftPressed = true;

		if ( e.getKeyCode() == KeyEvent.VK_I )
		{
			setInertiaEnabled( inertiaHandler == null );
			if ( inertiaHandler == null && transformAnimator != null )
			{
				synchronized ( transformAnimator )
				{
					transformAnimator = null;
				}
			}
		}
	}

	@Override
	public void keyReleased( final KeyEvent e )
	{
		if ( e.getKeyCode() == KeyEvent.VK_SHIFT )
			shiftPressed = false;
	}

	@Override
	public void keyTyped( final KeyEvent e )
	{}

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

	public static TransformEventHandlerFactory< ScreenTransform > factory()
	{
		return new TransformEventHandlerFactory< ScreenTransform >()
		{
			@Override
			public TransformEventHandler< ScreenTransform > create( final TransformListener< ScreenTransform > transformListener )
			{
				final DefaultTransformHandler handler = new DefaultTransformHandler( transformListener );
				return handler;
			}
		};
	}

	protected OverlayRenderer getOverlay()
	{
		return overlay;
	}

	@Override
	public void paint()
	{
		if ( transformAnimator != null )
			synchronized ( transformAnimator )
			{
				final ScreenTransform t = transformAnimator.getCurrent( System.currentTimeMillis() );
				transform.set( t );
				update();
				if ( transformAnimator.isComplete() )
					transformAnimator = null;
			}
	}

	private class ZoomBoxHandler extends MouseAdapter
	{

		private static final int MOUSE_MASK = InputEvent.BUTTON1_DOWN_MASK + InputEvent.ALT_DOWN_MASK;

		@Override
		public void mouseDragged( final MouseEvent e )
		{
			if ( ( e.getModifiersEx() == MOUSE_MASK ) && !zoomStarted )
			{
				oX = e.getX();
				oY = e.getY();
				zoomStarted = true;
			}
			eX = e.getX();
			eY = e.getY();
			update();
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
				update();
			}
		}
	}

	private class InertiaHandler
	{

		private void drift()
		{
			if ( Math.abs( vx0 ) > 0 || Math.abs( vy0 ) > 0 )
			{
				transformAnimator = new InertialTranslationAnimator( transform, vx0, vy0, 500 );
				update();
			}
		}

		private void moveTo( final double x, final int y )
		{
			transformAnimator = new TranslationAnimator( transform, x, y, 200 );
			update();
		}

		private void zoom( final int zoomSteps, final boolean zoomOut, final boolean zoomX, final boolean zoomY, final int x, final int y )
		{
			transformAnimator = new InertialZoomAnimator( transform,
					zoomSteps, zoomOut, zoomX, zoomY, x, y, 500 );
			update();
		}
	}

	private class Overlay implements OverlayRenderer
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
