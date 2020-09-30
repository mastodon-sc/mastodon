package org.mastodon.ui.keymap;

/**
 * Constants that specify to what context an action or a behaviour applies.
 */
// TODO move to somewhere in mamut package
public interface KeyConfigContexts
{
	/**
	 * The action or behaviour applies to the whole app.
	 */
	String MASTODON = "mastodon";

	/**
	 * The action or behaviour applies to the TrackScheme views.
	 */
	String TRACKSCHEME = "ts";

	/**
	 * The action or behaviour applies to the BDV views.
	 */
	String BIGDATAVIEWER = "bdv";
}
