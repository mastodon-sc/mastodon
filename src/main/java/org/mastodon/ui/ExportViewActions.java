/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui;

import java.awt.Component;

import javax.swing.JFrame;

import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.ui.util.ExportUtils;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

public class ExportViewActions
{
	public static final String EXPORT_VIEW_TO_SVG = "Export current view to SVG";

	protected static final String[] EXPORT_VIEW_TO_SVG_KEYS = new String[] { "ctrl P" };

	public static final String EXPORT_VIEW_TO_PNG = "Export current view to PNG";

	protected static final String[] EXPORT_VIEW_TO_PNG_KEYS = new String[] { "ctrl shift P" };

	private final RunnableAction exportToSvgAction;

	private final RunnableAction exportToPngAction;

	public static void install( final Actions actions, final Component comp, final JFrame frame,
			final String name )
	{
		new ExportViewActions( comp, frame, name ).install( actions );
	}

	private ExportViewActions( final Component comp, final JFrame frame, final String name )
	{
		exportToSvgAction = new RunnableAction( EXPORT_VIEW_TO_SVG, () -> ExportUtils.chooseFileAndExport(
				ExportUtils.SVG_EXTENSION, file -> ExportUtils.exportSvg( file, comp ), name, frame ) );
		exportToPngAction = new RunnableAction( EXPORT_VIEW_TO_PNG, () -> ExportUtils.chooseFileAndExport(
				ExportUtils.PNG_EXTENSION, file -> ExportUtils.exportPng( file, comp ), name, frame ) );
	}

	private void install( final Actions actions )
	{
		actions.namedAction( exportToSvgAction, EXPORT_VIEW_TO_SVG_KEYS );
		actions.namedAction( exportToPngAction, EXPORT_VIEW_TO_PNG_KEYS );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON,
					KeyConfigContexts.BIGDATAVIEWER,
					KeyConfigContexts.TRACKSCHEME,
					KeyConfigContexts.GRAPHER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( EXPORT_VIEW_TO_SVG, EXPORT_VIEW_TO_SVG_KEYS,
					"Capture the current view and export it to a SVG image file." );
			descriptions.add( EXPORT_VIEW_TO_PNG, EXPORT_VIEW_TO_PNG_KEYS,
					"Capture the current view and export it to a PNG image file." );
		}
	}
}
