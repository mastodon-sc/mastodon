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
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.PainterThread;
import org.joml.Matrix4f;
import org.mastodon.revised.bvv.BvvOptions.Values;
import tpietzsch.backend.jogl.JoglGpuContext;
import tpietzsch.offscreen.OffScreenFrameBufferWithDepth;
import tpietzsch.scene.TexturedUnitCube;
import tpietzsch.util.MatrixMath;
import tpietzsch.util.TransformHandler;

import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_RGB8;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;

public class BvvPanel extends JPanel implements
		PainterThread.Paintable
{
	private final PainterThread painterThread;

	private final AffineTransform3D viewerTransform;

	private final TransformHandler transformHandler;

	private final GLCanvas canvas;

	public BvvPanel(
			final BvvOptions optional )
	{
		super( new BorderLayout() );

		final Values options = optional.values;

		viewerTransform = new AffineTransform3D();
		painterThread = new PainterThread( this );

		final GLCapabilities capsReqUser = new GLCapabilities( GLProfile.getMaxProgrammableCore( true ) );
		canvas = new GLCanvas( capsReqUser );
		canvas.setPreferredSize( new Dimension( options.getWidth(), options.getHeight() ) );
		canvas.addGLEventListener( new Render( 640, 480 ) );

		add( canvas, BorderLayout.CENTER );

		transformHandler = new TransformHandler();
		transformHandler.setCanvasSize( canvas.getWidth(), canvas.getHeight(), false );
		transformHandler.setTransform( viewerTransform );
		transformHandler.listeners().add( this::transformChanged );

		painterThread.start();
	}

	class Render implements GLEventListener
	{
		private final AffineTransform3D worldToScreen;

		private final OffScreenFrameBufferWithDepth sceneBuf;

		// TODO...
		private final double dCam = 2000;
		private final double dClip = 1000;
		private double screenWidth = 640;
		private double screenHeight = 480;

		private final TexturedUnitCube cube = new TexturedUnitCube( "imglib2.png" );

		public Render(
				final int renderWidth,
				final int renderHeight )
		{
			worldToScreen = new AffineTransform3D();
			sceneBuf = new OffScreenFrameBufferWithDepth( renderWidth, renderHeight, GL_RGB8 );
		}

		@Override
		public void init( final GLAutoDrawable drawable )
		{
			final GL3 gl = drawable.getGL().getGL3();
			gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 2 );
		}

		@Override
		public void display( final GLAutoDrawable drawable )
		{
			final GL3 gl = drawable.getGL().getGL3();
			final JoglGpuContext context = JoglGpuContext.get( gl );

			synchronized ( viewerTransform )
			{
				worldToScreen.set( viewerTransform );
			}

			final Matrix4f view = MatrixMath.affine( worldToScreen, new Matrix4f() );
			final Matrix4f projection = MatrixMath.screenPerspective( dCam, dClip, screenWidth, screenHeight, 0, new Matrix4f() );
			final Matrix4f pv = new Matrix4f( projection ).mul( view );

			sceneBuf.bind( gl, false );
			gl.glEnable( GL_DEPTH_TEST );
			gl.glClearColor( 0.2f, 0.3f, 0.3f, 1.0f );
			gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
			cube.draw( gl, new Matrix4f( pv ).translate( 200, 200, 50 ).scale( 100 ) );
			sceneBuf.unbind( gl, false );
			gl.glDisable( GL_DEPTH_TEST );
			sceneBuf.drawQuad( gl );
		}

		@Override
		public void reshape( final GLAutoDrawable drawable, final int x, final int y, final int width, final int height )
		{
		}

		@Override
		public void dispose( final GLAutoDrawable drawable )
		{
		}
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
}
