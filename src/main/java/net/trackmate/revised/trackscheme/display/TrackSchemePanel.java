package net.trackmate.revised.trackscheme.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.UIManager;

import bdv.viewer.TimePointListener;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;
import net.trackmate.graph.listenable.GraphChangeListener;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.LineageTreeLayout.LayoutListener;
import net.trackmate.revised.trackscheme.ScreenColumn;
import net.trackmate.revised.trackscheme.ScreenEntities;
import net.trackmate.revised.trackscheme.ScreenEntitiesInterpolator;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.TrackSchemeOptions.Values;
import net.trackmate.revised.trackscheme.display.laf.DefaultTrackSchemeOverlay;
import net.trackmate.revised.trackscheme.util.TrackSchemeUtil;
import net.trackmate.revised.ui.selection.FocusListener;
import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.NavigationListener;
import net.trackmate.revised.ui.selection.SelectionListener;
import net.trackmate.trackscheme.animate.AbstractAnimator;

public class TrackSchemePanel extends JPanel implements
		TransformListener< ScreenTransform >,
		PainterThread.Paintable,
		HighlightListener,
		FocusListener,
		TimePointListener,
		GraphChangeListener,
		SelectionListener,
		NavigationListener< TrackSchemeVertex >
{

	private static final long ANIMATION_MILLISECONDS = 250;

	private static final int HEADER_WIDTH = 25;

	private static final int HEADER_HEIGHT = 20;

	/**
	 * If the time rows are smaller than this size in pixels, they won't be
	 * drawn.
	 */
	private static final int MIN_TIMELINE_SPACING = 20;

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
	 * Maximum layoutX coordinate in current layout.
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

//	private final TrackSchemeSelection selection;

	private final TrackSchemeFocus focus;

	private final TrackSchemeNavigator navigator;

	private final SelectionBehaviours selectionBehaviours;

	private final JPanel columnHeader;

	private final JPanel rowHeader;

	public TrackSchemePanel(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeFocus focus,
			final TrackSchemeSelection selection,
			final TrackSchemeNavigation navigation,
			final TrackSchemeOptions optional )
	{
		super( new BorderLayout(), false );
		this.graph = graph;
		this.focus = focus;
//		this.selection = selection;
		options = optional.values;

		graph.addGraphChangeListener( this );
		navigation.addNavigationListener( this );

		final int w = options.getWidth();
		final int h = options.getHeight();
		display = new InteractiveDisplayCanvasComponent< ScreenTransform >(	w, h, options.getTransformEventHandlerFactory() );
		display.addTransformListener( this );

		highlight.addHighlightListener( this );
		focus.addFocusListener( this );
		selection.addSelectionListener( this );

		layout = new LineageTreeLayout( graph );

		graphOverlay = new DefaultTrackSchemeOverlay( graph, highlight, focus, optional );
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
		final TransformEventHandler< ScreenTransform > tevl = display.getTransformEventHandler();
		if ( tevl instanceof LayoutListener )
		{
			final LayoutListener ll = ( LayoutListener ) tevl;
			layout.addLayoutListener( ll );
		}
		entityAnimator = new ScreenEntityAnimator();
		painterThread = new PainterThread( this );
		flags = new Flags();

		final MouseHighlightHandler highlightHandler = new MouseHighlightHandler( graphOverlay, highlight, graph );
		display.addMouseMotionListener( highlightHandler );
		display.addTransformListener( highlightHandler );

		navigator = new TrackSchemeNavigator( graph, layout, focus, navigation, selection );
		display.addTransformListener( navigator );

		selectionBehaviours = new SelectionBehaviours( display, graph, layout, graphOverlay, focus, navigation, selection );
		display.addTransformListener( selectionBehaviours );

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
		xScrollPanel.add( Box.createRigidArea( new Dimension( HEADER_WIDTH, 0 ) ), BorderLayout.WEST );
		add( xScrollPanel, BorderLayout.SOUTH );

		columnHeader = new JPanel()
		{
			@Override
			protected void paintComponent( final Graphics g )
			{
				final Graphics2D g2 = ( Graphics2D ) g;
				g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

				g2.drawLine( 0, 0, 0, getHeight() );

				final FontMetrics fm = g.getFontMetrics( graphOverlay.getFont() );
				g2.setFont( graphOverlay.getFont() );

				// Avoid ConcurrentModificationException. We assume there is a
				// small (<1000) number of columns.
				final ScreenColumn[] it = entityAnimator.getLastComputedScreenEntities().getColumns().toArray( new ScreenColumn[] {} );
				for ( final ScreenColumn column : it )
				{
					g2.drawLine( column.xLeft, 0, column.xLeft, getWidth() );
					g2.drawLine( column.xLeft + column.width, 0, column.xLeft + column.width, getWidth() );

					final String str = column.label;
					final int stringWidth = fm.stringWidth( str );
					if ( column.width < stringWidth + 5 || ( getWidth() - column.xLeft ) < stringWidth + 5 )
						continue;

					final int xtext = ( Math.min( column.xLeft + column.width, getWidth() ) + Math.max( 0, column.xLeft ) - stringWidth ) / 2;
					if ( xtext < stringWidth / 2 )
						continue;

					g.drawString( str, xtext, HEADER_HEIGHT / 2 );
				}
			}
		};
		columnHeader.setPreferredSize( new Dimension( HEADER_WIDTH, HEADER_HEIGHT ) );
		columnHeader.setOpaque( false );
		final JPanel columnHeaderPanel = new JPanel( new BorderLayout() );
		columnHeaderPanel.add( Box.createRigidArea( new Dimension( HEADER_WIDTH, 0 ) ), BorderLayout.WEST );
		columnHeaderPanel.add( columnHeader, BorderLayout.CENTER );
		columnHeaderPanel.add( Box.createRigidArea( new Dimension( space, 0 ) ), BorderLayout.EAST );
		columnHeaderPanel.setOpaque( false );
		add( columnHeaderPanel, BorderLayout.NORTH );

		rowHeader = new JPanel()
		{
			@Override
			protected void paintComponent( final Graphics g )
			{
				final double yScale = screenTransform.getScaleY();
				final double minY = screenTransform.getMinY();
				final double maxY = screenTransform.getMaxY();

				final Graphics2D g2 = ( Graphics2D ) g;
				g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

				g2.setColor( getForeground() );
				g2.drawLine( 0, 0, getWidth(), 0 );

				final FontMetrics fm = g.getFontMetrics( graphOverlay.getFont() );
				g.setFont( graphOverlay.getFont() );

				final int stepT = 1 + MIN_TIMELINE_SPACING / ( int ) ( 1 + yScale );
				int tstart = Math.max( graphOverlay.getMinTimepoint(), ( int ) minY - 1 );
				tstart = ( tstart / stepT ) * stepT;
				int tend = Math.min( graphOverlay.getMaxTimepoint(), 1 + ( int ) maxY );
				tend = ( 1 + tend / stepT ) * stepT;

				final int fontInc = fm.getHeight() / 2;
				for ( int t = tstart; t < tend; t = t + stepT )
				{
					final int yline = ( int ) ( ( t - minY - 0.5 ) * yScale );
					g2.drawLine( 0, yline, getWidth(), yline );

					final int ytext = ( int ) ( ( t - minY + stepT / 2 ) * yScale ) + fontInc;
					g2.drawString( "" + t, 5, ytext );
				}
			}
		};
		rowHeader.setPreferredSize( new Dimension( HEADER_WIDTH, HEADER_HEIGHT ) );
		rowHeader.setOpaque( false );
		add( rowHeader, BorderLayout.WEST );

		styleChanged();

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

	/**
	 * Refresh this panel component with the most recent style settings. TODO
	 * change visibility and use when there is a view configuration.
	 */
	private void styleChanged()
	{
		columnHeader.setBackground( graphOverlay.getBackground() );
		columnHeader.setForeground( graphOverlay.getForeground() );
		columnHeader.setFont( graphOverlay.getFont() );
		rowHeader.setBackground( graphOverlay.getBackground() );
		rowHeader.setForeground( graphOverlay.getForeground() );
		rowHeader.setFont( graphOverlay.getFont() );
		setBackground( graphOverlay.getBackground() );
		setFont( graphOverlay.getFont() );
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
			layout.layout( TrackSchemeUtil.labelComparator() );
			layoutMinX = layout.getCurrentLayoutMinX();
			layoutMaxX = layout.getCurrentLayoutMaxX();
			entityAnimator.startAnimation( transform, ANIMATION_MILLISECONDS );
		}
		else if ( flags.transformChanged )
		{
			entityAnimator.startAnimation( transform, 0 );
		}
		else if ( flags.selectionChanged )
		{
			entityAnimator.startAnimation( transform, ANIMATION_MILLISECONDS );
		}
		else if ( flags.contextChanged )
		{
			System.out.println( "if ( flags.contextChanged ): NOT IMPLEMENTED ");
		}

		entityAnimator.setTime( System.currentTimeMillis() );
		entityAnimator.setPaintEntities( graphOverlay );
		display.repaint();
		columnHeader.repaint();
		rowHeader.repaint();

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
		if ( graphOverlay.getCurrentTimepoint() != timepoint )
		{
			graphOverlay.setCurrentTimepoint( timepoint );
			display.repaint();
		}
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
	public void focusChanged()
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
		focus.focusVertex( v );
		graphOverlay.setCurrentTimepoint( v.getTimepoint() );

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

	// TODO remove??? revise TrackSchemePanel / TrackSchemeFrame construction.
	protected InteractiveDisplayCanvasComponent< ScreenTransform > getDisplay()
	{
		return display;
	}

	// TODO remove. revise TrackSchemePanel / TrackSchemeFrame construction
	public TrackSchemeNavigator getNavigator()
	{
		return navigator;
	}

	// TODO remove. revise TrackSchemePanel / TrackSchemeFrame construction
	public SelectionBehaviours getSelectionBehaviours()
	{
		return selectionBehaviours;
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
			if ( lastComputedScreenEntities == screenEntities )
			{
				final ScreenEntities tmp = overlay.setScreenEntities( screenEntities );
				if ( tmp == null )
				{
					screenEntities = new ScreenEntities( graph, capacity );
				}
				else
					screenEntities = tmp;
				screenEntities.clear();
			}
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

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder(super.toString());
			str.append( '\n' );
			str.append( "  - transformChanged: " + transformChanged + "\n" );
			str.append( "  - selectionChanged: " + selectionChanged + "\n" );
			str.append( "  - graphChanged:     " + graphChanged + "\n" );
			str.append( "  - contextChanged:   " + contextChanged + "\n" );
			return str.toString();
		}
	}
}
