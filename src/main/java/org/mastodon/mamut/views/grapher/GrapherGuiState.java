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
package org.mastodon.mamut.views.grapher;

import java.util.Map;

import org.mastodon.Ref;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HasLabel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.FeatureGraphConfig;
import org.mastodon.views.grapher.display.FeatureSpecPair;
import org.mastodon.views.grapher.display.GrapherSidePanel;
import org.mastodon.views.grapher.display.ScreenTransformState;

public class GrapherGuiState
{
	private GrapherGuiState()
	{
		// Prevent instantiation.
	}

	/**
	 * Key for the transform in a Grapher view. Value is a Grapher ScreenTransform instance.
	 */
	public static final String GRAPHER_TRANSFORM_KEY = "GrapherTransform";

	/**
	 * Key for the selected feature for the X axis in a Grapher view. Value is a Boolean.
	 */
	public static final String GRAPHER_X_AXIS_FEATURE_IS_EDGE_KEY = "GrapherXAxisFeatureIsEdge";

	/**
	 * Key for the selected feature spec for the X axis in a Grapher view. Value is a string.
	 */
	public static final String GRAPHER_X_AXIS_FEATURE_SPEC_KEY = "GrapherXAxisFeatureSpec";

	/**
	 * Key for the selected projection for the X axis in a Grapher view. Value is a string.
	 */
	public static final String GRAPHER_X_AXIS_FEATURE_PROJECTION_KEY = "GrapherXAxisFeatureProjection";

	/**
	 * Key for the selected incoming edge for the X axis in a Grapher view. Value is a boolean.
	 */
	public static final String GRAPHER_X_AXIS_INCOMING_EDGE_KEY = "GrapherXAxisFeatureIsIncomingEdge";

	/**
	 * Key for the selected feature for the Y axis in a Grapher view. Value is a Boolean.
	 */
	public static final String GRAPHER_Y_AXIS_FEATURE_IS_EDGE_KEY = "GrapherYAxisFeatureIsEdge";

	/**
	 * Key for the selected feature spec for the Y axis in a Grapher view. Value is a string.
	 */
	public static final String GRAPHER_Y_AXIS_FEATURE_SPEC_KEY = "GrapherYAxisFeatureSpec";

	/**
	 * Key for the selected projection for the Y axis in a Grapher view. Value is a string.
	 */
	public static final String GRAPHER_Y_AXIS_FEATURE_PROJECTION_KEY = "GrapherYAxisFeatureProjection";

	/**
	 * Key for the selected incoming edge for the Y axis in a Grapher view. Value is a boolean.
	 */
	public static final String GRAPHER_Y_AXIS_INCOMING_EDGE_KEY = "GrapherYAxisFeatureIsIncomingEdge";

	/**
	 * Key for whether show edges is checked in a Grapher view. Value is a boolean.
	 */
	public static final String GRAPHER_SHOW_EDGES_KEY = "GrapherShowEdges";

	public static < V extends Vertex< E > & HasTimepoint & HasLabel & Ref< V >, E extends Edge< V > & Ref< E > > void
			writeGuiState( final ScreenTransformState screenTransformState, final FeatureGraphConfig config,
					final Map< String, Object > guiState )
	{
		// Transform.
		final ScreenTransform transform = screenTransformState.get();
		guiState.put( GRAPHER_TRANSFORM_KEY, transform );
		// Feature graph config.
		writeFeatureGraphConfig( config, guiState );
	}

	public static < V extends Vertex< E > & HasTimepoint & HasLabel & Ref< V >, E extends Edge< V > & Ref< E > > void
			loadGuiState( final Map< String, Object > guiState, final ScreenTransformState screenTransformState,
					final GrapherSidePanel sidePanel, final FeatureGraphConfig defaultConfig )
	{
		loadGuiState( guiState, screenTransformState, sidePanel, defaultConfig, null );
	}

	static < V extends Vertex< E > & HasTimepoint & HasLabel & Ref< V >, E extends Edge< V > & Ref< E > > void
			loadGuiState( final Map< String, Object > guiState, final ScreenTransformState screenTransformState,
					final GrapherSidePanel sidePanel, final FeatureGraphConfig defaultConfig,
					final DataDisplayFrameSupplier< V, E > frameSupplier )
	{
		// Read Screen Transform.
		final ScreenTransform screenTransform = ( ScreenTransform ) guiState.get( GRAPHER_TRANSFORM_KEY );
		if ( null != screenTransform )
			screenTransformState.set( screenTransform );

		// Read Feature graph config.
		FeatureGraphConfig config = loadFeatureGraphConfig( sidePanel, guiState, defaultConfig );
		sidePanel.setGraphConfig( config );

		// Plot with loaded transform and config.
		if ( frameSupplier != null )
			frameSupplier.getFrame().plot( screenTransform );
	}

	private static < V extends Vertex< E > & HasTimepoint & HasLabel & Ref< V >, E extends Edge< V > & Ref< E > > void
			writeFeatureGraphConfig( final FeatureGraphConfig config, final Map< String, Object > guiState )
	{
		// X-axis feature.
		FeatureSpecPair featureSpecPairX = config.getXFeature();
		guiState.put( GRAPHER_X_AXIS_FEATURE_IS_EDGE_KEY, featureSpecPairX.isEdgeFeature() );
		guiState.put( GRAPHER_X_AXIS_FEATURE_SPEC_KEY, featureSpecPairX.getFeatureSpecKey() );
		guiState.put( GRAPHER_X_AXIS_FEATURE_PROJECTION_KEY, featureSpecPairX.projectionKey().toString() );
		guiState.put( GRAPHER_X_AXIS_INCOMING_EDGE_KEY, featureSpecPairX.isIncomingEdge() );
		// Y-axis feature.
		FeatureSpecPair featureSpecPairY = config.getYFeature();
		guiState.put( GRAPHER_Y_AXIS_FEATURE_IS_EDGE_KEY, featureSpecPairY.isEdgeFeature() );
		guiState.put( GRAPHER_Y_AXIS_FEATURE_SPEC_KEY, featureSpecPairY.getFeatureSpecKey() );
		guiState.put( GRAPHER_Y_AXIS_FEATURE_PROJECTION_KEY, featureSpecPairY.projectionKey().toString() );
		guiState.put( GRAPHER_Y_AXIS_INCOMING_EDGE_KEY, featureSpecPairY.isIncomingEdge() );
		// Show edges.
		guiState.put( GRAPHER_SHOW_EDGES_KEY, config.drawConnected() );
	}

	public static < V extends Vertex< E > & HasTimepoint & HasLabel & Ref< V >, E extends Edge< V > & Ref< E > > FeatureGraphConfig
			loadFeatureGraphConfig(
					final GrapherSidePanel sidePanel, final Map< String, Object > guiState,
					final FeatureGraphConfig defaultConfig
			)
	{
		FeatureSpecPair featureSpecPairX = loadFeatureSpecPair( guiState, sidePanel, GRAPHER_X_AXIS_FEATURE_IS_EDGE_KEY,
				GRAPHER_X_AXIS_FEATURE_SPEC_KEY, GRAPHER_X_AXIS_FEATURE_PROJECTION_KEY, GRAPHER_X_AXIS_INCOMING_EDGE_KEY );
		FeatureSpecPair featureSpecPairY = loadFeatureSpecPair( guiState, sidePanel, GRAPHER_Y_AXIS_FEATURE_IS_EDGE_KEY,
				GRAPHER_Y_AXIS_FEATURE_SPEC_KEY, GRAPHER_Y_AXIS_FEATURE_PROJECTION_KEY, GRAPHER_Y_AXIS_INCOMING_EDGE_KEY );
		Boolean showEdges = ( Boolean ) guiState.get( GRAPHER_SHOW_EDGES_KEY );
		if ( featureSpecPairX == null || featureSpecPairY == null || showEdges == null )
			return defaultConfig;
		return new FeatureGraphConfig( featureSpecPairX, featureSpecPairY, FeatureGraphConfig.GraphDataItemsSource.CONTEXT, showEdges );
	}

	private static FeatureSpecPair loadFeatureSpecPair( final Map< String, Object > guiState, final GrapherSidePanel sidePanel,
			final String edgeKey, final String featureSpecKey, final String projectionKey, final String incomingEdgeKey )
	{
		Boolean isEdgeFeature = ( Boolean ) guiState.get( edgeKey );
		final String yFeatureSpecKey = ( String ) guiState.get( featureSpecKey );
		final String yProjectionKey = ( String ) guiState.get( projectionKey );
		Boolean incomingEdge = ( Boolean ) guiState.get( incomingEdgeKey );
		FeatureSpec< ?, ? > featureSpec = sidePanel.getFeatureSpec( yFeatureSpecKey );
		FeatureProjectionSpec projectionSpec = sidePanel.getFeatureProjectionSpec( yProjectionKey );
		if ( isEdgeFeature == null || featureSpec == null || projectionSpec == null || incomingEdge == null )
			return null;
		return new FeatureSpecPair( featureSpec, projectionSpec, isEdgeFeature, incomingEdge );
	}
}
