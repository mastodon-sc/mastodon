package net.trackmate.trackscheme;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;

public class ScreenTransform
{
	double minX;

	double maxX;

	double minY;

	double maxY;

	int screenWidth;

	int screenHeight;

	public ScreenTransform()
	{}

	public ScreenTransform( final double minX, final double maxX, final double minY, final double maxY, final int screenWidth, final int screenHeight )
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	public ScreenTransform copy()
	{
		return new ScreenTransform( minX, maxX, minY, maxY, screenWidth, screenHeight );
	}

	public void set( final ScreenTransform t )
	{
		this.minX = t.minX;
		this.maxX = t.maxX;
		this.minY = t.minY;
		this.maxY = t.maxY;
		this.screenWidth = t.screenWidth;
		this.screenHeight = t.screenHeight;
	}

	void setScreenTranslated( final int dX, final int dY, final ScreenTransform source )
	{
		final double xInvScale = ( maxX - minX ) / ( screenWidth - 1 );
		final double yInvScale = ( maxY - minY ) / ( screenHeight - 1 );
		minX = source.minX + xInvScale * dX;
		maxX = source.maxX + xInvScale * dX;
		minY = source.minY + yInvScale * dY;
		maxY = source.maxY + yInvScale * dY;
	}

	double screenToLayoutX( final int x )
	{
		final double xInvScale = ( maxX - minX ) / ( screenWidth - 1 );
		return minX + xInvScale * x;
	}

	double screenToLayoutY( final int y )
	{
		final double yInvScale = ( maxY - minY ) / ( screenHeight - 1 );
		return minY + yInvScale * y;
	}

	void scale( final double scale, final int x, final int y )
	{
		final double lX = screenToLayoutX( x );
		final double lY = screenToLayoutY( y );
		final double newSizeX = ( maxX - minX ) * scale;
		final double newSizeY = ( maxY - minY ) * scale;
		final double newXInvScale = newSizeX / ( screenWidth - 1 );
		final double newYInvScale = newSizeY / ( screenHeight - 1 );
		minX = lX - newXInvScale * x;
		maxX = minX + newSizeX;
		minY = lY - newYInvScale * y;
		maxY = minY + newSizeY;
	}

	void scaleX( final double scale, final int x, final int y )
	{
		final double lX = screenToLayoutX( x );
		final double newSizeX = ( maxX - minX ) * scale;
		final double newXInvScale = newSizeX / ( screenWidth - 1 );
		minX = lX - newXInvScale * x;
		maxX = minX + newSizeX;
	}

	void scaleY( final double scale, final int x, final int y )
	{
		final double lY = screenToLayoutY( y );
		final double newSizeY = ( maxY - minY ) * scale;
		final double newYInvScale = newSizeY / ( screenHeight - 1 );
		minY = lY - newYInvScale * y;
		maxY = minY + newSizeY;
	}

	@Override
	public String toString()
	{
		return "X: " + minX + " -> " + maxX + ", Y: " + minY + " -> " + maxY + ", width = " + screenWidth + ", height = " + screenHeight;
	}

	public static class ScreenTransformEventHandler extends MouseAdapter implements KeyListener, TransformEventHandler< ScreenTransform >
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

		@Override
		public void keyTyped( final KeyEvent e )
		{}

		// ================ MouseAdapter ============================

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
		public void mouseDragged( final MouseEvent e )
		{
			final int modifiers = e.getModifiersEx();
			if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
			{
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
		public void keyPressed( final KeyEvent e )
		{}

		@Override
		public void keyReleased( final KeyEvent e )
		{}
	}
}
