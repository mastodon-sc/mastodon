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

import java.awt.Dialog;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.scijava.ui.behaviour.KeyStrokeAdder;

import bdv.tools.ToggleDialogAction;
import bdv.tools.VisibilityAndGroupingDialog;
import bdv.tools.bookmarks.BookmarksEditor;
import bdv.tools.brightness.BrightnessDialog;
import bdv.util.AbstractActions;
import bdv.viewer.InputActionBindings;

public class BigDataViewerActionsMaMuT extends AbstractActions
{
	public static final String BRIGHTNESS_SETTINGS = "brightness settings";
	public static final String VISIBILITY_AND_GROUPING = "visibility and grouping";
	public static final String SET_BOOKMARK = "set bookmark";
	public static final String GO_TO_BOOKMARK = "go to bookmark";
	public static final String GO_TO_BOOKMARK_ROTATION = "go to bookmark rotation";
	public static final String TOGGLE_SETTINGS_PANEL = "bdv toggle settings panel";

	static final String[] BRIGHTNESS_SETTINGS_KEYS         = new String[] { "S" };
	static final String[] VISIBILITY_AND_GROUPING_KEYS     = new String[] { "F6" };
	static final String[] SET_BOOKMARK_KEYS                = new String[] { "shift B" };
	static final String[] GO_TO_BOOKMARK_KEYS              = new String[] { "B" };
	static final String[] GO_TO_BOOKMARK_ROTATION_KEYS     = new String[] { "O" };
	static final String[] TOGGLE_SETTINGS_PANEL_KEYS       = new String[] { "T" };


	/**
	 * Create BigDataViewer actions and install them in the specified
	 * {@link InputActionBindings}.
	 *
	 * @param inputActionBindings
	 *            {@link InputMap} and {@link ActionMap} are installed here.
	 * @param bdv
	 *            Actions are targeted at this {@link BigDataViewerMaMuT}.
	 * @param keyConfig
	 *            user-defined key-bindings.
	 */
	public static void installActionBindings(
			final InputActionBindings inputActionBindings,
			final BigDataViewerMaMuT bdv,
			final KeyStrokeAdder.Factory keyConfig )
	{
		final BigDataViewerActionsMaMuT actions = new BigDataViewerActionsMaMuT( inputActionBindings, keyConfig );
		actions.dialog( bdv.getBrightnessDialog() );
		actions.dialog( bdv.getActiveSourcesDialog() );
		actions.bookmarks( bdv.getBookmarksEditor() );
		actions.runnableAction( () -> bdv.setSettingsPanelVisible( !bdv.isSettingsPanelVisible() ), TOGGLE_SETTINGS_PANEL, TOGGLE_SETTINGS_PANEL_KEYS );
	}

	public BigDataViewerActionsMaMuT(
			final InputActionBindings inputActionBindings,
			final KeyStrokeAdder.Factory keyConfig )
	{
		this( inputActionBindings, keyConfig, "bdv" );
	}

	public BigDataViewerActionsMaMuT(
			final InputActionBindings inputActionBindings,
			final KeyStrokeAdder.Factory keyConfig,
			final String name )
	{
		super( inputActionBindings, name, keyConfig, new String[] { "bdv" } );
	}

	public void toggleDialogAction( final Dialog dialog, final String name, final String... defaultKeyStrokes )
	{
		keyStrokeAdder.put( name, defaultKeyStrokes );
		new ToggleDialogAction( name, dialog ).put( actionMap );
	}

	public void dialog( final BrightnessDialog brightnessDialog )
	{
		toggleDialogAction( brightnessDialog, BRIGHTNESS_SETTINGS, BRIGHTNESS_SETTINGS_KEYS );
	}

	public void dialog( final VisibilityAndGroupingDialog visibilityAndGroupingDialog )
	{
		toggleDialogAction( visibilityAndGroupingDialog, VISIBILITY_AND_GROUPING, VISIBILITY_AND_GROUPING_KEYS );
	}

	public void bookmarks( final BookmarksEditor bookmarksEditor )
	{
		runnableAction( bookmarksEditor::initGoToBookmark, GO_TO_BOOKMARK, GO_TO_BOOKMARK_KEYS );
		runnableAction( bookmarksEditor::initGoToBookmarkRotation, GO_TO_BOOKMARK_ROTATION, GO_TO_BOOKMARK_ROTATION_KEYS );
		runnableAction( bookmarksEditor::initSetBookmark, SET_BOOKMARK, SET_BOOKMARK_KEYS );
	}
}
