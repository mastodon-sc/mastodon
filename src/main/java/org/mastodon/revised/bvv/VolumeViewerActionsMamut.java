/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2015 BigDataViewer authors
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
package org.mastodon.revised.bvv;

import org.mastodon.revised.mamut.MamutViewBvv;
import org.mastodon.revised.util.ToggleDialogAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

public class VolumeViewerActionsMamut
{
	public static final String VISIBILITY_AND_GROUPING = "visibility and grouping";
	public static final String BRIGHTNESS_SETTINGS = "brightness settings";
	public static final String TOGGLE_SCENE_VISIBILITY = "toggle scene visibility";

	static final String[] VISIBILITY_AND_GROUPING_KEYS     = new String[] { "F6" };
	static final String[] BRIGHTNESS_SETTINGS_KEYS         = new String[] { "S" };
	static final String[] TOGGLE_SCENE_VISIBILITY_KEYS = new String[] { "H" };

	private final ToggleDialogAction toggleVisibilityAndGroupingDialogAction;
	private final ToggleDialogAction toggleBrightnessDialogAction;
	private final RunnableAction toggleSceneVisibilityAction;

//	/*
//	 * Command descriptions for all provided commands
//	 */
//	@Plugin( type = Descriptions.class )
//	public static class Descriptions extends CommandDescriptionProvider
//	{
//		public Descriptions()
//		{
//			super( KeyConfigContexts.BIGDATAVIEWER );
//		}
//
//		@Override
//		public void getCommandDescriptions( final CommandDescriptions descriptions )
//		{
//			descriptions.add( VISIBILITY_AND_GROUPING, VISIBILITY_AND_GROUPING_KEYS, "Show the Visibility&Grouping dialog." );
//			descriptions.add( BRIGHTNESS_SETTINGS,BRIGHTNESS_SETTINGS_KEYS, "Show the Brightness&Colors dialog." );
//		}
//	}
//
	/**
	 * Create BigDataViewer actions and install them in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param bdv
	 *            Actions are targeted at this BDV window.
	 */
	public static void install(
			final Actions actions,
			final MamutViewBvv bdv )
	{
		final VolumeViewerActionsMamut ba = new VolumeViewerActionsMamut( bdv );
		actions.namedAction( ba.toggleVisibilityAndGroupingDialogAction, VISIBILITY_AND_GROUPING_KEYS );
		actions.namedAction( ba.toggleBrightnessDialogAction, BRIGHTNESS_SETTINGS_KEYS );
		actions.namedAction( ba.toggleSceneVisibilityAction, TOGGLE_SCENE_VISIBILITY_KEYS );
	}

	private VolumeViewerActionsMamut( final MamutViewBvv bvv )
	{
		toggleVisibilityAndGroupingDialogAction = new ToggleDialogAction( VISIBILITY_AND_GROUPING, bvv.getVisibilityAndGroupingDialog() );
		toggleBrightnessDialogAction = new ToggleDialogAction( BRIGHTNESS_SETTINGS, bvv.getBrightnessDialog() );
		toggleSceneVisibilityAction = new RunnableAction( TOGGLE_SCENE_VISIBILITY, () -> {
			bvv.getScene().setVisible( !bvv.getScene().isVisible() );
			bvv.requestRepaint();
		} );
	}
}
