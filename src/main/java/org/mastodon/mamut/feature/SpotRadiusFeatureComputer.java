package org.mastodon.mamut.feature;

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotRadiusFeatureComputer.class )
public class SpotRadiusFeatureComputer implements MamutFeatureComputer
{

	private static final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotRadiusFeature output;

	@Override
	public void run()
	{
		// Covariance holder.
		final double[][] cov = new double[ 3 ][ 3 ];

		output.map.beforeClearPool();
		for ( final Spot spot : model.getGraph().vertices() )
		{
			// Best radius is smallest radius of ellipse.
			spot.getCovariance( cov );
			eig.decomposeSymmetric( cov );
			// Eigenvalues are radius squared.
			final double[] eigVals = eig.getRealEigenvalues();
			// Geometric mean.
			final double radius = Math.pow( eigVals[0] * eigVals[1] * eigVals[2], 1. / 6. );
			output.map.set( spot, radius );
		}
	}

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotRadiusFeature(
					new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ),
					model.getSpaceUnits() );
	}
}
