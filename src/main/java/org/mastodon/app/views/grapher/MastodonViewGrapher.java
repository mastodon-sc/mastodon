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
package org.mastodon.app.views.grapher;

import static org.mastodon.app.ui.ViewMenuBuilder2.item;
import static org.mastodon.app.ui.ViewMenuBuilder2.separator;
import static org.mastodon.mamut.MamutMenuBuilder2.colorMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.colorbarMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder2.viewMenu;

import java.awt.Component;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.mastodon.app.UIModel;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewMenu2;
import org.mastodon.app.ui.ViewMenuBuilder2.JMenuHandle;
import org.mastodon.app.views.AbstractMastodonFrameView2;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutMenuBuilder2;
import org.mastodon.mamut.UndoActions;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.ExportViewActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.HasColorBarOverlay;
import org.mastodon.ui.coloring.HasColoringModel;
import org.mastodon.ui.commandfinder.CommandFinder;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.grapher.datagraph.DataContextListener;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataGraphProperties;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.display.DataDisplayFrame;
import org.mastodon.views.grapher.display.DataDisplayOptions;
import org.mastodon.views.grapher.display.DataDisplayPanel;
import org.mastodon.views.grapher.display.DataDisplayZoom;
import org.mastodon.views.grapher.display.FeatureGraphConfig;
import org.mastodon.views.grapher.display.FreeformSelectionBehaviour;
import org.mastodon.views.grapher.display.OffsetAxes;
import org.mastodon.views.grapher.display.style.DataDisplayStyle;
import org.mastodon.views.grapher.display.style.DataDisplayStyle.UpdateListener;
import org.mastodon.views.grapher.display.style.DataDisplayStyleManager;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;

/**
 * View class to display a Mastodon model as a Grapher view.
 * <p>
 * A grapher view is a 2D plot of features associated to the vertices of the
 * model's graph. Feature values will be read from the
 * {@link MastodonModel#getFeatureModel()} instance.
 * 
 * @param <M>
 *            the type of Mastodon model to plot.
 * @param <G>
 *            the type of graph in the model.
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class MastodonViewGrapher< 
		M extends MastodonModel< G, V, E >,
		G extends ListenableReadOnlyGraph< V, E >,
		V extends Vertex<  E >,
		E extends Edge< V > > 
		extends AbstractMastodonFrameView2< M, DataGraph< V, E >, V, E, DataVertex, DataEdge >
		implements HasContextChooser< V >, HasColorBarOverlay, HasColoringModel

{

	private final DataGraphLayout< V, E > layout;

	private final ContextChooser< V > contextChooser;

	private final ColoringModel coloringModel;

	private final ColorBarOverlay colorBarOverlay;

	private final DataDisplayPanel< V, E > panel;

	protected MastodonViewGrapher(
			final M dataModel,
			final UIModel< ? > uiModel,
			final DataGraphProperties< V, E > properties
	)
	{
		super( dataModel, uiModel,
				new DataGraph<>(
						dataModel.getGraph(),
						dataModel.getGraphIdBimap(),
						properties ),
				new String[] { KeyConfigContexts.GRAPHER } );

		// The layout.
		layout = new DataGraphLayout<>( viewGraph, selectionModel );

		// ContextChooser
		final DataContextListener< V > contextListener = new DataContextListener<>( viewGraph );
		this.contextChooser = new ContextChooser<>( contextListener );

		// Options
		final DataDisplayStyleManager styleManager = uiModel.getInstance( DataDisplayStyleManager.class );
		final DataDisplayStyle forwardDefaultStyle = styleManager.getForwardDefaultStyle();
		final DataDisplayOptions options = DataDisplayOptions.options()
				.shareKeyPressedEvents( uiModel.getKeyPressedManager() )
				.style( forwardDefaultStyle )
				.graphColorGenerator( coloringAdapter );

		// Navigation
		final AutoNavigateFocusModel< DataVertex, DataEdge > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );

		// Frame
		final DataDisplayFrame< V, E > frame = new DataDisplayFrame< V, E >(
				viewGraph,
				dataModel.getFeatureModel(),
				properties.getNSources(),
				layout,
				highlightModel,
				navigateFocusModel,
				selectionModel,
				navigationHandler,
				groupHandle,
				contextChooser,
				options );
		setFrame( frame );

		// Panel
		this.panel = frame.getDataDisplayPanel();
		if ( contextListener != null )
			contextListener.setContextListener( panel );

		// Style listener
		final UpdateListener styleUpdateListener = panel::repaint;
		forwardDefaultStyle.updateListeners().add( styleUpdateListener );
		onClose( () -> forwardDefaultStyle.updateListeners().remove( styleUpdateListener ) );

		// Listen to vertex labels being changed.
		final PropertyChangeListener< V > labelListener = vertex -> panel.entitiesAttributesChanged();
		properties.addVertexLabelListener( labelListener );
		onClose( () -> properties.removeVertexLabelListener( labelListener ) );

		// Add a listener to detect changes in the graph and trigger a re-plot of the Grapher View using the current screen transform.
		final GraphChangeListener graphChangeListener = () -> frame.plot( true );
		dataModel.getGraph().addGraphChangeListener( graphChangeListener );
		onClose( () -> dataModel.getGraph().removeGraphChangeListener( graphChangeListener ) );

		// Actions modifying the model -> need an undo point marker.
		if ( dataModel instanceof UndoPointMarker )
		{
			final UndoPointMarker undo = ( UndoPointMarker ) dataModel;
			HighlightBehaviours.install( viewBehaviours, viewGraph, viewGraph.getLock(), viewGraph, highlightModel, undo );
			EditTagActions.install(
					viewActions,
					frame.getKeybindings(),
					frame.getTriggerbindings(),
					dataModel.getTagSetModel(),
					dataModel.getSelectionModel(),
					viewGraph.getLock(),
					panel,
					panel.getDisplay(),
					undo );
		}

		// Read only actions.
		MastodonFrameViewActions.install( viewActions, () -> frame );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		DataDisplayZoom.install( viewBehaviours, panel );
		FreeformSelectionBehaviour.install( viewBehaviours, layout, viewGraph, selectionModel, focusModel, panel );
		ExportViewActions.install( viewActions, panel.getDisplay(), frame, frame.getTitle() );

		// Search box.
		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, panel );
		searchPanel.setAlignmentY( Component.CENTER_ALIGNMENT );
		frame.getSettingsPanel().add( searchPanel );

		// Navigation.
		panel.getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		panel.getNavigationBehaviours().install( viewBehaviours );
		panel.getTransformEventHandler().install( viewBehaviours );

		// Command finder
		final CommandFinder cf = CommandFinder.build()
				.context( uiModel.getContext() )
				.inputTriggerConfig( uiModel.getKeymap().getConfig() )
				.keyConfigContexts( keyConfigContexts )
				.descriptionProvider( uiModel.getViewFactories().getCommandDescriptions() )
				.register( viewActions )
				.register( uiModel.getModelActions() )
				.register( uiModel.getProjectActions() )
				.register( uiModel.getPlugins().getPluginActions() )
				.modificationListeners( uiModel.getKeymap().updateListeners() )
				.parent( frame )
				.installOn( viewActions );
		cf.getDialog().setTitle( cf.getDialog().getTitle() + " - " + frame.getTitle() );

		// Menus and color registration.
		final JMenuHandle coloringMenuHandle = new JMenuHandle();
		final JMenuHandle colorbarMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();

		final ViewMenu2 viewMenu = new ViewMenu2( this, uiModel.getKeymap(), keyConfigContexts );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();
		uiModel.getViewFactories().addWindowMenuTo( viewMenu, actionMap );
		MainWindow.addMenus( viewMenu, actionMap );
		MamutMenuBuilder2.build( viewMenu, actionMap,
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
		uiModel.getPlugins().addMenus( viewMenu );

		coloringModel = registerColoring( coloringAdapter, coloringMenuHandle,
				() -> panel.entitiesAttributesChanged() );
		colorBarOverlay = new ColorBarOverlay( coloringModel, panel::getBackground );
		final OffsetAxes offset = panel.getOffsetAxes();
		offset.listeners().add( ( w, h ) -> colorBarOverlay.setInsets( 15, w + 15, h + 15, 15 ) );
		registerColorbarOverlay( colorBarOverlay, colorbarMenuHandle, () -> panel.repaint() );
		panel.getDisplay().overlays().add( colorBarOverlay );

		// Listen to user changing the tag-set menu.
		registerTagSetMenu( tagSetMenuHandle, () -> panel.entitiesAttributesChanged() );

		// Default feature config.
		initFeatureConfig( GrapherGuiState.getDefaultFeatureGraphConfig() );

		// End
		panel.repaint();
		panel.getDisplay().requestFocusInWindow();
	}


	void initFeatureConfig( final FeatureGraphConfig featureGraphConfig )
	{
		@SuppressWarnings( "unchecked" )
		final DataDisplayFrame< V, E > ddf = ( ( DataDisplayFrame< V, E > ) frame );
		ddf.getVertexSidePanel().setGraphConfig( featureGraphConfig );
		panel.graphChanged();
	}

	void layout()
	{
		layout.layout();
		panel.repaint();
		panel.getDisplay().requestFocusInWindow();
	}

	@Override
	public ContextChooser< V > getContextChooser()
	{
		return contextChooser;
	}

	@Override
	public ColorBarOverlay getColorBarOverlay()
	{
		return colorBarOverlay;
	}

	@Override
	public ColoringModel getColoringModel()
	{
		return coloringModel;
	}
}
