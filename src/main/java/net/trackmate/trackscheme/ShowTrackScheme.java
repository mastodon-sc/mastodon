package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.util.GuiUtil;
import net.imglib2.util.BenchmarkHelper;
import net.trackmate.trackscheme.animate.AbstractAnimator;
import net.trackmate.trackscheme.animate.AbstractTransformAnimator;

public class ShowTrackScheme implements TransformListener< ScreenTransform >, SelectionListener, PainterThread.Paintable
{
	private static final long ANIMATION_MILLISECONDS = 250;

	final TrackSchemeGraph graph;

	final LineageTreeLayout layout;

	final VertexOrder order;

	private final ScreenTransform currentTransform;

	final GraphLayoutOverlay overlay;

	final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel;

	final JFrame frame;

	final InteractiveDisplayCanvasComponent< ScreenTransform > canvas;

	private final SelectionHandler selectionHandler;

	private final KeyHandler keyHandler;

	private final CanvasOverlay canvasOverlay;

	private final ZoomBoxHandler zoomHandler;

	private final PainterThread painterThread;

	SelectionNavigator selectionNavigator;

	private AbstractTransformAnimator< ScreenTransform > transformAnimator;

	public ShowTrackScheme( final TrackSchemeGraph graph )
	{
		this.graph = graph;

		screenEntities = new ScreenEntities( graph );
		screenEntities2 = new ScreenEntities( graph );
		screenEntitiesIpStart = new ScreenEntities( graph );
		screenEntitiesIpEnd = new ScreenEntities( graph );

		selectionModel = SelectionModel.create( graph );

		layout = new LineageTreeLayout( graph );
//		layout.reset();
//		layout.layoutX();

		System.out.println( "benchmarking layout of the full graph:" );
		BenchmarkHelper.benchmarkAndPrint( 10, true, new Runnable()
		{
			@Override
			public void run()
			{
				layout.reset();
				layout.layoutX();
			}
		} );
		System.out.println();

//		System.out.println( graph );

		order = new VertexOrder( graph );
		order.build();
//		order.print();

		overlay = new GraphLayoutOverlay();
		overlay.setCanvasSize( 800, 600 );

		canvas = new InteractiveDisplayCanvasComponent< ScreenTransform >( 800, 600, ScreenTransform.ScreenTransformEventHandler.factory() );
		final double minY = order.getMinTimepoint() - 0.5;
		final double maxY = order.getMaxTimepoint() + 0.5;
		final double minX = order.getMinX() - 1.0;
		final double maxX = order.getMaxX() + 1.0;
		final int w = overlay.getWidth();
		final int h = overlay.getHeight();

		currentTransform = new ScreenTransform( minX, maxX, minY, maxY, w, h );
		canvas.getTransformEventHandler().setTransform( currentTransform );
		canvas.getTransformEventHandler().setTransformListener( this );

		selectionHandler = new DefaultSelectionHandler( graph, order );
		selectionHandler.setSelectionModel( selectionModel );
		canvas.addMouseListener( selectionHandler );
		canvas.addMouseMotionListener( selectionHandler );

		final MouseAdapter inertiaListener = new MouseAdapter()
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

			private ScreenTransform transform;

			@Override
			public void mousePressed( final MouseEvent e )
			{
				this.transform = currentTransform.copy();
				vx0 = 0;
				vy0 = 0;
			}

			@Override
			public synchronized void mouseReleased( final MouseEvent e )
			{
				final int modifiers = e.getModifiers();
				if ( ( modifiers & ( MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK ) ) != 0 ) // translate
				{
					if ( Math.abs( vx0 ) > 0 || Math.abs( vy0 ) > 0 )
					{
						transformAnimator = new InertialTranslationAnimator( currentTransform, vx0, vy0, 500 );
						refresh();
					}
				}
			};

			@Override
			public void mouseDragged( final MouseEvent e )
			{
				final int modifiers = e.getModifiersEx();
				if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
				{
					final long t = System.currentTimeMillis();
					final double x = transform.screenToLayoutX( e.getX() );
					final double y = transform.screenToLayoutY( e.getY() );
					vx0 = ( x - x0 ) / ( ( double ) t - t0 );
					vy0 = ( y - y0 ) / ( ( double ) t - t0 );
					x0 = x;
					y0 = y;
					t0 = t;
				}
			}

			@Override
			public synchronized void mouseWheelMoved( final MouseWheelEvent e )
			{

				final int modifiers = e.getModifiersEx();
				final int s = e.getWheelRotation();
				final boolean ctrlPressed = ( modifiers & KeyEvent.CTRL_DOWN_MASK ) != 0;
				final boolean altPressed = ( modifiers & KeyEvent.ALT_DOWN_MASK ) != 0;
				final boolean shiftPressed = ( modifiers & KeyEvent.SHIFT_DOWN_MASK ) != 0;
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

					transformAnimator = new InertialZoomAnimator( currentTransform, zoomSteps, zoomOut, zoomX, zoomY, e.getX(), e.getY(), 500 );
//					System.out.println( zzom );// DEBUG
				}
				else
				{
					/*
					 * Scroll.
					 */

					final boolean dirX = ( modifiers & KeyEvent.SHIFT_DOWN_MASK ) != 0;
					if ( dirX )
					{
						vx0 = s * ( currentTransform.maxX - currentTransform.minX ) * MOUSEWHEEL_SCROLL_SPEED;
						vy0 = 0;
					}
					else
					{
						vx0 = 0;
						vy0 = s * ( currentTransform.maxY - currentTransform.minY ) * MOUSEWHEEL_SCROLL_SPEED;
					}
					transformAnimator = new InertialTranslationAnimator( currentTransform, vx0, vy0, 500 );
				}

				refresh();

			}
		};

		canvas.addMouseListener( inertiaListener );
		canvas.addMouseMotionListener( inertiaListener );
		canvas.addMouseWheelListener( inertiaListener );

		selectionHandler.setSelectionListener( this );

		selectionNavigator = new SelectionNavigator( selectionHandler, this );

		zoomHandler = new ZoomBoxHandler( canvas.getTransformEventHandler(), this );
		canvas.addMouseListener( zoomHandler );
		canvas.addMouseMotionListener( zoomHandler );

		keyHandler = new KeyHandler( this );

		canvasOverlay = new CanvasOverlay( layout, order );

		frame = new JFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		painterThread = new PainterThread( this );
		painterThread.start();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				painterThread.interrupt();
			}
		} );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );

		canvas.addOverlayRenderer( canvasOverlay );
		canvas.addOverlayRenderer( overlay );
		canvas.addOverlayRenderer( zoomHandler.getZoomOverlay() );
		canvas.addOverlayRenderer( selectionHandler.getSelectionOverlay() );
		canvas.addOverlayRenderer( new OverlayRenderer()
		{
			@Override
			public void setCanvasSize( final int width, final int height )
			{}

			@Override
			public void drawOverlays( final Graphics g )
			{
				painterThread.requestRepaint();
			}
		} );
	}

	public void centerOn( final TrackSchemeVertex vertex )
	{
		final double x = vertex.getLayoutX();
		final int y = vertex.getTimePoint();
		final TransformEventHandler< ScreenTransform > handler = canvas.getTransformEventHandler();
		final ScreenTransform transform = handler.getTransform();

		transformAnimator = new TranslationAnimator( transform, x, y, 200 );
		transformAnimator.setTime( System.currentTimeMillis() );
		refresh();
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		currentTransform.set( transform );
		repaint();
	}

	// ======== TODO MOVE TO ANIMATOR =====================

	private ScreenEntities screenEntities;

	private ScreenEntities screenEntities2;

	private ScreenEntities screenEntitiesIpStart;

	private ScreenEntities screenEntitiesIpEnd;

	private void swapPools()
	{
		final ScreenEntities tmp = screenEntities;
		screenEntities = screenEntities2;
		screenEntities2 = tmp;
		screenEntities.clear();
	}

	private void swapIpStart()
	{
		final ScreenEntities tmp = screenEntities;
		screenEntities = screenEntitiesIpStart;
		screenEntitiesIpStart = tmp;
		screenEntities.clear();
	}

	private void swapIpEnd()
	{
		final ScreenEntities tmp = screenEntities;
		screenEntities = screenEntitiesIpEnd;
		screenEntitiesIpEnd = tmp;
		screenEntities.clear();
	}

	// ====================================================

	public void repaint()
	{
		repaint( false );
	}

	public synchronized void repaint( final boolean startAnimation )
	{
		zoomHandler.setTransform( currentTransform );
		selectionHandler.setTransform( currentTransform );
		canvasOverlay.transformChanged( currentTransform );

		final double minX = currentTransform.minX;
		final double maxX = currentTransform.maxX;
		final double minY = currentTransform.minY;
		final double maxY = currentTransform.maxY;
		final int w = currentTransform.screenWidth;
		final int h = currentTransform.screenHeight;
		if ( startAnimation )
		{
			swapIpStart();
			order.cropAndScale( minX, maxX, minY, maxY, w, h, screenEntities );
			swapIpEnd();
			entitiesAnimator = new ScreenEntitiesAnimator( ANIMATION_MILLISECONDS );
			entitiesAnimator.setTime( System.currentTimeMillis() );
		}
		else
		{
			swapPools();
			order.cropAndScale( minX, maxX, minY, maxY, w, h, screenEntities );
			overlay.setScreenEntities( screenEntities );
		}

		frame.repaint();
	}

	private class ScreenEntitiesAnimator extends AbstractAnimator
	{
		private final ScreenEntitiesInterpolation ip;

		public ScreenEntitiesAnimator( final long duration )
		{
			super( duration );
			ip = new ScreenEntitiesInterpolation( screenEntitiesIpStart, screenEntitiesIpEnd );
		}

		void animate()
		{
			setTime( System.currentTimeMillis() );
			swapPools();
			ip.interpolate( ratioComplete(), screenEntities );
			overlay.setScreenEntities( screenEntities );
			if ( isComplete() )
				entitiesAnimator = null;
			frame.repaint();
		}
	}

	private volatile ScreenEntitiesAnimator entitiesAnimator;

	@Override
	public synchronized void paint()
	{
		if ( entitiesAnimator != null )
			entitiesAnimator.animate();

		if ( transformAnimator != null )
		{
			final ScreenTransform transform = transformAnimator.getCurrent( System.currentTimeMillis() );
			canvas.getTransformEventHandler().setTransform( transform );
			transformChanged( transform );
			if ( transformAnimator.isComplete() )
				transformAnimator = null;
		}
	}

	@Override
	public void refresh()
	{
		repaint();
	}

	public void DEBUG_printPools( final String title, final int from, final int to )
	{
		System.out.println( "=== " + title + " ===" );

		System.out.println( "Start" );
		for ( int i = from; i < Math.min( to, screenEntitiesIpStart.getVertices().size() ); ++i )
			System.out.println( "  " + screenEntitiesIpStart.getVertices().get( i ) );
		System.out.println();

		System.out.println( "End" );
		for ( int i = from; i < Math.min( to, screenEntitiesIpEnd.getVertices().size() ); ++i )
			System.out.println( "  " + screenEntitiesIpEnd.getVertices().get( i ) );
		System.out.println();

		System.out.println( "screenEntities" );
		for ( int i = from; i < Math.min( to, screenEntities.getVertices().size() ); ++i )
			System.out.println( "  " + screenEntities.getVertices().get( i ) );
		System.out.println();

		System.out.println( "screenEntities2" );
		for ( int i = from; i < Math.min( to, screenEntities2.getVertices().size() ); ++i )
			System.out.println( "  " + screenEntities2.getVertices().get( i ) );
		System.out.println();

		System.out.println();
		System.out.println();
	}

	/*
	 * STATIC METHODS AND CLASSES
	 */

	public static void main( final String[] args )
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		final TrackSchemeVertex v0 = graph.addVertex().init( "0", 0, false );
		final TrackSchemeVertex v1 = graph.addVertex().init( "1", 1, false );
		final TrackSchemeVertex v2 = graph.addVertex().init( "2", 1, false );;
		final TrackSchemeVertex v3 = graph.addVertex().init( "3", 2, false );;
		final TrackSchemeVertex v4 = graph.addVertex().init( "4", 3, false );;
		final TrackSchemeVertex v5 = graph.addVertex().init( "5", 4, false );;
		final TrackSchemeVertex v6 = graph.addVertex().init( "6", 3, false );;
		final TrackSchemeVertex v7 = graph.addVertex().init( "7", 4, false );;
		final TrackSchemeVertex v8 = graph.addVertex().init( "8", 5, false );;
		final TrackSchemeVertex v9 = graph.addVertex().init( "9", 2, false );;

		graph.addEdge( v0, v1 );
		graph.addEdge( v0, v2 );
		graph.addEdge( v1, v3 );
		graph.addEdge( v2, v6 );
		graph.addEdge( v6, v7 );
		graph.addEdge( v7, v8 );
		graph.addEdge( v4, v5 );
		graph.addEdge( v9, v6 );

		final ShowTrackScheme showTrackScheme = new ShowTrackScheme( graph );
	}
}
