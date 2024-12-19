/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import net.imglib2.loops.LoopBuilder;
import org.apache.commons.lang3.function.TriFunction;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.views.MamutBranchView;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.HasColorBarOverlay;
import org.mastodon.ui.coloring.HasColoringModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.display.DataDisplayFrame;
import org.mastodon.views.grapher.display.DataDisplayPanel;
import org.mastodon.views.grapher.display.FeatureGraphConfig;
import org.mastodon.views.grapher.display.FeatureGraphConfig.GraphDataItemsSource;
import org.mastodon.views.grapher.display.FeatureSpecPair;
import org.mastodon.views.grapher.display.InertialScreenTransformEventHandler;

import java.util.Iterator;
import java.util.function.BiConsumer;

public class MamutBranchViewGrapher extends MamutBranchView< DataGraph< BranchSpot, BranchLink >, DataVertex, DataEdge >
		implements HasColoringModel, HasColorBarOverlay, DataDisplayFrameSupplier< BranchSpot, BranchLink >
{

	private final GrapherInitializer< BranchSpot, BranchLink > grapherInitializer;

	MamutBranchViewGrapher( final ProjectModel appModel )
	{
		super( appModel,
				new DataGraph<>(
						appModel.getModel().getBranchGraph(),
						appModel.getModel().getBranchGraphIdBimap(),
						appModel.getModel().getGraph().getLock() ),
				new String[] { KeyConfigContexts.GRAPHER } );

		grapherInitializer = new GrapherInitializer<>( viewGraph, appModel, selectionModel, navigationHandler, focusModel, highlightModel,
				getGroupHandle(), null );
		grapherInitializer.getFrame().setTitle( "Grapher Branch" );
		InertialScreenTransformEventHandler handler = grapherInitializer.getFrame().getDataDisplayPanel().getTransformEventHandler();
		handler.setMinScaleX( 0.1d );
		handler.setMinScaleY( 0.1d );
		grapherInitializer.setOnClose( this );
		grapherInitializer.initFeatureConfig( getFeatureGraphConfig() );
		setFrame( grapherInitializer.getFrame() ); // this creates viewActions and viewBehaviours thus must be called before installActions
		grapherInitializer.installActions( viewActions, viewBehaviours );
		grapherInitializer.addSearchPanel( viewActions );

		TriFunction< JMenuHandle, GraphColorGeneratorAdapter< BranchSpot, BranchLink, DataVertex, DataEdge >,
				DataDisplayPanel< BranchSpot, BranchLink >, ColoringModel > colorModelRegistration =
						( menuHandle, coloringAdaptor, panel ) -> registerBranchColoring( coloringAdaptor, menuHandle,
								panel::entitiesAttributesChanged );
		LoopBuilder.TriConsumer< ColorBarOverlay, JMenuHandle, DataDisplayPanel< BranchSpot, BranchLink > > colorBarRegistration =
				( overlay, menuHandle, panel ) -> registerColorbarOverlay( overlay, menuHandle, panel::repaint );
		BiConsumer< JMenuHandle, DataDisplayPanel< BranchSpot, BranchLink > > tagSetMenuRegistration =
				( menuHandle, panel ) -> registerTagSetMenu( menuHandle, panel::entitiesAttributesChanged );

		grapherInitializer.addMenusAndRegisterColors( colorModelRegistration, colorBarRegistration, tagSetMenuRegistration,
				keyConfigContexts );
		grapherInitializer.layout();
	}

	static FeatureGraphConfig getFeatureGraphConfig()
	{
		// If they are available, set some sensible defaults for the feature.
		Iterator< FeatureProjectionSpec > projections = BranchDisplacementDurationFeature.SPEC.getProjectionSpecs().iterator();
		FeatureProjectionSpec displacementSpec = projections.next();
		FeatureProjectionSpec durationSpec = projections.next();
		final FeatureSpecPair featureSpecX =
				new FeatureSpecPair( BranchDisplacementDurationFeature.SPEC, displacementSpec, false, false );
		final FeatureSpecPair featureSpecY =
				new FeatureSpecPair( BranchDisplacementDurationFeature.SPEC, durationSpec, false, false );
		return new FeatureGraphConfig( featureSpecX, featureSpecY, GraphDataItemsSource.CONTEXT, false );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public DataDisplayFrame< BranchSpot, BranchLink > getFrame()
	{
		return ( DataDisplayFrame< BranchSpot, BranchLink > ) super.getFrame();
	}

	@Override
	public ColoringModel getColoringModel()
	{
		return grapherInitializer.getColoringModel();
	}

	@Override
	public ColorBarOverlay getColorBarOverlay()
	{
		return grapherInitializer.getColorBarOverlay();
	}
}
