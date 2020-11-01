package org.mastodon.views.bdv;

import java.util.stream.IntStream;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

import static bdv.viewer.NavigationActions.ALIGN_XY_PLANE;
import static bdv.viewer.NavigationActions.ALIGN_XY_PLANE_KEYS;
import static bdv.viewer.NavigationActions.ALIGN_XZ_PLANE;
import static bdv.viewer.NavigationActions.ALIGN_XZ_PLANE_KEYS;
import static bdv.viewer.NavigationActions.ALIGN_ZY_PLANE;
import static bdv.viewer.NavigationActions.ALIGN_ZY_PLANE_KEYS;
import static bdv.viewer.NavigationActions.NEXT_TIMEPOINT;
import static bdv.viewer.NavigationActions.NEXT_TIMEPOINT_KEYS;
import static bdv.viewer.NavigationActions.PREVIOUS_TIMEPOINT;
import static bdv.viewer.NavigationActions.PREVIOUS_TIMEPOINT_KEYS;
import static bdv.viewer.NavigationActions.SET_CURRENT_SOURCE;
import static bdv.viewer.NavigationActions.SET_CURRENT_SOURCE_KEYS_FORMAT;
import static bdv.viewer.NavigationActions.TOGGLE_FUSED_MODE;
import static bdv.viewer.NavigationActions.TOGGLE_FUSED_MODE_KEYS;
import static bdv.viewer.NavigationActions.TOGGLE_GROUPING;
import static bdv.viewer.NavigationActions.TOGGLE_GROUPING_KEYS;
import static bdv.viewer.NavigationActions.TOGGLE_INTERPOLATION;
import static bdv.viewer.NavigationActions.TOGGLE_INTERPOLATION_KEYS;
import static bdv.viewer.NavigationActions.TOGGLE_SOURCE_VISIBILITY;
import static bdv.viewer.NavigationActions.TOGGLE_SOURCE_VISIBILITY_KEYS_FORMAT;

/*
 * Command descriptions for all commands provided by {@link NavigationActions}
 */
@Plugin( type = CommandDescriptionProvider.class )
public class NavigationActionsDescriptions extends CommandDescriptionProvider
{
	public NavigationActionsDescriptions()
	{
		super( KeyConfigContexts.BIGDATAVIEWER );
	}

	@Override
	public void getCommandDescriptions( final CommandDescriptions descriptions )
	{
		descriptions.add( TOGGLE_INTERPOLATION, TOGGLE_INTERPOLATION_KEYS, "Switch between nearest-neighbor and n-linear interpolation mode in BigDataViewer." );
		descriptions.add( TOGGLE_FUSED_MODE, TOGGLE_FUSED_MODE_KEYS, "TODO" );
		descriptions.add( TOGGLE_GROUPING, TOGGLE_GROUPING_KEYS, "TODO" );

		final String[] numkeys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };
		IntStream.range( 0, numkeys.length ).forEach( i -> {
			descriptions.add( String.format( SET_CURRENT_SOURCE, i ), new String[] { String.format( SET_CURRENT_SOURCE_KEYS_FORMAT, numkeys[ i ] ) }, "TODO" );
			descriptions.add( String.format( TOGGLE_SOURCE_VISIBILITY, i ), new String[] { String.format( TOGGLE_SOURCE_VISIBILITY_KEYS_FORMAT, numkeys[ i ] ) }, "TODO" );
		} );

		descriptions.add( NEXT_TIMEPOINT, NEXT_TIMEPOINT_KEYS, "TODO" );
		descriptions.add( PREVIOUS_TIMEPOINT, PREVIOUS_TIMEPOINT_KEYS, "TODO" );
		descriptions.add( ALIGN_XY_PLANE, ALIGN_XY_PLANE_KEYS, "TODO" );
		descriptions.add( ALIGN_ZY_PLANE, ALIGN_ZY_PLANE_KEYS, "TODO" );
		descriptions.add( ALIGN_XZ_PLANE, ALIGN_XZ_PLANE_KEYS, "TODO" );
	}
}
