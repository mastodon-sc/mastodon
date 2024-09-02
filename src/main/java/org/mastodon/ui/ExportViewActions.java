package org.mastodon.ui;

import java.awt.Component;

import javax.swing.JFrame;

import org.mastodon.ui.util.ExportUtils;
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
}
