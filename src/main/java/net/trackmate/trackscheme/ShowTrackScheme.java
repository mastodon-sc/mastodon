package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.util.GuiUtil;
import net.imglib2.util.BenchmarkHelper;

public class ShowTrackScheme implements TransformListener< ScreenTransform >, SelectionListener
{
	public class SelectionHandler extends MouseAdapter
	{

		/**
		 * Whom to notify when selecting stuff.
		 */
		protected SelectionListener selectionListener;

		/**
		 * Coordinates where mouse dragging started.
		 */
		protected int oX, oY;

		private ScreenTransform transform;

		@Override
		public void mouseClicked( MouseEvent e )
		{
			if ( selectionListener != null )
			{
				if ( e.getButton() == MouseEvent.BUTTON1 )
				{
					selectionListener.selectAt( transform, e.getX(), e.getY() );
				}
			}
		}

		public void setSelectionListener( final SelectionListener selectionListener )
		{
			this.selectionListener = selectionListener;
		}

		public void setTransform( ScreenTransform transform )
		{
			this.transform = transform;
		}

	}

	final TrackSchemeGraph graph;

	final LineageTreeLayout layout;

	final VertexOrder order;

	final GraphLayoutOverlay overlay;

	final MyFrame frame;

	private final SelectionHandler selectionHandler;

	public ShowTrackScheme( final TrackSchemeGraph graph )
	{
		this.graph = graph;

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

		final InteractiveDisplayCanvasComponent< ScreenTransform > canvas = new InteractiveDisplayCanvasComponent< ScreenTransform >( 800, 600, ScreenTransform.ScreenTransformEventHandler.factory() );
		final double minY = order.getMinTimepoint() - 0.5;
		final double maxY = order.getMaxTimepoint() + 0.5;
		final double minX = order.getMinX() - 1.0;
		final double maxX = order.getMaxX() + 1.0;
		final int w = overlay.getWidth();
		final int h = overlay.getHeight();

		selectionHandler = new SelectionHandler();
		canvas.addMouseListener( selectionHandler );
		selectionHandler.setSelectionListener( this );

		final ScreenTransform screenTransform = new ScreenTransform( minX, maxX, minY, maxY, w, h );
		canvas.getTransformEventHandler().setTransform( screenTransform );
		canvas.getTransformEventHandler().setTransformListener( this );

		frame = new MyFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );
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
	public void selectAt( final ScreenTransform transform, final int x, final int y )
	{
		final double lx = transform.screenToLayoutX( x );
		final double ly = transform.screenToLayoutY( y );

		order.selectClosest( lx, ly );
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
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		final TrackSchemeVertex v0 = graph.addVertex().init( "0", 0, false );
		final TrackSchemeVertex v1 = graph.addVertex().init( "1", 1, false );
		final TrackSchemeVertex v2 = graph.addVertex().init( "2", 1, false );;
		final TrackSchemeVertex v3 = graph.addVertex().init( "3", 2, false );;
		final TrackSchemeVertex v4 = graph.addVertex().init( "4", 3, false );;
		final TrackSchemeVertex v5 = graph.addVertex().init( "5", 4, false );;

		graph.addEdge( v0, v1 );
		graph.addEdge( v0, v2 );
		graph.addEdge( v1, v3 );
		graph.addEdge( v4, v5 );

		final ShowTrackScheme showTrackScheme = new ShowTrackScheme( graph );
	}
}
