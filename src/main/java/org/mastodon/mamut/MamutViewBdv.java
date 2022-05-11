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
package org.mastodon.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.colorMenu;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;
import static org.mastodon.mamut.MamutViewStateSerialization.BDV_STATE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.BDV_TRANSFORM_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.NO_COLORING_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.TAG_SET_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.jdom2.Element;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.ModelOverlayProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.bdv.BdvContextProvider;
import org.mastodon.views.bdv.BigDataViewerActionsMamut;
import org.mastodon.views.bdv.BigDataViewerMamut;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.ViewerFrameMamut;
import org.mastodon.views.bdv.overlay.BdvHighlightHandler;
import org.mastodon.views.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.views.bdv.overlay.EditBehaviours;
import org.mastodon.views.bdv.overlay.EditSpecialBehaviours;
import org.mastodon.views.bdv.overlay.OverlayActions;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.bdv.overlay.OverlayNavigation;
import org.mastodon.views.bdv.overlay.RenderSettings;
import org.mastodon.views.bdv.overlay.RenderSettings.UpdateListener;
import org.mastodon.views.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.views.context.ContextProvider;

import bdv.BigDataViewerActions;
import bdv.tools.InitializeViewerState;
import bdv.util.Affine3DHelpers;
import bdv.viewer.NavigationActions;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;

public class MamutViewBdv extends MamutView< OverlayGraphWrapper< Spot, Link >, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > >
{
	// TODO
	private static int bdvName = 1;

	private final SharedBigDataViewerData sharedBdvData;

	private final BdvContextProvider< Spot, Link > contextProvider;

	private final ViewerPanel viewer;

	/**
	 * A reference on a supervising instance of the {@code ColoringModel} that
	 * is bound to this instance/window.
	 */
	private final ColoringModel< Spot, Link, BranchSpot, BranchLink > coloringModel;

	public MamutViewBdv( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewBdv( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel,
				new OverlayGraphWrapper<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getSpatioTemporalIndex(),
						appModel.getModel().getGraph().getLock(),
						new ModelOverlayProperties( appModel.getModel().getGraph(), appModel.getRadiusStats() ) ),
				new String[] { KeyConfigContexts.BIGDATAVIEWER } );

		sharedBdvData = appModel.getSharedBdvData();

		final String windowTitle = "BigDataViewer " + ( bdvName++ ); // TODO:
																		// use
																		// JY
																		// naming
																		// scheme
		final BigDataViewerMamut bdv = new BigDataViewerMamut( sharedBdvData, windowTitle, groupHandle );
		final ViewerFrameMamut frame = bdv.getViewerFrame();
		setFrame( frame );

		// Restore position.
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos )
			frame.setBounds( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] );
		else
			frame.setLocationRelativeTo( null );

		// Restore group handle.
		final Integer groupID = ( Integer ) guiState.get( GROUP_HANDLE_ID_KEY );
		if ( null != groupID )
			groupHandle.setGroupId( groupID.intValue() );

		// Restore settings panel visibility.
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );

		MastodonFrameViewActions.install( viewActions, this );
		BigDataViewerActionsMamut.install( viewActions, bdv );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final JMenuHandle menuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						separator(),
						item( BigDataViewerActions.LOAD_SETTINGS ),
						item( BigDataViewerActions.SAVE_SETTINGS ) ),
				viewMenu(
						colorMenu( menuHandle ),
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
						tagSetMenu( tagSetMenuHandle ) ),
				ViewMenuBuilder.menu( "Settings",
						item( BigDataViewerActions.BRIGHTNESS_SETTINGS ),
						item( BigDataViewerActions.VISIBILITY_AND_GROUPING ) ) );
		appModel.getPlugins().addMenus( menu );

		viewer = bdv.getViewer();

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

		final GraphColorGeneratorAdapter< Spot, Link, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > coloring =
				new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );

		final OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > tracksOverlay = createRenderer(
				viewGraph,
				highlightModel,
				focusModel,
				selectionModel,
				coloring );

		viewer.getDisplay().overlays().add( tracksOverlay );
		viewer.renderTransformListeners().add( tracksOverlay );
		viewer.addTimePointListener( tracksOverlay );

		final Model model = appModel.getModel();
		final ModelGraph modelGraph = model.getGraph();

		coloringModel = registerColoring( coloring, menuHandle,
				() -> viewer.getDisplay().repaint() );

		registerTagSetMenu( tagSetMenuHandle,
				() -> viewer.getDisplay().repaint() );

		// Restore coloring.
		final Boolean noColoring = ( Boolean ) guiState.get( NO_COLORING_KEY );
		if ( null != noColoring && noColoring )
		{
			coloringModel.colorByNone();
		}
		else
		{
			final String tagSetName = ( String ) guiState.get( TAG_SET_KEY );
			final String featureColorModeName = ( String ) guiState.get( FEATURE_COLOR_MODE_KEY );
			if ( null != tagSetName )
			{
				for ( final TagSet tagSet : coloringModel.getTagSetStructure().getTagSets() )
				{
					if ( tagSet.getName().equals( tagSetName ) )
					{
						coloringModel.colorByTagSet( tagSet );
						break;
					}
				}
			}
			else if ( null != featureColorModeName )
			{
				final List< FeatureColorMode > featureColorModes = new ArrayList<>();
				featureColorModes.addAll( coloringModel.getFeatureColorModeManager().getBuiltinStyles() );
				featureColorModes.addAll( coloringModel.getFeatureColorModeManager().getUserStyles() );
				for ( final FeatureColorMode featureColorMode : featureColorModes )
				{
					if ( featureColorMode.getName().equals( featureColorModeName ) )
					{
						coloringModel.colorByFeature( featureColorMode );
						break;
					}
				}
			}
		}

		highlightModel.listeners().add( () -> viewer.getDisplay().repaint() );
		focusModel.listeners().add( () -> viewer.getDisplay().repaint() );
		modelGraph.addGraphChangeListener( () -> viewer.getDisplay().repaint() );
		modelGraph.addVertexPositionListener( v -> viewer.getDisplay().repaint() );
		modelGraph.addVertexLabelListener( v -> viewer.getDisplay().repaint() );
		selectionModel.listeners().add( () -> viewer.getDisplay().repaint() );

		final OverlayNavigation< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > overlayNavigation = new OverlayNavigation<>( viewer, viewGraph );
		navigationHandler.listeners().add( overlayNavigation );

		final BdvHighlightHandler< ?, ? > highlightHandler = new BdvHighlightHandler<>( viewGraph, tracksOverlay, highlightModel );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.renderTransformListeners().add( highlightHandler );

		contextProvider = new BdvContextProvider<>( windowTitle, viewGraph, tracksOverlay );
		viewer.renderTransformListeners().add( contextProvider );

		final AutoNavigateFocusModel< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );

		BdvSelectionBehaviours.install( viewBehaviours, viewGraph, tracksOverlay, selectionModel, focusModel, navigationHandler );
		EditBehaviours.install( viewBehaviours, viewGraph, tracksOverlay, selectionModel, focusModel, model, getMinRadius( sharedBdvData ) );
		EditSpecialBehaviours.install( viewBehaviours, frame.getViewerPanel(), viewGraph, tracksOverlay, selectionModel, focusModel, model );
		HighlightBehaviours.install( viewBehaviours, viewGraph, viewGraph.getLock(), viewGraph, highlightModel, model );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
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

		NavigationActions.install( viewActions, viewer, sharedBdvData.is2D() );
		viewer.getTransformEventHandler().install( viewBehaviours );

		viewer.addTimePointListener( timePointIndex -> timepointModel.setTimepoint( timePointIndex ) );
		timepointModel.listeners().add( () -> viewer.setTimepoint( timepointModel.getTimepoint() ) );

		final RenderSettings renderSettings = appModel.getRenderSettingsManager().getForwardDefaultStyle();
		tracksOverlay.setRenderSettings( renderSettings );
		final UpdateListener updateListener = () -> {
			viewer.repaint();
			contextProvider.notifyContextChanged();
		};
		renderSettings.updateListeners().add( updateListener );
		onClose( () -> renderSettings.updateListeners().remove( updateListener ) );

		// Give focus to display so that it can receive key-presses immediately.
		viewer.getDisplay().requestFocusInWindow();

		// Notifies context provider that context changes when visibility mode
		// changes.
		tracksOverlay.getVisibilities().getVisibilityListeners().add( contextProvider::notifyContextChanged );

		frame.setVisible( true );

//		if ( !bdv.tryLoadSettings( bdvFile ) ) // TODO
//			InitializeViewerState.initBrightness( 0.001, 0.999, bdv.getViewer(), bdv.getSetupAssignments() );
	}

	protected OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > createRenderer(
			final OverlayGraphWrapper< Spot, Link > viewGraph,
			final HighlightModel< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > highlightModel,
			final FocusModel< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > focusModel,
			final SelectionModel< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > selectionModel,
			final GraphColorGenerator< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > coloring )
	{
		return new OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > >(
				viewGraph,
				highlightModel,
				focusModel,
				selectionModel,
				coloring );
	}

	public ContextProvider< Spot > getContextProvider()
	{
		return contextProvider;
	}

	public ViewerPanel getViewerPanelMamut()
	{
		return viewer;
	}

	public void requestRepaint()
	{
		viewer.requestRepaint();
	}

	public ColoringModel< Spot, Link, BranchSpot, BranchLink > getColoringModel()
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
