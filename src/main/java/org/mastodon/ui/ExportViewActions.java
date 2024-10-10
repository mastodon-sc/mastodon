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
