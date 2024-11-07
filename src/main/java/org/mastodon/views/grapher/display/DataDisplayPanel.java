/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.grapher.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.UIManager;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.mastodon.model.SelectionListener;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.NavigationEtiquette;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextListener;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenEntities;
import org.mastodon.views.grapher.datagraph.ScreenEntitiesInterpolator;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.DataDisplayOptions.Values;
import org.mastodon.views.trackscheme.display.animate.AbstractAnimator;

import bdv.viewer.InteractiveDisplayCanvas;
import bdv.viewer.OverlayRenderer;
import bdv.viewer.TransformListener;
import bdv.viewer.render.PainterThread;

public class DataDisplayPanel< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > > extends JPanel
		implements
		TransformListener< ScreenTransform >,
		PainterThread.Paintable,
		HighlightListener,
		FocusListener,
		GraphChangeListener,
		SelectionListener,
		NavigationListener< DataVertex, DataEdge >,
		ContextListener< DataVertex >
{
	private static final long serialVersionUID = 1L;

	static final double boundXLayoutBorder = 0.5;

	static final double boundYLayoutBorder = 0.5;

	private final long animationMilleseconds;

	private final DataGraph< V, E > graph;

	/**
	 * Canvas used for displaying the data graph.
	 */
	private final InteractiveDisplayCanvas display;

	private final JScrollBar xScrollBar;

	private final JScrollBar yScrollBar;

	/**
	 * The current transform from layout to screen coordinates.
	 */
	private final ScreenTransformState screenTransform;

	/**
	 * layout the {@link DataGraph} into layout coordinates.
	 */
	private final DataGraphLayout< V, E > layout;

	/**
	 * determine how layouted vertices and edges are colored.
	 */
	private final GraphColorGenerator< DataVertex, DataEdge > colorGenerator;

	/**
	 * compute {@link ScreenEntities} from the {@link DataGraphLayout} using the
	 * current {@link ScreenTransform} and interpolate between
	 * {@link ScreenEntities} for animation.
	 */
	private final ScreenEntityAnimator entityAnimator;

	private final PainterThread painterThread;

	private final DataDisplayOverlay graphOverlay;

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

	private final DataDisplayNavigationBehaviours navigationBehaviours;

	private final InertialScreenTransformEventHandler transformEventHandler;

	private final OffsetAxes offsetAxes;

	private NavigationEtiquette navigationEtiquette;

	private NavigationBehaviour navigationBehaviour;

	private final DataDisplayAutoFocus autoFocus;

	private DataDisplayNavigationActions navigationActions;

	private Context< DataVertex > context;

	private final SelectionModel< DataVertex, DataEdge > selection;

	/**
	 * If <code>true</code> the display will be updated live when the window
	 * target of context changes.
	 */
	private boolean trackContext;

	public DataDisplayPanel(
			final DataGraph< V, E > graph,
			final DataGraphLayout< V, E > layout,
			final HighlightModel< DataVertex, DataEdge > highlight,
			final FocusModel< DataVertex > focus,
			final SelectionModel< DataVertex, DataEdge > selection,
			final NavigationHandler< DataVertex, DataEdge > navigation,
			final DataDisplayOptions< DataVertex, DataEdge > optional )
	{
		super( new BorderLayout(), false );
		this.graph = graph;
		this.layout = layout;
		this.selection = selection;

		final Values< DataVertex, DataEdge > options = optional.values;
		animationMilleseconds = options.getAnimationDurationMillis();

		/*
		 * Canvas and transform.
		 */
		final int w = options.getWidth();
		final int h = options.getHeight();
		display = new InteractiveDisplayCanvas( w, h );
		screenTransform = new ScreenTransformState( new ScreenTransform( -10000, 10000, -10000, 10000, w, h ) );
		screenTransform.listeners().add( this );
		transformEventHandler = new InertialScreenTransformEventHandler( screenTransform );

		setPreferredSize( new Dimension( w, h ) );

		/*
		 * Make this instance listen to data graph and UI objects.
		 */
		graph.graphChangeListeners().add( this );
		navigation.listeners().add( this );
		highlight.listeners().add( this );
		focus.listeners().add( this );
		selection.listeners().add( this );

		/*
		 * Overlay.
		 */
		graphOverlay = options.getDataDisplayOverlayFactory().create( graph, highlight, focus, optional );
		display.overlays().add( graphOverlay );
		display.overlays().add( new OverlayRenderer()
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

		colorGenerator = options.getGraphColorGenerator();
		layout.layoutListeners().add( transformEventHandler );
		entityAnimator = new ScreenEntityAnimator();
		painterThread = new PainterThread( this );
		flags = new Flags();

		final MouseHighlightHandler highlightHandler = new MouseHighlightHandler( graphOverlay, highlight, graph );
		display.addMouseMotionListener( highlightHandler );
		display.addMouseListener( highlightHandler );
		screenTransform.listeners().add( highlightHandler );

		autoFocus = new DataDisplayAutoFocus( layout, focus );
		screenTransform.listeners().add( autoFocus );

		navigationActions = new DataDisplayNavigationActions( graph, autoFocus, selection );

		navigationBehaviours = new DataDisplayNavigationBehaviours( display, graph, layout, graphOverlay, focus,
				navigation, selection );
		screenTransform.listeners().add( navigationBehaviours );

		offsetAxes = new OffsetAxes();
		offsetAxes.listeners().add( transformEventHandler );
		offsetAxes.listeners().add( graphOverlay );
		offsetAxes.listeners().add( navigationBehaviours );
		offsetAxes.listeners().add( highlightHandler );
		offsetAxes.setAxesSize( 50, 35 );

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
				final ScreenTransform t = screenTransform.get();
				t.shiftLayoutX( s - t.getMinX() );
				screenTransform.set( t );

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

				final double s = layoutMaxY + layoutMinY - boundYLayoutBorder - yScrollBar.getValue() / yScrollScale;
				final ScreenTransform t = screenTransform.get();
				t.shiftLayoutY( ( s - t.getMaxY() ) );
				screenTransform.set( t );
				flags.setTransformChanged();
				painterThread.requestRepaint();
			}
		} );

		display.setTransformEventHandler( transformEventHandler );
		add( display, BorderLayout.CENTER );

		add( yScrollBar, BorderLayout.EAST );
		final JPanel xScrollPanel = new JPanel( new BorderLayout() );
		xScrollPanel.add( xScrollBar, BorderLayout.CENTER );
		final int space = ( Integer ) UIManager.getDefaults().get( "ScrollBar.width" );
		xScrollPanel.add( Box.createRigidArea( new Dimension( space, 0 ) ), BorderLayout.EAST );
		add( xScrollPanel, BorderLayout.SOUTH );

		setNavigationEtiquette( options.getNavigationEtiquette() );

		painterThread.start();
	}

	/**
	 * Stop the painter thread.
	 */
	public void stop()
	{
		painterThread.interrupt();
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
		final ReentrantReadWriteLock lock = graph.getLock();
		lock.readLock().lock();
		try
		{
			final ScreenTransform transform = screenTransform.get();
			final Flags flags = this.flags.clear();
			if ( flags.graphChanged )
			{
				layout.layout();
				layoutMinX = layout.getCurrentLayoutMinX();
				layoutMaxX = layout.getCurrentLayoutMaxX();
				layoutMinY = layout.getCurrentLayoutMinY();
				layoutMaxY = layout.getCurrentLayoutMaxY();
				entityAnimator.startAnimation( transform, animationMilleseconds );
			}
			else if ( flags.transformChanged )
			{
				if ( context != null )
				{
					layoutMinX = layout.getCurrentLayoutMinX();
					layoutMaxX = layout.getCurrentLayoutMaxX();
					layoutMinY = layout.getCurrentLayoutMinY();
					layoutMaxY = layout.getCurrentLayoutMaxY();
					entityAnimator.continueAnimation( transform, animationMilleseconds );
				}
				else
					entityAnimator.continueAnimation( transform, 0 );
			}
			else if ( flags.selectionChanged )
			{
				entityAnimator.startAnimation( transform, animationMilleseconds );
			}
			else if ( flags.contextChanged )
			{
				layout.layout();
				layoutMinX = layout.getCurrentLayoutMinX();
				layoutMaxX = layout.getCurrentLayoutMaxX();
				layoutMinY = layout.getCurrentLayoutMinY();
				layoutMaxY = layout.getCurrentLayoutMaxY();
				entityAnimator.startAnimation( transform, animationMilleseconds );
			}
			else if ( flags.entitiesAttributesChanged )
			{
				entityAnimator.continueAnimation( transform, 0 );
			}

			entityAnimator.setTime( System.currentTimeMillis() );
			entityAnimator.setPaintEntities( graphOverlay );
			display.repaint();
		}
		finally
		{
			lock.readLock().unlock();
		}
		// adjust scrollbars sizes
		final ScreenTransform t = new ScreenTransform();
		entityAnimator.getLastComputedScreenEntities().getScreenTransform( t );
		xScrollScale = 10000.0 / ( layoutMaxX - layoutMinX + 2 );
		final int xval = ( int ) ( xScrollScale * t.getMinX() );
		final int xext = ( int ) ( xScrollScale * ( t.getMaxX() - t.getMinX() ) );
		final int xmin = ( int ) ( xScrollScale * ( layoutMinX - boundXLayoutBorder ) );
		final int xmax = ( int ) ( xScrollScale * ( layoutMaxX + boundXLayoutBorder ) );
		yScrollScale = 10000.0 / ( layoutMaxY - layoutMinY + 2 );
		final int yext = ( int ) ( yScrollScale * ( t.getMaxY() - t.getMinY() ) );
		final int ymin = ( int ) ( yScrollScale * ( layoutMinY - boundYLayoutBorder ) );
		final int ymax = ( int ) ( yScrollScale * ( layoutMaxY + boundYLayoutBorder ) );
		final int yval = ( int ) ( yScrollScale * ( layoutMinY - boundYLayoutBorder + layoutMaxY - t.getMaxY() ) );
		ignoreScrollBarChanges = true;
		xScrollBar.setValues( xval, xext, xmin, xmax );
		yScrollBar.setValues( yval, yext, ymin, ymax );
		ignoreScrollBarChanges = false;
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
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

	public void entitiesAttributesChanged()
	{
		flags.setEntitiesAttributesChanged();
		painterThread.requestRepaint();
	}

	@Override
	public void contextChanged( final Context< DataVertex > context )
	{
		if ( this.context == null && context == null )
			return;

		if ( trackContext )
			layout.setVertices( fromContext() );

		this.context = context;
		flags.setContextChanged();
		painterThread.requestRepaint();
	}

	public NavigationEtiquette getNavigationEtiquette()
	{
		return navigationEtiquette;
	}

	public void setNavigationEtiquette( final NavigationEtiquette navigationEtiquette )
	{
		this.navigationEtiquette = navigationEtiquette;
		switch ( navigationEtiquette )
		{
		case MINIMAL:
			navigationBehaviour = new MinimalNavigationBehaviour( transformEventHandler, 100, 100 );
			break;
		case CENTER_IF_INVISIBLE:
			navigationBehaviour = new CenterIfInvisibleNavigationBehaviour( transformEventHandler );
			break;
		case CENTERING:
		default:
			navigationBehaviour = new CenteringNavigationBehaviour( transformEventHandler );
			break;
		}
	}

	@Override
	public void navigateToVertex( final DataVertex v )
	{
		navigationBehaviour.navigateToVertex( v, screenTransform.get() );
	}

	@Override
	public void navigateToEdge( final DataEdge edge )
	{
		final DataVertex source = edge.getSource( graph.vertexRef() );
		final DataVertex target = edge.getTarget( graph.vertexRef() );
		navigationBehaviour.navigateToEdge( edge, source, target, screenTransform.get() );
		graph.releaseRef( source );
		graph.releaseRef( target );
	}

	interface NavigationBehaviour
	{
		public void navigateToVertex( final DataVertex v, final ScreenTransform currentTransform );

		public void navigateToEdge( final DataEdge e, final DataVertex source, final DataVertex target,
				final ScreenTransform currentTransform );
	}

	private static class CenteringNavigationBehaviour implements NavigationBehaviour
	{
		private final InertialScreenTransformEventHandler transformEventHandler;

		public CenteringNavigationBehaviour( final InertialScreenTransformEventHandler transformEventHandler )
		{
			this.transformEventHandler = transformEventHandler;
		}

		@Override
		public void navigateToVertex( final DataVertex v, final ScreenTransform currentTransform )
		{
			final double lx = v.getLayoutX();
			final double ly = v.getLayoutY();
			transformEventHandler.centerOn( lx, ly );
		}

		@Override
		public void navigateToEdge( final DataEdge e, final DataVertex source, final DataVertex target,
				final ScreenTransform currentTransform )
		{
			System.err.println( "not implemented: CenteringNavigationBehaviour.navigateToEdge()" );
			new Throwable().printStackTrace( System.out );
		}
	}

	private static class CenterIfInvisibleNavigationBehaviour implements NavigationBehaviour
	{
		private final InertialScreenTransformEventHandler transformEventHandler;

		public CenterIfInvisibleNavigationBehaviour( final InertialScreenTransformEventHandler transformEventHandler )
		{
			this.transformEventHandler = transformEventHandler;
		}

		// With CENTER_IF_INVISIBLE etiquette, only navigate to the specified
		// vertex if not
		// is currently displayed.
		@Override
		public void navigateToVertex( final DataVertex v, final ScreenTransform currentTransform )
		{
			final double lx = v.getLayoutX();
			final double ly = v.getLayoutY();
			if ( currentTransform.getMaxX() < lx || currentTransform.getMinX() > lx
					|| currentTransform.getMaxY() < ly || currentTransform.getMinY() > ly )
			{
				transformEventHandler.centerOn( lx, ly );
			}
		}

		@Override
		public void navigateToEdge( final DataEdge e, final DataVertex source, final DataVertex target,
				final ScreenTransform currentTransform )
		{
			System.err.println( "not implemented: CenterIfInvisibleNavigationBehaviour.navigateToEdge()" );
			new Throwable().printStackTrace( System.out );
		}
	}

	private static class MinimalNavigationBehaviour implements NavigationBehaviour
	{
		private final InertialScreenTransformEventHandler transformEventHandler;

		private final int screenBorderX;

		private final int screenBorderY;

		public MinimalNavigationBehaviour( final InertialScreenTransformEventHandler transformEventHandler,
				final int screenBorderX, final int screenBorderY )
		{
			this.transformEventHandler = transformEventHandler;
			this.screenBorderX = screenBorderX;
			this.screenBorderY = screenBorderY;
		}

		@Override
		public void navigateToVertex( final DataVertex v, final ScreenTransform currentTransform )
		{
			final double lx = v.getLayoutX();
			final double ly = v.getLayoutY();

			final double minX = currentTransform.getMinX();
			final double maxX = currentTransform.getMaxX();
			final double minY = currentTransform.getMinY();
			final double maxY = currentTransform.getMaxY();
			final double bx = screenBorderX / currentTransform.getScaleX();
			final double by = screenBorderY / currentTransform.getScaleY();

			double sx = 0;
			if ( lx > maxX - bx )
				sx = lx - maxX + bx;
			else if ( lx < minX + bx )
				sx = lx - minX - bx;
			double sy = 0;
			if ( ly > maxY - by )
				sy = ly - maxY + by;
			else if ( ly < minY + by )
				sy = ly - minY - by;

			if ( sx != 0 || sy != 0 )
			{
				final double cx = ( minX + maxX ) / 2 + sx;
				final double cy = ( minY + maxY ) / 2 + sy;
				transformEventHandler.centerOn( cx, cy );
			}
		}

		@Override
		public void navigateToEdge( final DataEdge e, final DataVertex source, final DataVertex target,
				final ScreenTransform currentTransform )
		{
			final double minX = currentTransform.getMinX();
			final double maxX = currentTransform.getMaxX();
			final double minY = currentTransform.getMinY();
			final double maxY = currentTransform.getMaxY();
			final double bx = screenBorderX / currentTransform.getScaleX();
			final double by = screenBorderY / currentTransform.getScaleY();

			final double sourceX = source.getLayoutX();
			final double targetX = target.getLayoutX();
			final double eMinX = Math.min( sourceX, targetX );
			final double eMaxX = Math.max( sourceX, targetX );
			final double eMinY = source.getLayoutY();
			final double eMaxY = target.getLayoutY();
			final double lx = 0.5 * ( eMinX + eMaxX );
			final double ly = 0.5 * ( eMinY + eMaxY );

			double sx = 0;
			if ( ( eMaxX - eMinX ) > ( maxX - minX - 2 * bx ) )
				sx = lx - ( minX + maxX ) / 2;
			else if ( eMaxX > maxX - bx )
				sx = eMaxX - maxX + bx;
			else if ( eMinX < minX + bx )
				sx = eMinX - minX - bx;

			double sy = 0;
			if ( ( eMaxY - eMinY ) > ( maxY - minY - 2 * by ) )
				sy = ly - ( minY + maxY ) / 2;
			else if ( eMaxY > maxY - by )
				sy = eMaxY - maxY + by;
			else if ( eMinY < minY + by )
				sy = eMinY - minY - by;

			if ( sx != 0 || sy != 0 )
			{
				final double cx = ( minX + maxX ) / 2 + sx;
				final double cy = ( minY + maxY ) / 2 + sy;
				transformEventHandler.centerOn( cx, cy );
			}
		}
	}

	public ScreenTransformState getScreenTransform()
	{
		return screenTransform;
	}

	public InteractiveDisplayCanvas getDisplay()
	{
		return display;
	}

	public OffsetAxes getOffsetAxes()
	{
		return offsetAxes;
	}

	public DataGraphLayout< ?, ? > getDataGraphLayout()
	{
		return layout;
	}

	public InertialScreenTransformEventHandler getTransformEventHandler()
	{
		return transformEventHandler;
	}

	public DataDisplayNavigationBehaviours getNavigationBehaviours()
	{
		return navigationBehaviours;
	}

	public DataDisplayNavigationActions getNavigationActions()
	{
		return navigationActions;
	}

	/**
	 * Exposes the graph overlay renderer of this panel.
	 *
	 * @return the graph overlay renderer of this panel.
	 */
	public DataDisplayOverlay getGraphOverlay()
	{
		return graphOverlay;
	}

	class ScreenEntityAnimator extends AbstractAnimator
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
		 *            the screen transform to animate.
		 * @param duration
		 *            animation duration (in time units), may be 0.
		 */
		public void startAnimation( final ScreenTransform transform, final long duration )
		{
			reset( duration );
			if ( duration > 0 )
			{
				copyIpStart();
				layout.cropAndScale( transform, screenEntities, offsetAxes.getWidth(), offsetAxes.getHeight(),
						colorGenerator );
				swapIpEnd();
				interpolator = new ScreenEntitiesInterpolator( screenEntitiesIpStart, screenEntitiesIpEnd );
			}
			else
			{
				interpolator = null;
				swapPools();
				layout.cropAndScale( transform, screenEntities, offsetAxes.getWidth(), offsetAxes.getHeight(),
						colorGenerator );
				lastComputedScreenEntities = screenEntities;
			}
		}

		public void continueAnimation( final ScreenTransform transform, final long duration )
		{
			if ( interpolator != null )
			{
				layout.cropAndScale( transform, screenEntities, offsetAxes.getWidth(), offsetAxes.getHeight(),
						colorGenerator );
				swapIpEnd();
				interpolator = new ScreenEntitiesInterpolator(
						screenEntitiesIpStart,
						screenEntitiesIpEnd,
						null );
			}
			else
			{
				startAnimation( transform, duration );
			}
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
		 * {@link DataOverlay}. (This swaps {@link #screenEntities} with pending
		 * entities from the overlay.)
		 *
		 * @param overlay
		 *            the overlay to paint in.
		 */
		public void setPaintEntities( final DataDisplayOverlay overlay )
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

	void plot( final FeatureGraphConfig gc, final FeatureModel featureModel, final ScreenTransform transform )
	{
		trackContext = false;

		// X feature projection.
		final FeatureSpecPair spx = gc.getXFeature();
		final String xunits;
		if ( spx.isEdgeFeature() )
		{
			final FeatureProjection< E > xproj = spx.getProjection( featureModel );
			layout.setXFeatureEdge( xproj, spx.isIncomingEdge() );
			xunits = xproj.units();
		}
		else
		{
			final FeatureProjection< V > xproj = spx.getProjection( featureModel );
			layout.setXFeatureVertex( xproj );
			xunits = xproj.units();
		}

		// Y feature projection.
		final String yunits;
		final FeatureSpecPair spy = gc.getYFeature();
		if ( spy.isEdgeFeature() )
		{
			final FeatureProjection< E > yproj = spy.getProjection( featureModel );
			layout.setYFeatureEdge( yproj, spy.isIncomingEdge() );
			yunits = yproj.units();
		}
		else
		{
			final FeatureProjection< V > yproj = spy.getProjection( featureModel );
			layout.setYFeatureVertex( yproj );
			yunits = yproj.units();
		}

		// Vertices to plot.
		final RefSet< DataVertex > selectedVertices = selection.getSelectedVertices();
		final RefSet< DataEdge > selectedEdges = selection.getSelectedEdges();
		switch ( gc.itemSource() )
		{
		case CONTEXT:
		{
			trackContext = true;
			layout.setVertices( fromContext() );
			break;
		}
		case SELECTION:
		{
			layout.setVertices( selection.getSelectedVertices() );
			break;
		}
		case TRACK_OF_SELECTION:
		{
			final RefSet< DataVertex > vertices = fromTrackOfSelection( selectedVertices, selectedEdges );
			layout.setVertices( vertices );
			break;
		}
		case KEEP_CURRENT:
		default:
			break;
		}

		// Draw plot edges.
		layout.setPaintEdges( gc.drawConnected() );

		String xlabel = gc.getXFeature().toString();
		if ( !xunits.isEmpty() )
			xlabel += " (" + xunits + ")";
		graphOverlay.setXLabel( xlabel );

		String ylabel = gc.getYFeature().toString();
		if ( !yunits.isEmpty() )
			ylabel += " (" + yunits + ")";
		graphOverlay.setYLabel( ylabel );

		graphChanged();
		Executors.newSingleThreadScheduledExecutor().schedule( () -> {
			if ( transform == null )
				transformEventHandler.zoomOutFully();
			else
				transformEventHandler.zoomTo( transform.getMinX(), transform.getMaxX(), transform.getMinY(), transform.getMaxY() );
		}, 100, TimeUnit.MILLISECONDS );
	}

	private RefSet< DataVertex > fromContext()
	{
		final Iterable< DataVertex > iterable;
		if ( context != null )
		{
			iterable = context.getInsideVertices( context.getTimepoint() );
		}
		else
			iterable = graph.vertices();

		final RefSet< DataVertex > vertices = RefCollections.createRefSet( graph.vertices() );
		for ( final DataVertex v : iterable )
			vertices.add( v );
		return vertices;
	}

	private RefSet< DataVertex > fromTrackOfSelection(
			final RefSet< DataVertex > selectedVertices,
			final RefSet< DataEdge > selectedEdges )
	{
		final RefSet< DataVertex > toSearch = RefCollections.createRefSet( graph.vertices() );
		toSearch.addAll( selectedVertices );
		final DataVertex ref = graph.vertexRef();
		for ( final DataEdge e : selectedEdges )
		{
			toSearch.add( e.getSource( ref ) );
			toSearch.add( e.getTarget( ref ) );
		}
		graph.releaseRef( ref );

		// Prepare the iterator.
		final RefSet< DataVertex > set = RefCollections.createRefSet( graph.vertices() );
		final DepthFirstSearch< DataVertex, DataEdge > search =
				new DepthFirstSearch<>( graph, SearchDirection.UNDIRECTED );
		search.setTraversalListener(
				new SearchListener< DataVertex, DataEdge, DepthFirstSearch< DataVertex, DataEdge > >()
				{
					@Override
					public void processVertexLate( final DataVertex vertex,
							final DepthFirstSearch< DataVertex, DataEdge > search )
					{}

					@Override
					public void processVertexEarly( final DataVertex vertex,
							final DepthFirstSearch< DataVertex, DataEdge > search )
					{
						set.add( vertex );
					}

					@Override
					public void processEdge( final DataEdge edge, final DataVertex from, final DataVertex to,
							final DepthFirstSearch< DataVertex, DataEdge > search )
					{}

					@Override
					public void crossComponent( final DataVertex from, final DataVertex to,
							final DepthFirstSearch< DataVertex, DataEdge > search )
					{}
				} );

		for ( final DataVertex v : toSearch )
			if ( !set.contains( v ) )
				search.start( v );
		return set;
	}

	static class Flags
	{
		private boolean transformChanged;

		private boolean selectionChanged;

		private boolean graphChanged;

		private boolean contextChanged;

		private boolean entitiesAttributesChanged;

		public Flags()
		{
			transformChanged = false;
			selectionChanged = false;
			graphChanged = false;
			contextChanged = false;
			entitiesAttributesChanged = false;
		}

		public Flags( final Flags f )
		{
			transformChanged = f.transformChanged;
			selectionChanged = f.selectionChanged;
			graphChanged = f.graphChanged;
			contextChanged = f.contextChanged;
			entitiesAttributesChanged = f.entitiesAttributesChanged;
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

		public synchronized void setEntitiesAttributesChanged()
		{
			entitiesAttributesChanged = true;
		}

		public synchronized Flags clear()
		{
			final Flags copy = new Flags( this );
			transformChanged = false;
			selectionChanged = false;
			graphChanged = false;
			contextChanged = false;
			entitiesAttributesChanged = false;
			return copy;
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder( super.toString() );
			str.append( '\n' );
			str.append( "  - transformChanged: " + transformChanged + "\n" );
			str.append( "  - selectionChanged: " + selectionChanged + "\n" );
			str.append( "  - graphChanged:     " + graphChanged + "\n" );
			str.append( "  - contextChanged:   " + contextChanged + "\n" );
			str.append( "  - entitiesAttributesChanged:   " + entitiesAttributesChanged + "\n" );
			return str.toString();
		}
	}
}
