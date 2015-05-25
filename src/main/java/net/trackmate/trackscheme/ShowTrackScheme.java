package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.util.GuiUtil;
import net.imglib2.util.BenchmarkHelper;
import net.trackmate.graph.collection.RefSet;

public class ShowTrackScheme implements TransformListener< ScreenTransform >, SelectionListener
{
	final TrackSchemeGraph graph;

	final LineageTreeLayout layout;

	final VertexOrder order;

	final GraphLayoutOverlay overlay;

	final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel;

	final MyFrame frame;

	final InteractiveDisplayCanvasComponent< ScreenTransform > canvas;

	private final SelectionHandler selectionHandler;

	private final KeyHandler keyHandler;

	private final CanvasOverlay canvasOverlay;

	private final ZoomBoxHandler zoomHandler;

	SelectionNavigator selectionNavigator;

	public ShowTrackScheme( final TrackSchemeGraph graph )
	{
		this.graph = graph;

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

		selectionHandler = new DefaultSelectionHandler( graph, order );
		selectionHandler.setSelectionModel( selectionModel );
		canvas.addMouseListener( selectionHandler );
		canvas.addMouseMotionListener( selectionHandler );
		selectionHandler.setSelectionListener( this );

		selectionNavigator = new SelectionNavigator( selectionHandler, this );
		selectionNavigator.setSelectionListener( this );

		zoomHandler = new ZoomBoxHandler( canvas.getTransformEventHandler(), this );
		canvas.addMouseListener( zoomHandler );
		canvas.addMouseMotionListener( zoomHandler );

		keyHandler = new KeyHandler( this );

		final ScreenTransform screenTransform = new ScreenTransform( minX, maxX, minY, maxY, w, h );
		canvas.getTransformEventHandler().setTransform( screenTransform );
		canvas.getTransformEventHandler().setTransformListener( this );

		canvasOverlay = new CanvasOverlay( layout, order );

		frame = new MyFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );

		canvas.addOverlayRenderer( canvasOverlay );
		canvas.addOverlayRenderer( overlay );
		canvas.addOverlayRenderer( zoomHandler.getZoomOverlay() );
		canvas.addOverlayRenderer( selectionHandler.getSelectionOverlay() );
	}

	public void centerOn( TrackSchemeVertex vertex )
	{
		final double x = vertex.getLayoutX();
		final int y = vertex.getTimePoint();
		final TransformEventHandler< ScreenTransform > handler = canvas.getTransformEventHandler();

		final ScreenTransform transform = handler.getTransform();
		final double deltaX = transform.maxX - transform.minX;
		final double deltaY = transform.maxY - transform.minY;

		transform.minX = x - deltaX / 2;
		transform.maxX = x + deltaX / 2;
		transform.minY = y - deltaY / 2;
		transform.maxY = y + deltaY / 2;
		transformChanged( transform );
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		zoomHandler.setTransform( transform );
		selectionHandler.setTransform( transform );
		canvasOverlay.transformChanged( transform );

		System.out.println( transform );// DEBUG

//		System.out.println( "transformChanged" );
		final double minX = transform.minX;
		final double maxX = transform.maxX;
		final double minY = transform.minY;
		final double maxY = transform.maxY;
		final int w = transform.screenWidth;
		final int h = transform.screenHeight;
		final ScreenEntities entities = order.cropAndScale( minX, maxX, minY, maxY, w, h );
		overlay.setScreenEntities( entities );
		frame.repaint();
	}

	// =================== TODO ===========================

	public static interface HACK_SelectionListener
	{
		void select( TrackSchemeVertex v );

		public void repaint();
	}

	private HACK_SelectionListener sl = null;

	public void setSelectionListener( final HACK_SelectionListener sl )
	{
		this.sl = sl;
	}

	// =====================================================

	@Override
	public void refresh()
	{
		if ( sl != null )
		{
			final RefSet< TrackSchemeVertex > selectedVertices = selectionModel.getSelectedVertices();
			if ( !selectedVertices.isEmpty() )
				sl.select( selectedVertices.iterator().next() );
			sl.repaint();
		}
		frame.repaint();
	}

	/*
	 * STATIC METHODS AND CLASSES
	 */

	static class MyFrame extends JFrame implements PainterThread.Paintable
	{
		private static final long serialVersionUID = 1L;

		private final PainterThread painterThread;

		public MyFrame( final String title, final GraphicsConfiguration gc )
		{
			super( title, gc );
			painterThread = new PainterThread( this );
			setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final WindowEvent e )
				{
					painterThread.interrupt();
				}
			} );
		}

		@Override
		public void paint()
		{
			repaint();
		}
	}

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
