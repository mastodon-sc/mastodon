package net.trackmate.revised.bdv.overlay.wrap;

import static net.trackmate.revised.ui.selection.NavigationEtiquette.CENTERING;
import static net.trackmate.revised.ui.selection.NavigationEtiquette.MINIMAL;

import bdv.viewer.ViewerPanel;
import bdv.viewer.animate.TranslationAnimator;
import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayGraphRenderer;
import net.trackmate.revised.bdv.overlay.OverlayNavigation;
import net.trackmate.revised.bdv.overlay.ScreenVertexMath;
import net.trackmate.revised.ui.selection.NavigationEtiquette;
import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.revised.ui.selection.NavigationListener;

public class OverlayNavigationWrapper< V extends Vertex< E >, E extends Edge< V >> implements OverlayNavigation< OverlayVertexWrapper< V, E > >, NavigationListener< V >
{
	private final ViewerPanel panel;

	private final OverlayGraphWrapper< V, E > graph;

	private final NavigationHandler< V > navigation;

	private final NavigationEtiquette navigationEtiquette = MINIMAL;

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
		final AffineTransform3D t = panel.getDisplay().getTransformEventHandler().getTransform();
		final ScreenVertexMath screenVertexMath = new ScreenVertexMath( OverlayGraphRenderer.nSigmas );
		screenVertexMath.init( vertex, t );

		// Always move in T.
		final int tp = vertex.getTimepoint();
		panel.setTimepoint( tp );

		final int width = panel.getWidth();
		final int height = panel.getHeight();
		final double[] vPos = screenVertexMath.getViewPos();
		if ( navigationEtiquette == CENTERING || ( vPos[ 0 ] < 0 || vPos[ 0 ] > width || vPos[ 1 ] < 0 || vPos[ 1 ] > height ) )
		{
			final double dx = width / 2 - vPos[ 0 ] + t.get( 0, 3 );
			final double dy = height / 2 - vPos[ 1 ] + t.get( 1, 3 );
			final double dz = -vPos[ 2 ] + t.get( 2, 3 );
			final double[] target = new double[] { dx, dy, dz };
			final TranslationAnimator animator = new TranslationAnimator( t, target, 300 );
			animator.setTime( System.currentTimeMillis() );
			panel.setTransformAnimator( animator );
			panel.requestRepaint();
		}
		else if ( !screenVertexMath.intersectsViewPlane() )
		{
			// If X & Y are good but not Z, we only translate in Z.
			final double dx = t.get( 0, 3 );
			final double dy = t.get( 1, 3 );
			final double dz = -vPos[ 2 ] + t.get( 2, 3 );
			final double[] target = new double[] { dx, dy, dz };
			final TranslationAnimator animator = new TranslationAnimator( t, target, 300 );
			animator.setTime( System.currentTimeMillis() );
			panel.setTransformAnimator( animator );
			panel.requestRepaint();
		}

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

