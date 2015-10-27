package net.trackmate.revised.trackscheme.display;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.ScreenTransform;

public class ScreenTransformEventHandler extends MouseAdapter implements KeyListener, TransformEventHandler< ScreenTransform >
{
	final static private TransformEventHandlerFactory< ScreenTransform > factory = new TransformEventHandlerFactory< ScreenTransform >()
	{
		@Override
		public TransformEventHandler< ScreenTransform > create( final TransformListener< ScreenTransform > transformListener )
		{
			return new ScreenTransformEventHandler( transformListener );
		}
	};

	public static TransformEventHandlerFactory< ScreenTransform > factory()
	{
		return factory;
	}

	/**
	 * Current source to screen transform.
	 */
	final protected ScreenTransform transform = new ScreenTransform();

	/**
	 * Copy of {@link #affine current transform} when mouse dragging
	 * started.
	 */
	final protected ScreenTransform transformDragStart = new ScreenTransform();

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
	 * Screen coordinates to keep centered while zooming or rotating with
	 * the keyboard. For example set these to
	 * <em>(screen-width/2, screen-height/2)</em>
	 */
	protected int centerX = 0, centerY = 0;

	public ScreenTransformEventHandler( final TransformListener< ScreenTransform > listener )
	{
		this.listener = listener;
	}

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
			transform.setScreenSize( canvasW, canvasH );
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

	// ================ KeyListener =============================

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
	{}

	// ================ MouseAdapter ============================

	@Override
	public void mousePressed( final MouseEvent e )
	{
		oX = e.getX();
		oY = e.getY();
		synchronized ( transform )
		{
			transformDragStart.set( transform );
		}
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		final int modifiers = e.getModifiersEx();
		if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
		{
			synchronized ( transform )
			{
				final int dX = oX - e.getX();
				final int dY = oY - e.getY();
				transform.set( transformDragStart );
				transform.shift( dX, dY );
			}
			update();
		}
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		synchronized ( transform )
		{
			final double dScale = 1.1;
			final int modifiers = e.getModifiersEx();
			final int s = e.getWheelRotation();
			final boolean ctrlPressed = ( modifiers & KeyEvent.CTRL_DOWN_MASK ) != 0;
			final boolean altPressed = ( modifiers & KeyEvent.ALT_DOWN_MASK ) != 0;
			final boolean metaPressed = ( ( modifiers & KeyEvent.META_DOWN_MASK ) != 0 ) || ( ctrlPressed && shiftPressed );
			if ( metaPressed ) // zoom both axes
			{
				if ( s > 0 )
					transform.zoom( 1.0 / dScale, e.getX(), e.getY() );
				else
					transform.zoom( dScale, e.getX(), e.getY() );
			}
			else if ( shiftPressed ) // zoom X axis
			{
				if ( s > 0 )
					transform.zoomX( 1.0 / dScale, e.getX() );
				else
					transform.zoomX( dScale, e.getX() );
			}
			else if ( ctrlPressed || altPressed ) // zoom Y axis
			{
				if ( s > 0 )
					transform.zoomY( 1.0 / dScale, e.getY() );
				else
					transform.zoomY( dScale, e.getY() );
			}
			else
			{
				final int d = s * 15;
				transform.set( transformDragStart );
				if ( ( modifiers & KeyEvent.SHIFT_DOWN_MASK ) != 0 )
					transform.shiftX( d );
				else
					transform.shiftY( d );
			}
			update();
		}
	}

}