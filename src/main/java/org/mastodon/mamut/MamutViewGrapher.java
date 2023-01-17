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
import static org.mastodon.mamut.MamutMenuBuilder.colorMenu;
import static org.mastodon.mamut.MamutMenuBuilder.colorbarMenu;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;
import static org.mastodon.mamut.MamutViewStateSerialization.COLORBAR_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.COLORBAR_VISIBLE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.GRAPHER_TRANSFORM_KEY;
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

import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.mamut.feature.SpotFrameFeature;
import org.mastodon.mamut.feature.SpotQuickMeanIntensityFeature;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.grapher.datagraph.DataContextListener;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.DataDisplayFrame;
import org.mastodon.views.grapher.display.DataDisplayOptions;
import org.mastodon.views.grapher.display.DataDisplayPanel;
import org.mastodon.views.grapher.display.DataDisplayZoom;
import org.mastodon.views.grapher.display.FeatureGraphConfig;
import org.mastodon.views.grapher.display.FeatureGraphConfig.GraphDataItemsSource;
import org.mastodon.views.grapher.display.FeatureSpecPair;
import org.mastodon.views.grapher.display.OffsetAxes;
import org.mastodon.views.grapher.display.style.DataDisplayStyle;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;
import org.mastodon.views.trackscheme.display.ColorBarOverlay.Position;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;
import org.scijava.ui.behaviour.KeyPressedManager;

public class MamutViewGrapher extends MamutView< DataGraph< Spot, Link >, DataVertex, DataEdge >
{
	private final ContextChooser< Spot > contextChooser;

	/**
	 * a reference on the {@code GraphColorGeneratorAdapter} created and
	 * registered with this instance/window
	 */
	private final GraphColorGeneratorAdapter< Spot, Link, DataVertex, DataEdge > coloringAdapter;

	/**
	 * a reference on a supervising instance of the {@code ColoringModel} that
	 * is bound to this instance/window
	 */
	private final ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel;

	private final DataDisplayPanel< Spot, Link > dataDisplayPanel;

	private final ColorBarOverlay colorbarOverlay;

	public MamutViewGrapher( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewGrapher( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel,
				new DataGraph< Spot, Link >(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getGraph().getLock() ),
				new String[] { KeyConfigContexts.GRAPHER } );

		final KeyPressedManager keyPressedManager = appModel.getKeyPressedManager();
		final Model model = appModel.getModel();

		/*
		 * The layout.
		 */
		final DataGraphLayout< Spot, Link > layout = new DataGraphLayout<>( viewGraph, selectionModel );

		/*
		 * ContextChooser
		 */
		final DataContextListener< Spot > contextListener = new DataContextListener<>( viewGraph );
		contextChooser = new ContextChooser<>( contextListener );

		/*
		 * Show the frame
		 */

		final DataDisplayStyle forwardDefaultStyle = appModel.getDataDisplayStyleManager().getForwardDefaultStyle();
		coloringAdapter = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );
		final DataDisplayOptions options = DataDisplayOptions.options()
				.shareKeyPressedEvents( keyPressedManager )
				.style( forwardDefaultStyle )
				.graphColorGenerator( coloringAdapter );
		final AutoNavigateFocusModel< DataVertex, DataEdge > navigateFocusModel =
				new AutoNavigateFocusModel<>( focusModel, navigationHandler );

		final DataDisplayFrame< Spot, Link > frame = new DataDisplayFrame< Spot, Link >(
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
		dataDisplayPanel = frame.getDataDisplayPanel();

		// If they are available, set some sensible defaults for the feature.
		final FeatureSpecPair spvx = new FeatureSpecPair( SpotFrameFeature.SPEC,
				SpotFrameFeature.SPEC.getProjectionSpecs().iterator().next(), false, false );
		final FeatureSpecPair spvy = new FeatureSpecPair( SpotQuickMeanIntensityFeature.SPEC,
				SpotQuickMeanIntensityFeature.PROJECTION_SPEC, 0, false, false );
		final FeatureGraphConfig gcv =
				new FeatureGraphConfig( spvx, spvy, GraphDataItemsSource.TRACK_OF_SELECTION, true );
		frame.getVertexSidePanel().setGraphConfig( gcv );

		// Restore position
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos && pos.length == 4 )
			frame.setBounds( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] );
		else
		{
			frame.setSize( options.values.getWidth(), options.values.getHeight() );
			frame.setLocationRelativeTo( null );
		}

		// Restore group handle.
		final Integer groupID = ( Integer ) guiState.get( GROUP_HANDLE_ID_KEY );
		if ( null != groupID )
			groupHandle.setGroupId( groupID.intValue() );

		// Restore settings panel visibility.
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );

		dataDisplayPanel.graphChanged();
		contextListener.setContextListener( dataDisplayPanel );

		final DataDisplayStyle.UpdateListener updateListener = () -> dataDisplayPanel.repaint();
		forwardDefaultStyle.updateListeners().add( updateListener );
		onClose( () -> forwardDefaultStyle.updateListeners().remove( updateListener ) );

		setFrame( frame );

		// Transform.
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( GRAPHER_TRANSFORM_KEY );
		if ( null != tLoaded )
			dataDisplayPanel.getScreenTransform().set( tLoaded );

		MastodonFrameViewActions.install( viewActions, this );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), model.getTagSetModel(),
				appModel.getSelectionModel(), viewGraph.getLock(), dataDisplayPanel, dataDisplayPanel.getDisplay(),
				model );
		DataDisplayZoom.install( viewBehaviours, dataDisplayPanel );

		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel,
				focusModel, dataDisplayPanel );
		frame.getSettingsPanel().add( searchPanel );

		dataDisplayPanel.getNavigationActions().install( viewActions,
				TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		dataDisplayPanel.getNavigationBehaviours().install( viewBehaviours );
		dataDisplayPanel.getTransformEventHandler().install( viewBehaviours );

		/*
		 * Menus
		 */
		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final JMenuHandle coloringMenuHandle = new JMenuHandle();
		final JMenuHandle tagSetMenuHandle = new JMenuHandle();
		final JMenuHandle colorbarMenuHandle = new JMenuHandle();

		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
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
		appModel.getPlugins().addMenus( menu );

		/*
		 * Coloring & colobar.
		 */
		coloringModel = registerColoring( coloringAdapter, coloringMenuHandle,
				() -> dataDisplayPanel.entitiesAttributesChanged() );
		registerTagSetMenu( tagSetMenuHandle,
				() -> dataDisplayPanel.entitiesAttributesChanged() );
		colorbarOverlay = new ColorBarOverlay( coloringModel, () -> dataDisplayPanel.getBackground() );
		final OffsetAxes offset = dataDisplayPanel.getOffsetAxes();
		offset.listeners().add( ( w, h ) -> colorbarOverlay.setInsets( 15, w + 15, h + 15, 15 ) );
		registerColorbarOverlay( colorbarOverlay, colorbarMenuHandle, () -> dataDisplayPanel.repaint() );

		// Restore colorbar state.
		final boolean colorbarVisible = ( boolean ) guiState.getOrDefault( COLORBAR_VISIBLE_KEY, false );
		final Position colorbarPosition =
				( Position ) guiState.getOrDefault( COLORBAR_POSITION_KEY, Position.BOTTOM_RIGHT );
		colorbarOverlay.setVisible( colorbarVisible );
		colorbarOverlay.setPosition( colorbarPosition );

		// Listen to label changes.
		model.getGraph().addVertexLabelListener( v -> dataDisplayPanel.entitiesAttributesChanged() );

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
		dataDisplayPanel.getDisplay().overlays().add( colorbarOverlay );

		layout.layout();
		frame.setVisible( true );
		dataDisplayPanel.repaint();
		dataDisplayPanel.getDisplay().requestFocusInWindow();
	}

	ContextChooser< Spot > getContextChooser()
	{
		return contextChooser;
	}

	DataDisplayPanel< Spot, Link > getDataDisplayPanel()
	{
		return dataDisplayPanel;
	}

	ColoringModelMain< Spot, Link, BranchSpot, BranchLink > getColoringModel()
	{
		return coloringModel;
	}

	ColorBarOverlay getColorBarOverlay()
	{
		return colorbarOverlay;
	}
}
