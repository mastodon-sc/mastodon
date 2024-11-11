package org.mastodon.views.bdv;

import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.views.bdv.overlay.OverlayEdge;
import org.mastodon.views.bdv.overlay.OverlayVertex;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.ViewerPanel;

public class BigDataViewerBehavioursMamut
{

	public static final String SCROLL_TIMEPOINTS = "scroll timepoint";

	private static final String[] SCROLL_TIMEPOINTS_KEYS = new String[] { "not mapped" };

	public static < V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > void install(
			final Behaviours behaviours,
			final BigDataViewerMamut bdv )
	{
		behaviours.behaviour( new ScrollTimePointsBehaviour( bdv.getViewer() ), SCROLL_TIMEPOINTS, SCROLL_TIMEPOINTS_KEYS );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( SCROLL_TIMEPOINTS, SCROLL_TIMEPOINTS_KEYS, "Use mouse-wheel to scroll through time-points." );
		}
	}

	private static class ScrollTimePointsBehaviour implements ScrollBehaviour
	{

		private final ViewerPanel viewer;

		public ScrollTimePointsBehaviour( final ViewerPanel viewerPanel )
		{
			this.viewer = viewerPanel;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			if ( wheelRotation > 0 )
				viewer.nextTimePoint();
			else
				viewer.previousTimePoint();
		}

	}

}
