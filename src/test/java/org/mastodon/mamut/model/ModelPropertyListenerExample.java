/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
