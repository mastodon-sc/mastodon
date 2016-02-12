package net.trackmate.revised.bdv.overlay;

import java.util.concurrent.locks.Lock;

import bdv.viewer.TimePointListener;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.context.Context;
import net.trackmate.revised.trackscheme.context.ContextListener;
import net.trackmate.spatial.ClipConvexPolytope;
import net.trackmate.spatial.SpatioTemporalIndex;

public class BdvContext< V > implements
		TransformListener< AffineTransform3D >,
		TimePointListener,
		Context< V >
{
	private final OverlayGraphRenderer< ?, ? > tracksOverlay;

	private final SpatioTemporalIndex< V > modelIndex;

	private final ContextListener< V > contextListener;

	private final AffineTransform3D transform = new AffineTransform3D();

	private int timepoint = 0;

	public BdvContext( final OverlayGraphRenderer< ?, ? > tracksOverlay, final SpatioTemporalIndex< V > modelIndex, final ContextListener< V > contextListener )
	{
		this.modelIndex = modelIndex;
		this.tracksOverlay = tracksOverlay;
		this.contextListener = contextListener;
	}

	@Override
	public void timePointChanged( final int timePointIndex )
	{
		if ( timepoint != timePointIndex )
		{
			timepoint = timePointIndex;
			contextListener.contextChanged( this );
		}
	}

	@Override
	public void transformChanged( final AffineTransform3D t )
	{
		transform.set( t );
		contextListener.contextChanged( this );
	}

	@Override
	public Lock readLock()
	{
		return modelIndex.readLock();
	}

	@Override
	public Iterable< V > getInsideVertices( final int timepoint )
	{
		final ConvexPolytope visiblePolytope = tracksOverlay.getVisiblePolytopeGlobal( transform, timepoint );
		final ClipConvexPolytope< V > ccp = modelIndex.getSpatialIndex( timepoint ).getClipConvexPolytope();
		ccp.clip( visiblePolytope );
		return ccp.getInsideValues();
	}

	@Override
	public int getTimepoint()
	{
		return timepoint;
	}
}
