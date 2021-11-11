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
import static org.mastodon.mamut.MamutMenuBuilder.colorbarMenu;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.tagSetMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;
import static org.mastodon.mamut.MamutViewStateSerialization.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.NO_COLORING_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.TAG_SET_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.TRACKSCHEME_TRANSFORM_KEY;

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
import org.mastodon.mamut.feature.SpotIntensityFeature;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.grapher.SpecPair;
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
import org.mastodon.views.grapher.display.OffsetAxes;
import org.mastodon.views.grapher.display.style.DataDisplayStyle;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;
import org.scijava.ui.behaviour.KeyPressedManager;

public class MamutViewGrapher2 extends MamutView< DataGraph< Spot, Link >, DataVertex, DataEdge >
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
	private final ColoringModel coloringModel;

	public MamutViewGrapher2( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewGrapher2( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel,
				new DataGraph< Spot, Link >(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getGraph().getLock() ),
				new String[] { KeyConfigContexts.GRAPHER } );

		/*
		 * We hardcode the initial features for now.
		 */
		final DataGraphLayout< Spot, Link > layout = new DataGraphLayout<>(
				viewGraph,
				appModel.getModel().getFeatureModel(),
				selectionModel );
//		final SpecPair x = new SpecPair( SpotFrameFeature.SPEC, SpotFrameFeature.SPEC.getProjectionSpecs().iterator().next() );
		final SpecPair x = new SpecPair( SpotIntensityFeature.SPEC, SpotIntensityFeature.MEDIAN_PROJECTION_SPEC, 0 );
		final SpecPair y = new SpecPair( SpotIntensityFeature.SPEC, SpotIntensityFeature.MEAN_PROJECTION_SPEC, 0 );
		layout.setXFeature( x );
		layout.setYFeature( y );

		final KeyPressedManager keyPressedManager = appModel.getKeyPressedManager();
		final Model model = appModel.getModel();

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

		// Restore position
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos && pos.length == 4 )
			options
					.x( pos[ 0 ] )
					.y( pos[ 1 ] )
					.width( pos[ 2 ] )
					.height( pos[ 3 ] );

		// Restore group handle.
		final Integer groupID = ( Integer ) guiState.get( GROUP_HANDLE_ID_KEY );
		if ( null != groupID )
			groupHandle.setGroupId( groupID.intValue() );

		final AutoNavigateFocusModel< DataVertex, DataEdge > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );
		final DataDisplayFrame frame = new DataDisplayFrame(
				viewGraph,
				layout,
				highlightModel,
				navigateFocusModel,
				selectionModel,
				navigationHandler,
				model,
				groupHandle,
				contextChooser,
				options );
		final DataDisplayPanel dataDisplayPanel = frame.getDataDisplayPanel();

		// Restore settings panel visibility.
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );

		// Default location.
		if ( null == pos || pos.length != 4 )
			frame.setLocationRelativeTo( null );

		dataDisplayPanel.graphChanged();
		contextListener.setContextListener( dataDisplayPanel );

		final DataDisplayStyle.UpdateListener updateListener = () -> dataDisplayPanel.repaint();
		forwardDefaultStyle.updateListeners().add( updateListener );
		onClose( () -> forwardDefaultStyle.updateListeners().remove( updateListener ) );

		setFrame( frame );

		// Transform. // TODO!
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( TRACKSCHEME_TRANSFORM_KEY );
		if ( null != tLoaded )
			dataDisplayPanel.getScreenTransform().set( tLoaded );

		MastodonFrameViewActions.install( viewActions, this );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), model.getTagSetModel(), appModel.getSelectionModel(), viewGraph.getLock(), dataDisplayPanel, dataDisplayPanel.getDisplay(), model );
		DataDisplayZoom.install( viewBehaviours, dataDisplayPanel );

		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, dataDisplayPanel );
		frame.getSettingsPanel().add( searchPanel );

		dataDisplayPanel.getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
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
		 * Coloring
		 */

		coloringModel = registerColoring( coloringAdapter, coloringMenuHandle,
				() -> dataDisplayPanel.entitiesAttributesChanged() );
		registerTagSetMenu( tagSetMenuHandle,
				() -> dataDisplayPanel.entitiesAttributesChanged() );
		final ColorBarOverlay colorBarOverlay = new ColorBarOverlay( coloringModel, () -> dataDisplayPanel.getBackground() );
		final OffsetAxes offset = dataDisplayPanel.getOffsetAxes();
		offset.listeners().add( ( w, h ) -> {
			colorBarOverlay.setLeftXOffset(  w );
			// Because *for now* we paint the axis at the top:
			colorBarOverlay.setTopYOffset( h );
		} );
		registerColorbarOverlay( colorBarOverlay, colorbarMenuHandle,
				() -> dataDisplayPanel.repaint() );

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
		dataDisplayPanel.getDisplay().overlays().add( colorBarOverlay );

		layout.layout();
		frame.setVisible( true );
		dataDisplayPanel.repaint();
		dataDisplayPanel.getDisplay().requestFocusInWindow();
	}

	public ContextChooser< Spot > getContextChooser()
	{
		return contextChooser;
	}

	public GraphColorGeneratorAdapter< Spot, Link, DataVertex, DataEdge > getGraphColorGeneratorAdapter()
	{
		return coloringAdapter;
	}

	public ColoringModel getColoringModel()
	{
		return coloringModel;
	}

	public TimepointModel getTimepointModel()
	{
		return timepointModel;
	}

	/**
	 * Exposes the {@link DataDisplayPanel} displayed in this view.
	 *
	 * @return the {@link DataDisplayPanel}.
	 */
	DataDisplayPanel getDataDisplayPanel()
	{
		return ( ( DataDisplayFrame ) getFrame() ).getDataDisplayPanel();
	}
}
