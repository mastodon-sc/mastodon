package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import org.mastodon.RefPool;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureComputationSettings;
import org.mastodon.feature.FeatureComputationSettings.SourceSelection;
import org.mastodon.feature.update.Update;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
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
	private SpotUpdateStack update;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter
	private FeatureComputationSettings featureComputationSettings;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotGaussFilteredIntensityFeature output;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			//  Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotGaussFilteredIntensityFeature.SPEC );
			if (null != feature )
			{
				output = ( SpotGaussFilteredIntensityFeature ) feature;
				return;
			}

			// Create a new one.
			final int nSources = bdvData.getSources().size();
			final List< DoublePropertyMap< Spot > > means = new ArrayList<>(nSources);
			final List< DoublePropertyMap< Spot > > stds = new ArrayList<>(nSources);
			for ( int i = 0; i < nSources; i++ )
			{
				means.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
				stds.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
			}
			output = new SpotGaussFilteredIntensityFeature( means, stds );
		}
	}

	@Override
	public void run()
	{
		cancelReason = null;


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
		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		final int nSources = sources.size();

		// Spots to process, per time-point.
		final IndexToCompute index;
		final Update< Spot > changes = update.changesFor( SpotGaussFilteredIntensityFeature.SPEC );
		if ( null == changes )
		{
			// Redo all.
			index = new AllIndex( model );
			// Clear all.
			for ( final DoublePropertyMap< Spot > map : output.means )
				map.beforeClearPool();
			for ( final DoublePropertyMap< Spot > map : output.stds )
				map.beforeClearPool();
		}
		else
		{
			/*
			 * Determine what sources are empty. For these sources, we cannot
			 * use the update mechanism.
			 */
			final boolean[] emptySources = new boolean[ nSources ];
			for ( int iSource = 0; iSource < emptySources.length; iSource++ )
				emptySources[ iSource ] = ( output.means.get( iSource ).size() == 0 );

			// Only process modified spots.
			index = new UpdateIndex( model, emptySources, changes, model.getGraph().vertices().getRefPool() );
		}

		// Determine how many sources we will have to compute.
		final SourceSelection sourceSelection = featureComputationSettings.getSourceSelection( output.getSpec() );
		int nSourcesToCompute = 0;
		for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			if ( sourceSelection.isSourceSelected( iSource ) )
				nSourcesToCompute++;
		}
		final int todo = numTimepoints * nSourcesToCompute;

		int done = 0;
		MAIN_LOOP: for ( int iSource = 0; iSource < nSources; iSource++ )
		{

			if ( !sourceSelection.isSourceSelected( iSource ) )
			{
				/*
				 * We are told not to compute mean intensity on this source. So
				 * we don't, and clear any prior value that might exist.
				 */
				output.means.get( iSource ).beforeClearPool();
				output.stds.get( iSource ).beforeClearPool();
				continue;
			}

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

				for ( final Spot spot : index.getSpotsToCompute( timepoint, iSource ) )
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


	private static interface IndexToCompute
	{

		public Iterable< Spot > getSpotsToCompute(int timepoint, int iSource);

	}

	private static class AllIndex implements IndexToCompute
	{
		private final Model model;

		public AllIndex( final Model model )
		{
			this.model = model;
		}

		@Override
		public Iterable< Spot > getSpotsToCompute( final int timepoint, final int iSource )
		{
			return model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
		}
	}

	private static final class UpdateIndex extends AllIndex
	{

		private final Map< Integer, Collection< Spot > > index;

		private final boolean[] emptySources;

		public UpdateIndex( final Model model, final boolean[] emptySources, final Update< Spot > update, final RefPool< Spot > pool )
		{
			super( model );
			this.emptySources = emptySources;
			this.index = new HashMap<>();
			for ( final Spot spot : update.get() )
			{
				final int timepoint = spot.getTimepoint();
				index
						.computeIfAbsent( Integer.valueOf( timepoint ), t -> new RefArrayList<>( pool ) )
						.add( spot );
			}
		}

		@Override
		public Iterable< Spot > getSpotsToCompute( final int timepoint, final int iSource )
		{
			if ( emptySources[ iSource ] )
			{
				/*
				 * The feature values for this source is completely empty. It
				 * was not computed before, and therefore we cannot just compute
				 * an update. We need to recompute all.
				 */
				return super.getSpotsToCompute( timepoint, iSource );
			}
			else
			{
				/*
				 * This source was part of a computation before. We can just
				 * update values for this source.
				 */
				final Collection< Spot > collection = index.get( Integer.valueOf( timepoint ) );
				if ( null == collection )
					return Collections.emptyList();
				return collection;
			}
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
