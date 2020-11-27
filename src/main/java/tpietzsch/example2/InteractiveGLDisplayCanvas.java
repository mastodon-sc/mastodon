package tpietzsch.example2;

import bdv.TransformEventHandler;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import org.scijava.listeners.Listeners;

/*
 * {@code InteractiveGLDisplayCanvas} has a {@code TransformEventHandler} that is notified when the component size is changed.
 * <p>
 * {@link #addHandler}/{@link #removeHandler} provide simplified adding/removing of handlers that implement {@code MouseListener}, {@code KeyListener}, etc.
 *
 * @author Tobias Pietzsch
 */
public class InteractiveGLDisplayCanvas extends GLCanvas
{
	/**
	 * Mouse/Keyboard handler that manipulates the view transformation.
	 */
	private TransformEventHandler handler;

	/**
	 * Draw something to a {@link Graphics} canvas and receive notifications about
	 * changes of the canvas size.
	 *
	 * @author Tobias Pietzsch
	 */
	public interface CanvasSizeListener
	{
		/**
		 * This is called, when the screen size of the canvas (the component
		 * displaying the image and generating mouse events) changes. This can be
		 * used to determine scale of overlay or screen coordinates relative to the
		 * border.
		 *
		 * @param width
		 *            the new canvas width.
		 * @param height
		 *            the new canvas height.
		 */
		void setCanvasSize( int width, int height );
	}

	final private Listeners.List< CanvasSizeListener > canvasSizeListeners;

	/**
	 * Create a new {@code InteractiveDisplayCanvas}.
	 *
	 * @param width
	 *            preferred component width.
	 * @param height
	 *            preferred component height.
	 */
	public InteractiveGLDisplayCanvas( final int width, final int height )
	{
		super( new GLCapabilities( GLProfile.getMaxProgrammableCore( true ) ) );
		setPreferredSize( new Dimension( width, height ) );
		setFocusable( true );

		canvasSizeListeners = new Listeners.SynchronizedList<>( r -> r.setCanvasSize( getWidth(), getHeight() ) );

		addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				final int w = getWidth();
				final int h = getHeight();
				// NB: Update of canvasSizeListeners needs to happen before update of handler
				// Otherwise repaint might start before the render target receives the size change.
				canvasSizeListeners.list.forEach( r -> r.setCanvasSize( w, h ) );
				if ( handler != null )
					handler.setCanvasSize( w, h, true );
				// enableEvents( AWTEvent.MOUSE_MOTION_EVENT_MASK );
			}
		} );

		addMouseListener( new MouseAdapter()
		{
			@Override
			public void mousePressed( final MouseEvent e )
			{
				requestFocusInWindow();
			}
		} );
	}

	/**
	 * CanvasSizeListeners can be added/removed here.
	 */
	public Listeners< CanvasSizeListener > canvasSizeListeners()
	{
		return canvasSizeListeners;
	}

	/**
	 * Add new event handler. Depending on the interfaces implemented by
	 * <code>handler</code> calls {@link Component#addKeyListener(KeyListener)},
	 * {@link Component#addMouseListener(MouseListener)},
	 * {@link Component#addMouseMotionListener(MouseMotionListener)},
	 * {@link Component#addMouseWheelListener(MouseWheelListener)}.
	 *
	 * @param h handler to remove
	 */
	public void addHandler( final Object h )
	{
		if ( h instanceof KeyListener )
			addKeyListener( ( KeyListener ) h );

		if ( h instanceof MouseMotionListener )
			addMouseMotionListener( ( MouseMotionListener ) h );

		if ( h instanceof MouseListener )
			addMouseListener( ( MouseListener ) h );

		if ( h instanceof MouseWheelListener )
			addMouseWheelListener( ( MouseWheelListener ) h );

		if ( h instanceof FocusListener )
			addFocusListener( ( FocusListener ) h );
	}

	/**
	 * Remove an event handler. Add new event handler. Depending on the
	 * interfaces implemented by <code>handler</code> calls
	 * {@link Component#removeKeyListener(KeyListener)},
	 * {@link Component#removeMouseListener(MouseListener)},
	 * {@link Component#removeMouseMotionListener(MouseMotionListener)},
	 * {@link Component#removeMouseWheelListener(MouseWheelListener)}.
	 *
	 * @param h handler to remove
	 */
	public void removeHandler( final Object h )
	{
		if ( h instanceof KeyListener )
			removeKeyListener( ( KeyListener ) h );

		if ( h instanceof MouseMotionListener )
			removeMouseMotionListener( ( MouseMotionListener ) h );

		if ( h instanceof MouseListener )
			removeMouseListener( ( MouseListener ) h );

		if ( h instanceof MouseWheelListener )
			removeMouseWheelListener( ( MouseWheelListener ) h );

		if ( h instanceof FocusListener )
			removeFocusListener( ( FocusListener ) h );
	}

	/**
	 * Set the {@link TransformEventHandler} that will be notified when component is resized.
	 *
	 * @param transformEventHandler
	 *            handler to use
	 */
	public void setTransformEventHandler( final TransformEventHandler transformEventHandler )
	{
		if ( handler != null )
			removeHandler( handler );
		handler = transformEventHandler;
		int w = getWidth();
		int h = getHeight();
		if ( w <= 0 || h <= 0 )
		{
			final Dimension preferred = getPreferredSize();
			w = preferred.width;
			h = preferred.height;
		}
		handler.setCanvasSize( w, h, false );
		addHandler( handler );
	}
}
