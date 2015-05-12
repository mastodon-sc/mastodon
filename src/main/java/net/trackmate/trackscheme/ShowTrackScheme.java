package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.util.GuiUtil;
import net.imglib2.util.BenchmarkHelper;
import net.trackmate.graph.listenable.GraphChangeEvent;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraphWrapper;

public class ShowTrackScheme implements TransformListener< ScreenTransform >, SelectionListener
{
	final TrackSchemeGraph graph;

	final LineageTreeLayout layout;

	final VertexOrder order;

	final GraphLayoutOverlay overlay;

	final MyFrame frame;

	private final SelectionHandler selectionHandler;

	private final InteractiveDisplayCanvasComponent< ScreenTransform > canvas;

	private final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel;

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

		final ScreenTransform screenTransform = new ScreenTransform( minX, maxX, minY, maxY, w, h );
		canvas.getTransformEventHandler().setTransform( screenTransform );
		canvas.getTransformEventHandler().setTransformListener( this );

		frame = new MyFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );
		canvas.addOverlayRenderer( selectionHandler.getSelectionOverlay() );
		canvas.addOverlayRenderer( overlay );
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		selectionHandler.setTransform( transform );

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



	@Override
	public void refresh()
	{
		frame.repaint();
	}

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
		final TrackSchemeGraph tg = new TrackSchemeGraph();

		final TrackSchemeVertex v0 = tg.addVertex().init( "0", 0, false );
		final TrackSchemeVertex v1 = tg.addVertex().init( "1", 1, false );
		final TrackSchemeVertex v2 = tg.addVertex().init( "2", 1, false );;
		final TrackSchemeVertex v3 = tg.addVertex().init( "3", 2, false );;
		final TrackSchemeVertex v4 = tg.addVertex().init( "4", 3, false );;
		final TrackSchemeVertex v5 = tg.addVertex().init( "5", 4, false );;

		tg.addEdge( v0, v1 );
		tg.addEdge( v0, v2 );
		tg.addEdge( v1, v3 );
		tg.addEdge( v4, v5 );
		final ShowTrackScheme showTrackScheme = new ShowTrackScheme( tg );

		final ListenableGraphWrapper< TrackSchemeVertex, TrackSchemeEdge, TrackSchemeGraph > graph = ListenableGraphWrapper.wrap( tg );
		graph.addGraphListener( new GraphListener< TrackSchemeVertex, TrackSchemeEdge >()
		{
			@Override
			public void graphChanged( GraphChangeEvent< TrackSchemeVertex, TrackSchemeEdge > event )
			{
				System.out.println( "Model changed!" );// DEBUG
				showTrackScheme.layout.layoutX();
				showTrackScheme.order.build();
				showTrackScheme.canvas.repaint();
			}
		} );

		showTrackScheme.canvas.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyPressed( KeyEvent e )
			{
				if ( e.getKeyCode() == KeyEvent.VK_D )
				{
					final TrackSchemeVertex target = v1;
					System.out.println( "Removing " + target );// DEBUG
					graph.beginUpdate();
					graph.remove( target );
					graph.endUpdate();
					System.out.println( "Done" );// DEBUG
				}
			}
		} );

	}
}
