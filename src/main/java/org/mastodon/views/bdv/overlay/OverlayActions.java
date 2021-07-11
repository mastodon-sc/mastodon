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
package org.mastodon.views.bdv.overlay;

import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.bdv.overlay.Visibilities.VisibilityMode;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;

import bdv.viewer.ViewerPanel;

/**
 * Install actions related to the track overlay over a BDV window.
 */
public class OverlayActions
{
	public static final String CYCLE_VISIBILITY_MODE = "cycle visibility mode";

	public static final String[] CYCLE_VISIBILITY_MODE_KEYS = new String[] { "V" };

	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( CYCLE_VISIBILITY_MODE, CYCLE_VISIBILITY_MODE_KEYS,
					"Cycle across the visibility modes for the overlay in BDV." );
		}
	}

	public static void install(
			final Actions actions,
			final ViewerPanel viewerPanel,
			final SpotOverlayGraphRenderer< ?, ? > renderer )
	{
		actions.runnableAction( () -> {
			// Cycle mode.
			final VisibilityMode mode = renderer.nextVisibilityMode();
			// Show message.
			viewerPanel.showMessage( "Overlay visibility: " + mode );
		}, CYCLE_VISIBILITY_MODE, CYCLE_VISIBILITY_MODE_KEYS );
	}
}
