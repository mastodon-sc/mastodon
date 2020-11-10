package org.mastodon.views.dbvv;

import com.jogamp.opengl.GL3;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;
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

	private final SpatioTemporalIndex< Spot > index;

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
			final SpatioTemporalIndex< Spot > index, // TODO appModel.getModel().getSpatioTemporalIndex(),
			final DBvvEntities entities,
			final SelectionModel< Spot, Link > selection,
			final HighlightModel< Spot, Link > highlight )
	{
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.graph = graph;
		this.index = index;
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

		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
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
		// the maximum number of time-points into the past for which outgoing edges are painted
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

	public final class Closest< T >
	{
		private final T t;

		private final float distance;

		public Closest( final T t, final float distance )
		{
			this.t = t;
			this.distance = distance;
		}

		public T get()
		{
			return t;
		}

		public float distance()
		{
			return distance;
		}
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
	public Closest< Spot > getVertexAt( final int x, final int y, final double tolerance, final Spot ref )
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
		pvinv.unprojectInv( x + 0.5f, h - y - 0.5f, 0, viewport, pNear );
		pvinv.unprojectInv( x + 0.5f, h - y - 0.5f, 1, viewport, pFarMinusNear ).sub( pNear );

		final Matrix3f inve = new Matrix3f();
		final Vector3f t = new Vector3f();
		final Vector3f a = new Vector3f();
		final Vector3f b = new Vector3f();
		final Vector3f a1 = new Vector3f();

		float bestDist = Float.POSITIVE_INFINITY;
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
				final float dx = ( float ) Math.sqrt( ( 1.0 - aa1squ ) / b.lengthSquared() );
				final float d = abbb > dx ? abbb - dx : abbb + dx;
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
		return new Closest<>( best, bestDist );
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
	public Closest< Link > getEdgeAt( final int x, final int y, final double tolerance, final Link ref )
	{
		// TODO: graph locking?
		// TODO: prune candidate edges by some fast lookup structure

		final SceneRenderData data = renderData.copy();

		final int timepoint = data.getTimepoint();
		final int w = ( int ) data.getScreenWidth();
		final int h = ( int ) data.getScreenHeight();
		final int[] viewport = { 0, 0, w, h };

		// ray through pixel
		final Matrix4f pvinv = new Matrix4f();
		final Vector3f pNear = new Vector3f();
		final Vector3f pFar = new Vector3f();

		data.getPv().invert( pvinv );
		pvinv.unprojectInv( x + 0.5f, h - y - 0.5f, 0, viewport, pNear );
		pvinv.unprojectInv( x + 0.5f, h - y - 0.5f, 1, viewport, pFar );

		// TODO: these should come from SceneRenderData
		// the maximum number of time-points into the past for which outgoing edges are painted
		final int timeLimit = 10;
		final double rHead = 0.5;
		final double rTail = 0.01;
		final double f = ( rTail - rHead ) / ( timeLimit + 1 );

		final Spot vref = graph.vertexRef(); // TODO release
		final float[] spos = new float[ 3 ];
		final float[] tpos = new float[ 3 ];
		final Vector3f vspos = new Vector3f();
		final Vector3f vtpos = new Vector3f();

		float bestDist = Float.POSITIVE_INFINITY;
		Link best = null;

		EdgeDistance edgeDistance = new EdgeDistance( pNear, pFar );
		for ( int t = Math.max( 0, timepoint - timeLimit + 1 ); t <= timepoint; ++t )
		{
			final double r1 = rHead + f * ( timepoint - t + 1 );
			final SpatialIndex< Spot > si = index.getSpatialIndex( t );
			for ( final Spot target : si )
			{
				target.localize( tpos );
				vtpos.set( tpos );
				for ( final Link edge : target.incomingEdges() )
				{
					final Spot source = edge.getSource( vref );
					source.localize( spos );
					vspos.set( spos );
					final float d = edgeDistance.to( vspos, vtpos, ( float ) r1 );
					if ( d < bestDist )
					{
						bestDist = d;
						best = ref.refTo( edge );
					}
				}
			}
		}
		return new Closest<>( best, bestDist );
	}

	private static class EdgeDistance
	{
		private static final float SMALL_NUM = 1e-08f;

		private final Vector3fc p0;
		private final Vector3fc p1;
		private final Vector3fc u;
		private final float a;
		private final Vector3f v;
		private final Vector3f w;

		public EdgeDistance( final Vector3fc pNear, final Vector3fc pFar )
		{
			this.p0 = pNear;
			this.p1 = pFar;
			u = p1.sub( p0, new Vector3f() ); // always >= 0
			a = u.dot( u );
			v = new Vector3f();
			w = new Vector3f();
		}

		/**
		 * Adapted from:
		 * https://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment()
		 *
		 * Copyright 2001 softSurfer, 2012 Dan Sunday
		 * This code may be freely used and modified for any purpose
		 * providing that this copyright notice is included with it.
		 * SoftSurfer makes no warranty for this code, and cannot be held
		 * liable for any real or imagined damage resulting from its use.
		 * Users of this code must verify correctness for their application.
		 *
		 * @param q0
		 * 		start point of segment S2
		 * @param q1
		 * 		end point of segment S2
		 *
		 * @return the shortest distance between S1 and S2
		 */
		private float to( final Vector3fc q0, final Vector3fc q1, final float tolerance )
		{
			// TODO use the fact that S1 is always pNear, pFar for one frame
			q1.sub( q0, v );
			p0.sub( q0, w );
			final float b = u.dot( v );
			final float c = v.dot( v ); // always >= 0
			final float d = u.dot( w );
			final float e = v.dot( w );
			final float D = a * c - b * b; // always >= 0
			float sc, sN, sD = D; // sc = sN / sD, default sD = D >= 0
			float tc, tN, tD = D; // tc = tN / tD, default tD = D >= 0

			// compute the line parameters of the two closest points
			if ( D < SMALL_NUM ) // the lines are almost parallel
			{
				sN = 0f; // force using point P0 on segment S1
				sD = 1f; // to prevent possible division by 0.0 later
				tN = e;
				tD = c;
			}
			else // get the closest points on the infinite lines
			{
				sN = ( b * e - c * d );
				tN = ( a * e - b * d );
				if ( sN < 0f ) // sc < 0 => the s=0 edge is visible
				{
					sN = 0f;
					tN = e;
					tD = c;
				}
				else if ( sN > sD ) // sc > 1  => the s=1 edge is visible
				{
					sN = sD;
					tN = e + b;
					tD = c;
				}
			}

			if ( tN < 0f ) // tc < 0 => the t=0 edge is visible
			{
				tN = 0f;
				// recompute sc for this edge
				if ( -d < 0f )
					sN = 0f;
				else if ( -d > a )
					sN = sD;
				else
				{
					sN = -d;
					sD = a;
				}
			}
			else if ( tN > tD ) // tc > 1 => the t=1 edge is visible
			{
				tN = tD;
				// recompute sc for this edge
				if ( ( -d + b ) < 0.0 )
					sN = 0;
				else if ( ( -d + b ) > a )
					sN = sD;
				else
				{
					sN = ( -d + b );
					sD = a;
				}
			}
			// finally do the division to get sc and tc
			sc = ( Math.abs( sN ) < SMALL_NUM ? 0f : sN / sD );
			tc = ( Math.abs( tN ) < SMALL_NUM ? 0f : tN / tD );

			// get the difference of the two closest points
			final Vector3f dP = w.add( u.mul( sc, new Vector3f() ).sub( v.mul( tc ) ) ); // =  S1(sc) - S2(tc)

			if ( dP.length() < tolerance )
				return sc;
			return Float.POSITIVE_INFINITY;
		}
	}
}
