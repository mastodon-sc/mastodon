/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.Cursor;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;

@Plugin( type = MamutFeatureComputer.class )
public class SpotCenterIntensityFeatureComputer implements MamutFeatureComputer, Cancelable
{

	/**
	 * Multithreading.
	 */
	private final int numThreads = Runtime.getRuntime().availableProcessors();

	/** Convert from min radius to sigma by dividing radius by: */
	static final double SIGMA_FACTOR = 2.;

	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private AtomicBoolean forceComputeAll;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotCenterIntensityFeature output;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			// Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotCenterIntensityFeature.SPEC );
			if ( null != feature )
			{
				output = ( SpotCenterIntensityFeature ) feature;
				return;
			}

			// Create a new one.
			final int nSources = bdvData.getSources().size();
			final List< DoublePropertyMap< Spot > > maps = new ArrayList<>( nSources );
			for ( int i = 0; i < nSources; i++ )
				maps.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
			output = new SpotCenterIntensityFeature( maps );
		}
	}

	@Override
	public void run()
	{
		cancelReason = null;
		final boolean recomputeAll = forceComputeAll.get();

		if ( recomputeAll )
		{
			// Clear all.
			for ( final DoublePropertyMap< Spot > map : output.maps )
				map.beforeClearPool();
		}

		final int numTimepoints = bdvData.getNumTimepoints();
		final int nSourcesToCompute = bdvData.getSources().size();
		final int todo = numTimepoints * nSourcesToCompute;

		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		int done = 0;
		for ( int iSource = 0; iSource < sources.size(); iSource++ )
		{
			@SuppressWarnings( "unchecked" )
			final Source< RealType< ? > > source = ( Source< RealType< ? > > ) sources.get( iSource ).getSpimSource();
			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{
				status.notifyProgress( ( double ) done++ / todo );
				if ( isCanceled() )
					break;

				final SpatialIndex< Spot > toProcess = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
				final List< Callable< Void > > tasks = Collections.nCopies( numThreads, new SpotIntensityComputer(
						source,
						toProcess.iterator(),
						getCalibration( source, timepoint ),
						output.maps.get( iSource ),
						recomputeAll ) );
				try
				{
					final ExecutorService executor = Executors.newFixedThreadPool( numThreads );
					final List< Future< Void > > futures = executor.invokeAll( tasks );
					for ( final Future< Void > future : futures )
						future.get();
				}
				catch ( final InterruptedException e )
				{
					e.printStackTrace();
				}
				catch ( final ExecutionException e )
				{
					e.printStackTrace();
				}
			}
		}
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

	private final class SpotIntensityComputer implements Callable< Void >
	{

		private final Source< RealType< ? > > source;

		private final Iterator< Spot > iterator;

		private final double[] calibration;

		private final DoublePropertyMap< Spot > map;

		private final boolean recomputeAll;

		public SpotIntensityComputer(
				final Source< RealType< ? > > source,
				final Iterator< Spot > iterator,
				final double[] calibration,
				final DoublePropertyMap< Spot > map,
				final boolean recomputeAll )
		{
			this.source = source;
			this.iterator = iterator;
			this.calibration = calibration;
			this.map = map;
			this.recomputeAll = recomputeAll;
		}

		@Override
		public Void call() throws Exception
		{
			// Covariance holder.
			final double[][] cov = new double[ 3 ][ 3 ];
			final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );
			final Spot spot = model.getGraph().vertices().createRef();
			final EllipsoidIterable< RealType< ? > > iterable = new EllipsoidIterable<>( source );
			while ( true )
			{
				if ( isCanceled() )
					break;

				synchronized ( iterator )
				{
					if ( !iterator.hasNext() )
						break;
					spot.refTo( iterator.next() );
				}

				/*
				 * We can skip computing for this spot if it is computed already
				 * AND if we are not forced to recompute all.
				 */
				if ( !recomputeAll && map.isSet( spot ) )
					continue;

				// Compute kernels.
				final double minRadius = minRadius( spot, cov, eig );
				final double sigma = minRadius / SIGMA_FACTOR; // um

				// Gaussian normalization.
				final double A = 1 / ( sigma * sigma * sigma * Math.pow( 2. * Math.PI, 3. / 2. ) );

				iterable.reset( spot );
				final Cursor< RealType< ? > > cursor = iterable.localizingCursor();
				double weightedMean = 0.;
				double weightedSum = 0.;
				int npixels = 0;
				while ( cursor.hasNext() )
				{
					cursor.fwd();
					npixels++;
					final double val = cursor.get().getRealDouble();
					double dr2 = 0.;
					for ( int d = 0; d < 3; d++ )
					{ // nDims is hardcoded to 3.
						final double dx = ( cursor.getLongPosition( d ) - iterable.getDoublePosition( d ) ) * calibration[ d ];
						dr2 += dx * dx;
					}
					final double weight = A * Math.exp( -dr2 / ( 2 * sigma * sigma ) );
					weightedSum += weight;
					final double oldWeightedMean = weightedMean;
					weightedMean = oldWeightedMean + ( weight / weightedSum ) * ( val - oldWeightedMean );
				}

				synchronized ( map )
				{
					if ( npixels > 0 )
						map.set( spot, weightedMean );
					else
						map.remove( spot );
				}
			}
			return null;
		}
	}

	private static final double[] getCalibration( final Source< ? > source, final int timepoint )
	{
		// Calculation are made on resolution level 0.
		final int level = 0;
		// Affine transform holder.
		final AffineTransform3D transform = new AffineTransform3D();
		// Physical calibration holder.
		final double[] calibration = new double[ 3 ];
		source.getSourceTransform( timepoint, level, transform );
		for ( int d = 0; d < calibration.length; d++ )
			calibration[ d ] = Affine3DHelpers.extractScale( transform, d );
		return calibration;
	}

	private static final double minRadius( final Spot spot, final double[][] cov, final JamaEigenvalueDecomposition eig )
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
}
