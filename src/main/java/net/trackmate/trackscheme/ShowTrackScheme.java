package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;

import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformEventHandler2D;
import net.imglib2.ui.util.GuiUtil;

public class ShowTrackScheme
{
	final GraphLayoutOverlay overlay;

	final MyFrame frame;

	public ShowTrackScheme()
	{
		overlay = new GraphLayoutOverlay();
		final InteractiveDisplayCanvasComponent< AffineTransform2D > canvas = new InteractiveDisplayCanvasComponent< AffineTransform2D >( 800, 600, TransformEventHandler2D.factory() );
		frame = new MyFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );

		canvas.addOverlayRenderer( overlay );
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

		final LineageTreeLayout layout = new LineageTreeLayout( graph );
		layout.reset();
		layout.layoutX();
		System.out.println( graph );

		final VertexOrder order = new VertexOrder( graph );
		order.build();
		order.print();

		final double minY = order.getMinTimepoint() - 0.5;
		final double maxY = order.getMaxTimepoint() + 0.5;
		final double minX = order.getMinX() - 1.0;
		final double maxX = order.getMaxX() + 1.0;

		final ShowTrackScheme showTrackScheme = new ShowTrackScheme();
		final int w = showTrackScheme.overlay.getWidth();
		final int h = showTrackScheme.overlay.getHeight();

		final List< ScreenVertex > vertices = order.cropAndScale( minX, maxX, minY, maxY, w, h );
		showTrackScheme.overlay.setVertices( vertices );
		showTrackScheme.frame.repaint();
	}

}
