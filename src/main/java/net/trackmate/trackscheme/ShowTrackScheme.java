package net.trackmate.trackscheme;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformEventHandler2D;
import net.imglib2.ui.util.GuiUtil;

public class ShowTrackScheme
{
	final GraphLayoutOverlay overlay;

	public ShowTrackScheme()
	{
		overlay = new GraphLayoutOverlay();
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

	public void show()
	{
		final InteractiveDisplayCanvasComponent< AffineTransform2D > canvas = new InteractiveDisplayCanvasComponent< AffineTransform2D >( 800, 600, TransformEventHandler2D.factory() );
		final MyFrame frame = new MyFrame( "trackscheme", GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		frame.getContentPane().add( canvas, BorderLayout.CENTER );
		frame.pack();
		frame.setVisible( true );

		canvas.addOverlayRenderer( overlay );
	}

	public static void main( final String[] args )
	{
		new ShowTrackScheme().show();
	}
}
