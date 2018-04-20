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
package org.mastodon.revised.bdv;

import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.ui.keymap.CommandDescriptionProvider;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.mastodon.revised.util.ToggleDialogAction;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

public class BigDataViewerActionsMamut
{
	public static final String VISIBILITY_AND_GROUPING = "visibility and grouping";
	public static final String SET_BOOKMARK = "set bookmark";
	public static final String GO_TO_BOOKMARK = "go to bookmark";
	public static final String GO_TO_BOOKMARK_ROTATION = "go to bookmark rotation";

	static final String[] VISIBILITY_AND_GROUPING_KEYS     = new String[] { "F6" };
	static final String[] SET_BOOKMARK_KEYS                = new String[] { "shift B" };
	static final String[] GO_TO_BOOKMARK_KEYS              = new String[] { "B" };
	static final String[] GO_TO_BOOKMARK_ROTATION_KEYS     = new String[] { "O" };

	private final ToggleDialogAction toggleVisibilityAndGroupingDialogAction;

	private final RunnableAction goToBookmarkAction;

	private final RunnableAction goToBookmarkRotationAction;

	private final RunnableAction setBookmarkAction;




	public static final String BRIGHTNESS_SETTINGS = "brightness settings";
	public static final String SAVE_SETTINGS = "save settings";
	public static final String LOAD_SETTINGS = "load settings";

	static final String[] BRIGHTNESS_SETTINGS_KEYS         = new String[] { "S" };
	static final String[] SAVE_SETTINGS_KEYS               = new String[] { "F11" };
	static final String[] LOAD_SETTINGS_KEYS               = new String[] { "F12" };

	private final ToggleDialogAction toggleBrightnessDialogAction;

	private final RunnableAction saveBdvSettingsAction;

	private final RunnableAction loadBdvSettingsAction;


	/*
	 * Command descriptions for all provided commands
	 */
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
			descriptions.add( VISIBILITY_AND_GROUPING, VISIBILITY_AND_GROUPING_KEYS, "Show the Visibility&Grouping dialog." );
			descriptions.add( SET_BOOKMARK, SET_BOOKMARK_KEYS, "Set a labeled bookmark at the current location." );
			descriptions.add( GO_TO_BOOKMARK, GO_TO_BOOKMARK_KEYS, "Retrieve a labeled bookmark location." );
			descriptions.add( GO_TO_BOOKMARK_ROTATION, GO_TO_BOOKMARK_ROTATION_KEYS, "Retrieve a labeled bookmark, set only the orientation." );
			descriptions.add( BRIGHTNESS_SETTINGS,BRIGHTNESS_SETTINGS_KEYS, "Show the Brightness&Colors dialog." );
			descriptions.add( SAVE_SETTINGS, SAVE_SETTINGS_KEYS, "Save the BigDataViewer settings to a settings.xml file." );
			descriptions.add( LOAD_SETTINGS, LOAD_SETTINGS_KEYS, "Load the BigDataViewer settings from a settings.xml file." );
		}
	}

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
			final BigDataViewerMamut bdv )
	{
		final BigDataViewerActionsMamut ba = new BigDataViewerActionsMamut( bdv );
		actions.namedAction( ba.toggleVisibilityAndGroupingDialogAction, VISIBILITY_AND_GROUPING_KEYS );
		actions.namedAction( ba.goToBookmarkAction, GO_TO_BOOKMARK_KEYS );
		actions.namedAction( ba.goToBookmarkRotationAction, GO_TO_BOOKMARK_ROTATION_KEYS );
		actions.namedAction( ba.setBookmarkAction, SET_BOOKMARK_KEYS );

		/*
		 * TODO: move to app actions
		 *
		 * This requires modifications in bigdataviewer-core: The group setup
		 * should be shared between multiple windows.
		 */
		actions.namedAction( ba.toggleBrightnessDialogAction, BRIGHTNESS_SETTINGS_KEYS );
		actions.namedAction( ba.saveBdvSettingsAction, SAVE_SETTINGS_KEYS );
		actions.namedAction( ba.loadBdvSettingsAction, LOAD_SETTINGS_KEYS );
	}

	private BigDataViewerActionsMamut( final BigDataViewerMamut bdv )
	{
		toggleVisibilityAndGroupingDialogAction = new ToggleDialogAction( VISIBILITY_AND_GROUPING, bdv.getVisibilityAndGroupingDialog() );
		goToBookmarkAction = new RunnableAction( GO_TO_BOOKMARK, bdv.getBookmarksEditor()::initGoToBookmark );
		goToBookmarkRotationAction = new RunnableAction( GO_TO_BOOKMARK_ROTATION, bdv.getBookmarksEditor()::initGoToBookmarkRotation );
		setBookmarkAction = new RunnableAction( SET_BOOKMARK, bdv.getBookmarksEditor()::initSetBookmark );

		toggleBrightnessDialogAction = new ToggleDialogAction( BRIGHTNESS_SETTINGS, bdv.getBrightnessDialog() );
		saveBdvSettingsAction = new RunnableAction( SAVE_SETTINGS, bdv::saveSettings );
		loadBdvSettingsAction = new RunnableAction( LOAD_SETTINGS, bdv::loadSettings );
	}
}
