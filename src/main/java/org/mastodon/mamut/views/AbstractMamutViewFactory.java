package org.mastodon.mamut.views;

import static org.mastodon.mamut.views.MamutView.COLORBAR_POSITION_KEY;
import static org.mastodon.mamut.views.MamutView.COLORBAR_VISIBLE_KEY;
import static org.mastodon.mamut.views.MamutView.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.mamut.views.MamutView.FRAME_POSITION_KEY;
import static org.mastodon.mamut.views.MamutView.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.views.MamutView.NO_COLORING_KEY;
import static org.mastodon.mamut.views.MamutView.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.views.MamutView.TAG_SET_KEY;

import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewFrame;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.HasColorBarOverlay;
import org.mastodon.ui.coloring.HasColoringModel;
import org.mastodon.ui.coloring.feature.FeatureColorMode;

public abstract class AbstractMamutViewFactory< T extends MamutViewI > implements MamutViewFactory< T >
{

	@Override
	public T show( final ProjectModel projectModel, final Map< String, Object > guiState )
	{
		final T view = create( projectModel );
		restoreGuiState( view, guiState );
		view.getFrame().setVisible( true );
		return view;
	}

	@Override
	public void restoreGuiState( final T view, final Map< String, Object > guiState )
	{
		restoreFramePosition( view.getFrame(), guiState );
		restoreSettingsPanelVisibility( view.getFrame(), guiState );
		restoreGroupHandle( view.getGroupHandle(), guiState );
		restoreColoringModel( view, guiState );
		restoreColorbarState( view, guiState );
	}

	@Override
	public String[] getCommandKeys()
	{
		// Default: not mapped.
		return new String[] { "not mapped" };
	}

	@Override
	public Map< String, Object > getGuiState( final T view )
	{
		final Map< String, Object > guiState = new LinkedHashMap<>();

		// View type.
		guiState.put( VIEW_TYPE_KEY, view.getClass().getSimpleName() );

		// Frame position and size.
		final Rectangle bounds = view.getFrame().getBounds();
		guiState.put( FRAME_POSITION_KEY, new int[] {
				( int ) bounds.getMinX(),
				( int ) bounds.getMinY(),
				( int ) bounds.getWidth(),
				( int ) bounds.getHeight() } );

		// Lock groups.
		guiState.put( GROUP_HANDLE_ID_KEY, view.getGroupHandle().getGroupId() );

		// Settings panel visibility.
		guiState.put( SETTINGS_PANEL_VISIBLE_KEY, view.getFrame().isSettingsPanelVisible() );

		// Coloring.
		getColoringState( view, guiState );

		// Colorbar.
		getColorBarOverlayState( view, guiState );

		return guiState;
	}

	/**
	 * Reads the coloring state of a view and stores it into the specified map.
	 * 
	 * @param view
	 *            the view to get the coloring model from.
	 * @param guiState
	 *            the map to store it to.
	 */
	private static void getColoringState( final MamutViewI view, final Map< String, Object > guiState )
	{
		if ( !( view instanceof HasColoringModel ) )
			return;

		final ColoringModel coloringModel = ( ( HasColoringModel ) view ).getColoringModel();
		getColoringState( coloringModel, guiState );
	}

	/**
	 * Reads the coloring state and stores it into the specified map.
	 * 
	 * @param coloringModel
	 *            the coloring model to read from.
	 * @param guiState
	 *            the map to store it to.
	 */
	protected static void getColoringState( final ColoringModel coloringModel, final Map< String, Object > guiState )
	{
		final boolean noColoring = coloringModel.noColoring();
		guiState.put( NO_COLORING_KEY, noColoring );
		if ( !noColoring )
			if ( coloringModel.getTagSet() != null )
				guiState.put( TAG_SET_KEY, coloringModel.getTagSet().getName() );
			else if ( coloringModel.getFeatureColorMode() != null )
				guiState.put( FEATURE_COLOR_MODE_KEY, coloringModel.getFeatureColorMode().getName() );
	}

	private static void getColorBarOverlayState( final MamutViewI view, final Map< String, Object > guiState )
	{
		if ( !( view instanceof HasColorBarOverlay ) )
			return;

		final ColorBarOverlay colorBarOverlay = ( ( HasColorBarOverlay ) view ).getColorBarOverlay();
		guiState.put( COLORBAR_VISIBLE_KEY, colorBarOverlay.isVisible() );
		guiState.put( COLORBAR_POSITION_KEY, colorBarOverlay.getPosition() );
	}

	/*
	 * Restore GUI state utilities.
	 */

	private static void restoreColoringModel( final MamutViewI viewraw, final Map< String, Object > guiState )
	{
		if ( guiState == null || ( !( viewraw instanceof HasColoringModel ) ) )
			return;

		final ColoringModel coloringModel = ( ( HasColoringModel ) viewraw ).getColoringModel();
		restoreColoringModel( coloringModel, guiState );
	}

	protected static void restoreColoringModel( final ColoringModel coloringModel, final Map< String, Object > guiState )
	{
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

	private static void restoreColorbarState( final MamutViewI view, final Map< String, Object > guiState )
	{
		if ( !( view instanceof HasColorBarOverlay ) )
			return;

		final ColorBarOverlay colorBarOverlay = ( ( HasColorBarOverlay ) view ).getColorBarOverlay();
		final boolean colorbarVisible = ( boolean ) guiState.getOrDefault( COLORBAR_VISIBLE_KEY, false );
		final Position colorbarPosition =
				( Position ) guiState.getOrDefault( COLORBAR_POSITION_KEY, Position.BOTTOM_RIGHT );
		colorBarOverlay.setVisible( colorbarVisible );
		colorBarOverlay.setPosition( colorbarPosition );
	}

	private static void restoreFramePosition( final Window frame, final Map< String, Object > guiState )
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

	private static void restoreGroupHandle( final GroupHandle groupHandle, final Map< String, Object > guiState )
	{
		final Integer groupID = ( Integer ) guiState.get( GROUP_HANDLE_ID_KEY );
		if ( null != groupID )
			groupHandle.setGroupId( groupID.intValue() );
	}

	private static void restoreSettingsPanelVisibility( final ViewFrame frame, final Map< String, Object > guiState )
	{
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );
	}
}
