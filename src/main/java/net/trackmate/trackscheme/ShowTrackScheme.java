package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.util.GuiUtil;

public class ShowTrackScheme implements TransformListener< ScreenTransform >
{
	final TrackSchemeGraph graph;

	final LineageTreeLayout layout;

	final VertexOrder order;

	final GraphLayoutOverlay overlay;

	final MyFrame frame;

	public ShowTrackScheme( final TrackSchemeGraph graph )
	{
		this.graph = graph;

		layout = new LineageTreeLayout( graph );
		layout.reset();
		layout.layoutX();

//		for ( int i = 0; i < 100; ++i )
//		{
//			final long t0 = System.currentTimeMillis();
//			layout.reset();
//			layout.layoutX();
//			final long t1 = System.currentTimeMillis();
//			System.out.println( "layout: " + ( t1 - t0 ) + "ms");
//		}

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
		canvas.getTransformEventHandler().setTransform( new ScreenTransform( minX, maxX, minY, maxY, w, h ) );
		canvas.getTransformEventHandler().setTransformListener( this );

		frame = new MyFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );
		canvas.addOverlayRenderer( overlay );

//		transformChanged( canvas.getTransformEventHandler().getTransform() );
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
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
