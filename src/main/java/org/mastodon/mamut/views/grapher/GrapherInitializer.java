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
import org.mastodon.Ref;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.UndoActions;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.views.MamutViewI;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.ExportViewActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.grapher.datagraph.DataContextListener;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.display.DataDisplayFrame;
import org.mastodon.views.grapher.display.DataDisplayOptions;
import org.mastodon.views.grapher.display.DataDisplayPanel;
import org.mastodon.views.grapher.display.DataDisplayZoom;
import org.mastodon.views.grapher.display.FeatureGraphConfig;
import org.mastodon.views.grapher.display.OffsetAxes;
import org.mastodon.views.grapher.display.style.DataDisplayStyle;
import org.mastodon.views.grapher.display.style.DataDisplayStyleManager;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import java.util.function.BiConsumer;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.colorMenu;
import static org.mastodon.mamut.MamutMenuBuilder.colorbarMenu;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;

public class GrapherInitializer< V extends Vertex< E > & HasTimepoint & HasLabel & Ref< V >, E extends Edge< V > & Ref< E > >
{
	private final DataGraph< V, E > viewGraph;

	private final ProjectModel appModel;

	private final Model model;

	private final DataDisplayFrame< V, E > frame;

	private final DataDisplayPanel< V, E > panel;

	private final DataGraphLayout< V, E > layout;

	private final DataDisplayStyle forwardDefaultStyle;

	private final DataDisplayStyle.UpdateListener styleUpdateListener;

	private final ContextChooser< V > contextChooser;

	private ColoringModel coloringModel;

	private ColorBarOverlay colorBarOverlay;

	private final GraphColorGeneratorAdapter< V, E, DataVertex, DataEdge > coloringAdapter;

	private final AutoNavigateFocusModel< DataVertex, DataEdge > navigateFocusModel;

	private final SelectionModel< DataVertex, DataEdge > selectionModel;

	private final NavigationHandler< DataVertex, DataEdge > navigationHandler;

	private final FocusModel< DataVertex > focusModel;

	GrapherInitializer( final DataGraph< V, E > graph, final ProjectModel appModel,
			final SelectionModel< DataVertex, DataEdge > selectionModel, final NavigationHandler< DataVertex, DataEdge > navigationHandler,
			final FocusModel< DataVertex > focusModel, final HighlightModel< DataVertex, DataEdge > highlightModel,
			final GroupHandle groupHandle
	)
	{
		this.viewGraph = graph;
		this.appModel = appModel;
		this.model = appModel.getModel();
		this.selectionModel = selectionModel;
		this.navigationHandler = navigationHandler;
		this.focusModel = focusModel;

		// The layout.
		layout = new DataGraphLayout<>( viewGraph, selectionModel );

		// ContextChooser
		final DataContextListener< V > contextListener = new DataContextListener<>( viewGraph );
		contextChooser = new ContextChooser<>( contextListener );

		// Style
		final DataDisplayStyleManager dataDisplayStyleManager = appModel.getWindowManager().getManager( DataDisplayStyleManager.class );
		forwardDefaultStyle = dataDisplayStyleManager.getForwardDefaultStyle();

		// A reference on the {@code GraphColorGeneratorAdapter} created and registered with this instance/window
		coloringAdapter = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );

		// Options
		final DataDisplayOptions< DataVertex, DataEdge > options = DataDisplayOptions.options();
		options.shareKeyPressedEvents( appModel.getKeyPressedManager() ).style( forwardDefaultStyle )
				.graphColorGenerator( coloringAdapter );

		// Navigation
		navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );

		// Frame
		this.frame = new DataDisplayFrame<>(
				viewGraph,
				appModel.getModel().getFeatureModel(),
				appModel.getSharedBdvData().getSources().size(),
				layout,
				highlightModel,
				navigateFocusModel,
				selectionModel,
				navigationHandler,
				model,
				groupHandle,
				contextChooser,
				options );

		// Panel
		this.panel = frame.getDataDisplayPanel();
		contextListener.setContextListener( panel );

		// Style listener
		styleUpdateListener = panel::repaint;
		forwardDefaultStyle.updateListeners().add( styleUpdateListener );

		// Label listener
		appModel.getModel().getGraph().addVertexLabelListener( vertex -> panel.entitiesAttributesChanged() );
	}

	void setOnClose( final MamutViewI view )
	{
		view.onClose( () -> forwardDefaultStyle.updateListeners().remove( styleUpdateListener ) );
	}

	void initFeatureConfig( final FeatureGraphConfig featureGraphConfig )
	{
		frame.getVertexSidePanel().setGraphConfig( featureGraphConfig );
		panel.graphChanged();
	}

	void addMenusAndRegisterColors(
			final TriFunction< ViewMenuBuilder.JMenuHandle, GraphColorGeneratorAdapter< V, E, DataVertex, DataEdge >,
					DataDisplayPanel< V, E >, ColoringModel > colorModelRegistration,
			final LoopBuilder.TriConsumer< ColorBarOverlay, ViewMenuBuilder.JMenuHandle,
					DataDisplayPanel< V, E > > colorBarRegistration,
			final BiConsumer< ViewMenuBuilder.JMenuHandle, DataDisplayPanel< V, E > > tagSetMenuRegistration,
			final String[] keyConfigContexts )
	{
		final ViewMenu viewMenu = new ViewMenu( frame.getJMenuBar(), appModel.getKeymap(), keyConfigContexts );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final ViewMenuBuilder.JMenuHandle coloringMenuHandle = new ViewMenuBuilder.JMenuHandle();
		final ViewMenuBuilder.JMenuHandle colorbarMenuHandle = new ViewMenuBuilder.JMenuHandle();
		final ViewMenuBuilder.JMenuHandle tagSetMenuHandle = new ViewMenuBuilder.JMenuHandle();

		MainWindow.addMenus( viewMenu, actionMap );
		appModel.getWindowManager().addWindowMenu( viewMenu, actionMap );
		MamutMenuBuilder.build( viewMenu, actionMap,
				fileMenu(
						separator(),
						item( ExportViewActions.EXPORT_VIEW_TO_SVG ),
						item( ExportViewActions.EXPORT_VIEW_TO_PNG ) ),
				viewMenu(
						colorMenu( coloringMenuHandle ),
						colorbarMenu( colorbarMenuHandle ),
						separator(),
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL ) ),
				editMenu(
						item( UndoActions.UNDO ),
						item( UndoActions.REDO ),
						separator(),
						item( SelectionActions.DELETE_SELECTION ),
						item( SelectionActions.SELECT_WHOLE_TRACK ),
						item( SelectionActions.SELECT_TRACK_DOWNWARD ),
						item( SelectionActions.SELECT_TRACK_UPWARD ),
						separator(),
						tagSetMenu( tagSetMenuHandle ) ) );
		appModel.getPlugins().addMenus( viewMenu );

		registerColoring( colorModelRegistration, coloringMenuHandle );
		registerColorBar( colorBarRegistration, colorbarMenuHandle );
		registerTagSetMenu( tagSetMenuRegistration, tagSetMenuHandle );
	}

	void layout()
	{
		layout.layout();
		panel.repaint();
		panel.getDisplay().requestFocusInWindow();
	}

	void installActions( final Actions viewActions, final Behaviours viewBehaviours )
	{
		MastodonFrameViewActions.install( viewActions, () -> frame );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), model.getTagSetModel(),
				appModel.getSelectionModel(), viewGraph.getLock(), panel, panel.getDisplay(), model );
		DataDisplayZoom.install( viewBehaviours, panel );
		ExportViewActions.install( viewActions, panel.getDisplay(), frame, frame.getTitle() );

		panel.getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		panel.getNavigationBehaviours().install( viewBehaviours );
		panel.getTransformEventHandler().install( viewBehaviours );
	}

	void addSearchPanel( final Actions viewActions )
	{
		final JPanel searchPanel =
				SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, panel );
		frame.getSettingsPanel().add( searchPanel );
	}

	private void
			registerColoring(
					final TriFunction< ViewMenuBuilder.JMenuHandle, GraphColorGeneratorAdapter< V, E, DataVertex, DataEdge >,
							DataDisplayPanel< V, E >, ColoringModel > colorModelRegistration,
					final ViewMenuBuilder.JMenuHandle coloringMenuHandle )
	{
		coloringModel = colorModelRegistration.apply( coloringMenuHandle, coloringAdapter, panel );
	}

	private void registerColorBar( final LoopBuilder.TriConsumer< ColorBarOverlay, ViewMenuBuilder.JMenuHandle,
			DataDisplayPanel< V, E > > colorBarRegistration, final ViewMenuBuilder.JMenuHandle colorbarMenuHandle )
	{
		colorBarOverlay = new ColorBarOverlay( coloringModel, panel::getBackground );
		final OffsetAxes offset = panel.getOffsetAxes();
		offset.listeners().add( ( w, h ) -> colorBarOverlay.setInsets( 15, w + 15, h + 15, 15 ) );
		colorBarRegistration.accept( colorBarOverlay, colorbarMenuHandle, panel );
		panel.getDisplay().overlays().add( colorBarOverlay );
	}

	private void registerTagSetMenu( final BiConsumer< ViewMenuBuilder.JMenuHandle, DataDisplayPanel< V, E > > tagSetMenuRegistration,
			final ViewMenuBuilder.JMenuHandle tagSetMenuHandle )
	{
		tagSetMenuRegistration.accept( tagSetMenuHandle, panel );
	}

	ContextChooser< V > getContextChooser()
	{
		return contextChooser;
	}

	DataDisplayFrame< V, E > getFrame()
	{
		return frame;
	}

	ColoringModel getColoringModel()
	{
		return coloringModel;
	}

	ColorBarOverlay getColorBarOverlay()
	{
		return colorBarOverlay;
	}
}
