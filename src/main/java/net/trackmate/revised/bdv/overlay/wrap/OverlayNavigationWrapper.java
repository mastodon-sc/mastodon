package net.trackmate.revised.bdv.overlay.wrap;

import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayNavigation;
import bdv.viewer.ViewerPanel;
import bdv.viewer.animate.TranslationAnimator;

public class OverlayNavigationWrapper< V extends Vertex< ? >> implements OverlayNavigation< OverlayVertexWrapper< V, ? > >
{

	// TODO TODO
	/*
	 * This is not right. This guy should be the actual navigation listener and
	 * use the wrapping to transform modelvertex -> overlayvertex, and move to
	 * this point. here it emulates what is in OverlayHighlightWrapper, but this
	 * is not what we want
	 */

	private final ViewerPanel panel;

	public OverlayNavigationWrapper( final ViewerPanel panel )
	{
		this.panel = panel;
	}

	@Override
	public void navigateTo( final OverlayVertexWrapper< V, ? > vertex )
	{
		final double[] gPos = new double[ 3 ];
		vertex.localize( gPos );

		final int tp = vertex.getTimepoint();
		panel.setTimepoint( tp );

		final AffineTransform3D t = panel.getDisplay().getTransformEventHandler().getTransform();
		final int width = panel.getWidth();
		final int height = panel.getHeight();

		final double dx = width / 2 - ( t.get( 0, 0 ) * gPos[ 0 ] + t.get( 0, 1 ) * gPos[ 1 ] + t.get( 0, 2 ) * gPos[ 2 ] );
		final double dy = height / 2 - ( t.get( 1, 0 ) * gPos[ 0 ] + t.get( 1, 1 ) * gPos[ 1 ] + t.get( 1, 2 ) * gPos[ 2 ] );
		final double dz = -( t.get( 2, 0 ) * gPos[ 0 ] + t.get( 2, 1 ) * gPos[ 1 ] + t.get( 2, 2 ) * gPos[ 2 ] );

		final double[] target = new double[] { dx, dy, dz };
		final TranslationAnimator animator = new TranslationAnimator( t, target, 300 );
		animator.setTime( System.currentTimeMillis() );
		panel.setTransformAnimator( animator );
		panel.requestRepaint();
	}

}
