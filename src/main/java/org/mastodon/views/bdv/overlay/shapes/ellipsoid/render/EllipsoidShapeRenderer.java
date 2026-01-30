package org.mastodon.views.bdv.overlay.shapes.ellipsoid.render;

import java.awt.Graphics2D;

import org.mastodon.collection.RefCollection;
import org.mastodon.views.bdv.overlay.shapes.VertexShapeRenderer;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayEdge;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayVertex;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.style.EllipsoidRenderSettings;

import net.imglib2.realtransform.AffineTransform3D;

public class EllipsoidShapeRenderer<
		V extends EllipsoidOverlayVertex< V, E >,
		E extends EllipsoidOverlayEdge< E, V > >
		implements VertexShapeRenderer< V, E, EllipsoidRenderSettings >
{

	private final AffineTransform3D transform;

	private EllipsoidRenderSettings renderSettings;

	private int timepoint;

	public EllipsoidShapeRenderer()
	{
		this.transform = new AffineTransform3D();
	}

	@Override
	public void setTransform( final AffineTransform3D t )
	{
		this.transform.set( t );
	}

	@Override
	public void setShapeSettings( final EllipsoidRenderSettings renderSettings )
	{
		this.renderSettings = renderSettings;
	}

	@Override
	public void setCurrentTimepoint( final int timepoint )
	{
		this.timepoint = timepoint;
	}

	@Override
	public void drawVertex( final Graphics2D graphics2d, final V vertex )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsScreenPoint( final V vertex, final int screenX, final int screenY, final double tolerance )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RefCollection< V > getVisibleVertices( final AffineTransform3D transform, final int timepoint )
	{
		// TODO Auto-generated method stub
		return null;
	}

}
