package net.trackmate.revised.bdv.overlay.wrap;

import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayNavigation;
import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.revised.ui.selection.NavigationListener;
import bdv.viewer.ViewerPanel;
import bdv.viewer.animate.TranslationAnimator;

public class OverlayNavigationWrapper< V extends Vertex< E >, E extends Edge< V >> implements OverlayNavigation< OverlayVertexWrapper< V, E > >, NavigationListener< V >
{
	private final ViewerPanel panel;

	private final OverlayGraphWrapper< V, E > graph;

	private final NavigationHandler< V > navigation;

	public OverlayNavigationWrapper(
			final ViewerPanel panel,
			final OverlayGraphWrapper< V, E > graph,
			final NavigationHandler< V > navigation )
	{
		this.panel = panel;
		this.graph = graph;
		this.navigation = navigation;
		navigation.addNavigationListener( this );
	}

	@Override
	public void navigateToOverlayVertex( final OverlayVertexWrapper< V, E > vertex )
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

	@Override
	public void navigateToVertex( final V vertex )
	{
		final OverlayVertexWrapper< V, E > ref = graph.vertexRef();
		ref.wv = vertex;
		navigateToOverlayVertex( ref );
		graph.releaseRef( ref );
	}

	@Override
	public void notifyNavigateToVertex( final OverlayVertexWrapper< V, E > vertex )
	{
		navigation.notifyNavigateToVertex( vertex.wv );
	}
}
