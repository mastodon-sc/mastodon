package org.mastodon.mamut.model;

import org.mastodon.properties.PropertyChangeListener;

/**
 * Example that demonstrates how to listen to spot property changes.
 */
public class ModelPropertyListenerExample
{

	public static void main( final String[] args )
	{
		final Model model = new Model();
		final ModelGraph graph = model.getGraph();

		final double radius = 3.;
		final Spot spot = graph.addVertex().init( 0, new double[] { 0., 0., 0. }, radius );

		final PropertyChangeListener< Spot > listener = s -> System.out.println( "The covariance of " + s + " changed!" );
		graph.getVertexPool().covariance.propertyChangeListeners().add( listener );

		final double[][] cov = new double[ 3 ][ 3 ];
		final double newRadius = 10. * 10.;
		covarianceFromRadiusSquared( newRadius, cov );
		spot.setCovariance( cov );
	}

	private static void covarianceFromRadiusSquared( final double rsqu, final double[][] cov )
	{
		for ( int row = 0; row < 3; ++row )
			for ( int col = 0; col < 3; ++col )
				cov[ row ][ col ] = ( row == col ) ? rsqu : 0;
	}
}
