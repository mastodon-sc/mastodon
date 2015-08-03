package net.trackmate.model.tgmm.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.bdv.wrapper.OverlayEdgeWrapper;
import net.trackmate.bdv.wrapper.OverlayGraphWrapper;
import net.trackmate.bdv.wrapper.OverlayVertexWrapper;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.model.Link;
import net.trackmate.model.tgmm.SpotCovariance;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import bdv.viewer.ViewerPanel;

public class TracksOverlaySpotCovariance implements OverlayRenderer, TransformListener< AffineTransform3D >
{
	private final AffineTransform3D transform;

	private final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance >> model;

	private final ViewerPanel viewer;

	public TracksOverlaySpotCovariance( final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance >> overlayGraph, final ViewerPanel viewer, final int tpSize )
	{
		this.model = overlayGraph;
		this.viewer = viewer;
		this.transform = new AffineTransform3D();
	}

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

	private static Color getColor( final double sd, final double td, final double sdFade, final double tdFade, final boolean isSelected )
	{
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

		final int currentTimepoint = viewer.getState().getCurrentTimepoint();

//		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );

//		graphics.setStroke( new BasicStroke( 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND ) );
		graphics.setStroke( new BasicStroke() );

		final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> target = model.vertexRef();
		final double[] lPos = new double[ 3 ];
		final double[] gPos = new double[ 3 ];

		final double sliceDistanceCutoff = 100;
		final double sliceDistanceFade = 0.2;

		final double timepointDistanceCutoff = 20;
		final double timepointDistanceFade = 0.5;

		final double nSigmas = 2;
		final boolean drawEllipsoidProjection = true;

		for ( int timepointId = 0; timepointId < currentTimepoint; ++timepointId )
		{
			final RefSet< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> > spots = model.getSpots( timepointId );
			final int t = timepointId;
			for ( final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> spot : spots )
			{
				spot.get().localize( lPos );
				transform.apply( lPos, gPos );
				final int x0 = ( int ) gPos[ 0 ];
				final int y0 = ( int ) gPos[ 1 ];
				final double z0 = gPos[ 2 ];
				for ( final OverlayEdgeWrapper< SpotCovariance, Link< SpotCovariance >> edge : spot.outgoingEdges() )
				{
					edge.getTarget( target );
					target.get().localize( lPos );
					transform.apply( lPos, gPos );
					final int x1 = ( int ) gPos[ 0 ];
					final int y1 = ( int ) gPos[ 1 ];

					final double z1 = gPos[ 2 ];


					final double td0 = timeDistance( t, currentTimepoint, timepointDistanceCutoff );
					final double td1 = timeDistance( t+1, currentTimepoint, timepointDistanceCutoff );
					final double sd0 = sliceDistance( z0, sliceDistanceCutoff );
					final double sd1 = sliceDistance( z1, sliceDistanceCutoff );

					if ( td0 > -1 )
					{
						if ( ( sd0 > -1 && sd0 < 1 ) || ( sd1 > -1 && sd1 < 1 ) )
						{
							final Color c0 = getColor( sd0, td0, sliceDistanceFade, timepointDistanceFade, false );
							final Color c1 = getColor( sd1, td1, sliceDistanceFade, timepointDistanceFade, false );
//							graphics.setPaint( new GradientPaint( x0, y0, c0, x1, y1, c1 ) );
							graphics.drawLine( x0, y0, x1, y1 );
						}
					}
				}
			}
		}

		graphics.setStroke( new BasicStroke() );
//		graphics.setStroke( new BasicStroke( 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND ) );

		final int t = currentTimepoint;
		final RefSet< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> > spots = model.getSpots( t );
		final AffineTransform torig = graphics.getTransform();
		for ( final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> spot : spots )
		{
			spot.get().localize( lPos );
			transform.apply( lPos, gPos );

			final double z = gPos[ 2 ];
			final double sd = sliceDistance( z, sliceDistanceCutoff );
			if ( sd > -1 && sd < 1 )
			{
				final double[][] S = new double[ 3 ][ 3 ];
				spot.get().getCovariance( S );

				final double[][] T = new double[ 3 ][ 3 ];
				for ( int r = 0; r < 3; ++r )
					for ( int c = 0; c < 3; ++c )
						T[ r ][ c ] = transform.get( r, c );

				final double[][] TS = new double[ 3 ][ 3 ];
				LinAlgHelpers.mult( T, S, TS );
				LinAlgHelpers.multABT( TS, T, S );
				// We need make S exactly symmetric or jama eigendecomposition
				// will not return orthogonal V.
				S[ 0 ][ 1 ] = S[ 1 ][ 0 ];
				S[ 0 ][ 2 ] = S[ 2 ][ 0 ];
				S[ 1 ][ 2 ] = S[ 2 ][ 1 ];
				// now S is spot covariance transformed into view coordinates.

				if ( drawEllipsoidProjection )
				{
					final double[][] S2 = new double[ 2 ][ 2 ];
					for ( int r = 0; r < 2; ++r )
						for ( int c = 0; c < 2; ++c )
							S2[ r ][ c ] = S[ r ][ c ];
					final EigenvalueDecomposition eig2 = new Matrix( S2 ).eig();
					final double[] eigVals2 = eig2.getRealEigenvalues();
					final double w = nSigmas * Math.sqrt( eigVals2[ 0 ] );
					final double h = nSigmas * Math.sqrt( eigVals2[ 1 ] );
					final Matrix V2 = eig2.getV();
					final double c = V2.getArray()[ 0 ][ 0 ];
					final double s = V2.getArray()[ 1 ][ 0 ];
					final double theta = Math.atan2( s, c );
					graphics.translate( gPos[ 0 ], gPos[ 1 ] );
					graphics.rotate( theta );
					graphics.setColor( getColor( sd, 0, sliceDistanceFade, timepointDistanceFade, spot.isSelected() ) );
					graphics.draw( new Ellipse2D.Double( -w, -h, 2 * w, 2 * h ) );
					graphics.setTransform( torig );
				}
			}
		}
		model.releaseRef( target );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	@Override
	public void transformChanged( final AffineTransform3D t )
	{
		transform.set( t );
	}
}
