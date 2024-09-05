package org.mastodon.mamut.views.grapher;

import net.imglib2.loops.LoopBuilder;
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

import java.util.function.BiConsumer;

public class MamutViewGrapher extends MamutView< DataGraph< Spot, Link >, DataVertex, DataEdge >
		implements HasContextChooser< Spot >, HasColoringModel, HasColorBarOverlay
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

		TriFunction< ViewMenuBuilder.JMenuHandle, GraphColorGeneratorAdapter< Spot, Link, DataVertex, DataEdge >,
				DataDisplayPanel< Spot, Link >, ColoringModel > colorModelRegistration = ( menuHandle, coloringAdaptor,
						panel ) -> registerColoring( coloringAdaptor, menuHandle, panel::entitiesAttributesChanged );
		LoopBuilder.TriConsumer< ColorBarOverlay, ViewMenuBuilder.JMenuHandle, DataDisplayPanel< Spot, Link > > colorBarRegistration =
				( overlay, menuHandle, panel ) -> registerColorbarOverlay( overlay, menuHandle, panel::repaint );
		BiConsumer< ViewMenuBuilder.JMenuHandle, DataDisplayPanel< Spot, Link > > tagSetMenuRegistration =
				( menuHandle, panel ) -> registerTagSetMenu( menuHandle, panel::entitiesAttributesChanged );

		grapherInitializer.addMenusAndRegisterColors( colorModelRegistration, colorBarRegistration, tagSetMenuRegistration,
				keyConfigContexts );
		grapherInitializer.layout();
	}

	private static FeatureGraphConfig getFeatureGraphConfig()
	{
		// If they are available, set some sensible defaults for the feature.
		final FeatureSpecPair spvx = new FeatureSpecPair( SpotFrameFeature.SPEC,
				SpotFrameFeature.SPEC.getProjectionSpecs().iterator().next(), false, false );
		final FeatureSpecPair spvy = new FeatureSpecPair( SpotQuickMeanIntensityFeature.SPEC,
				SpotQuickMeanIntensityFeature.PROJECTION_SPEC, 0, false, false );
		return new FeatureGraphConfig( spvx, spvy, FeatureGraphConfig.GraphDataItemsSource.TRACK_OF_SELECTION, true );
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
