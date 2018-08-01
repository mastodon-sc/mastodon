package org.mastodon.revised.bvv.wrap;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix4f;
import org.mastodon.revised.bvv.scene.InstancedEllipsoid;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import tpietzsch.offscreen.OffScreenFrameBufferWithDepth;
import tpietzsch.scene.TexturedUnitCube;
import tpietzsch.util.MatrixMath;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_RGB8;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;

public class BvvRenderer
{
	private final BvvGraphWrapper< Spot, Link > viewGraph; // TODO HACK should be BvvGraph<?,?>

	private final OffScreenFrameBufferWithDepth sceneBuf;

	// TODO...
	private final double dCam = 2000;
	private final double dClip = 1000;
	private double screenWidth = 640;
	private double screenHeight = 480;

	private final TexturedUnitCube cube = new TexturedUnitCube( "imglib2.png" );

	private final InstancedEllipsoid instancedEllipsoid = new InstancedEllipsoid( 3 );

	public BvvRenderer(
			final int renderWidth,
			final int renderHeight,
			final BvvGraphWrapper< Spot, Link > viewGraph // TODO HACK should be BvvGraph<?,?>
	)
	{
		this.viewGraph = viewGraph;
		sceneBuf = new OffScreenFrameBufferWithDepth( renderWidth, renderHeight, GL_RGB8 );
	}

	public void init( final GL3 gl )
	{
		gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 2 );
	}

	public void display(
			final GL3 gl,
			final AffineTransform3D worldToScreen,
			final int timepoint )
	{
		final Matrix4f view = MatrixMath.affine( worldToScreen, new Matrix4f() );
		final Matrix4f projection = MatrixMath.screenPerspective( dCam, dClip, screenWidth, screenHeight, 0, new Matrix4f() );
		final Matrix4f pv = new Matrix4f( projection ).mul( view );

		sceneBuf.bind( gl, false );
		gl.glClearColor( 0.0f, 0.2f, 0.1f, 0.0f );
		gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		gl.glEnable( GL_DEPTH_TEST );
		cube.draw( gl, new Matrix4f( pv ).translate( 200, 200, 50 ).scale( 100 ) );

		FloatBuffer buf = viewGraph.ellipsoidsPerTimepoint.get( timepoint ).buffer().asFloatBuffer();
		instancedEllipsoid.draw( gl, new Matrix4f( projection ).mul( view ), view, buf );

		sceneBuf.unbind( gl, false );
		gl.glDisable( GL_DEPTH_TEST );
		sceneBuf.drawQuad( gl );
	}
}
