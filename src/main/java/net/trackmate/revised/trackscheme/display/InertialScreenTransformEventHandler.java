package net.trackmate.revised.trackscheme.display;

import gnu.trove.list.array.TIntArrayList;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Timer;
import java.util.TimerTask;

import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.LineageTreeLayout.LayoutListener;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.display.animate.AbstractTransformAnimator;
import net.trackmate.revised.trackscheme.display.animate.InertialTranslationAnimator;
import net.trackmate.revised.trackscheme.display.animate.InertialZoomAnimator;
import net.trackmate.revised.trackscheme.display.animate.UpdaterThread;
import net.trackmate.revised.trackscheme.display.animate.UpdaterThread.Updatable;

public class InertialScreenTransformEventHandler extends MouseAdapter implements KeyListener, TransformEventHandler< ScreenTransform >, LayoutListener
{
	final static private TransformEventHandlerFactory< ScreenTransform > factory = new TransformEventHandlerFactory< ScreenTransform >()
	{
		@Override
		public TransformEventHandler< ScreenTransform > create( final TransformListener< ScreenTransform > transformListener )
		{
			return new InertialScreenTransformEventHandler( transformListener );
		}
	};

	/**
	 * The delay in ms between inertial movements updates.
	 */
	private static final long INERTIAL_ANIMATION_PERIOD = 20;

	/**
	 * Sets the maximal zoom level in X.
	 */
	private static final double MIN_SIBLINGS_ON_CANVAS = 3.;

	/**
	 * Sets the maximal zoom level in Y.
	 */
	private static final double MIN_TIMEPOINTS_ON_CANVAS = 3.;

	public static TransformEventHandlerFactory< ScreenTransform > factory()
	{
		return factory;
	}

	/**
	 * Current source to screen transform.
	 */
	// Startup with a decent zoom level.
	final protected ScreenTransform transform = new ScreenTransform( 0, 20, 0, 10, 800, 600 );

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

	private final UpdaterThread updaterThread;

	private AbstractTransformAnimator< ScreenTransform > animator;

	public InertialScreenTransformEventHandler( final TransformListener< ScreenTransform > listener )
	{
		this.listener = listener;
		this.updaterThread = new UpdaterThread( new Updatable()
		{
			@Override
			public void update()
			{
				final long t = System.currentTimeMillis();
				final ScreenTransform c = animator.getCurrent( t );
				synchronized ( transform )
				{
					transform.set( c );
				}
				InertialScreenTransformEventHandler.this.update();
			}
		} );
		updaterThread.start();
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
		final double[] screenPosC = new double[] { centerX, centerY };
		final double[] layoutPosC = new double[ 2 ];
		transform.applyInverse( layoutPosC, screenPosC );

		final double sxratio = transform.getScaleX() / canvasW;
		final double syratio = transform.getScaleY() / canvasH;
		final double tlx = layoutPosC[ 0 ] - boundXMin;
		final double tly = layoutPosC[ 1 ] - boundYMin;
		final double brx = -layoutPosC[ 0 ] + boundXMax;
		final double bry = -layoutPosC[ 1 ] + boundYMax;
		if ( tlx < 0 || tly < 0 || brx < 0 || bry < 0 || sxratio > 1 / MIN_SIBLINGS_ON_CANVAS || syratio > 1 / MIN_TIMEPOINTS_ON_CANVAS )
		{
			synchronized ( transform )
			{
				if ( tlx < 0 )
					transform.shiftLayoutX( -tlx );
				if ( tly < 0 )
					transform.shiftLayoutY( -tly );
				if ( brx < 0 )
					transform.shiftLayoutX( brx );
				if ( bry < 0 )
					transform.shiftLayoutY( bry );
				if ( sxratio > 1 / MIN_SIBLINGS_ON_CANVAS )
					transform.zoomX( sxratio * MIN_SIBLINGS_ON_CANVAS, canvasW / 2 );
				if ( syratio > 1 / MIN_TIMEPOINTS_ON_CANVAS )
					transform.zoomY( syratio * MIN_TIMEPOINTS_ON_CANVAS, canvasH / 2 );

			}
		}

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

	private double x0;

	private double y0;

	private long t0;

	private double vx0;

	private double vy0;

	@Override
	public void mousePressed( final MouseEvent e )
	{
		oX = e.getX();
		oY = e.getY();
		synchronized ( transform )
		{
			transformDragStart.set( transform );
		}
		animator = null;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		vx0 = 0;
		vy0 = 0;
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		final int modifiers = e.getModifiersEx();
		if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
		{

			final long t = e.getWhen();
			if ( t > t0 )
			{
				final double x = transformDragStart.screenToLayoutX( e.getX() );
				final double y = transformDragStart.screenToLayoutY( e.getY() );
				final long dt = t-t0;
				vx0 = ( x - x0 ) / dt;
				vy0 = ( y - y0 ) / dt;
				x0 = x;
				y0 = y;
				t0 = t;
			}

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
	public void mouseReleased( final MouseEvent e )
	{
		final int modifiers = e.getModifiers();
		if ( ( modifiers == MouseEvent.BUTTON2_MASK ) || ( modifiers == MouseEvent.BUTTON3_MASK ) ) // translate
		{
			animator = new InertialTranslationAnimator( transform, vx0, vy0, 400 );
			final Timer timer = new Timer();
			timer.schedule( new TimerTask()
			{
				@Override
				public void run()
				{
					if ( null == animator || animator.isComplete() )
					{
						timer.cancel();
					}
					else
					{
						updaterThread.requestUpdate();
					}
				}
			}, 0, INERTIAL_ANIMATION_PERIOD );
		}
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		synchronized ( transform )
		{
			final int s = e.getWheelRotation();
			final double dScale = 1.1;
			final int modifiers = e.getModifiersEx();
			final boolean ctrlPressed = ( modifiers & KeyEvent.CTRL_DOWN_MASK ) != 0;
			final boolean altPressed = ( modifiers & KeyEvent.ALT_DOWN_MASK ) != 0;
			final boolean metaPressed = ( ( modifiers & KeyEvent.META_DOWN_MASK ) != 0 ) || ( ctrlPressed && shiftPressed );

			final boolean zoomX = shiftPressed;
			final boolean zoomY = ctrlPressed || altPressed;
			final boolean zoomXY = metaPressed;
			final boolean zoom = zoomX || zoomY || zoomXY;

			if ( zoom )
			{
				final int eX = e.getX();
				final int eY = e.getY();
				final boolean zoomOut = s > 0;

				if ( zoomXY ) // zoom both axes
				{
					if ( zoomOut )
						transform.zoom( 1.0 / dScale, eX, eY );
					else
						transform.zoom( dScale, eX, eY );
				}
				else if ( zoomX ) // zoom X axis
				{
					if ( zoomOut )
						transform.zoomX( 1.0 / dScale, eX );
					else
						transform.zoomX( dScale, eX );
				}
				else
				{
					// zoom Y axis
					if ( zoomOut )
						transform.zoomY( 1.0 / dScale, eY );
					else
						transform.zoomY( dScale, eY );
				}
				animator = new InertialZoomAnimator( transform, !zoomOut ? -s : s, zoomOut, zoomX, zoomY, eX, eY, 400 );
				final Timer timer = new Timer();
				timer.schedule( new TimerTask()
				{
					@Override
					public void run()
					{
						if ( null == animator || animator.isComplete() )
						{
							timer.cancel();
						}
						else
						{
							updaterThread.requestUpdate();
						}
					}
				}, 0, INERTIAL_ANIMATION_PERIOD );
			}
			else
			{
				final int d = s * 15;
				if ( ( modifiers & KeyEvent.SHIFT_DOWN_MASK ) != 0 )
					transform.shiftX( d );
				else
					transform.shiftY( d );
			}
			update();
		}
	}

	private double boundXMax = 1.;

	private double boundYMax = 1.;

	private double boundXMin = 2.;

	private double boundYMin = 2.;

	@Override
	public void layoutChanged( final LineageTreeLayout layout )
	{
		boundXMin = layout.getCurrentLayoutMinX();
		boundXMax = layout.getCurrentLayoutMaxX();
		final TIntArrayList timepoints = layout.getTimepoints();
		boundYMin = timepoints.getQuick( 0 );
		boundYMax = timepoints.getQuick( timepoints.size() - 1 );
	}

}