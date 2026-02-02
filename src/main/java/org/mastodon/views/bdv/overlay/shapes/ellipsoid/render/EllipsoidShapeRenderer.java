package org.mastodon.views.bdv.overlay.shapes.ellipsoid.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.mastodon.collection.RefCollection;
import org.mastodon.views.bdv.overlay.shapes.VertexShapeRenderer;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayEdge;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayVertex;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.render.ScreenVertexMath.Ellipse;
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

	/**
	 * Routine to draw an ellipse.
	 *
	 * @param graphics
	 *            the graphics context.
	 * @param ellipse
	 *            the ellipse to draw.
	 * @param torig
	 *            the original transform of the graphics context. If null, the
	 *            current transform of the graphics context is used.
	 * @param fillSpots
	 *            if true, the ellipse is filled, otherwise only the outline is
	 *            drawn.
	 */
	public static void drawEllipse( final Graphics2D graphics, final Ellipse ellipse, AffineTransform torig, final boolean fillSpots )
	{
		if ( torig == null )
			torig = graphics.getTransform();

		final double[] tr = ellipse.getCenter();
		final double theta = ellipse.getTheta();
		final double w = ellipse.getHalfWidth();
		final double h = ellipse.getHalfHeight();
		final Ellipse2D ellipse2D = new Ellipse2D.Double( -w, -h, 2. * w, 2. * h );

		graphics.translate( tr[ 0 ], tr[ 1 ] );
		graphics.rotate( theta );
		if ( fillSpots )
		{
			graphics.fill( ellipse2D );
			final Color color = graphics.getColor();
			graphics.setColor( Color.BLACK );
			graphics.draw( ellipse2D );
			graphics.setColor( color );
		}
		else
		{
			graphics.draw( ellipse2D );
		}

		graphics.setTransform( torig );
	}

	// TODO: move to RenderSettings
	static final Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	static void drawEllipseLabel( final Graphics2D graphics, final Ellipse ellipse, final String label )
	{
		final double[] tr = ellipse.getCenter();
		final FontRenderContext frc = graphics.getFontRenderContext();
		final TextLayout layout = new TextLayout( label, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float tx = ( float ) ( tr[ 0 ] - bounds.getCenterX() );
		final float ty = ( float ) ( tr[ 1 ] - bounds.getCenterY() );
		layout.draw( graphics, tx, ty );
	}
}
