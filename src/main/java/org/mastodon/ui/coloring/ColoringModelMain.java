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
package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;

/**
 * ColoringModel knows which coloring scheme is currently active. Possible
 * options are: none, by a tag set, by a feature.
 * <p>
 * This particular implementation also offers coloring of vertices and edges
 * based on the features defined for the branch graph it is associated with. The
 * branch graph instance needs to be specified.
 * <p>
 * Notifies listeners when coloring is changed.
 * <p>
 * Listens for disappearing tag sets or features.
 *
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 */
public class ColoringModelMain<
	V extends Vertex<E>,
	E extends Edge<V>,
	BV extends Vertex<BE>,
	BE extends Edge< BV > >
		extends ColoringModel
		implements TagSetModel.TagSetModelListener, FeatureColorModeManager.FeatureColorModesListener
{

	private final BranchGraph< BV, BE, V, E > branchGraph;

	public ColoringModelMain(
			final TagSetModel< ?, ? > tagSetModel,
			final FeatureColorModeManager featureColorModeManager,
			final FeatureModel featureModel,
			final BranchGraph< BV, BE, V, E > branchGraph )
	{
		super( tagSetModel, featureColorModeManager, featureModel );
		this.branchGraph = branchGraph;
	}

	@Override
	public boolean isValid( final FeatureColorMode mode )
	{
		if ( mode.getVertexColorMode() != FeatureColorMode.VertexColorMode.NONE
				&& null == projections.getFeatureProjection( mode.getVertexFeatureProjection() ) )
			return false;

		if ( mode.getEdgeColorMode() != FeatureColorMode.EdgeColorMode.NONE
				&& null == projections.getFeatureProjection( mode.getEdgeFeatureProjection() ) )
			return false;

		return true;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public GraphColorGenerator< V, E > getFeatureGraphColorGenerator()
	{
		final FeatureColorMode fcm = featureColorMode;
		if ( fcm == null )
			return new DefaultGraphColorGenerator<>();

		// Vertex.
		final ColorGenerator< V > vertexColorGenerator;
		final FeatureProjection< ? > vertexProjection = projections.getFeatureProjection( fcm.getVertexFeatureProjection() );
		if ( null == vertexProjection )
			vertexColorGenerator = new DefaultColorGenerator<>();
		else
		{
			final String vertexColorMap = fcm.getVertexColorMap();
			final double vertexRangeMin = fcm.getVertexRangeMin();
			final double vertexRangeMax = fcm.getVertexRangeMax();
			switch ( fcm.getVertexColorMode() )
			{
			case INCOMING_EDGE:
				vertexColorGenerator = new FeatureColorGeneratorIncomingEdge<>(
						( FeatureProjection< E > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case OUTGOING_EDGE:
				vertexColorGenerator = new FeatureColorGeneratorOutgoingEdge<>(
						( FeatureProjection< E > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case VERTEX:
				vertexColorGenerator = new FeatureColorGenerator<>(
						( FeatureProjection< V > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case NONE:
				vertexColorGenerator = new DefaultColorGenerator<>();
				break;
			case BRANCH_VERTEX_UP:
				vertexColorGenerator = new BranchUpFeatureColorGenerator<>(
						( FeatureProjection< BV > ) vertexProjection,
						branchGraph,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case BRANCH_VERTEX_DOWN:
				vertexColorGenerator = new BranchDownFeatureColorGenerator<>(
						( FeatureProjection< BV > ) vertexProjection,
						branchGraph,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case INCOMING_BRANCH_EDGE:
				vertexColorGenerator = new BranchFeatureColorGeneratorIncomingEdge<>(
						( FeatureProjection< BE > ) vertexProjection,
						branchGraph,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case OUTGOING_BRANCH_EDGE:
				vertexColorGenerator = new BranchFeatureColorGeneratorIncomingEdge<>(
						( FeatureProjection< BE > ) vertexProjection,
						branchGraph,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			default:
				throw new IllegalArgumentException( "Unknown vertex color mode: " + fcm.getVertexColorMode() );
			}
		}

		// Edge.
		final EdgeColorGenerator< V, E > edgeColorGenerator;
		final FeatureProjection< ? > edgeProjection = projections.getFeatureProjection( fcm.getEdgeFeatureProjection() );
		if ( null == edgeProjection )
			edgeColorGenerator = new DefaultEdgeColorGenerator<>();
		else
		{
			final String edgeColorMap = fcm.getEdgeColorMap();
			final double edgeRangeMin = fcm.getEdgeRangeMin();
			final double edgeRangeMax = fcm.getEdgeRangeMax();
			switch ( fcm.getEdgeColorMode() )
			{
			case SOURCE_VERTEX:
				edgeColorGenerator = new FeatureColorGeneratorSourceVertex<>(
						( FeatureProjection< V > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case TARGET_VERTEX:
				edgeColorGenerator = new FeatureColorGeneratorTargetVertex<>(
						( FeatureProjection< V > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case EDGE:
				edgeColorGenerator = new FeatureEdgeColorGenerator<>(
						( FeatureProjection< E > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case NONE:
				edgeColorGenerator = new DefaultEdgeColorGenerator<>();
				break;
			case SOURCE_BRANCH_VERTEX_UP:
				edgeColorGenerator = new BranchUpFeatureColorGeneratorSourceVertex<>(
						( FeatureProjection< BV > ) edgeProjection,
						branchGraph,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin,
						edgeRangeMax );
				break;
			case TARGET_BRANCH_VERTEX_UP:
				edgeColorGenerator = new BranchUpFeatureColorGeneratorTargetVertex<>(
						( FeatureProjection< BV > ) edgeProjection,
						branchGraph,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin,
						edgeRangeMax );
				break;
			case SOURCE_BRANCH_VERTEX_DOWN:
				edgeColorGenerator = new BranchDownFeatureColorGeneratorSourceVertex<>(
						( FeatureProjection< BV > ) edgeProjection,
						branchGraph,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin,
						edgeRangeMax );
				break;
			case TARGET_BRANCH_VERTEX_DOWN:
				edgeColorGenerator = new BranchDownFeatureColorGeneratorTargetVertex<>(
						( FeatureProjection< BV > ) edgeProjection,
						branchGraph,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin,
						edgeRangeMax );
				break;
			case BRANCH_EDGE:
				edgeColorGenerator = new BranchEdgeFeatureColorGenerator<>(
						( FeatureProjection< BE > ) edgeProjection,
						branchGraph,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin,
						edgeRangeMax );
				break;
			default:
				throw new IllegalArgumentException( "Unknown edge color mode: " + fcm.getEdgeColorMode() );
			}
		}

		return new CompositeGraphColorGenerator<>( vertexColorGenerator, edgeColorGenerator );
	}
}
