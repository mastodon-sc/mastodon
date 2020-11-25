package org.mastodon.views.dbvv;

import bdv.viewer.animate.TranslationAnimator;
import net.imglib2.realtransform.AffineTransform3D;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.NavigationListener;
import org.mastodon.ui.NavigationEtiquette;

public class DBvvNavigation implements NavigationListener< Spot, Link >
{
	private final DBvvPanel panel;

	private final ModelGraph graph;

	private NavigationEtiquette navigationEtiquette;

	private NavigationBehaviour navigationBehaviour;

	public DBvvNavigation(
			final DBvvPanel panel,
			final ModelGraph graph )
	{
		this.panel = panel;
		this.graph = graph;
		setNavigationEtiquette( NavigationEtiquette.MINIMAL );
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

	@Override
	public void navigateToVertex( final Spot vertex )
	{
		// Always move in T.
		final int tp = vertex.getTimepoint();
		panel.setTimepoint( tp );

		final AffineTransform3D currentTransform = panel.state().getViewerTransform();
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
	public void navigateToEdge( final Link edge )
	{
		// Always move in T.
		final Spot ref = graph.vertexRef();
		final int tp = edge.getTarget( ref ).getTimepoint();
		graph.releaseRef( ref );
		panel.setTimepoint( tp );

		final AffineTransform3D currentTransform = panel.state().getViewerTransform();
		final double[] target = navigationBehaviour.navigateToEdge( edge, currentTransform );
		if ( target != null )
		{
			final TranslationAnimator animator = new TranslationAnimator( currentTransform, target, 300 );
			animator.setTime( System.currentTimeMillis() );
			panel.setTransformAnimator( animator );
		}

		panel.requestRepaint();
	}

	/*
	 * Navigation behaviours
	 */

	interface NavigationBehaviour
	{
		double[] navigateToVertex( final Spot vertex, final AffineTransform3D currentTransform );

		double[] navigateToEdge( final Link edge, final AffineTransform3D currentTransform );
	}

	private class CenteringNavigationBehaviour implements NavigationBehaviour
	{
		private final ScreenVertexMath screenVertexMath = new ScreenVertexMath();

		@Override
		public double[] navigateToVertex( final Spot vertex, final AffineTransform3D t )
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

		@Override
		public double[] navigateToEdge( final Link edge, final AffineTransform3D currentTransform )
		{
			// TODO Auto-generated method stub
			System.err.println( "not implemented: CenteringNavigationBehaviour.navigateToEdge()" );
			new Throwable().printStackTrace( System.out );
			return null;
		}
	}

	private class CenterIfInvisibleNavigationBehaviour implements NavigationBehaviour
	{
		private final ScreenVertexMath screenVertexMath = new ScreenVertexMath();

		@Override
		public double[] navigateToVertex( final Spot vertex, final AffineTransform3D t )
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

		@Override
		public double[] navigateToEdge( final Link edge, final AffineTransform3D currentTransform )
		{
			// TODO Auto-generated method stub
			System.err.println( "not implemented: CenterIfInvisibleNavigationBehaviour.navigateToEdge()" );
			new Throwable().printStackTrace( System.out );
			return null;
		}
	}

	private class MinimalNavigationBehaviour implements NavigationBehaviour
	{
		private final ScreenVertexMath screenVertexMath;

		private final ScreenEdgeMath screenEdgeMath;

		private final int screenBorderX;

		private final int screenBorderY;

		public MinimalNavigationBehaviour( final int screenBorderX, final int screenBorderY )
		{
			this.screenBorderX = screenBorderX;
			this.screenBorderY = screenBorderY;
			screenVertexMath = new ScreenVertexMath();
			screenEdgeMath = new ScreenEdgeMath( graph.vertexRef(), graph.vertexRef() );
		}

		@Override
		public double[] navigateToVertex( final Spot vertex, final AffineTransform3D t )
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

		@Override
		public double[] navigateToEdge( final Link edge, final AffineTransform3D t )
		{
			final int width = panel.getWidth();
			final int height = panel.getHeight();
			final int edgeMaxWidth = width - 2 * screenBorderX;
			final int edgeMaxHeight = height- 2 * screenBorderY;

			screenEdgeMath.init( edge, t );
			final double[] min = screenEdgeMath.getViewMin();
			final double[] max = screenEdgeMath.getViewMax();
			final double[] c = screenEdgeMath.getViewPos();

			double dx = t.get( 0, 3 );
			if ( max[ 0 ] - min[ 0 ] > edgeMaxWidth )
				dx += ( width / 2 ) - c[ 0 ];
			else if ( min[ 0 ] < screenBorderX )
				dx += screenBorderX - min[ 0 ];
			else if ( max[ 0 ] > width - screenBorderX )
				dx += width - screenBorderX - max[ 0 ];

			double dy = t.get( 1, 3 );
			if ( max[ 1 ] - min[ 1 ] > edgeMaxHeight )
				dy += ( height / 2 ) - c[ 1 ];
			else if ( min[ 1 ] < screenBorderY )
				dy += screenBorderY - min[ 1 ];
			else if ( max[ 1 ] > height - screenBorderY )
				dy += height - screenBorderY - max[ 1 ];

			final double dz = -c[ 2 ] + t.get( 2, 3 );

			return new double[] { dx, dy, dz };
		}
	}

	private static class ScreenVertexMath
	{
		/**
		 * spot position in global coordinate system.
		 */
		private final double[] pos = new double[ 3 ];

		/**
		 * spot position in viewer coordinate system.
		 */
		private final double[] vPos = new double[ 3 ];

		public void init( final Spot vertex, final AffineTransform3D viewerTransform )
		{
			vertex.localize( pos );
			viewerTransform.apply( pos, vPos );
		}

		/**
		 * Get spot position in viewer coordinate system.
		 *
		 * @return spot position in viewer coordinate system.
		 */
		public double[] getViewPos()
		{
			return vPos;
		}
	}

	private static class ScreenEdgeMath
	{
		private final Spot ref1;

		private final Spot ref2;

		private final ScreenVertexMath svmSource = new ScreenVertexMath();

		private final ScreenVertexMath svmTarget = new ScreenVertexMath();

		/**
		 * min of edge bounding box in viewer coordinate system.
		 */
		private final double[] min = new double[ 3 ];

		/**
		 * max of edge bounding box in viewer coordinate system.
		 */
		private final double[] max = new double[ 3 ];

		/**
		 * edge position in viewer coordinate system. (center, between the
		 * source and target position)
		 */
		private final double[] c = new double[ 3 ];

		public ScreenEdgeMath( final Spot tempRef1, final Spot tempRef2 )
		{
			this.ref1 = tempRef1;
			this.ref2 = tempRef2;
		}

		public void init( final Link edge, final AffineTransform3D viewerTransform )
		{
			svmSource.init( edge.getSource( ref1 ), viewerTransform );
			svmTarget.init( edge.getTarget( ref2 ), viewerTransform );
			final double[] vPosSource = svmSource.getViewPos();
			final double[] vPosTarget = svmTarget.getViewPos();
			for ( int d = 0; d < 3; ++d )
			{
				if ( vPosSource[ d ] < vPosTarget[ d ] )
				{
					min[ d ] = vPosSource[ d ];
					max[ d ] = vPosTarget[ d ];
				}
				else
				{
					max[ d ] = vPosSource[ d ];
					min[ d ] = vPosTarget[ d ];
				}
				c[ d ] = 0.5 * ( min[ d ] + max[ d ] );
			}
		}

		/**
		 * Get min of edge bounding box in viewer coordinate system.
		 *
		 * @return min of edge bounding box in viewer coordinate system.
		 */
		public double[] getViewMin()
		{
			return min;
		}

		/**
		 * Get max of edge bounding box in viewer coordinate system.
		 *
		 * @return max of edge bounding box in viewer coordinate system.
		 */
		public double[] getViewMax()
		{
			return max;
		}
		/**
		 * Get edge position in viewer coordinate system.
		 *
		 * @return edge position in viewer coordinate system.
		 */
		public double[] getViewPos()
		{
			return c;
		}
	}
}
