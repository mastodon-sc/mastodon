package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.PainterThread.Paintable;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.util.GuiUtil;
import net.imglib2.util.BenchmarkHelper;
import net.trackmate.trackscheme.animate.AbstractAnimator;

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

	private final ZoomBoxHandler zoomBoxHandler;

	private final PainterThread painterThread;

	SelectionNavigator selectionNavigator;

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

//		canvas = new InteractiveDisplayCanvasComponent< ScreenTransform >( 800, 600, ScreenTransform.ScreenTransformEventHandler.factory() );
		canvas = new InteractiveDisplayCanvasComponent< ScreenTransform >( 800, 600, InertialTransformHandler.factory() );
		final double minY = order.getMinTimepoint() - 0.5;
		final double maxY = order.getMaxTimepoint() + 0.5;
		final double minX = order.getMinX() - 1.0;
		final double maxX = order.getMaxX() + 1.0;
		final int w = overlay.getWidth();
		final int h = overlay.getHeight();

		currentTransform = new ScreenTransform( minX, maxX, minY, maxY, w, h );
		canvas.getTransformEventHandler().setTransform( new ScreenTransform( minX, maxX, minY, maxY, w, h ) );
		canvas.getTransformEventHandler().setTransformListener( this );

		selectionHandler = new DefaultSelectionHandler( graph, order );
		selectionHandler.setSelectionModel( selectionModel );
		canvas.addMouseListener( selectionHandler );
		canvas.addMouseMotionListener( selectionHandler );

		selectionHandler.setSelectionListener( this );

		selectionNavigator = new SelectionNavigator( selectionHandler, this );

		zoomBoxHandler = new ZoomBoxHandler( canvas.getTransformEventHandler(), this );
		canvas.addMouseListener( zoomBoxHandler );
		canvas.addMouseMotionListener( zoomBoxHandler );

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
		canvas.addOverlayRenderer( zoomBoxHandler.getZoomOverlay() );
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

//		transformAnimator = new TranslationAnimator( transform, x, y, 200 );
//		transformAnimator.setTime( System.currentTimeMillis() );
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
		zoomBoxHandler.setTransform( currentTransform );
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
		
		( ( Paintable ) canvas.getTransformEventHandler() ).paint();
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
