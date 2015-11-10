package net.trackmate.revised.bdv.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import net.imglib2.RealPoint;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.spatial.ClipConvexPolytope;
import net.trackmate.spatial.SpatialIndex;
import net.trackmate.spatial.SpatioTemporalIndex;
import bdv.util.Affine3DHelpers;
import bdv.viewer.ViewerPanel;


/**
 * TODO: Review and revise. Should use SpatialIndex search to determine what to paint.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class OverlayGraphRenderer< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		implements OverlayRenderer, TransformListener< AffineTransform3D >
{
	private int width;

	private int height;

	private final AffineTransform3D renderTransform;

	// TODO: remove. Only needed to get timepoint. there should be a better way to do that.
	private final ViewerPanel viewer;

	private final OverlayGraph< V, E > graph;

	private final OverlayHighlight< V, E > highlight;

	public OverlayGraphRenderer( final ViewerPanel viewer, final OverlayGraph< V, E > graph, final OverlayHighlight< V, E > highlight )
	{
		this.viewer = viewer;
		this.graph = graph;
		this.highlight = highlight;
		renderTransform = new AffineTransform3D();
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{
		this.width = width;
		this.height = height;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	@Override
	public void transformChanged( final AffineTransform3D transform )
	{
		synchronized ( renderTransform )
		{
			renderTransform.set( transform );
		}
	}

	public void mouseOverHighlight( final int x, final int y )
	{
		final AffineTransform3D transform = new AffineTransform3D();
		synchronized ( renderTransform )
		{
			transform.set( renderTransform );
		}

		// TODO: fix in BDV. This is stupid, BDV should have addTimepointListener() or something similar.
		final int currentTimepoint = viewer.getState().getCurrentTimepoint();

		final double[] lPos = new double[] { x, y, 0 };
		final double[] gPos = new double[ 3 ];
		final ScreenVertexMath svm = new ScreenVertexMath( nSigmas );
		transform.applyInverse( gPos, lPos );

		final SpatioTemporalIndex< V > index = graph.getIndex();
		index.readLock().lock();
		try
		{
			if ( drawSliceIntersection )
			{
				final NearestNeighborSearch< V > nns = index.getSpatialIndex( currentTimepoint ).getNearestNeighborSearch();
				nns.search( RealPoint.wrap( gPos ) );
				final V v = nns.getSampler().get();
				if ( v != null )
				{
					svm.init( v, transform );
					if ( svm.containsGlobal( gPos ) )
					{
						highlight.highlightVertex( v );
						return;
					}
				}
			}
			else // drawSliceProjection
			{
				final double globalToViewerScale = Affine3DHelpers.extractScale( transform, 0 );
				final double border = globalToViewerScale * Math.sqrt( graph.getMaxBoundingSphereRadiusSquared( currentTimepoint ) );
				final ConvexPolytope cropPolytopeViewer = new ConvexPolytope(
						new HyperPlane(  0,  0,  1, -focusLimit ),
						new HyperPlane(  0,  0, -1, -focusLimit ),
						new HyperPlane(  1,  0,  0, x - border ),
						new HyperPlane( -1,  0,  0, -x - border ),
						new HyperPlane(  0,  1,  0, y - border ),
						new HyperPlane(  0, -1,  0, -y - border ) );
				final ConvexPolytope cropPolytopeGlobal = ConvexPolytope.transform( cropPolytopeViewer, transform.inverse() );
				final ClipConvexPolytope< V > ccp = index.getSpatialIndex( currentTimepoint ).getClipConvexPolytope();
				ccp.clip( cropPolytopeGlobal );

				final double[] xy = new double[] { x, y };
				final double[] vPos = new double[ 3 ];
				double minDist = Double.MAX_VALUE;
				final V minV = graph.vertexRef();
				boolean found = false;
				for ( final V v : ccp.getInsideValues() )
				{
					svm.init( v, transform );
					if ( svm.projectionContainsView( xy ) )
					{
						found = true;
						v.localize( vPos );
						final double d = LinAlgHelpers.squareDistance( vPos, gPos );
						if ( d < minDist )
						{
							minDist = d;
							minV.refTo( v );
						}
					}
				}
				if ( found )
				{
					highlight.highlightVertex( minV );
					graph.releaseRef( minV );
					return;
				}
				graph.releaseRef( minV );
			}
		}
		finally
		{
			index.readLock().unlock();
		}

		highlight.highlightVertex( null );
	}

	/*
	 * PUBLIC DISPLAY CONFIG DEFAULTS.
	 */

	public static final double DEFAULT_LIMIT_TIME_RANGE = 20.;

	public static final double DEFAULT_LIMIT_FOCUS_RANGE = 100.;

	public static final Object DEFAULT_ALIASING_MODE = RenderingHints.VALUE_ANTIALIAS_ON;

	public static final boolean DEFAULT_USE_GRADIENT = false;

	public static final boolean DEFAULT_DRAW_SPOTS = true;

	public static final boolean DEFAULT_DRAW_LINKS = false;//true;

	public static final boolean DEFAULT_DRAW_ELLIPSE = true;

	public static final boolean DEFAULT_DRAW_SLICE_INTERSECTION = false;//true;

	/*
	 * DISPLAY SETTINGS FIELDS.
	 */

	private final Object antialiasing = DEFAULT_ALIASING_MODE;

	private final boolean useGradient = DEFAULT_USE_GRADIENT;

	private final double focusLimit = DEFAULT_LIMIT_FOCUS_RANGE;

	private final double timeLimit = DEFAULT_LIMIT_TIME_RANGE;

	private final boolean drawSpots = DEFAULT_DRAW_SPOTS;

	private final boolean drawLinks = DEFAULT_DRAW_LINKS;

	private final boolean drawSpotEllipse = DEFAULT_DRAW_ELLIPSE;

	private final boolean drawSliceIntersection = DEFAULT_DRAW_SLICE_INTERSECTION;

	private final double nSigmas = 2;

	/**
	 * Return signed distance of p to z=0 plane, truncated at cutoff and scaled
	 * by 1/cutoff. A point on the plane has d=0. A Point that is at cutoff or
	 * farther behind the plane has d=1. A point that is at -cutoff or more in
	 * front of the plane has d=-1.
	 */
	private static double sliceDistance( final double z, final double cutoff )
	{
		if ( z > 0 )
			return Math.min( z, cutoff ) / cutoff;
		else
			return Math.max( z, -cutoff ) / cutoff;
	}

	/**
	 * Return signed distance of timepoint t to t0, truncated at cutoff and
	 * scaled by 1/cutoff. t=t0 has d=0. t<=t0-cutoff has d=-1. t=>t0+cutoff has
	 * d=1.
	 */
	private static double timeDistance( final double t, final double t0, final double cutoff )
	{
		final double d = t - t0;
		if ( d > 0 )
			return Math.min( d, cutoff ) / cutoff;
		else
			return Math.max( d, -cutoff ) / cutoff;
	}

	private static int trunc255( final int i )
	{
		return Math.min( 255, Math.max( 0, i ) );
	}

	private static int truncRGBA( final int r, final int g, final int b, final int a )
	{
		return ARGBType.rgba(
				trunc255( r ),
				trunc255( g ),
				trunc255( b ),
				trunc255( a ) );
	}

	private static int truncRGBA( final double r, final double g, final double b, final double a )
	{
		return truncRGBA(
				( int ) ( 255 * r ),
				( int ) ( 255 * g ),
				( int ) ( 255 * b ),
				( int ) ( 255 * a ) );
	}

	/**
	 * TODO
	 *
	 * @param sd sliceDistande, between -1 and 1. see {@link #sliceDistance(double, double)}.
	 * @param td timeDistande, between -1 and 1. see {@link #timeDistance(double, double)}.
	 * @param sdFade between 0 and 1, from which |sd| value color starts to fade (alpha value decreases).
	 * @param tdFade between 0 and 1, from which |td| value color starts to fade (alpha value decreases).
	 * @param isSelected whether to use selected or un-selected color scheme.
	 * @return vertex/edge color.
	 */
	private static Color getColor( final double sd, final double td, final double sdFade, final double tdFade, final boolean isSelected )
	{
		/*
		 * |sf| = {                  0  for  |sd| <= sdFade,
		 *          linear from 0 to 1  for  |sd| = sdFade to |sd| = 1 }
		 *
		 * sgn(sf) = sgn(sd)
		 */
		final double sf;
		if ( sd > 0 )
		{
			sf = Math.max( 0, ( sd - sdFade ) / ( 1 - sdFade ) );
		}
		else
		{
			sf = -Math.max( 0, ( -sd - sdFade ) / ( 1 - sdFade ) );
		}

		final double tf;
		if ( td > 0 )
		{
			tf = Math.max( 0, ( td - tdFade ) / ( 1 - tdFade ) );
		}
		else
		{
			tf = -Math.max( 0, ( -td - tdFade ) / ( 1 - tdFade ) );
		}

		final double a = -2 * td;
		final double b = 1 + 2 * td;
		final double r = isSelected ? b : a;
		final double g = isSelected ? a : b;
		return new Color( truncRGBA( r, g, 0.1, ( 1 + tf ) * ( 1 - Math.abs( sf ) ) ), true );
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D graphics = ( Graphics2D ) g;
		final BasicStroke defaultVertexStroke = new BasicStroke();
		final BasicStroke highlightedVertexStroke = new BasicStroke( 5 );

		final AffineTransform3D transform = new AffineTransform3D();
		synchronized ( renderTransform )
		{
			transform.set( renderTransform );
		}

		// TODO: fix in BDV. This is stupid, BDV should have addTimepointListener() or something similar.
		final int currentTimepoint = viewer.getState().getCurrentTimepoint();


		// TODO: acquire SpatialIndex.readLock()

		final double globalToViewerScale = Affine3DHelpers.extractScale( transform, 0 );
		final double border = globalToViewerScale * Math.sqrt( graph.getMaxBoundingSphereRadiusSquared( currentTimepoint ) );
		final ConvexPolytope visiblePolytopeViewer = new ConvexPolytope(
				new HyperPlane(  0,  0,  1, -focusLimit ),
				new HyperPlane(  0,  0, -1, -focusLimit ),
				new HyperPlane(  1,  0,  0, -border ),
				new HyperPlane( -1,  0,  0, -width - border ),
				new HyperPlane(  0,  1,  0, -border ),
				new HyperPlane(  0, -1,  0, -height - border ) );
		final ConvexPolytope visiblePolytopeGlobal = ConvexPolytope.transform( visiblePolytopeViewer, transform.inverse() );

		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialiasing );

//		graphics.setStroke( new BasicStroke( 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND ) );
		graphics.setStroke( new BasicStroke() );

		final V target = graph.vertexRef();
		final double[] lPos = new double[ 3 ];
		final double[] gPos = new double[ 3 ];

		final double sliceDistanceFade = 0.2;
		final double timepointDistanceFade = 0.5;

//		/*
//		 * Let z = orthogonal distance to viewer plane in viewer coordinate
//		 * system, and let zg = orthogonal distance to viewer plane in global
//		 * coordinate system. Then zScale * z = zg,
//		 */
//		final double zScale = Math.sqrt(
//				transform.inverse().get( 0, 2 ) * transform.inverse().get( 0, 2 ) +
//				transform.inverse().get( 1, 2 ) * transform.inverse().get( 1, 2 ) +
//				transform.inverse().get( 2, 2 ) * transform.inverse().get( 2, 2 ) );

		final ScreenVertexMath screenVertexMath = new ScreenVertexMath( nSigmas );

		if ( drawLinks )
		{

			graphics.setPaint( getColor( 0, 0, sliceDistanceFade, timepointDistanceFade, false ) );
			for ( int t = Math.max( 0, currentTimepoint - ( int ) timeLimit ); t < currentTimepoint; ++t )
			{
				final SpatialIndex< V > si = graph.getIndex().getSpatialIndex( t );
				final ClipConvexPolytope< V > ccp = si.getClipConvexPolytope();
				ccp.clip( visiblePolytopeGlobal );
				for ( final V vertex : ccp.getInsideValues() )
				{
					vertex.localize( lPos );
					transform.apply( lPos, gPos );
					final int x0 = ( int ) gPos[ 0 ];
					final int y0 = ( int ) gPos[ 1 ];
					final double z0 = gPos[ 2 ];
					for ( final E edge : vertex.outgoingEdges() )
					{
						edge.getTarget( target );
						target.localize( lPos );
						transform.apply( lPos, gPos );
						final int x1 = ( int ) gPos[ 0 ];
						final int y1 = ( int ) gPos[ 1 ];

						final double z1 = gPos[ 2 ];

						final double td0 = timeDistance( t, currentTimepoint, timeLimit );
						final double td1 = timeDistance( t + 1, currentTimepoint, timeLimit );
						final double sd0 = sliceDistance( z0, focusLimit );
						final double sd1 = sliceDistance( z1, focusLimit );

						if ( td0 > -1 )
						{
							if ( ( sd0 > -1 && sd0 < 1 ) || ( sd1 > -1 && sd1 < 1 ) )
							{
								final Color c1 = getColor( sd1, td1, sliceDistanceFade, timepointDistanceFade, edge.isSelected() );
								if ( useGradient )
								{
									final Color c0 = getColor( sd0, td0, sliceDistanceFade, timepointDistanceFade, edge.isSelected() );
									graphics.setPaint( new GradientPaint( x0, y0, c0, x1, y1, c1 ) );
								}
								else
								{
									graphics.setPaint( c1 );
								}
								graphics.drawLine( x0, y0, x1, y1 );
							}
						}
					}
				}
			}
		}

		if ( drawSpots )
		{
			final V highlighted = highlight.getHighlightedVertex( target );

			graphics.setStroke( defaultVertexStroke );
			final int t = currentTimepoint;
			final AffineTransform torig = graphics.getTransform();

			final SpatialIndex< V > si = graph.getIndex().getSpatialIndex( t );
			final ClipConvexPolytope< V > ccp = si.getClipConvexPolytope();
			ccp.clip( visiblePolytopeGlobal );
			for ( final V vertex : ccp.getInsideValues() )
			{
				final boolean isHighlighted = vertex.equals( highlighted );

				screenVertexMath.init( vertex, transform );

				final double x = screenVertexMath.getViewPos()[ 0 ];
				final double y = screenVertexMath.getViewPos()[ 1 ];
				final double z = screenVertexMath.getViewPos()[ 2 ];

				final double sd = sliceDistance( z, focusLimit );
				if ( sd > -1 && sd < 1 )
				{
					if ( drawSpotEllipse )
					{
//						final double rd = zScale * z;
//						if ( rd * rd > vertex.getBoundingSphereRadiusSquared() )
//						{
//							graphics.setColor( getColor( sd, 0, sliceDistanceFade, timepointDistanceFade, vertex.isSelected() ) );
//							graphics.fillOval( ( int ) ( x - 2.5 ), ( int ) ( y - 2.5 ), 5, 5 );
//						}
//						else
						{
							if ( drawSliceIntersection )
							{
								if ( !screenVertexMath.intersectsViewPlane() )
								{
									graphics.setColor( getColor( sd, 0, sliceDistanceFade, timepointDistanceFade, vertex.isSelected() ) );
									graphics.fillOval( ( int ) ( x - 2.5 ), ( int ) ( y - 2.5 ), 5, 5 );
								}
								else
								{
									final double[] tr = screenVertexMath.getIntersectCenter();
									final double theta = screenVertexMath.getIntersectTheta();
									final Ellipse2D ellipse = screenVertexMath.getIntersectEllipse();

									graphics.translate( tr[ 0 ], tr[ 1 ] );
									graphics.rotate( theta );
									graphics.setColor( getColor( 0, 0, sliceDistanceFade, timepointDistanceFade, vertex.isSelected() ) );
									if ( isHighlighted )
										graphics.setStroke( highlightedVertexStroke );
									graphics.draw( ellipse );
									if ( isHighlighted )
										graphics.setStroke( defaultVertexStroke );
									graphics.setTransform( torig );
								}
							}
							else
							{
								final double[] tr = screenVertexMath.getProjectCenter();
								final double theta = screenVertexMath.getProjectTheta();
								final Ellipse2D ellipse = screenVertexMath.getProjectEllipse();

								graphics.translate( tr[ 0 ], tr[ 1 ] );
								graphics.rotate( theta );
								graphics.setColor( getColor( sd, 0, sliceDistanceFade, timepointDistanceFade, vertex.isSelected() ) );
								if ( isHighlighted )
									graphics.setStroke( highlightedVertexStroke );
								graphics.draw( ellipse );
								if ( isHighlighted )
									graphics.setStroke( defaultVertexStroke );
								graphics.setTransform( torig );
							}
						}
					}
					else
					{
						graphics.setColor( getColor( sd, 0, sliceDistanceFade, timepointDistanceFade, vertex.isSelected() ) );
						graphics.fillOval( ( int ) ( x - 2.5 ), ( int ) ( x - 2.5 ), 5, 5 );
					}
				}
			}
		}
		graph.releaseRef( target );
	}
}
