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
package net.trackmate.revised.bdv;

import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.tools.InitializeViewerState;
import bdv.tools.VisibilityAndGroupingDialog;
import bdv.tools.bookmarks.BookmarksEditor;
import bdv.tools.brightness.BrightnessDialog;
import bdv.viewer.NavigationActions;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerPanel;

public class BigDataViewerMaMuT
{
	private final ViewerFrame viewerFrame;

	private final ViewerPanel viewer;

	private final BrightnessDialog brightnessDialog;

	private final VisibilityAndGroupingDialog activeSourcesDialog;

	private final BookmarksEditor bookmarkEditor;

	public void initSetBookmark()
	{
		bookmarkEditor.initSetBookmark();
	}

	public void initGoToBookmark()
	{
		bookmarkEditor.initGoToBookmark();
	}

	public void initGoToBookmarkRotation()
	{
		bookmarkEditor.initGoToBookmarkRotation();
	}

	/**
	 *
	 * @param shared
	 * @param windowTitle
	 *            title of the viewer window.
	 */
	public BigDataViewerMaMuT(
			final SharedBigDataViewerData shared,
			final String windowTitle )
	{
		final InputTriggerConfig inputTriggerConfig = shared.getInputTriggerConfig();

		viewerFrame = new ViewerFrame(
				shared.getSources(),
				shared.getNumTimepoints(),
				shared.getCache(),
				shared.getOptions() );
		if ( windowTitle != null )
			viewerFrame.setTitle( windowTitle );
		viewer = viewerFrame.getViewerPanel();

		bookmarkEditor = new BookmarksEditor( viewer, viewerFrame.getKeybindings(), shared.getBookmarks() );

		brightnessDialog = shared.getBrightnessDialog();
		activeSourcesDialog = new VisibilityAndGroupingDialog( viewerFrame, viewer.getVisibilityAndGrouping() );

		NavigationActions.installActionBindings( viewerFrame.getKeybindings(), viewer, inputTriggerConfig );
		BigDataViewerActionsMaMuT.installActionBindings( viewerFrame.getKeybindings(), this, inputTriggerConfig );

		final JMenuBar menubar = new JMenuBar();
		final ActionMap actionMap = viewerFrame.getKeybindings().getConcatenatedActionMap();

		JMenu menu = new JMenu( "File" );
		menubar.add( menu );

		menu = new JMenu( "Settings" );
		menubar.add( menu );

		final JMenuItem miBrightness = new JMenuItem( actionMap.get( BigDataViewerActionsMaMuT.BRIGHTNESS_SETTINGS ) );
		miBrightness.setText( "Brightness & Color" );
		menu.add( miBrightness );

		final JMenuItem miVisibility = new JMenuItem( actionMap.get( BigDataViewerActionsMaMuT.VISIBILITY_AND_GROUPING ) );
		miVisibility.setText( "Visibility & Grouping" );
		menu.add( miVisibility );

		menu = new JMenu( "Help" );
		menubar.add( menu );

		viewerFrame.setJMenuBar( menubar );
	}

	public static BigDataViewerMaMuT open( final SharedBigDataViewerData shared, final String windowTitle )
	{
		final BigDataViewerMaMuT bdv = new BigDataViewerMaMuT( shared, windowTitle );
		bdv.viewerFrame.setVisible( true );
		InitializeViewerState.initTransform( bdv.viewer );
		return bdv;
	}

	public ViewerPanel getViewer()
	{
		return viewer;
	}

	public ViewerFrame getViewerFrame()
	{
		return viewerFrame;
	}

	public BrightnessDialog getBrightnessDialog()
	{
		return brightnessDialog;
	}

	public VisibilityAndGroupingDialog getActiveSourcesDialog()
	{
		return activeSourcesDialog;
	}
}
