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
package org.mastodon.mamut.views.trackscheme;

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
import javax.swing.SwingUtilities;

import org.mastodon.adapter.FadingModelAdapter;
import org.mastodon.app.AppModel;
import org.mastodon.app.ui.AbstractMastodonFrameView2;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.UIModel;
import org.mastodon.app.ui.ViewMenu2;
import org.mastodon.app.ui.ViewMenuBuilder2.JMenuHandle;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.MamutMenuBuilder2;
import org.mastodon.mamut.UndoActions;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.DefaultRootsModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.RootsModel;
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
import org.mastodon.views.TimepointAndNumberOfSpotsPanel;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.trackscheme.TrackSchemeContextListener;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.views.trackscheme.display.ShowSelectedTracksActions;
import org.mastodon.views.trackscheme.display.ToggleLinkBehaviour;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;
import org.mastodon.views.trackscheme.display.TrackSchemeOptions;
import org.mastodon.views.trackscheme.display.TrackSchemeZoom;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyle;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;

/**
 * A generic Mastodon view that displays the graph in the specified model in a
 * TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <M>
 *            the type of model used in the application.
 * @param <G>
 *            the type of graph used in the model. Must be listenable.
 * @param <V>
 *            the type of vertex in the graph.
 * @param <E>
 *            the type of edge in the graph.
 */
public class MastodonViewTrackScheme2<
		M extends MastodonModel< G, V, E >,
		G extends ListenableReadOnlyGraph< V, E >,
		V extends Vertex<  E >,
		E extends Edge< V > >
		extends AbstractMastodonFrameView2< M, TrackSchemeGraph< V, E >, V, E, TrackSchemeVertex, TrackSchemeEdge >
		implements HasContextChooser< V >, HasColorBarOverlay, HasColoringModel
{

	private final ContextChooser< V > contextChooser;

	/**
	 * A reference on a supervising instance of the {@code ColoringModel} that
	 * is bound to this instance/window
	 */
	private final ColoringModel coloringModel;

	private final ColorBarOverlay colorBarOverlay;

	public MastodonViewTrackScheme2(
			final AppModel< M, G, V, E, ? > appModel,
			final ModelGraphProperties< V, E > modelGraphProperties )
	{
		this(
				appModel.dataModel(),
				appModel.uiModel(),
				modelGraphProperties,
				appModel.getTimepointMin(),
				appModel.getTimepointMax());
	}

	public MastodonViewTrackScheme2(
			final M dataModel,
			final UIModel< ? > uiModel,
			final ModelGraphProperties< V, E > modelGraphProperties )
	{
		this( dataModel, uiModel, modelGraphProperties,
				minTimepoint( dataModel.getGraph().vertices(), modelGraphProperties ),
				maxTimepoint( dataModel.getGraph().vertices(), modelGraphProperties ) );
	}

	public MastodonViewTrackScheme2(
			final M dataModel,
			final UIModel< ? > uiModel,
			final ModelGraphProperties< V, E > modelGraphProperties,
			final int timepointMin,
			final int timepointMax )
	{
		super( dataModel, uiModel,
				new TrackSchemeGraph<>(
						dataModel.getGraph(),
						dataModel.getGraphIdBimap(),
						modelGraphProperties ),
				new String[] { KeyConfigContexts.TRACKSCHEME } );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< V > contextListener = new TrackSchemeContextListener<>( viewGraph );
		this.contextChooser = new ContextChooser<>( contextListener );

		/*
		 * Show TrackSchemeFrame
		 */
		final TrackSchemeStyleManager trackSchemeStyleManager = uiModel.getInstance( TrackSchemeStyleManager.class );
		final TrackSchemeStyle forwardDefaultStyle = trackSchemeStyleManager.getForwardDefaultStyle();
		final KeyPressedManager keyPressedManager = uiModel.getKeyPressedManager();
		final TrackSchemeOptions options = TrackSchemeOptions.options()
				.shareKeyPressedEvents( keyPressedManager )
				.style( forwardDefaultStyle )
				.graphColorGenerator( coloringAdapter );

		final AutoNavigateFocusModel< TrackSchemeVertex, TrackSchemeEdge > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler, timepointModel );

		final RootsModel< TrackSchemeVertex > rootsModel = new DefaultRootsModel<>( dataModel.getGraph(), viewGraph );
		onClose( () -> rootsModel.close() );

		final FadingModelAdapter< V, E, TrackSchemeVertex, TrackSchemeEdge > fadingModelAdapter = new FadingModelAdapter<>( null, viewGraph.getVertexMap(), viewGraph.getEdgeMap() );

		final TrackSchemeFrame frame = new TrackSchemeFrame(
				viewGraph,
				highlightModel,
				navigateFocusModel,
				timepointModel,
				fadingModelAdapter,
				selectionModel,
				rootsModel,
				navigationHandler,
				groupHandle,
				contextChooser,
				options );

		frame.getTrackschemePanel().setTimepointRange( timepointMin, timepointMax );
		frame.getTrackschemePanel().graphChanged();
		contextListener.setContextListener( frame.getTrackschemePanel() );

		final TrackSchemeStyle.UpdateListener updateListener = () -> frame.getTrackschemePanel().repaint();
		forwardDefaultStyle.updateListeners().add( updateListener );
		onClose( () -> forwardDefaultStyle.updateListeners().remove( updateListener ) );

		setFrame( frame );

		// Read only actions.
		MastodonFrameViewActions.install( viewActions, this );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		ShowSelectedTracksActions.install( viewActions, viewGraph, selectionModel, rootsModel, frame.getTrackschemePanel() );
		ExportViewActions.install( viewActions, frame.getTrackschemePanel().getDisplay(), frame, "TrackScheme" );
		TrackSchemeZoom.install( viewBehaviours, frame.getTrackschemePanel() );

		// Actions modifying the model -> need an undo point marker.
		if ( dataModel instanceof UndoPointMarker )
		{
			final UndoPointMarker undo = ( UndoPointMarker ) dataModel;
			HighlightBehaviours.install( viewBehaviours, viewGraph, viewGraph.getLock(), viewGraph, highlightModel, undo );
			ToggleLinkBehaviour.install( viewBehaviours, frame.getTrackschemePanel(), viewGraph, viewGraph.getLock(), viewGraph, undo );
			EditFocusVertexLabelAction.install( viewActions, frame.getTrackschemePanel(), focusModel, undo );
			EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), dataModel.getTagSetModel(),
					dataModel.getSelectionModel(), viewGraph.getLock(), frame.getTrackschemePanel(),
					frame.getTrackschemePanel().getDisplay(), undo );
		}

		// Timepoint and number of spots.
		final TimepointAndNumberOfSpotsPanel timepointAndNumberOfSpotsPanel = new TimepointAndNumberOfSpotsPanel( timepointModel, dataModel.getSpatioTemporalIndex() );
		timepointAndNumberOfSpotsPanel.setAlignmentY( Component.CENTER_ALIGNMENT );
		frame.getSettingsPanel().add( timepointAndNumberOfSpotsPanel );

		// Search box.
		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, frame.getTrackschemePanel() );
		searchPanel.setAlignmentY( Component.CENTER_ALIGNMENT );
		frame.getSettingsPanel().add( searchPanel );

		// TODO Let the user choose between the two selection/focus modes.
		frame.getTrackschemePanel().getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		frame.getTrackschemePanel().getNavigationBehaviours().install( viewBehaviours );
		frame.getTrackschemePanel().getTransformEventHandler().install( viewBehaviours );

		// Command finder.
		final Context context = uiModel.getContext();
		if ( context != null )
		{
			final CommandFinder cf = CommandFinder.build()
					.context( context )
					.inputTriggerConfig( uiModel.getKeymap().getConfig() )
					.keyConfigContexts( keyConfigContexts )
					.descriptionProvider( uiModel.getCommandDescriptions() )
					.register( viewActions )
					.register( uiModel.getModelActions() )
					.register( uiModel.getProjectActions() )
					.register( uiModel.getPlugins().getPluginActions() )
					.modificationListeners( uiModel.getKeymap().updateListeners() )
					.parent( frame )
					.installOn( viewActions );
			cf.getDialog().setTitle( cf.getDialog().getTitle() + " - " + frame.getTitle() );
		}


		final JMenuHandle coloringMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		final JMenuHandle colorbarMenuHandle = new JMenuHandle();

		final ViewMenu2 menu = new ViewMenu2( this, uiModel.getKeymap(), keyConfigContexts );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();
		uiModel.getViewFactories().addWindowMenuTo( menu, actionMap );
		MamutMenuBuilder2.build( menu, actionMap,
				fileMenu(
						separator(),
						item( ExportViewActions.EXPORT_VIEW_TO_SVG ),
						item( ExportViewActions.EXPORT_VIEW_TO_PNG ) ),
				viewMenu(
						colorMenu( coloringMenuHandle ),
						colorbarMenu( colorbarMenuHandle ),
						separator(),
						item( ShowSelectedTracksActions.SHOW_TRACK_DOWNWARD ),
						item( ShowSelectedTracksActions.SHOW_SELECTED_TRACKS ),
						item( ShowSelectedTracksActions.SHOW_ALL_TRACKS ),
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
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_CHILD ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_PARENT ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_LEFT ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_RIGHT ),
						separator(),
						item( TrackSchemeNavigationActions.NAVIGATE_CHILD ),
						item( TrackSchemeNavigationActions.NAVIGATE_PARENT ),
						item( TrackSchemeNavigationActions.NAVIGATE_LEFT ),
						item( TrackSchemeNavigationActions.NAVIGATE_RIGHT ),
						separator(),
						item( EditFocusVertexLabelAction.EDIT_FOCUS_LABEL ),
						tagSetMenu( tagSetMenuHandle ) ) );
		uiModel.getPlugins().addMenus( menu );

		coloringModel = registerColoring( coloringAdapter, coloringMenuHandle,
				() -> frame.getTrackschemePanel().entitiesAttributesChanged() );
		colorBarOverlay = new ColorBarOverlay( coloringModel, () -> frame.getTrackschemePanel().getBackground() );
		frame.getTrackschemePanel().getOffsetHeaders().listeners()
				.add( ( w, h ) -> colorBarOverlay.setInsets( h + 15, w + 15, 15, 15 ) );
		registerColorbarOverlay( colorBarOverlay, colorbarMenuHandle, () -> frame.getTrackschemePanel().repaint() );

		// Listen to user changing the tag-set menu.
		registerTagSetMenu( tagSetMenuHandle, () -> frame.getTrackschemePanel().entitiesAttributesChanged() );

		// Listen to vertex labels being changed.
		modelGraphProperties.addVertexLabelListener( v -> SwingUtilities.invokeLater( () -> frame.getTrackschemePanel().entitiesAttributesChanged() ) );

		frame.getTrackschemePanel().getDisplay().overlays().add( colorBarOverlay );

		frame.getTrackschemePanel().repaint();

		// Give focus to the display so that it can receive key presses
		// immediately.
		frame.getTrackschemePanel().getDisplay().requestFocusInWindow();
	}

	@Override
	public TrackSchemeFrame getFrame()
	{
		return ( TrackSchemeFrame ) super.getFrame();
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

	private static < V > int minTimepoint( final RefCollection< V > vertices, final ModelGraphProperties< V, ? > modelGraphProperties )
	{
		int min = Integer.MAX_VALUE;
		for ( final V v : vertices )
		{
			final int t = modelGraphProperties.getTimepoint( v );
			if ( t < min )
				min = t;
		}
		if ( min == Integer.MAX_VALUE )
			min = 0;
		return min;
	}

	private static < V > int maxTimepoint( final RefCollection< V > vertices, final ModelGraphProperties< V, ? > modelGraphProperties )
	{
		int max = Integer.MIN_VALUE;
		for ( final V v : vertices )
		{
			final int t = modelGraphProperties.getTimepoint( v );
			if ( t > max )
				max = t;
		}
		if ( max == Integer.MIN_VALUE )
			max = 10;
		return max;
	}
}
