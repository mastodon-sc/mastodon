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
package org.mastodon.views.bdv;

import static org.mastodon.app.MastodonIcons.BDV_VIEW_ICON;
import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.colorMenu;
import static org.mastodon.mamut.MamutMenuBuilder.colorbarMenu;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.TimepointAndNumberOfSpotsPanel;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.UndoActions;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.app.WindowManager;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.ExportViewActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.HasColorBarOverlay;
import org.mastodon.ui.coloring.HasColoringModel;
import org.mastodon.ui.commandfinder.CommandFinder;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.AbstractMastodonFrameView;
import org.mastodon.views.bdv.display.BdvContextProvider;
import org.mastodon.views.bdv.display.BigDataViewerActionsMamut;
import org.mastodon.views.bdv.display.BigDataViewerBehavioursMamut;
import org.mastodon.views.bdv.display.BigDataViewerMamut;
import org.mastodon.views.bdv.display.ViewerFrameMamut;
import org.mastodon.views.bdv.export.RecordMaxProjectionMovieDialog;
import org.mastodon.views.bdv.export.RecordMovieDialog;
import org.mastodon.views.bdv.overlay.BdvHighlightHandler;
import org.mastodon.views.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.views.bdv.overlay.EditBehaviours;
import org.mastodon.views.bdv.overlay.EditSpecialBehaviours;
import org.mastodon.views.bdv.overlay.OverlayActions;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.bdv.overlay.OverlayNavigation;
import org.mastodon.views.bdv.overlay.RenderSettings;
import org.mastodon.views.bdv.overlay.RenderSettings.UpdateListener;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsManager;
import org.mastodon.views.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.views.bdv.overlay.wrap.BdvOverlayProperties;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.views.context.ContextProvider;
import org.mastodon.views.context.HasContextProvider;

import bdv.BigDataViewerActions;
import bdv.util.Affine3DHelpers;
import bdv.viewer.NavigationActions;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;

public class MastodonViewBdv< 
			M extends MastodonModel< G, V, E >, 
			G extends ListenableReadOnlyGraph< V, E >, 
			V extends Vertex< E >, 
			E extends Edge< V > >
		extends AbstractMastodonFrameView< M, OverlayGraphWrapper< V, E >, V, E, OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
		implements HasContextProvider< V >, HasColoringModel, HasColorBarOverlay
{

	// TODO
	private static int bdvName = 1;

	private final SharedBigDataViewerData sharedBdvData;

	private final BdvContextProvider< V, E > contextProvider;

	private final ViewerPanel viewer;

	/**
	 * A reference on a supervising instance of the {@code ColoringModel} that
	 * is bound to this instance/window.
	 */
	private final ColoringModel coloringModel;

	private final ColorBarOverlay colorBarOverlay;

	public MastodonViewBdv(
			final M dataModel,
			final WindowManager< ? > windowManager,
			final SharedBigDataViewerData imageData,
			final BdvOverlayProperties< V, E > properties )
	{
		super( dataModel, windowManager,
				new OverlayGraphWrapper< V, E >(
						dataModel.getGraph(),
						dataModel.getGraphIdBimap(),
						dataModel.getSpatioTemporalIndex(),
						properties.getLock(),
						properties ),
				new String[] { KeyConfigContexts.BIGDATAVIEWER } );

		sharedBdvData = imageData;

		final String windowTitle = "BigDataViewer " + ( bdvName++ );
		final BigDataViewerMamut bdv = new BigDataViewerMamut( sharedBdvData, windowTitle, groupHandle );
		final ViewerFrameMamut frame = bdv.getViewerFrame();
		setFrame( frame );
		frame.setIconImages( BDV_VIEW_ICON );

		/*
		 * We have to build the coloring menu handles now. But the other actions
		 * need to be included in the menus later, after they have been
		 * installed (otherwise they won't be active). To keep the future menu
		 * order, we build an empty menu but already with all sub-menus in
		 * order.
		 */
		final JMenuHandle menuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		final JMenuHandle colorbarMenuHandle = new JMenuHandle();
		final ViewMenu menu = new ViewMenu( this, windowManager.getKeymap(), keyConfigContexts );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(),
				viewMenu(
						colorMenu( menuHandle ),
						colorbarMenu( colorbarMenuHandle ) ),
				editMenu() );

		// The view panel.
		viewer = bdv.getViewer();

		// we need the coloring now.
		final GraphColorGeneratorAdapter< V, E, OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > coloring =
						new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );
		coloringModel = registerColoring( coloring, menuHandle, () -> viewer.getDisplay().repaint() );
		colorBarOverlay = new ColorBarOverlay( coloringModel, () -> viewer.getBackground() );
		registerColorbarOverlay( colorBarOverlay, colorbarMenuHandle, () -> viewer.getDisplay().repaint() );

		final OverlayGraphRenderer< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > tracksOverlay = createRenderer(
						viewGraph,
						highlightModel,
						focusModel,
						selectionModel,
						coloring );

		viewer.getDisplay().overlays().add( colorBarOverlay );
		viewer.getDisplay().overlays().add( tracksOverlay );
		viewer.renderTransformListeners().add( tracksOverlay );
		viewer.timePointListeners().add( tracksOverlay );

		final G modelGraph = dataModel.getGraph();

		highlightModel.listeners().add( () -> viewer.getDisplay().repaint() );
		focusModel.listeners().add( () -> viewer.getDisplay().repaint() );
		modelGraph.addGraphChangeListener( () -> viewer.getDisplay().repaint() );
		properties.addVertexPositionListener( v -> viewer.getDisplay().repaint() );
		properties.addVertexLabelListener( v -> viewer.getDisplay().repaint() );
		selectionModel.listeners().add( () -> viewer.getDisplay().repaint() );

		final OverlayNavigation< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > overlayNavigation = new OverlayNavigation<>( viewer, viewGraph );
		navigationHandler.listeners().add( overlayNavigation );

		final BdvHighlightHandler< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > highlightHandler = new BdvHighlightHandler<>( viewGraph, tracksOverlay, highlightModel );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.renderTransformListeners().add( highlightHandler );

		contextProvider = new BdvContextProvider<>( windowTitle, viewGraph, tracksOverlay );
		viewer.renderTransformListeners().add( contextProvider );

		final AutoNavigateFocusModel< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );

		// Read-only actions.
		MastodonFrameViewActions.install( viewActions, this );
		BigDataViewerActionsMamut.install( viewActions, bdv );
		BigDataViewerBehavioursMamut.install( viewBehaviours, bdv );
		BdvSelectionBehaviours.install( viewBehaviours, viewGraph, tracksOverlay, selectionModel, focusModel, navigationHandler );

		// Actions modifying the model -> need an undo point marker.
		if ( dataModel instanceof UndoPointMarker )
		{
			final UndoPointMarker undo = ( UndoPointMarker ) dataModel;
			EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), dataModel.getTagSetModel(), dataModel.getSelectionModel(), viewGraph.getLock(), bdv.getViewer(), bdv.getViewer().getDisplay(), undo );
			EditBehaviours.install( viewBehaviours, viewer, viewGraph, tracksOverlay, focusModel, undo, getMinRadius( sharedBdvData ) );
			EditSpecialBehaviours.install( viewBehaviours, frame.getViewerPanel(), viewGraph, tracksOverlay, selectionModel, focusModel, undo );
			HighlightBehaviours.install( viewBehaviours, viewGraph, viewGraph.getLock(), viewGraph, highlightModel, undo );
		}

		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		OverlayActions.install( viewActions, viewer, tracksOverlay );
		final Runnable onCloseDialog = RecordMovieDialog.install( viewActions, bdv, tracksOverlay, colorBarOverlay, windowManager.getKeymap() );
		onClose( onCloseDialog );
		final Runnable onCloseMIPDialog = RecordMaxProjectionMovieDialog.install( viewActions, bdv, tracksOverlay, colorBarOverlay, windowManager.getKeymap() );
		onClose( onCloseMIPDialog );

		// Add the timepoint and number of spots panel.
		final TimepointAndNumberOfSpotsPanel timepointAndNumberOfSpotsPanel = new TimepointAndNumberOfSpotsPanel( this.timepointModel, dataModel.getSpatioTemporalIndex() );
		frame.getSettingsPanel().add( timepointAndNumberOfSpotsPanel );

		/*
		 * We must make a search action using the underlying model graph,
		 * because we cannot iterate over the OverlayGraphWrapper properly
		 * (vertices are object vertices that wrap a pool vertex...)
		 */
		final JPanel searchField = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, viewer );
		frame.getSettingsPanel().add( searchField );

		NavigationActions.install( viewActions, viewer, sharedBdvData.is2D() );
		viewer.getTransformEventHandler().install( viewBehaviours );

		viewer.timePointListeners().add( timePointIndex -> timepointModel.setTimepoint( timePointIndex ) );
		timepointModel.listeners().add( () -> viewer.setTimepoint( timepointModel.getTimepoint() ) );

		ExportViewActions.install( viewActions, frame.getViewerPanel().getDisplayComponent(), frame, "BDV" );

		final RenderSettingsManager renderSettingsManager = windowManager.getInstance( RenderSettingsManager.class );
		final RenderSettings renderSettings = renderSettingsManager.getForwardDefaultStyle();
		tracksOverlay.setRenderSettings( renderSettings );
		final UpdateListener updateListener = () -> {
			viewer.repaint();
			contextProvider.notifyContextChanged();
		};
		renderSettings.updateListeners().add( updateListener );
		onClose( () -> renderSettings.updateListeners().remove( updateListener ) );

		// Give focus to display so that it can receive key-presses immediately.
		viewer.getDisplay().requestFocusInWindow();

		// Notifies context provider that context changes when visibility mode changes.
		tracksOverlay.getVisibilities().getVisibilityListeners().add( contextProvider::notifyContextChanged );

		// Command finder.
		final CommandFinder cf = CommandFinder.build()
				.context( windowManager.getContext() )
				.inputTriggerConfig( windowManager.getKeymap().getConfig() )
				.keyConfigContexts( keyConfigContexts )
				.descriptionProvider( windowManager.getViewFactories().getCommandDescriptions() )
				.register( viewActions )
				.register( windowManager.getModelActions() )
				.register( windowManager.getProjectActions() )
				.register( windowManager.getPlugins().getPluginActions() )
				.modificationListeners( windowManager.getKeymap().updateListeners() )
				.parent( frame )
				.installOn( viewActions );
		cf.getDialog().setTitle( cf.getDialog().getTitle() + " - " + frame.getTitle() );

		MainWindow.addMenus( menu, actionMap );
		windowManager.getViewFactories().addWindowMenuTo( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						separator(),
						item( BigDataViewerActions.LOAD_SETTINGS ),
						item( BigDataViewerActions.SAVE_SETTINGS ),
						separator(),
						item( RecordMovieDialog.RECORD_MOVIE_DIALOG ),
						item( RecordMaxProjectionMovieDialog.RECORD_MIP_MOVIE_DIALOG ),
						separator(),
						item( ExportViewActions.EXPORT_VIEW_TO_SVG ),
						item( ExportViewActions.EXPORT_VIEW_TO_PNG ) ),
				viewMenu(
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
		windowManager.getPlugins().addMenus( menu );

		registerTagSetMenu( tagSetMenuHandle, () -> viewer.getDisplay().repaint() );
	}

	protected OverlayGraphRenderer< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
			createRenderer(
					final OverlayGraphWrapper< V, E > viewGraph,
					final HighlightModel< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > highlightModel,
					final FocusModel< OverlayVertexWrapper< V, E > > focusModel,
					final SelectionModel< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > selectionModel,
					final GraphColorGenerator< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > coloring )
	{
		return new OverlayGraphRenderer< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >(
				viewGraph,
				highlightModel,
				focusModel,
				selectionModel,
				coloring );
	}

	@Override
	public ContextProvider< V > getContextProvider()
	{
		return contextProvider;
	}

	public ViewerPanel getViewerPanelMamut()
	{
		return viewer;
	}

	void requestRepaint()
	{
		viewer.requestRepaint();
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

	/**
	 * Determine min radius to pass to the edit actions, so that we cannot
	 * create spots that have a radius smaller than 0.5 pixels in the worst
	 * case.
	 */
	private static final double getMinRadius( final SharedBigDataViewerData sharedBdvData )
	{
		double minRadius = 0.5;
		final int level = 0;
		for ( final SourceAndConverter< ? > sac : sharedBdvData.getSources() )
		{
			for ( int t = 0; t < sharedBdvData.getNumTimepoints(); t++ )
			{
				final Source< ? > source = sac.getSpimSource();
				if ( source.isPresent( t ) )
				{
					final AffineTransform3D transform = new AffineTransform3D();
					source.getSourceTransform( t, level, transform );
					for ( int d = 0; d < 3; d++ )
					{
						final double l = Affine3DHelpers.extractScale( transform, d ) / 2.;
						if ( l < minRadius )
							minRadius = l;
					}
					break;
				}
			}
		}
		return minRadius;
	}
}
