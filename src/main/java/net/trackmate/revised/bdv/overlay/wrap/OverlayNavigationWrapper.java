package net.trackmate.revised.bdv.overlay.wrap;

import bdv.viewer.ViewerPanel;
import bdv.viewer.animate.TranslationAnimator;
import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayNavigation;
import net.trackmate.revised.bdv.overlay.ScreenVertexMath;
import net.trackmate.revised.ui.selection.NavigationEtiquette;
import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.revised.ui.selection.NavigationListener;

public class OverlayNavigationWrapper< V extends Vertex< E >, E extends Edge< V > > implements OverlayNavigation< OverlayVertexWrapper< V, E > >, NavigationListener< V >
{
	private final ViewerPanel panel;

	private final OverlayGraphWrapper< V, E > graph;

	private final NavigationHandler< V > navigation;

	private NavigationEtiquette navigationEtiquette;

	private NavigationBehaviour< V, E > navigationBehaviour;

	public OverlayNavigationWrapper(
			final ViewerPanel panel,
			final OverlayGraphWrapper< V, E > graph,
			final NavigationHandler< V > navigation )
	{
		this.panel = panel;
		this.graph = graph;
		this.navigation = navigation;
		setNavigationEtiquette( NavigationEtiquette.MINIMAL );
		navigation.addNavigationListener( this );
	}

	public NavigationEtiquette getNavigationEtiquette()
	{
		return navigationEtiquette;
	}

	public void setNavigationEtiquette( final NavigationEtiquette navigationEtiquette )
	{
		this.navigationEtiquette = navigationEtiquette;

		switch( navigationEtiquette )
		{
		case MINIMAL:
			navigationBehaviour = new MinimalNavigationBehaviour( 100, 100 );
			break;
		case CENTER_IF_INVISIBLE:
			navigationBehaviour = new CenterIfInvisibleNavigationBehaviour();
			break;
		case CENTERING:
		default:
			navigationBehaviour = new CenteringNavigationBehaviour();
			break;
		}
	}

	/*
	 * NavigationListener< V >
	 */

	@Override
	public void navigateToVertex( final V vertex )
	{
		final OverlayVertexWrapper< V, E > ref = graph.vertexRef();
		ref.wv = vertex;
		navigateToOverlayVertex( ref );
		graph.releaseRef( ref );
	}

	/*
	 * OverlayNavigation< OverlayVertexWrapper< V, E > >
	 */

	@Override
	public void navigateToOverlayVertex( final OverlayVertexWrapper< V, E > vertex )
	{
		// Always move in T.
		final int tp = vertex.getTimepoint();
		panel.setTimepoint( tp );

		final AffineTransform3D currentTransform = panel.getDisplay().getTransformEventHandler().getTransform();
		final double[] target = navigationBehaviour.navigateToVertex( vertex, currentTransform );
		if ( target != null )
		{
			final TranslationAnimator animator = new TranslationAnimator( currentTransform, target, 300 );
			animator.setTime( System.currentTimeMillis() );
			panel.setTransformAnimator( animator );
		}

		panel.requestRepaint();
	}

	@Override
	public void notifyNavigateToVertex( final OverlayVertexWrapper< V, E > vertex )
	{
		navigation.notifyNavigateToVertex( vertex.wv );
	}

	/*
	 * Navigation behaviours
	 */

	interface NavigationBehaviour< V extends Vertex< E >, E extends Edge< V > >
	{
		public double[] navigateToVertex( final OverlayVertexWrapper< V, E > vertex, final AffineTransform3D currentTransform );
	}

	private class CenteringNavigationBehaviour implements NavigationBehaviour< V, E >
	{
		private final ScreenVertexMath screenVertexMath = new ScreenVertexMath();

		@Override
		public double[] navigateToVertex( final OverlayVertexWrapper< V, E > vertex, final AffineTransform3D t )
		{
			final int width = panel.getWidth();
			final int height = panel.getHeight();

			screenVertexMath.init( vertex, t );
			final double[] vPos = screenVertexMath.getViewPos();
			final double dx = width / 2 - vPos[ 0 ] + t.get( 0, 3 );
			final double dy = height / 2 - vPos[ 1 ] + t.get( 1, 3 );
			final double dz = -vPos[ 2 ] + t.get( 2, 3 );

			return new double[] { dx, dy, dz };
		}
	}

	private class CenterIfInvisibleNavigationBehaviour implements NavigationBehaviour< V, E >
	{
		private final ScreenVertexMath screenVertexMath = new ScreenVertexMath();

		@Override
		public double[] navigateToVertex( final OverlayVertexWrapper< V, E > vertex, final AffineTransform3D t )
		{
			final int width = panel.getWidth();
			final int height = panel.getHeight();

			screenVertexMath.init( vertex, t );
			final double[] vPos = screenVertexMath.getViewPos();
			double dx = t.get( 0, 3 );
			if ( vPos[ 0 ] < 0 || vPos[ 0 ] > width )
				dx += width / 2 - vPos[ 0 ];
			double dy = t.get( 1, 3 );
			if ( vPos[ 1 ] < 0 || vPos[ 1 ] > height )
				dy += height / 2 - vPos[ 1 ];
			final double dz = -vPos[ 2 ] + t.get( 2, 3 );

			return new double[] { dx, dy, dz };
		}
	}

	private class MinimalNavigationBehaviour implements NavigationBehaviour< V, E >
	{
		private final ScreenVertexMath screenVertexMath = new ScreenVertexMath();

		private final int screenBorderX;

		private final int screenBorderY;

		public MinimalNavigationBehaviour( final int screenBorderX, final int screenBorderY )
		{
			this.screenBorderX = screenBorderX;
			this.screenBorderY = screenBorderY;
		}

		@Override
		public double[] navigateToVertex( final OverlayVertexWrapper< V, E > vertex, final AffineTransform3D t )
		{
			final int width = panel.getWidth();
			final int height = panel.getHeight();

			screenVertexMath.init( vertex, t );
			final double[] vPos = screenVertexMath.getViewPos();
			double dx = t.get( 0, 3 );
			if ( vPos[ 0 ] < screenBorderX )
				dx += screenBorderX - vPos[ 0 ];
			else if ( vPos[ 0 ] > width - screenBorderX )
				dx += width - screenBorderX - vPos[ 0 ];

			double dy = t.get( 1, 3 );
			if ( vPos[ 1 ] < screenBorderY )
				dy += screenBorderY - vPos[ 1 ];
			else if ( vPos[ 1 ] > height - screenBorderY )
				dy += height - screenBorderY - vPos[ 1 ];

			final double dz = -vPos[ 2 ] + t.get( 2, 3 );

			return new double[] { dx, dy, dz };
		}
	}
}

