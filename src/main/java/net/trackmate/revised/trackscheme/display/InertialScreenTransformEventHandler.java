package net.trackmate.revised.trackscheme.display;

import java.util.Timer;
import java.util.TimerTask;

import bdv.BehaviourTransformEventHandler;
import bdv.behaviour.Behaviour;
import bdv.behaviour.BehaviourMap;
import bdv.behaviour.DragBehaviour;
import bdv.behaviour.InputTriggerAdder;
import bdv.behaviour.InputTriggerMap;
import bdv.behaviour.ScrollBehaviour;
import bdv.behaviour.io.InputTriggerConfig;
import bdv.viewer.TriggerBehaviourBindings;
import gnu.trove.list.array.TIntArrayList;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.LineageTreeLayout.LayoutListener;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.display.animate.AbstractTransformAnimator;
import net.trackmate.revised.trackscheme.display.animate.InertialScreenTransformAnimator;

public class InertialScreenTransformEventHandler
	implements
		BehaviourTransformEventHandler< ScreenTransform >,
		TransformEventHandler< ScreenTransform >,
		LayoutListener
{
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

	public static TransformEventHandlerFactory< ScreenTransform > factory( final InputTriggerConfig config )
	{
		return new TransformEventHandlerFactory< ScreenTransform >()
		{
			@Override
			public TransformEventHandler< ScreenTransform > create( final TransformListener< ScreenTransform > transformListener )
			{
				return new InertialScreenTransformEventHandler( transformListener, config );
			}
		};
	}

	private final BehaviourMap behaviourMap = new BehaviourMap();

	private final InputTriggerMap inputMap = new InputTriggerMap();

	private final InputTriggerAdder inputAdder;

	/**
	 * Current source to screen transform.
	 */
	// Startup with a decent zoom level.
	final private ScreenTransform transform = new ScreenTransform( 0, 20, 0, 10, 800, 600 );

	/**
	 * Copy of {@link #transform current transform} when mouse dragging
	 * started.
	 */
	final private ScreenTransform transformDragStart = new ScreenTransform();

	/**
	 * Whom to notify when the current transform is changed.
	 */
	private TransformListener< ScreenTransform > listener;

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * The screen size of the canvas (the component displaying the image and
	 * generating mouse events).
	 */
	private int canvasW = 1, canvasH = 1;

	/**
	 * Screen coordinates to keep centered while zooming or rotating with
	 * the keyboard. For example set these to
	 * <em>(screen-width/2, screen-height/2)</em>
	 */
	private int centerX = 0, centerY = 0;

	/**
	 * Timer that runs {@link #currentTimerTask}.
	 */
	private final Timer timer;

	/**
	 * The task running the current animation.
	 */
	private TimerTask currentTimerTask;

	private AbstractTransformAnimator< ScreenTransform > animator;

	public InertialScreenTransformEventHandler( final TransformListener< ScreenTransform > listener, final InputTriggerConfig config )
	{
		this.listener = listener;

		timer = new Timer( "TrackScheme transform animation", true );
		currentTimerTask = null;

		final String DRAG_TRANSLATE = "drag translate";
		final String SCROLL_TRANSLATE = "scroll translate";
		final String ZOOM_X = "zoom horizontal";
		final String ZOOM_Y = "zoom vertical";
		final String ZOOM_XY = "zoom";

		inputAdder = config.inputTriggerAdder( inputMap, "ts" );

		new TranslateDragBehaviour( DRAG_TRANSLATE, "button2", "button3" ).register();
		new TranslateScrollBehaviour( SCROLL_TRANSLATE, "scroll" ).register();
		new ZoomScrollBehaviour( ZOOM_X, ScrollAxis.X, "shift scroll" ).register();
		new ZoomScrollBehaviour( ZOOM_Y, ScrollAxis.Y, "ctrl scroll", "alt scroll" ).register();
		new ZoomScrollBehaviour( ZOOM_XY, ScrollAxis.XY, "meta scroll", "ctrl shift scroll" ).register();
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
			notifyListeners();
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

	@Override
	public void install( final TriggerBehaviourBindings bindings )
	{
		bindings.addBehaviourMap( "transform", behaviourMap );
		bindings.addInputTriggerMap( "transform", inputMap );
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

	/**
	 * notifies {@link #listener} that the current transform changed.
	 */
	private void notifyListeners()
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

	private abstract class SelfRegisteringBehaviour implements Behaviour
	{
		private final String name;

		private final String[] defaultTriggers;

		public SelfRegisteringBehaviour( final String name, final String ... defaultTriggers )
		{
			this.name = name;
			this.defaultTriggers = defaultTriggers;
		}

		public void register()
		{
			behaviourMap.put( name, this );
			inputAdder.put( name, defaultTriggers );
		}
	}

	private class TranslateDragBehaviour extends SelfRegisteringBehaviour implements DragBehaviour
	{
		public TranslateDragBehaviour( final String name, final String... defaultTriggers )
		{
			super( name, defaultTriggers );
		}

		final private ScreenTransform previousTransform = new ScreenTransform();
		private long previousTime = 0;
		private long dt = 0;

		@Override
		public void init( final int x, final int y )
		{
			final long t = System.currentTimeMillis();
			synchronized ( transform )
			{
				oX = x;
				oY = y;
				transformDragStart.set( transform );
				previousTransform.set( transform );
				previousTime = t;
				dt = 0;
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			final long t = System.currentTimeMillis();
			synchronized ( transform )
			{
				if ( t > previousTime )
				{
					previousTransform.set( transform );
					dt = t - previousTime;
					previousTime = t;
				}

				final int dX = oX - x;
				final int dY = oY - y;
				transform.set( transformDragStart );
				transform.shift( dX, dY );
			}
			notifyListeners();
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( dt > 0 )
			{
				synchronized ( transform )
				{
					animator = new InertialScreenTransformAnimator( previousTransform, transform, dt, 400 );
				}
				runAnimation();
			}
		}
	}

	private static enum ScrollAxis
	{
		X, Y, XY
	}

	private class ZoomScrollBehaviour extends SelfRegisteringBehaviour implements ScrollBehaviour
	{
		private final ScrollAxis axis;

		public ZoomScrollBehaviour( final String name, final ScrollAxis axis, final String... defaultTriggers )
		{
			super( name, defaultTriggers );
			this.axis = axis;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			final double dScale = 1.1;
			final boolean zoomOut = wheelRotation > 0;

			synchronized ( transform )
			{
				switch ( axis )
				{
				case X: // zoom X axis
					if ( zoomOut )
						transform.zoomX( 1.0 / dScale, x );
					else
						transform.zoomX( dScale, x );
					break;
				case Y: // zoom Y axis
					if ( zoomOut )
						transform.zoomY( 1.0 / dScale, y );
					else
						transform.zoomY( dScale, y );
					break;
				default:
				case XY: // zoom both axes
					if ( zoomOut )
						transform.zoom( 1.0 / dScale, x, y );
					else
						transform.zoom( dScale, x, y );
				}
			}
			notifyListeners();

//			animator = new InertialZoomAnimator( transform, !zoomOut ? -s : s, zoomOut, zoomX, zoomY, eX, eY, 400 );
//			runAnimation();

		}
	}

	private class TranslateScrollBehaviour extends SelfRegisteringBehaviour implements ScrollBehaviour
	{
		public TranslateScrollBehaviour( final String name, final String... defaultTriggers )
		{
			super( name, defaultTriggers );
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( transform )
			{
				final double d = wheelRotation * 15;
				if ( isHorizontal )
					transform.shiftX( d );
				else
					transform.shiftY( d );
			}
			notifyListeners();
		}
	}

	public void centerOn( final double lx, final double ly )
	{
		final double minX = transform.getMinX();
		final double maxX = transform.getMaxX();
		final double cx = ( maxX + minX ) / 2;
		final double dx = lx - cx;

		final double minY = transform.getMinY();
		final double maxY = transform.getMaxY();
		final double cy = ( maxY + minY ) / 2;
		final double dy = ly - cy;

		final ScreenTransform tstart = new ScreenTransform( transform );
		final ScreenTransform tend = new ScreenTransform( transform );
		tend.shiftLayoutX( dx );
		tend.shiftLayoutY( dy );

		animator = new AbstractTransformAnimator< ScreenTransform >( 200 )
		{
			@Override
			protected ScreenTransform get( final double t )
			{
				transform.interpolate( tstart, tend, t );
				return transform;
			}
		};
		runAnimation();
	}

	private void animate()
	{
		final long t = System.currentTimeMillis();
		final ScreenTransform c = animator.getCurrent( t );
		synchronized ( transform )
		{
			transform.set( c );
		}
		notifyListeners();
	}

	private synchronized void runAnimation()
	{
		if ( currentTimerTask != null )
			currentTimerTask.cancel();
		timer.purge();
		currentTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if ( null == animator || animator.isComplete() )
				{
					cancel();
					currentTimerTask = null;
				}
				else
				{
					animate();
				}
			}
		};
		timer.schedule( currentTimerTask, 0, INERTIAL_ANIMATION_PERIOD );
	}
}
