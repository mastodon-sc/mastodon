/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import net.imglib2.util.Cast;

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
		V extends Vertex< E >,
		E extends Edge< V >,
		BV extends Vertex< BE >,
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
	public GraphColorGenerator< V, E > getFeatureGraphColorGenerator()
	{
		final FeatureColorMode fcm = featureColorMode;
		if ( fcm == null )
			return new DefaultGraphColorGenerator<>();

		// Vertex.
		final FeatureProjection< ? > vertexProjection =
				projections.getFeatureProjection( fcm.getVertexFeatureProjection() );
		final ColorGenerator< V > vertexColorGenerator = ( vertexProjection == null )
				? new DefaultColorGenerator<>()
				: createVertexColorGenerator( fcm, vertexProjection );

		// Edge.
		final FeatureProjection< ? > edgeProjection =
				projections.getFeatureProjection( fcm.getEdgeFeatureProjection() );
		final EdgeColorGenerator< V, E > edgeColorGenerator = ( null == edgeProjection )
				? new DefaultEdgeColorGenerator<>()
				: createEdgeColorGenerator( fcm, edgeProjection );

		return new CompositeGraphColorGenerator<>( vertexColorGenerator, edgeColorGenerator );
	}

	private ColorGenerator< V > createVertexColorGenerator( final FeatureColorMode fcm,
			final FeatureProjection< ? > vertexProjection )
	{
		final String vertexColorMap = fcm.getVertexColorMap();
		final double vertexRangeMin = fcm.getVertexRangeMin();
		final double vertexRangeMax = fcm.getVertexRangeMax();
		switch ( fcm.getVertexColorMode() )
		{
		case INCOMING_EDGE:
			return new FeatureColorGeneratorIncomingEdge<>(
					Cast.unchecked( vertexProjection ),
					ColorMap.getColorMap( vertexColorMap ),
					vertexRangeMin, vertexRangeMax );
		case OUTGOING_EDGE:
			return new FeatureColorGeneratorOutgoingEdge<>(
					Cast.unchecked( vertexProjection ),
					ColorMap.getColorMap( vertexColorMap ),
					vertexRangeMin, vertexRangeMax );
		case VERTEX:
			return new FeatureColorGenerator<>(
					Cast.unchecked( vertexProjection ),
					ColorMap.getColorMap( vertexColorMap ),
					vertexRangeMin, vertexRangeMax );
		case NONE:
			return new DefaultColorGenerator<>();
		case BRANCH_VERTEX:
			return new BranchFeatureColorGenerator<>(
					Cast.unchecked( vertexProjection ),
					branchGraph,
					ColorMap.getColorMap( vertexColorMap ),
					vertexRangeMin, vertexRangeMax );
		case INCOMING_BRANCH_EDGE:
			return new BranchFeatureColorGeneratorIncomingEdge<>(
					Cast.unchecked( vertexProjection ),
					branchGraph,
					ColorMap.getColorMap( vertexColorMap ),
					vertexRangeMin, vertexRangeMax );
		case OUTGOING_BRANCH_EDGE:
			return new BranchFeatureColorGeneratorOutgoingEdge<>(
					Cast.unchecked( vertexProjection ),
					branchGraph,
					ColorMap.getColorMap( vertexColorMap ),
					vertexRangeMin, vertexRangeMax );
		default:
			throw new IllegalArgumentException( "Unknown vertex color mode: " + fcm.getVertexColorMode() );
		}
	}

	private EdgeColorGenerator< V, E > createEdgeColorGenerator( final FeatureColorMode fcm,
			final FeatureProjection< ? > edgeProjection )
	{
		final String edgeColorMap = fcm.getEdgeColorMap();
		final double edgeRangeMin = fcm.getEdgeRangeMin();
		final double edgeRangeMax = fcm.getEdgeRangeMax();
		switch ( fcm.getEdgeColorMode() )
		{
		case SOURCE_VERTEX:
			return new FeatureColorGeneratorSourceVertex<>(
					Cast.unchecked( edgeProjection ),
					ColorMap.getColorMap( edgeColorMap ),
					edgeRangeMin, edgeRangeMax );
		case TARGET_VERTEX:
			return new FeatureColorGeneratorTargetVertex<>(
					Cast.unchecked( edgeProjection ),
					ColorMap.getColorMap( edgeColorMap ),
					edgeRangeMin, edgeRangeMax );
		case EDGE:
			return new FeatureEdgeColorGenerator<>(
					Cast.unchecked( edgeProjection ),
					ColorMap.getColorMap( edgeColorMap ),
					edgeRangeMin, edgeRangeMax );
		case NONE:
			return new DefaultEdgeColorGenerator<>();
		case SOURCE_BRANCH_VERTEX:
			return new BranchFeatureColorGeneratorSourceVertex<>(
					Cast.unchecked( edgeProjection ),
					branchGraph,
					ColorMap.getColorMap( edgeColorMap ),
					edgeRangeMin,
					edgeRangeMax );
		case TARGET_BRANCH_VERTEX:
			return new BranchFeatureColorGeneratorTargetVertex<>(
					Cast.unchecked( edgeProjection ),
					branchGraph,
					ColorMap.getColorMap( edgeColorMap ),
					edgeRangeMin,
					edgeRangeMax );
		case INCOMING_BRANCH_EDGE:
			return new IncomingBranchEdgeFeatureColorGenerator<>(
					Cast.unchecked( edgeProjection ),
					branchGraph,
					ColorMap.getColorMap( edgeColorMap ),
					edgeRangeMin,
					edgeRangeMax );
		case OUTGOING_BRANCH_EDGE:
			return new OutgoingBranchEdgeFeatureColorGenerator<>(
					Cast.unchecked( edgeProjection ),
					branchGraph,
					ColorMap.getColorMap( edgeColorMap ),
					edgeRangeMin,
					edgeRangeMax );
		default:
			throw new IllegalArgumentException( "Unknown edge color mode: " + fcm.getEdgeColorMode() );
		}
	}

}
