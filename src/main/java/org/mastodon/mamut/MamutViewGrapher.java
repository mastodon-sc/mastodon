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

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.grapher.GrapherViewFrame;
import org.mastodon.views.table.TableViewActions;

public class MamutViewGrapher extends MamutView< ViewGraph< Spot, Link, Spot, Link >, Spot, Link >
{

	private static final String[] CONTEXTS = new String[] { KeyConfigContexts.GRAPHER };

	private final ColoringModel coloringModel;

	public MamutViewGrapher( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewGrapher( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, IdentityViewGraph.wrap( appModel.getModel().getGraph(), appModel.getModel().getGraphIdBimap() ), CONTEXTS );

		final GraphColorGeneratorAdapter< Spot, Link, Spot, Link > coloring = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );

		final GrapherViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > frame = new GrapherViewFrame<>(
				appModel,
				viewGraph,
				appModel.getModel().getFeatureModel(),
				groupHandle,
				navigationHandler,
				coloring,
				appModel.getModel().getSpaceUnits(),
				appModel.getModel().getTimeUnits() );

		setFrame( frame );

		// Restore position.
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos )
			frame.setBounds( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] );
		else
		{
			frame.setSize( 400, 400 );
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

		/*
		 * Deal with actions.
		 */

		final Model model = appModel.getModel();
		final FeatureModel featureModel = model.getFeatureModel();

		focusModel.listeners().add( frame );
		highlightModel.listeners().add( frame );
		featureModel.listeners().add( frame );

		MastodonFrameViewActions.install( viewActions, this );

		onClose( () -> {
			focusModel.listeners().remove( frame );
			highlightModel.listeners().remove( frame );
			featureModel.listeners().remove( frame );
		} );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final JMenuHandle menuHandle = new JMenuHandle();

		MamutMenuBuilder.build( menu, actionMap,
				MamutMenuBuilder.fileMenu(
						item( TableViewActions.EXPORT_TO_CSV ),
						separator() ) );
		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				MamutMenuBuilder.viewMenu(
						MamutMenuBuilder.colorMenu( menuHandle ),
						separator(),
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL ) ),
				MamutMenuBuilder.editMenu(
						item( TableViewActions.EDIT_LABEL ),
						item( TableViewActions.TOGGLE_TAG ),
						separator(),
						item( SelectionActions.DELETE_SELECTION ) ) );
		appModel.getPlugins().addMenus( menu );

		coloringModel = registerColoring( coloring, menuHandle, () -> {
			System.out.println( "Refresh coloring TODO" ); // DEBUG
		} );

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

		/*
		 * Show table.
		 */

		frame.setVisible( true );
	}

	@Override
	public GrapherViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > getFrame()
	{
		final ViewFrame f = super.getFrame();
		@SuppressWarnings( "unchecked" )
		final GrapherViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > vf = ( GrapherViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > ) f;
		return vf;
	}
}
