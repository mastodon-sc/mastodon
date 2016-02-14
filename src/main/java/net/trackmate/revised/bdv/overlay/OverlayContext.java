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

public class OverlayContext< V extends OverlayVertex< V, ? > > implements
		Context< V >,
		TransformListener< AffineTransform3D >,
		TimePointListener
{
	private final OverlayGraph< V, ? > graph;

	private final SpatioTemporalIndex< V > index;

	private final OverlayGraphRenderer< V, ? > renderer;

	private ContextListener< V > contextListener = null;

	public OverlayContext(
			final OverlayGraph< V, ? > overlayGraph,
			final OverlayGraphRenderer< V, ? > renderer )
	{
		this.graph = overlayGraph;
		this.index = graph.getIndex();
		this.renderer = renderer;
	}

	@Override
	public Lock readLock()
	{
		return index.readLock();
	}

	@Override
	public int getTimepoint()
	{
		return timepoint;
	}

	@Override
	public Iterable< V > getInsideVertices( final int timepoint )
	{
		final ConvexPolytope visiblePolytope = renderer.getVisiblePolytopeGlobal( transform, timepoint );
		final ClipConvexPolytope< V > ccp = index.getSpatialIndex( timepoint ).getClipConvexPolytope();
		ccp.clip( visiblePolytope );
		return ccp.getInsideValues();
	}

	private final AffineTransform3D transform = new AffineTransform3D();

	private int timepoint = 0;

	@Override
	public void timePointChanged( final int timePointIndex )
	{
		if ( timepoint != timePointIndex )
		{
			timepoint = timePointIndex;
			if ( contextListener != null )
				contextListener.contextChanged( this );
		}
	}

	@Override
	public void transformChanged( final AffineTransform3D t )
	{
		transform.set( t );
		if ( contextListener != null )
			contextListener.contextChanged( this );
	}

	public void setContextListener( final ContextListener< V > contextListener )
	{
		this.contextListener = contextListener;
	}
}
