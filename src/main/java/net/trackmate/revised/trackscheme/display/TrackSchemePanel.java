package net.trackmate.revised.trackscheme.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.UIManager;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;
import net.trackmate.graph.listenable.GraphChangeListener;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.LineageTreeLayout.LayoutListener;
import net.trackmate.revised.trackscheme.ScreenEntities;
import net.trackmate.revised.trackscheme.ScreenEntitiesInterpolator;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.TrackSchemeOptions.Values;
import net.trackmate.revised.trackscheme.display.laf.DefaultTrackSchemeOverlay;
import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.NavigationListener;
import net.trackmate.revised.ui.selection.SelectionListener;
import net.trackmate.trackscheme.animate.AbstractAnimator;
import bdv.viewer.TimePointListener;

public class TrackSchemePanel extends JPanel implements
		TransformListener< ScreenTransform >,
		PainterThread.Paintable,
		HighlightListener,
		TimePointListener,
		GraphChangeListener,
		SelectionListener,
		NavigationListener< TrackSchemeVertex >
{

	private static final long ANIMATION_MILLISECONDS = 250;

	private final TrackSchemeGraph< ?, ? > graph;

	/**
	 * trackscheme options.
	 */
	private final Values options;

	/**
	 * Canvas used for displaying the trackscheme graph.
	 */
	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final JScrollBar xScrollBar;

	private final JScrollBar yScrollBar;

	/**
	 * The current transform from layout to screen coordinates.
	 */
	private final ScreenTransform screenTransform;

	/**
	 * layout the {@link TrackSchemeGraph} into layout coordinates.
	 */
	private final LineageTreeLayout layout;

	/**
	 * compute {@link ScreenEntities} from {@link LineageTreeLayout} using the
	 * current {@link ScreenTransform} and interpolate between
	 * {@link ScreenEntities} for animation.
	 */
	private final ScreenEntityAnimator entityAnimator;

	private final PainterThread painterThread;

	private final AbstractTrackSchemeOverlay graphOverlay;

	/**
	 * TODO
	 */
	// TODO rename
	private final Flags flags;

	/**
	 * Minimum timepoint in dataset.
	 */
	private double layoutMinY;

	/**
	 * Maximum timepoint in dataset.
	 */
	private double layoutMaxY;

	/**
	 * Minimum layoutX coordinate in current layout.
	 */
	private double layoutMinX;

	/**
	 * <aximum layoutX coordinate in current layout.
	 */
	private double layoutMaxX;

	/**
	 * Ratio of {@link #xScrollBar} values to layoutX coordinates.
	 */
	private double xScrollScale;

	/**
	 * Ratio of {@link #yScrollBar} values to layoutY (timepoint) coordinates.
	 */
	private double yScrollScale;

	/**
	 * If {@code true}, then scroll-bar {@link AdjustmentListener}s ignore
	 * events (when {@link #screenTransform} is changed by means other than the
	 * user dragging the scroll-bar).
	 */
	private boolean ignoreScrollBarChanges;

	private final TrackSchemeSelection selection;

	private final TrackSchemeNavigation navigation;

	public TrackSchemePanel(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeSelection selection,
			final TrackSchemeNavigation navigation,
			final TrackSchemeOptions optional )
	{
		super( new BorderLayout(), false );
		this.graph = graph;
		this.selection = selection;
		this.navigation = navigation;
		options = optional.values;

		graph.addGraphChangeListener( this );
		navigation.addNavigationListener( this );

		final int w = options.getWidth();
		final int h = options.getHeight();
		display = new InteractiveDisplayCanvasComponent< ScreenTransform >(	w, h, options.getTransformEventHandlerFactory() );
		display.addTransformListener( this );


		highlight.addHighlightListener( this );
		selection.addSelectionListener( this );

		graphOverlay = new DefaultTrackSchemeOverlay( graph, highlight, optional );
		display.addOverlayRenderer( graphOverlay );

		// This should be the last OverlayRenderer in display.
		// It triggers repainting if there is currently an ongoing animation.
		display.addOverlayRenderer( new OverlayRenderer()
		{
			@Override
			public void setCanvasSize( final int width, final int height )
			{}

			@Override
			public void drawOverlays( final Graphics g )
			{
				checkAnimate();
			}
		} );

		screenTransform = new ScreenTransform();
		layout = new LineageTreeLayout( graph );
		final TransformEventHandler< ScreenTransform > tevl = display.getTransformEventHandler();
		if ( tevl instanceof LayoutListener )
		{
			final LayoutListener ll = ( LayoutListener ) tevl;
			layout.addLayoutListener( ll );
		}
		entityAnimator = new ScreenEntityAnimator();
		painterThread = new PainterThread( this );
		flags = new Flags();

		display.addMouseMotionListener( new MouseHighlightHandler( graphOverlay, highlight ) );

		final MouseSelectionHandler mouseSelectionHandler = new MouseSelectionHandler( graphOverlay, selection, display, layout, graph );
		display.addHandler( mouseSelectionHandler );
		display.addOverlayRenderer( mouseSelectionHandler );

		xScrollBar = new JScrollBar( JScrollBar.HORIZONTAL );
		yScrollBar = new JScrollBar( JScrollBar.VERTICAL );
		xScrollBar.addAdjustmentListener( new AdjustmentListener()
		{
			@Override
			public void adjustmentValueChanged( final AdjustmentEvent e )
			{
				if ( ignoreScrollBarChanges )
					return;

				final double s = xScrollBar.getValue() / xScrollScale;
				synchronized ( screenTransform )
				{
					screenTransform.shiftLayoutX( s - screenTransform.getMinX() );
					display.getTransformEventHandler().setTransform( screenTransform );
				}
				flags.setTransformChanged();
				painterThread.requestRepaint();
			}
		} );
		yScrollBar.addAdjustmentListener( new AdjustmentListener()
		{
			@Override
			public void adjustmentValueChanged( final AdjustmentEvent e )
			{
				if ( ignoreScrollBarChanges )
					return;

				final double s = yScrollBar.getValue() / yScrollScale;
				synchronized ( screenTransform )
				{
					screenTransform.shiftLayoutY( s - screenTransform.getMinY() );
					display.getTransformEventHandler().setTransform( screenTransform );
				}
				flags.setTransformChanged();
				painterThread.requestRepaint();
			}
		} );


		add( display, BorderLayout.CENTER );

		add( yScrollBar, BorderLayout.EAST );
		final JPanel xScrollPanel = new JPanel( new BorderLayout() );
		xScrollPanel.add( xScrollBar, BorderLayout.CENTER );
		final int space = ( Integer ) UIManager.getDefaults().get( "ScrollBar.width" );
		xScrollPanel.add( Box.createRigidArea( new Dimension( space, 0 ) ), BorderLayout.EAST );
		add( xScrollPanel, BorderLayout.SOUTH );

		painterThread.start();
	}

	/**
	 * Stop the {@link #painterThread}.
	 */
	public void stop()
	{
		painterThread.interrupt();
	}

	/**
	 * Set the timepoint range of the dataset.
	 *
	 * @param minTimepoint
	 * @param maxTimepoint
	 */
	public void setTimepointRange( final int minTimepoint, final int maxTimepoint )
	{
		layoutMinY = minTimepoint;
		layoutMaxY = maxTimepoint;
		graphOverlay.setTimepointRange( minTimepoint, maxTimepoint );
	}

	/**
	 * request repainting if there is currently an ongoing animation.
	 */
	void checkAnimate()
	{
		if ( !entityAnimator.isComplete() )
			painterThread.requestRepaint();
	}

	@Override
	public void paint()
	{
		final ScreenTransform transform = new ScreenTransform();
		synchronized( screenTransform )
		{
			transform.set( screenTransform );
		}

		final Flags flags = this.flags.clear();
		if ( flags.graphChanged )
		{
			layout.layout();
			layoutMinX = layout.getCurrentLayoutMinX();
			layoutMaxX = layout.getCurrentLayoutMaxX();
			entityAnimator.startAnimation( transform, ANIMATION_MILLISECONDS );
		}
		else if ( flags.transformChanged || flags.selectionChanged )
		{
			entityAnimator.startAnimation( transform, 0 );
		}
		else if ( flags.contextChanged )
		{
			System.out.println( "if ( flags.contextChanged ): NOT IMPLEMENTED ");
		}

		entityAnimator.setTime( System.currentTimeMillis() );
		entityAnimator.setPaintEntities( graphOverlay );
		display.repaint();

		// adjust scrollbars sizes
		final ScreenTransform t = new ScreenTransform();
		entityAnimator.getLastComputedScreenEntities().getScreenTransform( t );
		xScrollScale = 10000.0 / ( layoutMaxX - layoutMinX + 2 );
		final int xval = ( int ) ( xScrollScale * t.getMinX() );
		final int xext = ( int ) ( xScrollScale * ( t.getMaxX() - t.getMinX() ) );
		final int xmin = ( int ) ( xScrollScale * ( layoutMinX - 1 ) );
		final int xmax = ( int ) ( xScrollScale * ( layoutMaxX + 1 ) );
		yScrollScale = 10000.0 / ( layoutMaxY - layoutMinY + 2 );
		final int yval = ( int ) ( yScrollScale * t.getMinY() );
		final int yext = ( int ) ( yScrollScale * ( t.getMaxY() - t.getMinY() ) );
		final int ymin = ( int ) ( yScrollScale * ( layoutMinY - 1 ) );
		final int ymax = ( int ) ( yScrollScale * ( layoutMaxY + 1 ) );
		ignoreScrollBarChanges = true;
		xScrollBar.setValues( xval, xext, xmin, xmax );
		yScrollBar.setValues( yval, yext, ymin, ymax );
		ignoreScrollBarChanges = false;
	}

	@Override
	public void timePointChanged( final int timepoint )
	{
		graphOverlay.setCurrentTimepoint( timepoint );
		display.repaint();
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		synchronized( screenTransform )
		{
			screenTransform.set( transform );
		}
		flags.setTransformChanged();
		painterThread.requestRepaint();
	}

	@Override
	public void graphChanged()
	{
		flags.setGraphChanged();
		painterThread.requestRepaint();
	}

	@Override
	public void highlightChanged()
	{
		display.repaint();
	}

	@Override
	public void selectionChanged()
	{
		flags.setSelectionChanged();
		painterThread.requestRepaint();
	}

	@Override
	public void navigateToVertex( final TrackSchemeVertex v )
	{
		final double lx = v.getLayoutX();
		final double ly = v.getTimepoint();

		/*
		 * TODO: This will fail if there is a different transform event handler.
		 * TODO: Also it shouldn't require display here.
		 * TODO: Instead it should be routed through a new interface that forwards to InertialScreenTransformEventHandler or does whatever is appropriate (e.g. ignores it).
		 */
		final InertialScreenTransformEventHandler transformEventHandler = ( InertialScreenTransformEventHandler ) display.getTransformEventHandler();

		transformEventHandler.centerOn( lx, ly );
	}

	protected InteractiveDisplayCanvasComponent< ScreenTransform > getDisplay()
	{
		return display;
	}

	protected LineageTreeLayout getLineageTreeLayout()
	{
		return layout;
	}

	protected class ScreenEntityAnimator extends AbstractAnimator
	{
		private ScreenEntities screenEntities;

		private ScreenEntities screenEntities2;

		private final ScreenEntities screenEntitiesIpStart;

		private ScreenEntities screenEntitiesIpEnd;

		private ScreenEntitiesInterpolator interpolator;

		private ScreenEntities lastComputedScreenEntities;

		private final int capacity = 1000;

		public ScreenEntityAnimator()
		{
			super( 0 );
			screenEntities = new ScreenEntities( graph, capacity );
			screenEntities2 = new ScreenEntities( graph, capacity );
			screenEntitiesIpStart = new ScreenEntities( graph, capacity );
			screenEntitiesIpEnd = new ScreenEntities( graph, capacity );
			interpolator = null;
			lastComputedScreenEntities = screenEntities;
		}

		/**
		 * Swap screenEntities and screenEntities2.
		 */
		private void swapPools()
		{
			final ScreenEntities tmp = screenEntities;
			screenEntities = screenEntities2;
			screenEntities2 = tmp;
			screenEntities.clear();
		}

		/**
		 * Swap screenEntities and screenEntitiesIpStart.
		 */
		private void copyIpStart()
		{
			screenEntitiesIpStart.set( lastComputedScreenEntities );
			screenEntities.clear();
		}

		/**
		 * Swap screenEntities and screenEntitiesIpEnd.
		 */
		private void swapIpEnd()
		{
			final ScreenEntities tmp = screenEntities;
			screenEntities = screenEntitiesIpEnd;
			screenEntitiesIpEnd = tmp;
			screenEntities.clear();
		}

		/**
		 *
		 * @param transform
		 * @param duration animation duration (in time units), may be 0.
		 */
		public void startAnimation( final ScreenTransform transform, final long duration )
		{
			reset( duration );
			if (duration > 0 )
			{
				copyIpStart();
				layout.cropAndScale( transform, screenEntities );
				swapIpEnd();
				interpolator = new ScreenEntitiesInterpolator( screenEntitiesIpStart, screenEntitiesIpEnd );
			}
			else
			{
				interpolator = null;
				swapPools();
				layout.cropAndScale( transform, screenEntities );
			}
			lastComputedScreenEntities = screenEntities;
		}

		@Override
		public void setTime( final long time )
		{
			super.setTime( time );
			if ( interpolator != null )
			{
				swapPools();
				interpolator.interpolate( ratioComplete(), screenEntities );
				if ( isComplete() )
					interpolator = null;
				lastComputedScreenEntities = screenEntities;
			}
		}

		/**
		 * Set entities for painting into the specified double-buffered
		 * {@link AbstractTrackSchemeOverlay}. (This swaps
		 * {@link #screenEntities} with pending entities from the overlay.)
		 */
		public void setPaintEntities( final AbstractTrackSchemeOverlay overlay )
		{
			final ScreenEntities tmp = overlay.setScreenEntities( screenEntities );
			if ( tmp == null )
				screenEntities = new ScreenEntities( graph, capacity );
			else
				screenEntities = tmp;
			screenEntities.clear();
		}

		private ScreenEntities getLastComputedScreenEntities()
		{
			return lastComputedScreenEntities;
		}
	}

	protected static class Flags
	{
		private boolean transformChanged;
		private boolean selectionChanged;
		private boolean graphChanged;
		private boolean contextChanged;

		public Flags()
		{
			transformChanged = false;
			selectionChanged = false;
			graphChanged = false;
			contextChanged = false;
		}

		public Flags( final Flags f )
		{
			transformChanged = f.transformChanged;
			selectionChanged = f.selectionChanged;
			graphChanged = f.graphChanged;
			contextChanged = f.contextChanged;
		}

		public synchronized void setTransformChanged()
		{
			transformChanged = true;
		}

		public synchronized void setSelectionChanged()
		{
			selectionChanged = true;
		}

		public synchronized void setGraphChanged()
		{
			graphChanged = true;
		}

		public synchronized void setContextChanged()
		{
			contextChanged = true;
		}

		public synchronized Flags clear()
		{
			final Flags copy = new Flags( this );
			transformChanged = false;
			selectionChanged = false;
			graphChanged = false;
			contextChanged = false;
			return copy;
		}
	}
}
