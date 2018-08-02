package org.mastodon.revised.bvv;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix4f;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.bvv.scene.InstancedEllipsoid;
import tpietzsch.offscreen.OffScreenFrameBufferWithDepth;
import tpietzsch.scene.TexturedUnitCube;
import tpietzsch.util.MatrixMath;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_RGB8;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;

public class BvvRenderer< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > >
{
	private final BvvGraph< V, E > graph;

	private final SelectionModel< V, E > selection;

	private final OffScreenFrameBufferWithDepth sceneBuf;

	// TODO...
	private final double dCam = 2000;
	private final double dClip = 1000;
	private double screenWidth = 640;
	private double screenHeight = 480;

	private final TexturedUnitCube cube = new TexturedUnitCube( "imglib2.png" );

	private final InstancedEllipsoid instancedEllipsoid;

	private final ReusableInstanceArrays< InstancedEllipsoid.InstanceArray > reusableInstanceArrays;

	public BvvRenderer(
			final int renderWidth,
			final int renderHeight,
			final BvvGraph< V, E > graph,
			final SelectionModel< V, E > selection )
	{
		this.graph = graph;
		this.selection = selection;
		sceneBuf = new OffScreenFrameBufferWithDepth( renderWidth, renderHeight, GL_RGB8 );
		instancedEllipsoid = new InstancedEllipsoid( 3 );

		final int numInstanceArrays = 10;
		reusableInstanceArrays = new ReusableInstanceArrays<>(
				t -> graph.getEllipsoids().forTimepoint( t ).getModCount(),
				numInstanceArrays,
				instancedEllipsoid::createInstanceArray
		);
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
		final Matrix4f camview = new Matrix4f()
			.translation( ( float ) ( -( screenWidth - 1 ) / 2 ), ( float ) ( -( screenHeight - 1 ) / 2 ), ( float ) dCam )
			.mul( view );
		final Matrix4f projection = MatrixMath.screenPerspective( dCam, dClip, screenWidth, screenHeight, 0, new Matrix4f() );
		final Matrix4f pv = new Matrix4f( projection ).mul( view );

		sceneBuf.bind( gl, false );
		gl.glClearColor( 0.0f, 0.2f, 0.1f, 0.0f );
		gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		gl.glEnable( GL_DEPTH_TEST );
		cube.draw( gl, new Matrix4f( pv ).translate( 200, 200, 50 ).scale( 100 ) );

		final InstancedEllipsoid.InstanceArray instanceArray = reusableInstanceArrays.getForTimepoint( timepoint );
		final EllipsoidInstances< V, E > instances = graph.getEllipsoids().forTimepoint( timepoint );
		final int modCount = instances.getModCount();
		if ( instanceArray.getModCount() != modCount )
		{
			instanceArray.setModCount( modCount );
			instanceArray.updateShapes( gl, instances.buffer().asFloatBuffer() );

			float[] colors = new float[ 3 * instances.size() ];

//			selection.isSelected(  )

			for ( int i = 0; i < instances.size(); ++i )
			{
				colors[ 3 * i ] = 1f;
				colors[ 3 * i + 1 ] = ( float ) Math.random();
				colors[ 3 * i + 2 ] = ( float ) Math.random();
			}
			instanceArray.updateColors( gl, FloatBuffer.wrap( colors ) );
		}
		instancedEllipsoid.draw( gl, pv, camview, instanceArray );

		sceneBuf.unbind( gl, false );
		gl.glDisable( GL_DEPTH_TEST );
		sceneBuf.drawQuad( gl );
	}
}
