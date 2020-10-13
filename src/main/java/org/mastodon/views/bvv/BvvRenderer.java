package org.mastodon.views.bvv;

import com.jogamp.opengl.GL3;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.views.bvv.scene.InstancedCylinder;
import org.mastodon.views.bvv.scene.InstancedEllipsoid;
import tpietzsch.offscreen.OffScreenFrameBufferWithDepth;
import tpietzsch.util.MatrixMath;

import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_RGB8;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;

public class BvvRenderer< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > >
{
	private final BvvGraph< V, E > graph;

	private final SelectionModel< V, E > selection;

	private final HighlightModel< V, E > highlight;

	private final OffScreenFrameBufferWithDepth sceneBuf;

	// TODO...
	private final double dCam = 2000;
	private final double dClip = 1000;
	private double screenWidth = 640;
	private double screenHeight = 480;

	private final InstancedEllipsoid instancedEllipsoid;
	private final InstancedCylinder instancedCylinder;

	private final ReusableInstanceArrays< InstancedEllipsoid.InstanceArray > reusableInstanceArrays;

	public BvvRenderer(
			final int renderWidth,
			final int renderHeight,
			final BvvGraph< V, E > graph,
			final SelectionModel< V, E > selection,
			final HighlightModel< V, E > highlight )
	{
		this.graph = graph;
		this.selection = selection;
		this.highlight = highlight;
		sceneBuf = new OffScreenFrameBufferWithDepth( renderWidth, renderHeight, GL_RGB8 );
		instancedEllipsoid = new InstancedEllipsoid( 3 );
		instancedCylinder = new InstancedCylinder( 36 );

		final int numInstanceArrays = 10;
		reusableInstanceArrays = new ReusableInstanceArrays<>(
				t -> graph.getEllipsoids().forTimepoint( t ).getModCount(),
				numInstanceArrays,
				instancedEllipsoid::createInstanceArray
		);

		selection.listeners().add( this::selectionChanged );
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
		gl.glEnable( GL_CULL_FACE );
		gl.glCullFace( GL_BACK );
		gl.glFrontFace( GL_CCW );
		final InstancedEllipsoid.InstanceArray instanceArray = reusableInstanceArrays.getForTimepoint( timepoint );
		final EllipsoidInstances< V, E > instances = graph.getEllipsoids().forTimepoint( timepoint );
		final int modCount = instances.getModCount();
		final boolean needShapeUpdate = instanceArray.getModCount() != modCount;
		if ( needShapeUpdate )
		{
			instanceArray.setModCount( modCount );
			instanceArray.updateShapes( gl, instances.ellipsoidBuffer().asFloatBuffer() );
		}

		final boolean needColorUpdate = instances.getColorModCount() != colorModCount;
		if ( needColorUpdate )
		{
			instances.setColorModCount( colorModCount );
			final Vector3f defaultColor = new Vector3f( 0.5f, 1.0f, 0.5f );
			final Vector3f selectedColor = new Vector3f( 1.0f, 0.7f, 0.7f );
			instances.updateColors( v -> selection.isSelected( v ) ? selectedColor : defaultColor );
		}

		if ( needShapeUpdate || needColorUpdate )
		{
			instanceArray.updateColors( gl, instances.colorBuffer().asFloatBuffer() );
		}

		final V vref = graph.vertexRef();
		final V vertex = highlight.getHighlightedVertex( vref );
		final int highlightId = instances.indexOf( vertex );
		graph.releaseRef( vref );
		instancedEllipsoid.draw( gl, pv, camview, instanceArray, highlightId );

		if ( cylInstanceArray == null )
		{
			cylInstanceArray = instancedCylinder.createInstanceArray();
			CylinderInstances cylinders = new CylinderInstances();
			cylinders.addInstanceFor( new Vector3f( 0, 0, 0 ), new Vector3f( 0, 100, 0 ) );
			cylInstanceArray.updateShapes( gl, cylinders.buffer().asFloatBuffer() );
		}
		instancedCylinder.draw( gl, pv, camview, cylInstanceArray );

		sceneBuf.unbind( gl, false );
		gl.glDisable( GL_DEPTH_TEST );
		sceneBuf.drawQuad( gl );
	}

	private InstancedCylinder.InstanceArray cylInstanceArray;

	private int colorModCount = 1;

	private void selectionChanged()
	{
		++colorModCount;
	}
}
