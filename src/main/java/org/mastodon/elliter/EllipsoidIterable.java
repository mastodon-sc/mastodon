package org.mastodon.elliter;

import java.util.Iterator;
import java.util.function.Predicate;

import org.mastodon.mamut.model.Spot;

import bdv.viewer.Source;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.BoundaryType;
import net.imglib2.roi.KnownConstant;
import net.imglib2.roi.Regions;
import net.imglib2.roi.mask.integer.DefaultMask;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.Views;

/**
 * An iterable that iterates over the pixels of spot over a specified
 * {@link Source}. The source transform is taken into account.
 * <p>
 * Call {@link #reset(Spot)} before iterating over a spot. The
 * {@link Localizable} methods return the spot center in pixel coordinates.
 * 
 * @author Tobias Pietzsch
 *
 * @param <T>
 *            the pixel type in the source.
 */
public class EllipsoidIterable< T > implements IterableInterval< T >, Localizable
{
	// bounding box min/max
	private final long[] min = new long[ 3 ];

	private final long[] max = new long[ 3 ];

	// spot covariance in source coordinates
	private final double[][] S = new double[ 3 ][ 3 ];

	// spot precision (= S^-1)
	private final double[][] P = new double[ 3 ][ 3 ];

	// spot position in source coordinates
	private final double[] pos = new double[ 3 ];

	// temporary transformation matrices
	private final double[][] T = new double[ 3 ][ 3 ];

	private final double[][] TS = new double[ 3 ][ 3 ];

	// transform of current source to global coordinates
	private final AffineTransform3D sourceTransform = new AffineTransform3D();

	private final double[] p = new double[ 3 ];

	private final double[] diff = new double[ 3 ];

	private final Source< T > source;

	public EllipsoidIterable( final Source< T > source )
	{
		this.source = source;
	}

	/**
	 * Resets this iterable to that it iterates over the specified spot. The
	 * pixel iterated are taken from the resolution level 0,
	 * 
	 * @param spot
	 *            the spot to iterate.
	 */
	public void reset( final Spot spot )
	{
		reset( spot, 0 );
	}

	/**
	 * Resets this iterable to that it iterates over the specified spot, at the
	 * specified resolution level in the source. Generate an error of the
	 * specified resolution level is not present in the source.
	 * 
	 * @param spot
	 *            the spot to iterate.
	 * @param resolutionLevel
	 *            the resolution level to use in the source.
	 */
	public void reset( final Spot spot, final int resolutionLevel )
	{
		final int t = spot.getTimepoint();
		source.getSourceTransform( t, resolutionLevel, sourceTransform );
		final RandomAccessibleInterval< T > img = source.getSource( t, resolutionLevel );

		// transform spot covariance into source coordinates
		spot.getCovariance( S );
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[ r ][ c ] = sourceTransform.inverse().get( r, c );
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, S );

		// transform spot position into source coordinates
		spot.localize( pos );
		sourceTransform.inverse().apply( pos, pos );

		// get bounding box
		for ( int d = 0; d < 3; ++d )
		{
			final double radius = Math.sqrt( S[ d ][ d ] );
			min[ d ] = Math.max( 0, ( long ) Math.floor( pos[ d ] - radius ) );
			max[ d ] = Math.min( img.max( d ), ( long ) Math.ceil( pos[ d ] + radius ) );
		}

		// if bounding box is empty, we set it to cover pixel at (0,0,0)
		// this will hopefully not cause problems, because it would not overlap
		// with ellipsoid,
		// so the ellipsoidVoxels iterable bould be empty.
		if ( Intervals.isEmpty( this ) )
			for ( int d = 0; d < 3; ++d )
				min[ d ] = max[ d ] = 0;

		// inflate ellipsoid by .5 pixels on either side
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				if ( r == c )
				{
					final double radius = Math.sqrt( S[ r ][ c ] );
					T[ r ][ c ] = ( radius + 0.5 ) / radius;
				}
				else
				{
					T[ r ][ c ] = 0;
				}
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, S );

		// get precision from covariance
		LinAlgHelpers.invertSymmetric3x3( S, P );

		final Predicate< Localizable > contains = l -> {
			l.localize( p );
			LinAlgHelpers.subtract( pos, p, diff );
			LinAlgHelpers.mult( P, diff, p );
			final double d2 = LinAlgHelpers.dot( diff, p );
			return d2 < 1;
		};
		final DefaultMask mask = new DefaultMask( 3, BoundaryType.UNSPECIFIED, contains, KnownConstant.UNKNOWN );
		ellipsoidVoxels = Regions.sampleWithMask( mask, Views.interval( img, this ) );
	}

	private IterableInterval< T > ellipsoidVoxels;

	@Override
	public Cursor< T > cursor()
	{
		return ellipsoidVoxels.cursor();
	}

	@Override
	public Cursor< T > localizingCursor()
	{
		return ellipsoidVoxels.localizingCursor();
	}

	@Override
	public long size()
	{
		return ellipsoidVoxels.size();
	}

	@Override
	public T firstElement()
	{
		return ellipsoidVoxels.firstElement();
	}

	@Override
	public Object iterationOrder()
	{
		return ellipsoidVoxels.iterationOrder();
	}

	@Override
	public Iterator< T > iterator()
	{
		return ellipsoidVoxels.iterator();
	}

	@Override
	public long min( final int d )
	{
		return min[ d ];
	}

	@Override
	public long max( final int d )
	{
		return max[ d ];
	}

	@Override
	public int numDimensions()
	{
		return 3;
	}

	@Override
	public double realMin( final int d )
	{
		return min( d );
	}

	@Override
	public double realMax( final int d )
	{
		return max( d );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return Math.round( pos[ d ] );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return pos[ d ];
	}
}
