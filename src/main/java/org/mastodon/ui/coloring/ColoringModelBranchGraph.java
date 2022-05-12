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

import java.util.Optional;
import java.util.stream.Stream;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorMode.VertexColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.coloring.feature.Projections;
import org.mastodon.ui.coloring.feature.ProjectionsFromFeatureModel;
import org.scijava.listeners.Listeners;

/**
 * A ColoringModel that excludes modes defined for core-graph objects. The only
 * modes this coloring models return as valid are the ones defined for the
 * branch-graph objects.
 *
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 */
public class ColoringModelBranchGraph< V extends Vertex< E >, E extends Edge< V > >
		implements TagSetModel.TagSetModelListener, FeatureColorModeManager.FeatureColorModesListener, ColoringModel
{

	private final TagSetModel< ?, ? > tagSetModel;

	private TagSetStructure.TagSet tagSet;

	private FeatureColorMode featureColorMode;

	private final FeatureColorModeManager featureColorModeManager;

	private final Projections projections;

	private final Listeners.List< ColoringChangedListener > listeners;

	public ColoringModelBranchGraph(
			final TagSetModel< ?, ? > tagSetModel,
			final FeatureColorModeManager featureColorModeManager,
			final FeatureModel featureModel )
	{
		this.tagSetModel = tagSetModel;
		this.featureColorModeManager = featureColorModeManager;
		this.projections = new ProjectionsFromFeatureModel( featureModel );
		this.listeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public Listeners< ColoringChangedListener > listeners()
	{
		return listeners;
	}

	@Override
	public void colorByNone()
	{
		tagSet = null;
		featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	@Override
	public void colorByTagSet( final TagSetStructure.TagSet tagSet )
	{
		this.tagSet = tagSet;
		this.featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	@Override
	public TagSetStructure.TagSet getTagSet()
	{
		return tagSet;
	}

	@Override
	public void colorByFeature( final FeatureColorMode featureColorMode )
	{
		this.featureColorMode = featureColorMode;
		this.tagSet = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	@Override
	public FeatureColorMode getFeatureColorMode()
	{
		return featureColorMode;
	}

	@Override
	public boolean noColoring()
	{
		return tagSet == null && featureColorMode == null;
	}

	@Override
	public void tagSetStructureChanged()
	{
		if ( tagSet != null )
		{
			final int id = tagSet.id();
			final TagSetStructure tss = tagSetModel.getTagSetStructure();
			final Optional< TagSetStructure.TagSet > ts = tss.getTagSets().stream().filter( t -> t.id() == id ).findFirst();
			if ( ts.isPresent() )
				colorByTagSet( ts.get() );
			else
				colorByNone();
		}
	}

	@Override
	public void featureColorModesChanged()
	{
		if ( featureColorMode != null )
		{
			final String name = featureColorMode.getName();
			final Optional< FeatureColorMode > mode = Stream.concat(
					featureColorModeManager.getBuiltinStyles().stream(),
					featureColorModeManager.getUserStyles().stream() )
					.filter( m -> m.getName().equals( name ) && isValid( m ) )
					.findFirst();
			if ( mode.isPresent() )
				colorByFeature( mode.get() );
			else
				colorByNone();
		}
	}

	@Override
	public TagSetStructure getTagSetStructure()
	{
		return tagSetModel.getTagSetStructure();
	}

	@Override
	public FeatureColorModeManager getFeatureColorModeManager()
	{
		return featureColorModeManager;
	}

	/**
	 * Returns {@code true} if the specified color mode is valid against the
	 * {@link FeatureModel}. That is: the feature projections that the color
	 * mode rely on are declared in the feature model, and of the right class.
	 *
	 * @param mode
	 *            the color mode
	 * @return {@code true} if the color mode is valid.
	 */
	@Override
	public boolean isValid( final FeatureColorMode mode )
	{
		final VertexColorMode vmode = mode.getVertexColorMode();
		if ( vmode != FeatureColorMode.VertexColorMode.NONE )
		{
			// Forbid modes for which we don't have the projection.
			if ( null == projections.getFeatureProjection( mode.getVertexFeatureProjection() ) )
				return false;

			// Forbid modes that are not defined on branch objects.
			if ( vmode == VertexColorMode.INCOMING_EDGE ||
					vmode == VertexColorMode.OUTGOING_EDGE ||
					vmode == VertexColorMode.VERTEX )
				return false;
		}

		final EdgeColorMode emode = mode.getEdgeColorMode();
		if ( emode != FeatureColorMode.EdgeColorMode.NONE )
		{
			if ( null == projections.getFeatureProjection( mode.getEdgeFeatureProjection() ) )
				return false;

			if ( emode == EdgeColorMode.EDGE ||
					emode == EdgeColorMode.SOURCE_VERTEX ||
					emode == EdgeColorMode.TARGET_VERTEX )
				return false;
		}

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
			case INCOMING_BRANCH_EDGE:
				vertexColorGenerator = new FeatureColorGeneratorIncomingEdge<>(
						( FeatureProjection< E > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case OUTGOING_EDGE:
			case OUTGOING_BRANCH_EDGE:
				vertexColorGenerator = new FeatureColorGeneratorOutgoingEdge<>(
						( FeatureProjection< E > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case VERTEX:
			case BRANCH_VERTEX_DOWN:
			case BRANCH_VERTEX_UP:
				vertexColorGenerator = new FeatureColorGenerator<>(
						( FeatureProjection< V > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case NONE:
				vertexColorGenerator = new DefaultColorGenerator<>();
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
			case SOURCE_BRANCH_VERTEX_DOWN:
			case SOURCE_BRANCH_VERTEX_UP:
				edgeColorGenerator = new FeatureColorGeneratorSourceVertex<>(
						( FeatureProjection< V > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case TARGET_VERTEX:
			case TARGET_BRANCH_VERTEX_DOWN:
			case TARGET_BRANCH_VERTEX_UP:
				edgeColorGenerator = new FeatureColorGeneratorTargetVertex<>(
						( FeatureProjection< V > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case EDGE:
			case BRANCH_EDGE:
				edgeColorGenerator = new FeatureEdgeColorGenerator<>(
						( FeatureProjection< E > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case NONE:
				edgeColorGenerator = new DefaultEdgeColorGenerator<>();
				break;
			default:
				throw new IllegalArgumentException( "Unknown edge color mode: " + fcm.getEdgeColorMode() );
			}
		}

		return new CompositeGraphColorGenerator<>( vertexColorGenerator, edgeColorGenerator );
	}
}