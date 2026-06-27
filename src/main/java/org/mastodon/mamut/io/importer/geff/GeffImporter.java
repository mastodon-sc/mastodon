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
package org.mastodon.mamut.io.importer.geff;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.geff.GeffEdge;
import org.mastodon.geff.GeffMetadata;
import org.mastodon.geff.GeffNode;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.ModelImporter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

public class GeffImporter extends ModelImporter
{

	/**
	 * Imports the Geff Zarr dataset at {@code zarrPath} into the model of
	 * {@code projectModel}.
	 */
	public static void importGeff( final String zarrPath, final ProjectModel projectModel ) throws IOException
	{
		importGeff( zarrPath, projectModel.getModel() );
	}

	/**
	 * Imports the Geff Zarr dataset at {@code zarrPath} into {@code model}.
	 */
	public static void importGeff( final String zarrPath, final Model model ) throws IOException
	{
		new GeffImporter( model ).read( zarrPath );
	}

	private final Model model;

	private GeffImporter( final Model model )
	{
		super( model );
		this.model = model;
	}

	private void read( final String zarrPath ) throws IOException
	{
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();

		final Spot vRef1 = graph.vertexRef();
		final Spot vRef2 = graph.vertexRef();
		final Link eRef = graph.edgeRef();

		startImport();
		try
		{
			final GeffMetadata metadata = GeffMetadata.readFromZarr( zarrPath );
			final List< GeffNode > nodes = GeffNode.readFromZarr( zarrPath, metadata );
			final List< GeffEdge > edges = GeffEdge.readFromZarr( zarrPath, metadata.getGeffVersion() );

			// Missing axes generate NaNs -> replace with 0
			for ( final GeffNode node : nodes )
			{
				node.setX( Double.isNaN( node.getX() ) ? 0 : node.getX() );
				node.setY( Double.isNaN( node.getY() ) ? 0 : node.getY() );
				node.setZ( Double.isNaN( node.getZ() ) ? 0 : node.getZ() );
			}

			// Feature storage
			final GeffImportedSpotFeatures spotFeatures = new GeffImportedSpotFeatures();
			final GeffImportedLinkFeatures linkFeatures = new GeffImportedLinkFeatures();
			final String noUnits = Dimension.NONE.getUnits( model.getSpaceUnits(), model.getTimeUnits() );
			final IntPropertyMap< Spot > segmentIdMap = new IntPropertyMap<>( graph.vertices(), Integer.MIN_VALUE );
			final DoublePropertyMap< Link > scoreMap = new DoublePropertyMap<>( graph.edges(), Double.NaN );
			final DoublePropertyMap< Link > distanceMap = new DoublePropertyMap<>( graph.edges(), Double.NaN );

			// Import nodes → Spots
			final Map< Integer, Spot > spotMap = new HashMap<>( nodes.size() );
			final double[] pos = new double[ 3 ];
			for ( final GeffNode node : nodes )
			{
				pos[ 0 ] = node.getX();
				pos[ 1 ] = node.getY();
				pos[ 2 ] = node.getZ();

				final Spot spot;
				final double[] cov3d = node.getCovariance3d();
				if ( cov3d != null )
				{
					final double[][] cov = flatToMatrix3x3( cov3d );
					spot = graph.addVertex( vRef1 ).init( node.getT(), pos, cov );
				}
				else if ( node.getRadius() > 0 )
				{
					spot = graph.addVertex( vRef1 ).init( node.getT(), pos, node.getRadius() );
				}
				else
				{
					spot = graph.addVertex( vRef1 ).init( node.getT(), pos, GeffNode.DEFAULT_RADIUS );
				}

				// Store segmentId as feature if set
				if ( node.getSegmentId() != 0 )
					segmentIdMap.set( spot, node.getSegmentId() );

				// Keep a copy of the Spot reference for link creation
				final Spot copy = graph.vertexRef();
				copy.refTo( spot );
				spotMap.put( node.getId(), copy );
			}

			// Import edges → Links
			for ( final GeffEdge edge : edges )
			{
				final Spot source = spotMap.get( edge.getSourceNodeId() );
				final Spot target = spotMap.get( edge.getTargetNodeId() );
				if ( source == null || target == null )
					continue;

				final Link link = graph.addEdge( source, target, eRef ).init();

				if ( edge.getScore() != GeffEdge.DEFAULT_SCORE )
					scoreMap.set( link, edge.getScore() );
				if ( edge.getDistance() != GeffEdge.DEFAULT_DISTANCE )
					distanceMap.set( link, edge.getDistance() );
			}

			// Release copied vertex refs
			for ( final Spot s : spotMap.values() )
				graph.releaseRef( s );

			// Register features
			spotFeatures.store( "Segment ID", Dimension.NONE, noUnits, segmentIdMap );
			linkFeatures.store( "Score", Dimension.NONE, noUnits, scoreMap );
			linkFeatures.store( "Distance", Dimension.NONE, noUnits, distanceMap );

			featureModel.pauseListeners();
			featureModel.declareFeature( spotFeatures );
			featureModel.declareFeature( linkFeatures );
		}
		finally
		{
			graph.releaseRef( vRef1 );
			graph.releaseRef( vRef2 );
			graph.releaseRef( eRef );
			model.getFeatureModel().resumeListeners();
			finishImport();
		}
	}

	/**
	 * Converts a flat 6-element upper-triangular covariance vector
	 * {@code [c0,c1,c2,c3,c4,c5]} (row-major) to a symmetric 3×3 matrix:
	 * 
	 * <pre>
	 * [[c0, c1, c2],
	 *  [c1, c3, c4],
	 *  [c2, c4, c5]]
	 * </pre>
	 */
	static double[][] flatToMatrix3x3( final double[] c )
	{
		return new double[][] {
				{ c[ 0 ], c[ 1 ], c[ 2 ] },
				{ c[ 1 ], c[ 3 ], c[ 4 ] },
				{ c[ 2 ], c[ 4 ], c[ 5 ] }
		};
	}
}
