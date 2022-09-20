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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.ui.AvailableFeatureProjectionsImp.createAvailableFeatureProjections;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.AvailableFeatureProjections;
import org.mastodon.feature.ui.FeatureProjectionsManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.ui.coloring.feature.DefaultFeatureRangeCalculator;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.FeatureRangeCalculator;
import org.mastodon.ui.coloring.feature.Projections;
import org.mastodon.ui.coloring.feature.ProjectionsFromFeatureModel;
import org.mastodon.ui.coloring.feature.TargetType;
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
public class MamutFeatureProjectionsManager implements FeatureProjectionsManager
{
	private final FeatureSpecsService featureSpecsService;

	private final FeatureColorModeManager featureColorModeManager;

	private final AggregateFeatureRangeCalculator featureRangeCalculator;

	private final Listeners.List< AvailableFeatureProjectionsListener > listeners;

	private Model model;

	private int numSources = 1;

	public MamutFeatureProjectionsManager(
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
	public void setModel( final Model model, final int numSources )
	{
		this.model = model;
		this.numSources = Math.max( 1, numSources );

		if ( model != null )
		{
			final FeatureModel featureModel = model.getFeatureModel();
			final Projections projections = new ProjectionsFromFeatureModel( featureModel );
			featureRangeCalculator.vertexCalculator = new DefaultFeatureRangeCalculator<>( model.getGraph().vertices(), projections );
			featureRangeCalculator.edgeCalculator = new DefaultFeatureRangeCalculator<>( model.getGraph().edges(), projections );
			featureModel.listeners().add( this::notifyAvailableFeatureProjectionsChanged );
		}
		else
		{
			featureRangeCalculator.vertexCalculator = null;
			featureRangeCalculator.edgeCalculator = null;
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
		final FeatureModel featureModel = ( model != null ) ? model.getFeatureModel() : null;
		return createAvailableFeatureProjections(
				featureSpecsService,
				numSources,
				featureModel,
				featureColorModeManager,
				Spot.class,
				Link.class );
	}

	private void notifyAvailableFeatureProjectionsChanged()
	{
		listeners.list.forEach( AvailableFeatureProjectionsListener::availableFeatureProjectionsChanged );
	}

	private static class AggregateFeatureRangeCalculator implements FeatureRangeCalculator
	{
		FeatureRangeCalculator vertexCalculator;

		FeatureRangeCalculator edgeCalculator;

		@Override
		public double[] computeMinMax( final FeatureProjectionId projection )
		{
			if ( projection == null )
				return null;

			if ( projection.getTargetType() == TargetType.VERTEX )
			{
				return vertexCalculator == null
						? null
						: vertexCalculator.computeMinMax( projection );
			}
			else // if ( projection.getTargetType() == TargetType.EDGE )
			{
				return edgeCalculator == null
						? null
						: edgeCalculator.computeMinMax( projection );
			}
		}
	};

	@Override
	public FeatureRangeCalculator getFeatureRangeCalculator()
	{
		return featureRangeCalculator;
	}
}
