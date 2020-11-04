package org.mastodon.views.dbvv;

import com.jogamp.opengl.GL3;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.views.bvv.scene.InstancedLink;
import org.mastodon.views.bvv.scene.InstancedSpot;
import tpietzsch.util.MatrixMath;

import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;

public class DBvvRenderer
{
	private final ModelGraph graph;

	private final DBvvEntities entities;

	private final SelectionModel< Spot, Link > selection;

	private final HighlightModel< Spot, Link > highlight;

	// TODO...
	private final double dCam = 2000;
	private final double dClip = 1000;

	private double screenWidth;
	private double screenHeight;

	private final InstancedSpot instancedEllipsoid;
	private final InstancedLink instancedLink;

	public DBvvRenderer(
			final int screenWidth,
			final int screenHeight,
			final ModelGraph graph,
			final DBvvEntities entities,
			final SelectionModel< Spot, Link > selection,
			final HighlightModel< Spot, Link > highlight )
	{
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.graph = graph;
		this.entities = entities;
		this.selection = selection;
		this.highlight = highlight;
		instancedEllipsoid = new InstancedSpot( 3, 10 );
		instancedLink = new InstancedLink( 36, 3, 20 );
		selection.listeners().add( this::selectionChanged );
	}

	public void setScreenSize( final double screenWidth, final double screenHeight )
	{
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
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
		// NB: The following shift-to-scree-center and offset-to-camera transformation
		//     is also done inside MatrixMath.screenPerspective(). However, we need the
		//     camview transformation also inside the geometry draw calls.
		// TODO: Maybe MatrixMath.screenPerspective() should be split up to make this more explicit
		final Matrix4f camview = new Matrix4f()
			.translation( ( float ) ( -( screenWidth - 1 ) / 2 ), ( float ) ( -( screenHeight - 1 ) / 2 ), ( float ) dCam )
			.mul( view );
		final Matrix4f projection = MatrixMath.screenPerspective( dCam, dClip, screenWidth, screenHeight, 0, new Matrix4f() );
		// TODO also possible:    = MatrixMath.screenPerspective( dCam, dClipNear, dClipFar, screenWidth, screenHeight, 0, new Matrix4f() );
		final Matrix4f pv = new Matrix4f( projection ).mul( view );

		gl.glClearColor( 0.0f, 0.2f, 0.1f, 0.0f );
		gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		gl.glEnable( GL_DEPTH_TEST );
		gl.glEnable( GL_CULL_FACE );
		gl.glCullFace( GL_BACK );
		gl.glFrontFace( GL_CCW );

		// -- paint vertices --------------------------------------------------
		final DColoredEllipsoids ellipsoids = entities.forTimepoint( timepoint ).ellipsoids;
		final Vector3f defaultColor = new Vector3f( 0.5f, 1.0f, 0.5f );
		final Vector3f selectedColor = new Vector3f( 1.0f, 0.7f, 0.7f );
		ellipsoids.updateColors( colorModCount, v -> selection.isSelected( v ) ? selectedColor : defaultColor );

		final Spot vref = graph.vertexRef();
		int highlightId = ellipsoids.indexOf( highlight.getHighlightedVertex( vref ) );
		graph.releaseRef( vref );

		instancedEllipsoid.draw( gl, pv, camview, ellipsoids.getEllipsoids(), highlightId );

		// -- paint edges -----------------------------------------------------
		// the maximum number of time-points into the past for which outgoing
		final int timeLimit = 10;
		final double rHead = 0.5;
		final double rTail = 0.01;
		final double f = ( rTail - rHead ) / ( timeLimit + 1 );

		final Link eref = graph.edgeRef();
		for ( int t = Math.max( 0, timepoint - timeLimit + 1 ); t <= timepoint; ++t )
		{
			final DColoredCylinders cylinders = entities.forTimepoint( t ).cylinders;
			cylinders.updateColors( colorModCount, e -> selection.isSelected( e ) ? selectedColor : defaultColor );
			highlightId = cylinders.indexOf( highlight.getHighlightedEdge( eref ) );
			final double r0 = rHead + f * ( timepoint - t );
			instancedLink.draw( gl, pv, camview, cylinders.getCylinders(), highlightId, r0 + f, r0 );
		}
		graph.releaseRef( eref );
	}

	private int colorModCount = 1;

	private void selectionChanged()
	{
		++colorModCount;
	}
}
