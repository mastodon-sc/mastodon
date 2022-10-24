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
package org.mastodon.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.branchColorMenu;
import static org.mastodon.mamut.MamutMenuBuilder.colorbarMenu;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;
import static org.mastodon.mamut.MamutViewStateSerialization.BDV_STATE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.BDV_TRANSFORM_KEY;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.jdom2.Element;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.BranchGraphModelOverlayProperties;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.NavigationHandler;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.bdv.BigDataViewerActionsMamut;
import org.mastodon.views.bdv.BigDataViewerMamut;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.ViewerFrameMamut;
import org.mastodon.views.bdv.overlay.BdvHighlightHandler;
import org.mastodon.views.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.views.bdv.overlay.OverlayActions;
import org.mastodon.views.bdv.overlay.OverlayBranchGraphRenderer;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.bdv.overlay.OverlayNavigation;
import org.mastodon.views.bdv.overlay.RenderSettings;
import org.mastodon.views.bdv.overlay.RenderSettings.UpdateListener;
import org.mastodon.views.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayProperties;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;

import bdv.BigDataViewerActions;
import bdv.tools.InitializeViewerState;
import bdv.viewer.NavigationActions;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;

public class MamutBranchViewBdv extends MamutBranchView< 
	OverlayGraphWrapper< BranchSpot, BranchLink >,
	OverlayVertexWrapper< BranchSpot, BranchLink >, 
	OverlayEdgeWrapper< BranchSpot, BranchLink > >
{

	private static int bdvName = 1;

	private final ColoringModel coloringModel;

	private final ViewerPanel viewer;

	private final ColorBarOverlay colorBarOverlay;

	public MamutBranchViewBdv( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutBranchViewBdv( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, createViewBranchGraph( appModel ), new String[] { KeyConfigContexts.BIGDATAVIEWER } );
		
		// Image data.
		final SharedBigDataViewerData sharedBdvData = appModel.getSharedBdvData();

		// BDV.
		final String windowTitle = "BigDataViewer Branch " + ( bdvName++ );
		final BigDataViewerMamut bdv = new BigDataViewerMamut( sharedBdvData, windowTitle, groupHandle );
		final ViewerFrameMamut viewerFrame = bdv.getViewerFrame();
		setFrame( viewerFrame );
		viewer = bdv.getViewer();

		// Restore position.
		MamutView.restoreFramePosition( viewerFrame, guiState );

		// Restore group handle.
		MamutView.restoreGroupHandle( groupHandle, guiState );

		// Restore settings panel visibility.
		MamutView.restoreSettingsPanelVisibility( viewerFrame, guiState );

		// Menus
		final ViewMenu menu = new ViewMenu( frame.getJMenuBar(), appModel.getKeymap(), keyConfigContexts );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final JMenuHandle coloringMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		final JMenuHandle colorbarMenuHandle = new JMenuHandle();
		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						separator(),
						item( BigDataViewerActions.LOAD_SETTINGS ),
						item( BigDataViewerActions.SAVE_SETTINGS ) ),
				viewMenu(
						branchColorMenu( coloringMenuHandle ),
						colorbarMenu( colorbarMenuHandle ),
						separator(),
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL ) ),
				editMenu(
						item( SelectionActions.SELECT_WHOLE_TRACK ),
						item( SelectionActions.SELECT_TRACK_DOWNWARD ),
						item( SelectionActions.SELECT_TRACK_UPWARD ),
						separator(),
						tagSetMenu( tagSetMenuHandle ) ),
				ViewMenuBuilder.menu( "Settings",
						item( BigDataViewerActions.BRIGHTNESS_SETTINGS ),
						item( BigDataViewerActions.VISIBILITY_AND_GROUPING ) ) );
		appModel.getPlugins().addMenus( menu );

		// Register coloring and tag-sets.

		// Coloring.
		final GraphColorGeneratorAdapter< BranchSpot, BranchLink, OverlayVertexWrapper< BranchSpot, BranchLink >, OverlayEdgeWrapper< BranchSpot, BranchLink > > coloring =
				new GraphColorGeneratorAdapter<>( vertexMap, edgeMap );

		coloringModel = registerBranchColoring( coloring, coloringMenuHandle,
				() -> viewer.getDisplay().repaint() );
		colorBarOverlay = new ColorBarOverlay( coloringModel, () -> viewer.getBackground() );
		registerColorbarOverlay( colorBarOverlay, colorbarMenuHandle, () -> viewer.getDisplay().repaint() );

		registerTagSetMenu( tagSetMenuHandle,
				() -> viewer.getDisplay().repaint() );

		// Restore coloring.
		MamutView.restoreColoring( coloringModel, guiState );

		// Restore colorbar state.
		MamutView.restoreColorbarState( colorBarOverlay, guiState );
		viewer.getDisplay().overlays().add( colorBarOverlay );

		// Restore BDV state.
		final Element stateEl = ( Element ) guiState.get( BDV_STATE_KEY );
		if ( null != stateEl )
			viewer.stateFromXml( stateEl );

		// Restore transform.
		final AffineTransform3D tLoaded = ( AffineTransform3D ) guiState.get( BDV_TRANSFORM_KEY );
		if ( null == tLoaded )
			InitializeViewerState.initTransform( viewer );
		else
			viewer.state().setViewerTransform( tLoaded );

		// Renderer.
		final OverlayGraphRenderer< OverlayVertexWrapper< BranchSpot, BranchLink >, OverlayEdgeWrapper< BranchSpot, BranchLink > > tracksOverlay =
				new OverlayBranchGraphRenderer<>(
						viewGraph,
						highlightModel,
						focusModel,
						selectionModel,
						coloring );

		viewer.getDisplay().overlays().add( tracksOverlay );
		viewer.renderTransformListeners().add( tracksOverlay );
		viewer.addTimePointListener( tracksOverlay );

		// Core graph.
		final Model model = appModel.getModel();
		final ModelGraph graph = model.getGraph();

		// Listen to changes in models -> repaint.
		highlightModel.listeners().add( () -> viewer.getDisplay().repaint() );
		focusModel.listeners().add( () -> viewer.getDisplay().repaint() );
		selectionModel.listeners().add( () -> viewer.getDisplay().repaint() );
		graph.addGraphChangeListener( () -> viewer.getDisplay().repaint() );
		graph.addVertexPositionListener( ( v ) -> viewer.getDisplay().repaint() );

		// Forward navigation to view.
		final OverlayNavigation< OverlayVertexWrapper< BranchSpot, BranchLink >, OverlayEdgeWrapper< BranchSpot, BranchLink > > overlayNavigation =
				new OverlayNavigation<>( viewer, viewGraph );
		navigationHandler.listeners().add( overlayNavigation );

		final BdvHighlightHandler< ?, ? > highlightHandler = new BdvHighlightHandler<>( viewGraph, tracksOverlay, highlightModel );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.renderTransformListeners().add( highlightHandler );

		// Actions & Behaviors.
		MastodonFrameViewActions.install( viewActions, this );
		BigDataViewerActionsMamut.install( viewActions, bdv );
		NavigationActions.install( viewActions, viewer, sharedBdvData.is2D() );
		viewer.getTransformEventHandler().install( viewBehaviours );
		HighlightBehaviours.install( viewBehaviours, viewGraph, graph.getLock(), graph, highlightModel, model );
		FocusActions.install( viewActions, viewGraph, graph.getLock(), focusModel, selectionModel );
		BdvSelectionBehaviours.install( viewBehaviours, viewGraph, tracksOverlay, selectionModel, focusModel, navigationHandler );
		OverlayActions.install( viewActions, viewer, tracksOverlay );

		/*
		 * We must make a search action using the underlying model graph,
		 * because we cannot iterate over the OverlayGraphWrapper properly
		 * (vertices are object vertices that wrap a pool vertex...)
		 */
		final NavigationHandler< Spot, Link > navigationHandlerAdapter = groupHandle.getModel( appModel.NAVIGATION );
		final JPanel searchField = SearchVertexLabel.install(
				viewActions,
				appModel.getModel().getGraph(),
				navigationHandlerAdapter,
				appModel.getSelectionModel(),
				appModel.getFocusModel(),
				viewer );
		frame.getSettingsPanel().add( searchField );

		// Moving in the BDV.
		NavigationActions.install( viewActions, viewer, sharedBdvData.is2D() );
		viewer.getTransformEventHandler().install( viewBehaviours );

		// Time-point listener.
		viewer.addTimePointListener( timePointIndex -> timepointModel.setTimepoint( timePointIndex ) );
		timepointModel.listeners().add( () -> viewer.setTimepoint( timepointModel.getTimepoint() ) );

		// Render settings.
		final RenderSettings renderSettings = appModel.getRenderSettingsManager().getForwardDefaultStyle();
		tracksOverlay.setRenderSettings( renderSettings );
		final UpdateListener updateListener = () -> viewer.repaint();
		renderSettings.updateListeners().add( updateListener );
		onClose( () -> renderSettings.updateListeners().remove( updateListener ) );

		// Give focus to display so that it can receive key-presses immediately.
		viewer.getDisplay().requestFocusInWindow();

		viewerFrame.setVisible( true );
	}

	ColoringModel getColoringModel()
	{
		return coloringModel;
	}

	ColorBarOverlay getColorBarOverlay()
	{
		return colorBarOverlay;
	}

	ViewerPanel getViewerPanelMamut()
	{
		return viewer;
	}

	private static OverlayGraphWrapper<BranchSpot, BranchLink> createViewBranchGraph( final MamutAppModel appModel )
	{
		// Model.
		final Model model = appModel.getModel();

		// Branch graph.
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		final GraphIdBimap< BranchSpot, BranchLink > branchGraphIdBimap = model.getBranchGraphIdBimap();

		// Graph.
		final ModelGraph graph = model.getGraph();
		final BoundingSphereRadiusStatistics radiusStats = appModel.getRadiusStats();

		final OverlayProperties< BranchSpot, BranchLink > properties =
				new BranchGraphModelOverlayProperties( branchGraph, graph, radiusStats );
		final OverlayGraphWrapper< BranchSpot, BranchLink > overlayBranchGraph = new OverlayGraphWrapper<>(
				branchGraph,
				branchGraphIdBimap,
				model.getBranchGraphSpatioTemporalIndex(),
				graph.getLock(),
				properties );
		return overlayBranchGraph;
	}
}
