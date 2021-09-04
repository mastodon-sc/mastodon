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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.DoubleArray;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.type.numeric.RealType;

@Plugin( type = MamutFeatureComputer.class )
public class SpotIntensityFeatureComputer implements MamutFeatureComputer, Cancelable
{

	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private AtomicBoolean forceComputeAll;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotIntensityFeature output;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			// Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotIntensityFeature.SPEC );
			if ( null != feature )
			{
				output = ( SpotIntensityFeature ) feature;
				return;
			}

			// Create a new one.
			final int nSources = bdvData.getSources().size();
			final List< DoublePropertyMap< Spot > > means = new ArrayList<>( nSources );
			final List< DoublePropertyMap< Spot > > stds = new ArrayList<>( nSources );
			final List< DoublePropertyMap< Spot > > mins = new ArrayList<>( nSources );
			final List< DoublePropertyMap< Spot > > maxs = new ArrayList<>( nSources );
			final List< DoublePropertyMap< Spot > > medians = new ArrayList<>( nSources );
			final List< DoublePropertyMap< Spot > > sums = new ArrayList<>( nSources );
			for ( int i = 0; i < nSources; i++ )
			{
				means.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
				stds.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
				mins.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
				maxs.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
				medians.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
				sums.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
			}
			output = new SpotIntensityFeature( means, stds, mins, maxs, medians, sums );
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
			for ( final DoublePropertyMap< Spot > map : output.means )
				map.beforeClearPool();
			for ( final DoublePropertyMap< Spot > map : output.stds )
				map.beforeClearPool();
			for ( final DoublePropertyMap< Spot > map : output.mins )
				map.beforeClearPool();
			for ( final DoublePropertyMap< Spot > map : output.maxs )
				map.beforeClearPool();
			for ( final DoublePropertyMap< Spot > map : output.sums )
				map.beforeClearPool();
			for ( final DoublePropertyMap< Spot > map : output.medians )
				map.beforeClearPool();
		}

		final int numTimepoints = bdvData.getNumTimepoints();
		final int nSourcesToCompute = bdvData.getSources().size();
		final int todo = numTimepoints * nSourcesToCompute;

		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		final int nSources = sources.size();
		int done = 0;
		MAIN_LOOP: for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			@SuppressWarnings( "unchecked" )
			final Source< RealType< ? > > source = ( Source< RealType< ? > > ) sources.get( iSource ).getSpimSource();
			// Calculation are made on resolution level 0 by default.
			final EllipsoidIterable< RealType< ? > > ellipsoidIter = new EllipsoidIterable<>( source );
			final DoubleArray store = new DoubleArray();

			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{

				status.notifyProgress( ( double ) done++ / todo );

				final SpatialIndex< Spot > toProcess = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
				for ( final Spot spot : toProcess )
				{
					if ( isCanceled() )
						break MAIN_LOOP;

					/*
					 * Skip if we are not force to recompute all and if a value
					 * is already computed.
					 */
					if ( !recomputeAll && output.means.get( iSource ).isSet( spot ) )
						continue;

					ellipsoidIter.reset( spot );
					store.clear();
					ellipsoidIter.forEach( p -> store.addValue( p.getRealDouble() ) );

					final int size = store.size();
					if ( size < 1 )
						continue;

					final double[] array = store.getArray();
					if ( size < 2 )
					{
						output.means.get( iSource ).set( spot, array[ 0 ] );
						output.stds.get( iSource ).set( spot, 0 );
						output.mins.get( iSource ).set( spot, array[ 0 ] );
						output.maxs.get( iSource ).set( spot, array[ 0 ] );
						output.medians.get( iSource ).set( spot, array[ 0 ] );
						output.sums.get( iSource ).set( spot, array[ 0 ] );
						continue;
					}
					
					double sum = 0.;
					for ( int i = 0; i < size; i++ )
						sum += array[ i ];

					final double mean = sum / size;
					double sum2 = 0.;
					for ( int i = 0; i < size; i++ )
					{
						final double dx = array[ i ] - mean;
						sum2 += dx * dx;
					}
					final double variance = sum2 / ( size - 1 );

					Arrays.sort( array, 0, size );
					final double min = array[ 0 ];
					final double max = array[ size - 1 ];
					final double median = array[ size / 2 ];

					output.means.get( iSource ).set( spot, mean );
					output.stds.get( iSource ).set( spot, Math.sqrt( variance ) );
					output.mins.get( iSource ).set( spot, min );
					output.maxs.get( iSource ).set( spot, max );
					output.medians.get( iSource ).set( spot, median );
					output.sums.get( iSource ).set( spot, sum );
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
			for ( @SuppressWarnings( "unused" )
			final Spot spot : iterable )
				nSpots++;
		}
		return nSpots;
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
