package org.mastodon.views.dbvv;

import com.jogamp.opengl.GL3;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.views.bvv.scene.Ellipsoid;
import org.mastodon.views.bvv.scene.InstancedLink;
import org.mastodon.views.bvv.scene.InstancedSpot;
import org.mastodon.views.bvv.scene.InstancedSpot.SpotDrawingMode;

import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static org.mastodon.views.bvv.scene.InstancedSpot.SpotDrawingMode.ELLIPSOIDS;

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




	private final SceneRenderData renderData = new SceneRenderData();

	public void display(
			final GL3 gl,
			final AffineTransform3D worldToScreen,
			final int timepoint )
	{
		final SceneRenderData data = new SceneRenderData( timepoint, worldToScreen, dCam, dClip, dClip, screenWidth, screenHeight );
		renderData.set( data );
		final Matrix4fc pv = data.getPv();
		final Matrix4fc camview = data.getCamview();

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

		instancedEllipsoid.draw( gl, pv, camview, ellipsoids.getEllipsoids(), highlightId, data.getSpotDrawingMode() );

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

	/**
	 * Returns the vertex currently painted close to the specified location.
	 * <p>
	 * It is the responsibility of the caller to lock the graph it inspects for
	 * reading operations, prior to calling this method. A typical call from
	 * another method would happen like this:
	 *
	 * <pre>
	 * ReentrantReadWriteLock lock = graph.getLock();
	 * lock.readLock().lock();
	 * try
	 * {
	 * 	V vertex = renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, ref );
	 * 	... // do something with the vertex
	 * 	... // vertex is guaranteed to stay valid while the lock is held
	 * }
	 * finally
	 * {
	 * 	lock.readLock().unlock();
	 * }
	 * </pre>
	 *
	 * @param x
	 *            the x location to search, in viewer coordinates (screen).
	 * @param y
	 *            the y location to search, in viewer coordinates (screen).
	 * @param tolerance
	 *            the distance tolerance to accept close vertices.
	 * @param ref
	 *            a vertex reference, that might be used to return the vertex
	 *            found.
	 * @return the closest vertex within tolerance, or <code>null</code> if it
	 *         could not be found.
	 */
	public Spot getVertexAt( final int x, final int y, final double tolerance, final Spot ref )
	{
		// TODO: graph locking?
		// TODO: KDTree clipping to region around ray.

		final SceneRenderData data = renderData.copy();
		final SpotDrawingMode spotDrawingMode = data.getSpotDrawingMode();

		final int timepoint = data.getTimepoint();
		final int w = ( int ) data.getScreenWidth();
		final int h = ( int ) data.getScreenHeight();
		final int[] viewport = { 0, 0, w, h };

		final DColoredEllipsoids ellipsoids = entities.forTimepoint( timepoint ).ellipsoids;

		// ray through pixel
		final Matrix4f pvinv = new Matrix4f();
		final Vector3f pNear = new Vector3f();
		final Vector3f pFarMinusNear = new Vector3f();

		data.getPv().invert( pvinv );
//		pvinv.unprojectInvRay( x + 0.5f, h - y - 0.5f, viewport, pNear, pFarMinusNear );
		pvinv.unprojectInv( x + 0.5f, h - y - 0.5f, 0, viewport, pNear );
		pvinv.unprojectInv( x + 0.5f, h - y - 0.5f, 1, viewport, pFarMinusNear ).sub( pNear );

		final Matrix3f inve = new Matrix3f();
		final Vector3f t = new Vector3f();
		final Vector3f a = new Vector3f();
		final Vector3f b = new Vector3f();
		final Vector3f a1 = new Vector3f();

		double bestDist = Double.POSITIVE_INFINITY;
		Spot best = null;

		for ( final Ellipsoid ellipsoid : ellipsoids.getEllipsoids() )
		{
			if ( spotDrawingMode == ELLIPSOIDS )
			{
				ellipsoid.invte.get( inve ).transpose();
				inve.transform( ellipsoid.t.get( t ).sub( pNear ), a );
				inve.transform( pFarMinusNear, b );
			}
			else // if ( spotDrawingMode == SPHERES )
			{
				final float radius = 3f;
				ellipsoid.t.get( t ).sub( pNear ).div( radius, a );
				pFarMinusNear.div( radius, b );
			}

			final float abbb = a.dot( b ) / b.dot( b );
			b.mul( abbb, a1 );
			final float aa1squ = a.sub( a1 ).lengthSquared();
			if ( aa1squ <= 1.0f )
			{
				final double dx = Math.sqrt( ( 1.0 - aa1squ ) / b.lengthSquared() );
				final double d = abbb > dx ? abbb - dx : abbb + dx;
				if ( d >= 0 && d <= 1 )
				{
					if ( d <= bestDist )
					{
						bestDist = d;
						best = ellipsoids.getVertex( ellipsoid, ref );
					}
				}
			}
		}
		return best;
	}

	/**
	 * Returns the edge currently painted close to the specified location.
	 * <p>
	 * It is the responsibility of the caller to lock the graph it inspects for
	 * reading operations, prior to calling this method. A typical call from
	 * another method would happen like this:
	 *
	 * <pre>
	 * ReentrantReadWriteLock lock = graph.getLock();
	 * lock.readLock().lock();
	 * boolean found = false;
	 * try
	 * {
	 * 	E edge = renderer.getEdgeAt( x, y, EDGE_SELECT_DISTANCE_TOLERANCE, ref )
	 * 	... // do something with the edge
	 * 	... // edge is guaranteed to stay valid while the lock is held
	 * }
	 * finally
	 * {
	 * 	lock.readLock().unlock();
	 * }
	 * </pre>
	 *
	 * @param x
	 *            the x location to search, in viewer coordinates (screen).
	 * @param y
	 *            the y location to search, in viewer coordinates (screen).
	 * @param tolerance
	 *            the distance tolerance to accept close edges.
	 * @param ref
	 *            an edge reference, that might be used to return the vertex
	 *            found.
	 * @return the closest edge within tolerance, or <code>null</code> if it
	 *         could not be found.
	 */
	public Link getEdgeAt( final int x, final int y, final double tolerance, final Link ref )
	{
		// TODO
		return null;
	}
}
