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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.mastodon.grouping.GroupHandle;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.tools.VisibilityAndGroupingDialog;
import bdv.tools.bookmarks.BookmarksEditor;
import bdv.tools.brightness.BrightnessDialog;
import bdv.viewer.NavigationActions;
import bdv.viewer.ViewerPanel;

public class BigDataViewerMaMuT
{
	private final ViewerFrameMamut viewerFrame;

	private final ViewerPanel viewer;

	private final BrightnessDialog brightnessDialog;

	private final VisibilityAndGroupingDialog activeSourcesDialog;

	private final BookmarksEditor bookmarkEditor;

	private final JFileChooser fileChooser;

	private final SharedBigDataViewerData shared;

	/**
	 * Creates a new BDV window showing the image data overlaid with MaMuT
	 * annotations.
	 *
	 * @param shared
	 *            the shared BDV data.
	 * @param windowTitle
	 *            title of the viewer window.
	 * @param groupHandle
	 *            the group handle to manage view synchronization.
	 */
	public BigDataViewerMaMuT(
			final SharedBigDataViewerData shared,
			final String windowTitle,
			final GroupHandle groupHandle )
	{
		this.shared = shared;
		final InputTriggerConfig inputTriggerConfig = shared.getInputTriggerConfig();

		viewerFrame = new ViewerFrameMamut(
				windowTitle,
				shared.getSources(),
				shared.getNumTimepoints(),
				shared.getCache(),
				groupHandle,
				shared.getOptions() );
		viewer = viewerFrame.getViewerPanel();

		fileChooser = new JFileChooser();
		fileChooser.setFileFilter( new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return "xml files";
			}

			@Override
			public boolean accept( final File f )
			{
				if ( f.isDirectory() )
					return true;
				if ( f.isFile() )
				{
					final String s = f.getName();
					final int i = s.lastIndexOf( '.' );
					if ( i > 0 && i < s.length() - 1 )
					{
						final String ext = s.substring( i + 1 ).toLowerCase();
						return ext.equals( "xml" );
					}
				}
				return false;
			}
		} );

		bookmarkEditor = new BookmarksEditor( viewer, viewerFrame.getKeybindings(), shared.getBookmarks() );
		bookmarkEditor.setInputMapsToBlock( Arrays.asList( "all" ) );

		brightnessDialog = shared.getBrightnessDialog();
		activeSourcesDialog = new VisibilityAndGroupingDialog( viewerFrame, viewer.getVisibilityAndGrouping() );

		NavigationActions.installActionBindings( viewerFrame.getKeybindings(), viewer, inputTriggerConfig );
		BigDataViewerActionsMaMuT.installActionBindings( viewerFrame.getKeybindings(), this, inputTriggerConfig );
	}

	public static BigDataViewerMaMuT open( final SharedBigDataViewerData shared, final String windowTitle, final GroupHandle groupHandle )
	{
		final BigDataViewerMaMuT bdv = new BigDataViewerMaMuT( shared, windowTitle, groupHandle );
		return bdv;
	}

	public ViewerPanel getViewer()
	{
		return viewer;
	}

	public ViewerFrameMamut getViewerFrame()
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

	public BookmarksEditor getBookmarksEditor()
	{
		return bookmarkEditor;
	}

	protected void saveSettings()
	{
		fileChooser.setSelectedFile( shared.getProposedSettingsFile() );
		final int returnVal = fileChooser.showSaveDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			final File file = fileChooser.getSelectedFile();
			shared.setProposedSettingsFile( file );
			try
			{
				shared.saveSettings( file.getCanonicalPath(), viewer );
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	protected void loadSettings()
	{
		fileChooser.setSelectedFile( shared.getProposedSettingsFile()  );
		final int returnVal = fileChooser.showOpenDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			final File file = fileChooser.getSelectedFile();
			shared.setProposedSettingsFile( file );
			try
			{
				shared.loadSettings( file.getCanonicalPath(), viewer );
				activeSourcesDialog.update();
				viewer.repaint();
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
		}
	}
}
