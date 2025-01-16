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
package org.mastodon.mamut.views;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameView;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.TagSetMenu;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.ui.coloring.ColorBarOverlayMenu;
import org.mastodon.ui.coloring.ColoringMenu;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.TagSetGraphColorGenerator;
import org.mastodon.ui.coloring.TrackGraphColorGenerator;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;

/**
 * Mother class for views that display the core graph.
 * 
 * @param <VG>
 *            the type of the view graph used in this view.
 * @param <V>
 *            the type of vertices in the view graph.
 * @param <E>
 *            the type of edges in the view graph.
 */
public class MamutView< VG extends ViewGraph< Spot, Link, V, E >, V extends Vertex< E >, E extends Edge< V > >
		extends MastodonFrameView< ProjectModel, VG, Spot, Link, V, E >
		implements MamutViewI
{

	/**
	 * Key that specifies whether the colorbar is visible.
	 */
	public static final String COLORBAR_VISIBLE_KEY = "ColorbarVisible";

	/**
	 * Key that specifies the colorbar position. Values are {@link Position}
	 * enum values.
	 */
	public static final String COLORBAR_POSITION_KEY = "ColorbarPosition";

	/**
	 * Key that specifies the name of the feature color mode to use for coloring
	 * scheme based on feature color modes. A non-<code>null</code> value means
	 * the coloring scheme is based on feature values.
	 *
	 * @see #NO_COLORING_KEY
	 * @see #TAG_SET_KEY
	 * @see #TRACK_COLORING_KEY
	 * 
	 */
	public static final String FEATURE_COLOR_MODE_KEY = "FeatureColorMode";

	/**
	 * Key that specifies whether we do not use a special coloring scheme on the
	 * view. If <code>true</code>, then we do not use a special coloring scheme.
	 *
	 * @see #TAG_SET_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 * @see #TRACK_COLORING_KEY
	 */
	public static final String NO_COLORING_KEY = "NoColoring";

	/**
	 * Key that specifies whether we use an automatic color scheme that assigns
	 * the same color to all the spots and links of one track.
	 *
	 * @see #TAG_SET_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 * @see #NO_COLORING_KEY
	 */
	public static final String TRACK_COLORING_KEY = "ColorByTrack";

	/**
	 * Key that specifies the name of the tag-set to use for coloring scheme
	 * based on tag-sets. A non-<code>null</code> value means the coloring
	 * scheme is based on tag-sets.
	 *
	 * @see #NO_COLORING_KEY
	 * @see #FEATURE_COLOR_MODE_KEY
	 * @see #TRACK_COLORING_KEY
	 */
	public static final String TAG_SET_KEY = "TagSet";

	/**
	 * Key to the parameter that stores the frame position for
	 * {@link MastodonFrameView}s. Value is an <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 */
	public static final String FRAME_POSITION_KEY = "FramePosition";

	/**
	 * Key that specifies whether the settings panel is visible or not.
	 */
	public static final String SETTINGS_PANEL_VISIBLE_KEY = "SettingsPanelVisible";

	/**
	 * Key to the lock group id. Value is an int.
	 */
	public static final String GROUP_HANDLE_ID_KEY = "LockGroupId";

	public MamutView( final ProjectModel appModel, final VG viewGraph, final String[] keyConfigContexts )
	{
		super( appModel, viewGraph, keyConfigContexts );
	}

	/**
	 * Sets up and registers the coloring menu item and related actions and
	 * listeners. A new instance of the {@code ColoringModel} is created here
	 * and a reference on it is returned. This instance is bound to all relevant
	 * actions and is therefore knowledgeable of the currently used coloring
	 * style.
	 *
	 * @param colorGeneratorAdapter
	 *            adapts a (modifiable) model coloring to view vertices/edges.
	 * @param menuHandle
	 *            handle to the JMenu corresponding to the coloring submenu.
	 *            Coloring options will be installed here.
	 * @param refresh
	 *            triggers repaint of the graph (called when coloring changes)
	 *
	 * @return reference on the underlying {@code ColoringModel}
	 */
	protected ColoringModelMain< Spot, Link, BranchSpot, BranchLink > registerColoring(
			final GraphColorGeneratorAdapter< Spot, Link, V, E > colorGeneratorAdapter,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final FeatureModel featureModel = appModel.getModel().getFeatureModel();
		final FeatureColorModeManager featureColorModeManager = appModel.getWindowManager().getManager( FeatureColorModeManager.class );
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final ColoringModelMain< Spot, Link, BranchSpot, BranchLink > coloringModel = new ColoringModelMain<>( tagSetModel, featureColorModeManager, featureModel, branchGraph );
		final ColoringMenu coloringMenu = new ColoringMenu( menuHandle.getMenu(), coloringModel );

		tagSetModel.listeners().add( coloringModel );
		onClose( () -> tagSetModel.listeners().remove( coloringModel ) );
		tagSetModel.listeners().add( coloringMenu );
		onClose( () -> tagSetModel.listeners().remove( coloringMenu ) );

		featureColorModeManager.listeners().add( coloringModel );
		onClose( () -> featureColorModeManager.listeners().remove( coloringModel ) );
		featureColorModeManager.listeners().add( coloringMenu );
		onClose( () -> featureColorModeManager.listeners().remove( coloringMenu ) );

		featureModel.listeners().add( coloringMenu );
		onClose( () -> featureModel.listeners().remove( coloringMenu ) );

		// Handle track color generator.
		@SuppressWarnings( "unchecked" )
		final TrackGraphColorGenerator< Spot, Link > tgcg = appModel.getWindowManager().getManager( TrackGraphColorGenerator.class );

		final ColoringModelMain.ColoringChangedListener coloringChangedListener = () -> {
			final GraphColorGenerator< Spot, Link > colorGenerator;
			switch ( coloringModel.getColoringStyle() )
			{
			case BY_FEATURE:
				colorGenerator = coloringModel.getFeatureGraphColorGenerator();
				break;
			case BY_TAGSET:
				colorGenerator = new TagSetGraphColorGenerator<>( tagSetModel, coloringModel.getTagSet() );
				break;
			case BY_TRACK:
				colorGenerator = tgcg;
				break;
			case NONE:
				colorGenerator = null;
				break;
			default:
				throw new IllegalArgumentException( "Unknown coloring style: " + coloringModel.getColoringStyle() );
			}
			colorGeneratorAdapter.setColorGenerator( colorGenerator );
			refresh.run();
		};
		coloringModel.listeners().add( coloringChangedListener );

		return coloringModel;
	}

	protected void registerColorbarOverlay(
			final ColorBarOverlay colorBarOverlay,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final ColorBarOverlayMenu menu = new ColorBarOverlayMenu( menuHandle.getMenu(), colorBarOverlay, refresh );
		colorBarOverlay.listeners().add( menu );
	}

	protected void registerTagSetMenu(
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();
		final Model model = appModel.getModel();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final TagSetMenu< Spot, Link > tagSetMenu = new TagSetMenu<>( menuHandle.getMenu(), tagSetModel, selectionModel,
				model.getGraph().getLock(), model, refresh );
		tagSetModel.listeners().add( tagSetMenu );
		onClose( () -> tagSetModel.listeners().remove( tagSetMenu ) );
	}

	protected static void restoreColoring( final ColoringModel coloringModel, final Map< String, Object > guiState )
	{
		if ( guiState == null )
			return;

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
	}

	protected static void restoreColorbarState( final ColorBarOverlay colorBarOverlay,
			final Map< String, Object > guiState )
	{
		final boolean colorbarVisible = ( boolean ) guiState.getOrDefault( COLORBAR_VISIBLE_KEY, false );
		final Position colorbarPosition =
				( Position ) guiState.getOrDefault( COLORBAR_POSITION_KEY, Position.BOTTOM_RIGHT );
		colorBarOverlay.setVisible( colorbarVisible );
		colorBarOverlay.setPosition( colorbarPosition );
	}

	protected static void restoreFramePosition( final Window frame, final Map< String, Object > guiState )
	{
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos )
			frame.setBounds( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] );
		else
		{
			frame.setSize( 650, 400 );
			frame.setLocationRelativeTo( null );
		}
	}

	protected static void restoreGroupHandle( final GroupHandle groupHandle, final Map< String, Object > guiState )
	{
		final Integer groupID = ( Integer ) guiState.get( GROUP_HANDLE_ID_KEY );
		if ( null != groupID )
			groupHandle.setGroupId( groupID.intValue() );
	}

	protected static void restoreSettingsPanelVisibility( final ViewFrame frame, final Map< String, Object > guiState )
	{
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );
	}
}
