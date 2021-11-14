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
import static org.mastodon.mamut.MamutViewStateSerialization.COLORBAR_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.COLORBAR_VISIBLE_KEY;
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
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraphTrackSchemeProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.EditTagActions;
import org.mastodon.ui.FocusActions;
import org.mastodon.ui.HighlightBehaviours;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.TrackSchemeContextListener;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;
import org.mastodon.views.trackscheme.display.ColorBarOverlay.Position;
import org.mastodon.views.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.views.trackscheme.display.ToggleLinkBehaviour;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;
import org.mastodon.views.trackscheme.display.TrackSchemeOptions;
import org.mastodon.views.trackscheme.display.TrackSchemePanel;
import org.mastodon.views.trackscheme.display.TrackSchemeZoom;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyle;
import org.scijava.ui.behaviour.KeyPressedManager;

public class MamutViewTrackScheme extends MamutView< TrackSchemeGraph< Spot, Link >, TrackSchemeVertex, TrackSchemeEdge >
{
	private final ContextChooser< Spot > contextChooser;

	/**
	 * a reference on the {@code GraphColorGeneratorAdapter} created and
	 * registered with this instance/window
	 */
	private final GraphColorGeneratorAdapter< Spot, Link, TrackSchemeVertex, TrackSchemeEdge > coloringAdapter;

	/**
	 * a reference on a supervising instance of the {@code ColoringModel} that
	 * is bound to this instance/window
	 */
	private final ColoringModel coloringModel;

	private final ColorBarOverlay colorBarOverlay;

	public MamutViewTrackScheme( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewTrackScheme( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel,
				new TrackSchemeGraph<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						new ModelGraphTrackSchemeProperties( appModel.getModel().getGraph() ),
						appModel.getModel().getGraph().getLock() ),
				new String[] { KeyConfigContexts.TRACKSCHEME } );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener<>( viewGraph );
		contextChooser = new ContextChooser<>( contextListener );

		final KeyPressedManager keyPressedManager = appModel.getKeyPressedManager();
		final Model model = appModel.getModel();

		/*
		 * show TrackSchemeFrame
		 */
		final TrackSchemeStyle forwardDefaultStyle = appModel.getTrackSchemeStyleManager().getForwardDefaultStyle();
		coloringAdapter = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );
		final TrackSchemeOptions options = TrackSchemeOptions.options()
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

		final AutoNavigateFocusModel< TrackSchemeVertex, TrackSchemeEdge > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				viewGraph,
				highlightModel,
				navigateFocusModel,
				timepointModel,
				selectionModel,
				navigationHandler,
				model,
				groupHandle,
				contextChooser,
				options );

		// Restore settings panel visibility.
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );

		// Default location.
		if ( null == pos || pos.length != 4 )
			frame.setLocationRelativeTo( null );

		frame.getTrackschemePanel().setTimepointRange( appModel.getMinTimepoint(), appModel.getMaxTimepoint() );
		frame.getTrackschemePanel().graphChanged();
		contextListener.setContextListener( frame.getTrackschemePanel() );

		final TrackSchemeStyle.UpdateListener updateListener = () -> frame.getTrackschemePanel().repaint();
		forwardDefaultStyle.updateListeners().add( updateListener );
		onClose( () -> forwardDefaultStyle.updateListeners().remove( updateListener ) );

		setFrame( frame );

		// Transform.
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( TRACKSCHEME_TRANSFORM_KEY );
		if ( null != tLoaded )
			frame.getTrackschemePanel().getScreenTransform().set( tLoaded );

		MastodonFrameViewActions.install( viewActions, this );
		HighlightBehaviours.install( viewBehaviours, viewGraph, viewGraph.getLock(), viewGraph, highlightModel, model );
		ToggleLinkBehaviour.install( viewBehaviours, frame.getTrackschemePanel(), viewGraph, viewGraph.getLock(), viewGraph, model );
		EditFocusVertexLabelAction.install( viewActions, frame.getTrackschemePanel(), focusModel, model );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		TrackSchemeZoom.install( viewBehaviours, frame.getTrackschemePanel() );
		EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), model.getTagSetModel(), appModel.getSelectionModel(), viewGraph.getLock(), frame.getTrackschemePanel(), frame.getTrackschemePanel().getDisplay(), model );

		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, frame.getTrackschemePanel() );
		frame.getSettingsPanel().add( searchPanel );

		// TODO Let the user choose between the two selection/focus modes.
		frame.getTrackschemePanel().getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		frame.getTrackschemePanel().getNavigationBehaviours().install( viewBehaviours );
		frame.getTrackschemePanel().getTransformEventHandler().install( viewBehaviours );

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
		appModel.getPlugins().addMenus( menu );

		coloringModel = registerColoring( coloringAdapter, coloringMenuHandle,
				() -> frame.getTrackschemePanel().entitiesAttributesChanged() );
		colorBarOverlay = new ColorBarOverlay( coloringModel, () -> frame.getTrackschemePanel().getBackground() );
		frame.getTrackschemePanel().getOffsetHeaders().listeners().add( ( w, h ) -> colorBarOverlay.setInsets( h + 15, w + 15, 15, 15 ) );
		registerColorbarOverlay( colorBarOverlay, colorbarMenuHandle, () -> frame.getTrackschemePanel().repaint() );

		// Listen to user changing the tag-set menu.
		registerTagSetMenu( tagSetMenuHandle, () -> frame.getTrackschemePanel().entitiesAttributesChanged() );

		// Listen to vertex labels being changed.
		model.getGraph().addVertexLabelListener( v -> frame.getTrackschemePanel().entitiesAttributesChanged() );

		// Restore colorbar state.
		final boolean colorbarVisible = ( boolean ) guiState.getOrDefault( COLORBAR_VISIBLE_KEY, false );
		final Position colorbarPosition = ( Position ) guiState.getOrDefault( COLORBAR_POSITION_KEY, Position.BOTTOM_RIGHT );
		colorBarOverlay.setVisible( colorbarVisible );
		colorBarOverlay.setPosition( colorbarPosition );

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
		frame.getTrackschemePanel().getDisplay().overlays().add( colorBarOverlay );

		frame.setVisible( true );
		frame.getTrackschemePanel().repaint();

		// Give focus to the display so that it can receive key presses
		// immediately.
		frame.getTrackschemePanel().getDisplay().requestFocusInWindow();
	}

	ContextChooser< Spot > getContextChooser()
	{
		return contextChooser;
	}

	ColoringModel getColoringModel()
	{
		return coloringModel;
	}

	/**
	 * Exposes the {@link TrackSchemePanel} displayed in this view.
	 *
	 * @return the {@link TrackSchemePanel}.
	 */
	TrackSchemePanel getTrackschemePanel()
	{
		return ( ( TrackSchemeFrame ) getFrame() ).getTrackschemePanel();
	}

	ColorBarOverlay getColorBarOverlay()
	{
		return colorBarOverlay;
	}
}
