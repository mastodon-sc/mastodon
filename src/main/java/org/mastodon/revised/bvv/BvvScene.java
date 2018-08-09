package org.mastodon.revised.bvv;

import com.jogamp.opengl.GL3;

import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.bvv.scene.InstancedEllipsoid;

import tpietzsch.example2.VolumeViewerPanel.RenderData;
import tpietzsch.example2.VolumeViewerPanel.RenderScene;
import tpietzsch.util.MatrixMath;

public class BvvScene< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > >
	implements RenderScene
{
	private final BvvGraph< V, E > graph;

	private final SelectionModel< V, E > selection;

	private final HighlightModel< V, E > highlight;

	private final InstancedEllipsoid instancedEllipsoid;

	private final ReusableInstanceArrays< InstancedEllipsoid.InstanceArray > reusableInstanceArrays;

	private final RenderData renderData = new RenderData();

	private boolean isVisible = true;

	public BvvScene(
			final BvvGraph< V, E > graph,
			final SelectionModel< V, E > selection,
			final HighlightModel< V, E > highlight )
	{
		this.graph = graph;
		this.selection = selection;
		this.highlight = highlight;
		instancedEllipsoid = new InstancedEllipsoid( 3 );

		final int numInstanceArrays = 10;
		reusableInstanceArrays = new ReusableInstanceArrays<>(
				t -> graph.getEllipsoids().forTimepoint( t ).getModCount(),
				numInstanceArrays,
				instancedEllipsoid::createInstanceArray
		);

		selection.listeners().add( this::selectionChanged );
	}

	public boolean isVisible()
	{
		return isVisible;
	}

	public void setVisible( final boolean isVisible )
	{
		this.isVisible = isVisible;
	}

	@Override
	public void render(
			final GL3 gl,
			final RenderData data )
	{
		synchronized ( renderData )
		{
			renderData.set( data );
		}

		if ( !isVisible )
			return;

		final AffineTransform3D worldToScreen = data.getRenderTransformWorldToScreen();
		final int timepoint = data.getTimepoint();
		final double dCam = data.getDCam();
		final double screenWidth = data.getScreenWidth();
		final double screenHeight = data.getScreenHeight();
		final Matrix4f pv = data.getPv();

		final Matrix4f view = MatrixMath.affine( worldToScreen, new Matrix4f() );
		final Matrix4f camview = new Matrix4f()
			.translation( ( float ) ( -( screenWidth - 1 ) / 2 ), ( float ) ( -( screenHeight - 1 ) / 2 ), ( float ) dCam )
			.mul( view );

		final InstancedEllipsoid.InstanceArray instanceArray = reusableInstanceArrays.getForTimepoint( timepoint );
		final EllipsoidInstances< V, E > instances = graph.getEllipsoids().forTimepoint( timepoint );
		final int modCount = instances.getModCount();
		final boolean needShapeUpdate = instanceArray.getModCount() != modCount;
		if ( needShapeUpdate )
		{
			instanceArray.setModCount( modCount );
			instanceArray.updateShapes( gl, instances.buffer().asFloatBuffer() );
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
	public V getVertexAt( final int x, final int y, final double tolerance, final V ref )
	{
		// TODO: grpah locking?
		// TODO: KDTree clipping to region around ray.

		final RenderData data = getRenderDataCopy();
		final int timepoint = data.getTimepoint();
		final int w = ( int ) data.getScreenWidth();
		final int h = ( int ) data.getScreenHeight();

		final EllipsoidInstances< V, E > instances = graph.getEllipsoids().forTimepoint( timepoint );

		// ray through pixel
		final Matrix4f pvinv = new Matrix4f();
		final Vector3f pNear = new Vector3f();
		final Vector3f pFarMinusNear = new Vector3f();

		data.getPv().invert( pvinv );
		pvinv.unprojectInvRay( x + 0.5f, h - y - 0.5f, new int[] { 0, 0, w, h }, pNear, pFarMinusNear );


		final Matrix3f e = new Matrix3f();
		final Matrix3f inve = new Matrix3f();
		final Vector3f t = new Vector3f();
		final Vector3f a = new Vector3f();
		final Vector3f b = new Vector3f();
		final Vector3f a1 = new Vector3f();

		double bestDist = Double.POSITIVE_INFINITY;
		V best = null;

		for ( final EllipsoidInstance instance : instances )
		{
			instance.inve.get( inve ).transpose();
			inve.transform( instance.t.get( t ).sub( pNear ), a );
			inve.transform( pFarMinusNear, b );

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
						best = instances.getVertex( instance, ref );
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
	public E getEdgeAt( final int x, final int y, final double tolerance, final E ref )
	{
		// TODO
		return null;
	}


	/**
	 * Get a copy of the {@code renderTransform} (avoids synchronizing on it for
	 * a longer time period).
	 *
	 * @return a copy of the {@code renderTransform}.
	 */
	private RenderData getRenderDataCopy()
	{
		final RenderData copy = new RenderData();
		synchronized ( renderData )
		{
			copy.set( renderData );
		}
		return copy;
	}


	private int colorModCount = 1;

	private void selectionChanged()
	{
		++colorModCount;
	}
}
