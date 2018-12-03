package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import org.mastodon.RefPool;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.update.GraphUpdate;
import org.mastodon.feature.update.GraphUpdate.UpdateLocality;
import org.mastodon.feature.update.GraphUpdateStack;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotGaussFilteredIntensityFeatureComputer.class )
public class SpotGaussFilteredIntensityFeatureComputer implements MamutFeatureComputer, Cancelable
{

	/** Convert from min radius to sigma by dividing radius by: */
	static final double SIGMA_FACTOR = 2.;

	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private GraphUpdateStack< Spot, Link > update;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotGaussFilteredIntensityFeature output;

	private boolean[] processSource;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotGaussFilteredIntensityFeature(
					bdvData.getSources().size(),
					model.getGraph().vertices().getRefPool() );
	}

	@Override
	public void run()
	{
		cancelReason = null;

		// TODO Take into account that some sources might not be computed.
		this.processSource = new boolean[ bdvData.getSources().size() ];
		Arrays.fill( processSource, true );

		// Spots to process, per time-point.
		final IntFunction< Iterable< Spot > > index;
		final GraphUpdate< Spot, Link > changes = update.changesFor( SpotGaussFilteredIntensityFeature.SPEC );

		if (null == changes)
		{
			// Redo all.
			index = ( timepoint ) -> model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			// Clear all.
			for ( final DoublePropertyMap< Spot > map : output.means )
				map.beforeClearPool();
			for ( final DoublePropertyMap< Spot > map : output.stds )
				map.beforeClearPool();
		}
		else
		{
			// Only process modified spots.
			index = new MyIndex( changes, model.getGraph().vertices().getRefPool() );
		}

		// Calculation are made on resolution level 0.
		final int level = 0;
		// Covariance holder.
		final double[][] cov = new double[ 3 ][ 3 ];
		// Affine transform holder.
		final AffineTransform3D transform = new AffineTransform3D();
		// Physical calibration holder.
		final double[] calibration = new double[ 3 ];
		// Half-kernel holder.
		final double[][] kernels = new double[ 3 ][];
		// Half-kernel size holder.
		final int[] halfkernelsizes = new int[ 3 ];
		// Spot center position holder in image coords.
		final double[] pos = new double[ 3 ];
		// Spot center holder in image coords.
		final RealPoint center = RealPoint.wrap( pos );
		// Spot center position holder in integer image coords.
		final long[] p = new long[ 3 ];

		final int numTimepoints = bdvData.getNumTimepoints();
		int nSourcesToCompute = 0;
		for ( final boolean process : processSource )
			if (process)
				nSourcesToCompute++;
		final int todo = numTimepoints * nSourcesToCompute;

		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		final int nSources = sources.size();
		int done = 0;
		MAIN_LOOP: for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			if ( !processSource[ iSource ] )
				continue;

			final Source< ? > source = sources.get( iSource ).getSpimSource();
			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{

				status.notifyProgress( ( double ) done++ / todo );

				source.getSourceTransform( timepoint, level, transform );
				for ( int d = 0; d < calibration.length; d++ )
					calibration[ d ] = Affine3DHelpers.extractScale( transform, d );

				@SuppressWarnings( "unchecked" )
				final RandomAccessibleInterval< RealType< ? > > rai = ( RandomAccessibleInterval< RealType< ? > > ) source.getSource( timepoint, level );
				final RandomAccess< RealType< ? > > ra = rai.randomAccess( rai );

				for ( final Spot spot : index.apply( timepoint ) )
				{
					if ( isCanceled() )
						break MAIN_LOOP;

					// Spot location in pixel units.
					transform.applyInverse( center, spot );
					for ( int d = 0; d < pos.length; d++ )
						p[ d ] = Math.round( pos[ d ] );

					// Compute kernels.
					final double minRadius = minRadius( spot, cov );
					final double sigma = minRadius / SIGMA_FACTOR; // um
					for ( int d = 0; d < 3; d++ )
					{
						final double s = sigma / calibration[ d ];
						halfkernelsizes[ d ] = Math.max( 2, ( int ) ( SIGMA_FACTOR * s + 0.5 ) + 1 );
						kernels[ d ] = halfkernel( s, pos[ d ] - p[ d ], halfkernelsizes[ d ] );
					}

					// Unsubtle loops.
					final long minX = Math.max( rai.min( 0 ), p[ 0 ] - halfkernelsizes[ 0 ] + 1 );
					final long maxX = Math.min( rai.max( 0 ), p[ 0 ] + halfkernelsizes[ 0 ] - 1 );
					final long minY = Math.max( rai.min( 1 ), p[ 1 ] - halfkernelsizes[ 1 ] + 1 );
					final long maxY = Math.min( rai.max( 1 ), p[ 1 ] + halfkernelsizes[ 1 ] - 1 );
					final long minZ = Math.max( rai.min( 2 ), p[ 2 ] - halfkernelsizes[ 2 ] + 1 );
					final long maxZ = Math.min( rai.max( 2 ), p[ 2 ] + halfkernelsizes[ 2 ] - 1 );

					/*
					 * Compute running mean & std.
					 * https://en.wikipedia.org/wiki/
					 * Algorithms_for_calculating_variance#
					 * Weighted_incremental_algorithm
					 */
					double weightedMean = 0.;
					double weightedSum = 0.;
					double S = 0.;

					for ( long z = minZ; z <= maxZ; z++ )
					{
						ra.setPosition( z, 2 );
						final int iz = ( int ) ( z - minZ );
						final double wz = kernels[ 2 ][ iz ];
						for ( long y = minY; y <= maxY; y++ )
						{
							ra.setPosition( y, 1 );
							final int iy = ( int ) ( y - minY );
							final double wy = kernels[ 1 ][ iy ];
							for ( long x = minX; x <= maxX; x++ )
							{
								ra.setPosition( x, 0 );
								final int ix = ( int ) ( x - minX );
								final double wx = kernels[ 0 ][ ix ];
								final double val = ra.get().getRealDouble();
								final double weight = wx * wy * wz;

								weightedSum += weight;
								final double oldWeightedMean = weightedMean;
								weightedMean = oldWeightedMean + ( weight / weightedSum ) * ( val - oldWeightedMean );
								S = S + weight * ( val - oldWeightedMean ) * ( val - weightedMean );
							}
						}
					}

					final double variance = S / weightedSum;
					output.means.get( iSource ).set( spot, weightedMean );
					output.stds.get( iSource ).set( spot, Math.sqrt( variance ) );
				}
			}
		}
	}

	public static final long nSpots( final IntFunction< Iterable< Spot > > index, final int numTimepoints )
	{
		long nSpots = 0l;
		for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
		{
			final Iterable< Spot > iterable = index.apply( timepoint );
			for ( @SuppressWarnings( "unused" ) final Spot spot : iterable )
				nSpots++;
		}
		return nSpots;
	}

	private static final class MyIndex implements IntFunction< Iterable< Spot > >
	{

		private final Map< Integer, Collection< Spot > > index;

		public MyIndex( final GraphUpdate< Spot, Link > update, final RefPool< Spot > pool )
		{
			this.index = new HashMap<>();
			for ( final Spot spot : update.vertices( UpdateLocality.SELF ) )
			{
				final int timepoint = spot.getTimepoint();
				index
						.computeIfAbsent( Integer.valueOf( timepoint ), t -> new RefArrayList<>( pool ) )
						.add( spot );
			}
		}

		@Override
		public Iterable< Spot > apply( final int timepoint )
		{
			final Collection< Spot > collection = index.get( Integer.valueOf( timepoint ) );
			if ( null == collection )
				return Collections.emptyList();
			return collection;
		}
	}

	private static final double[] halfkernel( final double sigma, final double offset, final int size )
	{
		final double two_sq_sigma = 2 * sigma * sigma;
		final double[] kernel = new double[ 2 * size - 1 ];

		for ( int i = 0; i < kernel.length; ++i )
		{
			final double x = i - size + 1 - offset;
			kernel[ i ] = Math.exp( -( x * x ) / two_sq_sigma );
		}

		double sum = 0.;
		for ( int i = 0; i < kernel.length; i++ )
			sum += kernel[ i ];

		for ( int i = 0; i < kernel.length; ++i )
			kernel[ i ] /= sum;

		return kernel;
	}

	private static final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	private static final double minRadius( final Spot spot, final double[][] cov )
	{
		// Best radius is smallest radius of ellipse.
		spot.getCovariance( cov );
		eig.decomposeSymmetric( cov );
		final double[] eigVals = eig.getRealEigenvalues();
		double minEig = Double.POSITIVE_INFINITY;
		for ( int k = 0; k < eigVals.length; k++ )
			minEig = Math.min( minEig, eigVals[ k ] );
		final double radius = Math.sqrt( minEig );
		return radius;
	}

	@Override
	public boolean isCanceled()
	{
		return null != cancelReason;
	}

	@Override
	public void cancel( final String reason )
	{
		cancelReason = reason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}
}
