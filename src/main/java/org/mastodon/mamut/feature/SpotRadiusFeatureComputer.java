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
