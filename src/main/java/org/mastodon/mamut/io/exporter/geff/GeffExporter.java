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
package org.mastodon.mamut.io.exporter.geff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import org.mastodon.geff.GeffAxis;
import org.mastodon.geff.GeffEdge;
import org.mastodon.geff.GeffMetadata;
import org.mastodon.geff.GeffNode;
import org.mastodon.geff.GeffUtils;

public class GeffExporter
{

	/**
	 * Exports the model of {@code projectModel} to a Geff Zarr directory at
	 * {@code zarrPath}.
	 */
	public static void exportGeff( final ProjectModel projectModel, final String zarrPath ) throws IOException
	{
		exportGeff( projectModel.getModel(), zarrPath );
	}

	/**
	 * Exports {@code model} to a Geff Zarr directory at {@code zarrPath}.
	 */
	public static void exportGeff( final Model model, final String zarrPath ) throws IOException
	{
		final ModelGraph graph = model.getGraph();

		// Build GeffNodes from Spots
		final List< GeffNode > nodes = new ArrayList<>();
		final Map< Spot, Integer > spotToGeffId = new HashMap<>();
		int geffId = 0;
		final double[][] cov = new double[ 3 ][ 3 ];
		final Spot vRef = graph.vertexRef();
		try
		{
			for ( final Spot spot : graph.vertices() )
			{
				spot.getCovariance( cov );
				final double radius = Math.sqrt( spot.getBoundingSphereRadiusSquared() );
				final GeffNode node = new GeffNode.Builder()
						.id( geffId )
						.timepoint( spot.getTimepoint() )
						.x( spot.getDoublePosition( 0 ) )
						.y( spot.getDoublePosition( 1 ) )
						.z( spot.getDoublePosition( 2 ) )
						.radius( radius )
						.covariance3d( matrixToFlat3x3( cov ) )
						.build();
				nodes.add( node );
				// Use a copy of the spot reference as the map key
				final Spot copy = graph.vertexRef();
				copy.refTo( spot );
				spotToGeffId.put( copy, geffId++ );
			}

			// Build GeffEdges from Links
			final List< GeffEdge > edges = new ArrayList<>();
			int edgeId = 0;
			for ( final Link link : graph.edges() )
			{
				final Integer srcId = spotToGeffId.get( link.getSource( vRef ) );
				final Integer tgtId = spotToGeffId.get( link.getTarget( vRef ) );
				if ( srcId == null || tgtId == null )
					continue;

				final GeffEdge edge = new GeffEdge.Builder()
						.setId( edgeId++ )
						.setSourceNodeId( srcId )
						.setTargetNodeId( tgtId )
						.build();
				edges.add( edge );
			}

			// Build metadata with axes derived from model units
			final List< GeffAxis > axes = buildAxes( model.getTimeUnits(), model.getSpaceUnits() );
			final GeffMetadata metadata = new GeffMetadata( "1.0.0", true, axes );

			GeffNode.writeToZarr( nodes, zarrPath, metadata );
			GeffEdge.writeToZarr( edges, zarrPath, GeffUtils.DEFAULT_CHUNK_SIZE, metadata );
			GeffMetadata.writeToZarr( metadata, zarrPath );
		}
		finally
		{
			// Release all copied spot refs used as map keys
			for ( final Spot s : spotToGeffId.keySet() )
				graph.releaseRef( s );
			graph.releaseRef( vRef );
		}
	}

	private static List< GeffAxis > buildAxes( final String timeUnit, final String spaceUnit )
	{
		return Arrays.asList(
				GeffAxis.createTimeAxis( GeffAxis.NAME_TIME, timeUnit, null, null ),
				GeffAxis.createSpaceAxis( GeffAxis.NAME_SPACE_X, spaceUnit, null, null ),
				GeffAxis.createSpaceAxis( GeffAxis.NAME_SPACE_Y, spaceUnit, null, null ),
				GeffAxis.createSpaceAxis( GeffAxis.NAME_SPACE_Z, spaceUnit, null, null ) );
	}

	/**
	 * Flattens a symmetric 3×3 covariance matrix to the upper-triangular
	 * 6-element vector {@code [m00, m01, m02, m11, m12, m22]} used by Geff.
	 */
	static double[] matrixToFlat3x3( final double[][] m )
	{
		return new double[] { m[ 0 ][ 0 ], m[ 0 ][ 1 ], m[ 0 ][ 2 ], m[ 1 ][ 1 ], m[ 1 ][ 2 ], m[ 2 ][ 2 ] };
	}
}
