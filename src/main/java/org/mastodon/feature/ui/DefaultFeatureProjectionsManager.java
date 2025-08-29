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
package org.mastodon.feature.ui;

import static org.mastodon.feature.ui.AvailableFeatureProjectionsImp.createAvailableFeatureProjections;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.branch.BranchGraphImp;
import org.mastodon.mamut.model.Model;
import org.mastodon.model.AbstractModelBranch;
import org.mastodon.model.MastodonModel;
import org.mastodon.ui.coloring.feature.DefaultFeatureRangeCalculator;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.FeatureRangeCalculator;
import org.mastodon.ui.coloring.feature.Projections;
import org.mastodon.ui.coloring.feature.ProjectionsFromFeatureModel;
import org.scijava.listeners.Listeners;

/**
 * Provides and up-to-date set of feature projections, as well as
 * {@code FeatureRangeCalculator}s for vertices and edges.
 * <p>
 * Used for FeatureColorModes.
 * <p>
 * This implementation feeds from a {@link Model}: It provides
 * {@code FeatureRangeCalculator} on the {@code Model}s vertices and edges. It
 * listens to changes in the {@code Model}s {@link FeatureModel} to update the
 * available feature projections.
 *
 * @author Tobias Pietzsch
 */
public class DefaultFeatureProjectionsManager implements FeatureProjectionsManager
{
	private final FeatureSpecsService featureSpecsService;

	private final FeatureColorModeManager featureColorModeManager;

	private final AggregateFeatureRangeCalculator featureRangeCalculator;

	private final Listeners.List< AvailableFeatureProjectionsListener > listeners;

	private MastodonModel< ?, ?, ? > model;

	private FeatureModel featureModel;

	private int numSources = 1;

	public DefaultFeatureProjectionsManager(
			final FeatureSpecsService featureSpecsService,
			final FeatureColorModeManager featureColorModeManager )
	{
		this.featureSpecsService = featureSpecsService;
		this.featureColorModeManager = featureColorModeManager;
		this.featureRangeCalculator = new AggregateFeatureRangeCalculator();
		this.listeners = new Listeners.List<>();
	}

	/**
	 * Sets the current {@code Model}. This will update the available
	 * projections and listen to the model's {@code FeatureModel}.
	 *
	 * @param model
	 *            the current {@code Model} (or {@code null}).
	 * @param numSources
	 *            the number of sources in the image data.
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public void setModel( final MastodonModel< ?, ?, ? > model, final FeatureModel featureModel, final int numSources )
	{
		this.model = model;
		this.featureModel = featureModel;
		this.numSources = Math.max( 1, numSources );
		if ( model != null )
		{
			final Projections projections = new ProjectionsFromFeatureModel( featureModel );
			featureRangeCalculator.vertexCalculator = new DefaultFeatureRangeCalculator<>( model.getGraph().vertices(), projections );
			featureRangeCalculator.edgeCalculator = new DefaultFeatureRangeCalculator<>( model.getGraph().edges(), projections );

			if ( model instanceof AbstractModelBranch )
			{
				final AbstractModelBranch mb = ( AbstractModelBranch ) model;
				final BranchGraphImp branchGraph = mb.branchModel().getGraph();
				featureRangeCalculator.branchVertexCalculator = new DefaultFeatureRangeCalculator<>( branchGraph.vertices(), projections );
				featureRangeCalculator.branchEdgeCalculator = new DefaultFeatureRangeCalculator<>( branchGraph.edges(), projections );
			}
			else
			{
				featureRangeCalculator.branchVertexCalculator = null;
				featureRangeCalculator.branchEdgeCalculator = null;
			}
			featureModel.listeners().add( this::notifyAvailableFeatureProjectionsChanged );
		}
		else
		{
			featureRangeCalculator.vertexCalculator = null;
			featureRangeCalculator.edgeCalculator = null;
			featureRangeCalculator.branchVertexCalculator = null;
			featureRangeCalculator.branchEdgeCalculator = null;
		}
		notifyAvailableFeatureProjectionsChanged();
	}

	/**
	 * Exposes the list of listeners that are notified when a change happens to
	 */
	@Override
	public Listeners< AvailableFeatureProjectionsListener > listeners()
	{
		return listeners;
	}

	@Override
	public AvailableFeatureProjections getAvailableFeatureProjections()
	{
		// Determine the vertex and edge classes.
		final ListenableGraph< ?, ? > graph = model.getGraph();
		final Class< ? > vertexClass;
		final Class< ? > edgeClass ;
		final Class< ? > branchVertexClass;
		final Class< ? > branchEdgeClass ;
		if ( model == null )
		{
			vertexClass = null;
			edgeClass = null;
			branchVertexClass = null;
			branchEdgeClass = null;
		}
		else
		{
			vertexClass = graph.vertices().iterator().next().getClass();
			edgeClass = graph.edges().iterator().next().getClass();

			if ( model instanceof AbstractModelBranch )
			{
				@SuppressWarnings( "rawtypes" )
				final AbstractModelBranch mb = ( AbstractModelBranch ) model;
				@SuppressWarnings( "rawtypes" )
				final BranchGraphImp branchGraph = mb.branchModel().getGraph();
				branchVertexClass = branchGraph.vertices().iterator().next().getClass();
				branchEdgeClass = branchGraph.edges().iterator().next().getClass();
			}
			else
			{
				branchVertexClass = null;
				branchEdgeClass = null;
			}
		}

		return createAvailableFeatureProjections(
				featureSpecsService,
				numSources,
				featureModel,
				featureColorModeManager,
				vertexClass,
				edgeClass,
				branchVertexClass,
				branchEdgeClass );
	}

	private void notifyAvailableFeatureProjectionsChanged()
	{
		listeners.list.forEach( AvailableFeatureProjectionsListener::availableFeatureProjectionsChanged );
	}

	private static class AggregateFeatureRangeCalculator implements FeatureRangeCalculator
	{

		FeatureRangeCalculator vertexCalculator;

		FeatureRangeCalculator edgeCalculator;

		FeatureRangeCalculator branchVertexCalculator;

		FeatureRangeCalculator branchEdgeCalculator;

		@Override
		public double[] computeMinMax( final FeatureProjectionId projection )
		{
			if ( projection == null )
				return null;

			switch ( projection.getTargetType() )
			{
			case VERTEX:
				return vertexCalculator == null
						? null
						: vertexCalculator.computeMinMax( projection );
			case EDGE:
				return edgeCalculator == null
						? null
						: edgeCalculator.computeMinMax( projection );
			case BRANCH_VERTEX:
				return branchVertexCalculator == null
						? null
						: branchVertexCalculator.computeMinMax( projection );
			case BRANCH_EDGE:
				return branchEdgeCalculator == null
						? null
						: branchEdgeCalculator.computeMinMax( projection );
			default:
				throw new IllegalArgumentException(
						"Unknown target type: " + projection.getTargetType() + " of projection " + projection );
			}
		}
	};

	@Override
	public FeatureRangeCalculator getFeatureRangeCalculator()
	{
		return featureRangeCalculator;
	}
}
