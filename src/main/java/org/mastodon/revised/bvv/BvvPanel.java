package org.mastodon.revised.bvv;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.PainterThread;
import org.mastodon.revised.bvv.BvvOptions.Values;
import org.mastodon.revised.bvv.wrap.BvvRenderer;
import tpietzsch.util.TransformHandler;

public class BvvPanel extends JPanel implements
		PainterThread.Paintable
{
	private final PainterThread painterThread;

	private final AffineTransform3D viewerTransform;

	private final TransformHandler transformHandler;

	private final BvvRenderer renderer;

	private final GLCanvas canvas;

	private final JSlider sliderTime;

	public BvvPanel(
			final int numTimepoints,
			final BvvOptions optional )
	{
		super( new BorderLayout() );

		final Values options = optional.values;

		viewerTransform = new AffineTransform3D();
		painterThread = new PainterThread( this );
		renderer = new BvvRenderer( 640, 480 );

		final GLCapabilities capsReqUser = new GLCapabilities( GLProfile.getMaxProgrammableCore( true ) );
		canvas = new GLCanvas( capsReqUser );
		canvas.setPreferredSize( new Dimension( options.getWidth(), options.getHeight() ) );
		canvas.addGLEventListener( glEventListener );

		sliderTime = new JSlider( SwingConstants.HORIZONTAL, 0, numTimepoints - 1, 0 );
		sliderTime.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				if ( e.getSource().equals( sliderTime ) )
					setTimepoint( sliderTime.getValue() );
			}
		} );

		add( canvas, BorderLayout.CENTER );
		if ( numTimepoints > 1 )
			add( sliderTime, BorderLayout.SOUTH );

		transformHandler = new TransformHandler();
		transformHandler.setCanvasSize( canvas.getWidth(), canvas.getHeight(), false );
		transformHandler.setTransform( viewerTransform );
		transformHandler.listeners().add( this::transformChanged );

		painterThread.start();
	}

	/**
	 * Show the specified time-point.
	 *
	 * @param timepoint
	 *            time-point index.
	 */
	public synchronized void setTimepoint( final int timepoint )
	{
		// TODO
		// TODO
		// TODO
		// TODO
//		if ( state.getCurrentTimepoint() != timepoint )
//		{
//			state.setCurrentTimepoint( timepoint );
//			sliderTime.setValue( timepoint );
//			for ( final TimePointListener l : timePointListeners )
//				l.timePointChanged( timepoint );
//			requestRepaint();
//		}
	}

	public TransformHandler getTransformEventHandler()
	{
		return transformHandler;
	}

	public void transformChanged( final AffineTransform3D transform )
	{
		synchronized( viewerTransform )
		{
			viewerTransform.set( transform );
		}
		painterThread.requestRepaint();
	}

	/**
	 * Stop the painter thread.
	 */
	public void stop()
	{
		painterThread.interrupt();
	}

	public Component getDisplay()
	{
		return canvas;
	}

	@Override
	public void paint()
	{
		canvas.display();
	}

	private final GLEventListener glEventListener = new GLEventListener()
	{
		private final AffineTransform3D worldToScreen = new AffineTransform3D();

		@Override
		public void init( final GLAutoDrawable drawable )
		{
			final GL3 gl = drawable.getGL().getGL3();
			renderer.init( gl );
		}

		@Override
		public void display( final GLAutoDrawable drawable )
		{
			final GL3 gl = drawable.getGL().getGL3();
			synchronized ( viewerTransform )
			{
				worldToScreen.set( viewerTransform );
			}
			renderer.display( gl, worldToScreen );
		}

		@Override
		public void reshape( final GLAutoDrawable drawable, final int x, final int y, final int width, final int height )
		{
		}

		@Override
		public void dispose( final GLAutoDrawable drawable )
		{
		}
	};
}
