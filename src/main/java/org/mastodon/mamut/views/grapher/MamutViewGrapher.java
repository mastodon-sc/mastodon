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

import java.util.function.BiConsumer;

import org.apache.commons.lang3.function.TriFunction;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.SpotFrameFeature;
import org.mastodon.mamut.feature.SpotQuickMeanIntensityFeature;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.views.MamutView;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.HasColorBarOverlay;
import org.mastodon.ui.coloring.HasColoringModel;
import org.mastodon.ui.commandfinder.CommandFinder;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.display.DataDisplayFrame;
import org.mastodon.views.grapher.display.DataDisplayPanel;
import org.mastodon.views.grapher.display.FeatureGraphConfig;
import org.mastodon.views.grapher.display.FeatureSpecPair;

import net.imglib2.loops.LoopBuilder;

public class MamutViewGrapher extends MamutView< DataGraph< Spot, Link >, DataVertex, DataEdge >
		implements HasContextChooser< Spot >, HasColoringModel, HasColorBarOverlay, DataDisplayFrameSupplier< Spot, Link >
{

	private final GrapherInitializer< Spot, Link > grapherInitializer;

	MamutViewGrapher( final ProjectModel appModel )
	{
		super( appModel,
				new DataGraph<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getGraph().getLock()
				),
				new String[] { KeyConfigContexts.GRAPHER } );


		grapherInitializer = new GrapherInitializer<>( viewGraph, appModel, selectionModel, navigationHandler, focusModel, highlightModel,
				getGroupHandle() );
		grapherInitializer.setOnClose( this );
		grapherInitializer.initFeatureConfig( getFeatureGraphConfig() );
		setFrame( grapherInitializer.getFrame() ); // this creates viewActions and viewBehaviours thus must be called before installActions
		grapherInitializer.installActions( viewActions, viewBehaviours );
		grapherInitializer.addSearchPanel( viewActions );

		final TriFunction< ViewMenuBuilder.JMenuHandle, GraphColorGeneratorAdapter< Spot, Link, DataVertex, DataEdge >,
				DataDisplayPanel< Spot, Link >, ColoringModel > colorModelRegistration = ( menuHandle, coloringAdaptor,
						panel ) -> registerColoring( coloringAdaptor, menuHandle, panel::entitiesAttributesChanged );
		final LoopBuilder.TriConsumer< ColorBarOverlay, ViewMenuBuilder.JMenuHandle, DataDisplayPanel< Spot, Link > > colorBarRegistration =
				( overlay, menuHandle, panel ) -> registerColorbarOverlay( overlay, menuHandle, panel::repaint );
		final BiConsumer< ViewMenuBuilder.JMenuHandle, DataDisplayPanel< Spot, Link > > tagSetMenuRegistration =
				( menuHandle, panel ) -> registerTagSetMenu( menuHandle, panel::entitiesAttributesChanged );

		grapherInitializer.addMenusAndRegisterColors( colorModelRegistration, colorBarRegistration, tagSetMenuRegistration,
				keyConfigContexts );
		grapherInitializer.layout();

		final CommandFinder cf = CommandFinder.build()
				.context( appModel.getContext() )
				.inputTriggerConfig( appModel.getKeymap().getConfig() )
				.keyConfigContexts( keyConfigContexts )
				.descriptionProvider( appModel.getWindowManager().getViewFactories().getCommandDescriptions() )
				.register( viewActions )
				.register( appModel.getModelActions() )
				.register( appModel.getProjectActions() )
				.register( appModel.getPlugins().getPluginActions() )
				.modificationListeners( appModel.getKeymap().updateListeners() )
				.parent( frame )
				.installOn( viewActions );
		cf.getDialog().setTitle( cf.getDialog().getTitle() + " - " + frame.getTitle() );
	}

	static FeatureGraphConfig getFeatureGraphConfig()
	{
		// If they are available, set some sensible defaults for the feature.
		final FeatureSpecPair spvx = new FeatureSpecPair( SpotFrameFeature.SPEC,
				SpotFrameFeature.SPEC.getProjectionSpecs().iterator().next(), false, false );
		final FeatureSpecPair spvy = new FeatureSpecPair( SpotQuickMeanIntensityFeature.SPEC,
				SpotQuickMeanIntensityFeature.PROJECTION_SPEC, 0, false, false );
		return new FeatureGraphConfig( spvx, spvy, FeatureGraphConfig.GraphDataItemsSource.CONTEXT, false );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public DataDisplayFrame< Spot, Link > getFrame()
	{
		return ( DataDisplayFrame< Spot, Link > ) super.getFrame();
	}

	@Override
	public ContextChooser< Spot > getContextChooser()
	{
		return grapherInitializer.getContextChooser();
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
