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

import java.util.concurrent.atomic.AtomicBoolean;

import org.mastodon.feature.Dimension;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotRadiusFeatureComputer.class )
public class SpotRadiusFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private Model model;

	@Parameter
	private AtomicBoolean forceComputeAll;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotRadiusFeature output;

	@Override
	public void run()
	{
		final boolean recomputeAll = forceComputeAll.get();

		if ( recomputeAll )
			output.map.beforeClearPool();

		final double[][] cov = new double[3][3];
		final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

		for ( final Spot spot : model.getGraph().vertices() )
		{

			/*
			 * Skip if we are not force to recompute all and if a value
			 * is already computed.
			 */
			if ( !recomputeAll && output.map.isSet( spot ) )
				continue;

			spot.getCovariance( cov );
			eig.decomposeSymmetric( cov );
			final double[] eigVals = eig.getRealEigenvalues();
			double volume = 4. / 3. * Math.PI;
			for ( int k = 0; k < eigVals.length; k++ )
			{
				final double semiAxis = Math.sqrt( eigVals[ k ] );
				volume *= semiAxis;
			}
			final double radius = Math.pow( volume * 3. / 4. / Math.PI,  1. / 3.  );
			output.map.set( spot, radius );
		}
	}

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotRadiusFeature(
					new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ),
					Dimension.LENGTH.getUnits( model.getSpaceUnits(), model.getTimeUnits() ) );
	}
}
