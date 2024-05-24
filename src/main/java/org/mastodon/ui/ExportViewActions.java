package org.mastodon.ui;

import org.mastodon.ui.util.ExportUtils;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExportViewActions
{
	public static final String EXPORT_VIEW_TO_SVG = "Export current view to SVG";

	protected static final String[] EXPORT_VIEW_TO_SVG_KEYS = new String[] { "ctrl P" };

	public static final String EXPORT_VIEW_TO_PNG = "Export current view to PNG";

	protected static final String[] EXPORT_VIEW_TO_PNG_KEYS = new String[] { "ctrl shift P" };

	private final RunnableAction exportToSvgAction;

	private final RunnableAction exportToPngAction;

	public static void install( final Actions actions, final JPanel jPanel, final JFrame jFrame,
			final String name )
	{
		new ExportViewActions( jPanel, jFrame, name ).install( actions );
	}

	private ExportViewActions( final JPanel jPanel, final JFrame jFrame, final String name )
	{
		exportToSvgAction = new RunnableAction( EXPORT_VIEW_TO_SVG, () -> ExportUtils.chooseFileAndExport(
				ExportUtils.SVG_EXTENSION, file -> ExportUtils.exportSvg( file, jPanel ), name, jFrame ) );
		exportToPngAction = new RunnableAction( EXPORT_VIEW_TO_PNG, () -> ExportUtils.chooseFileAndExport(
				ExportUtils.PNG_EXTENSION, file -> ExportUtils.exportPng( file, jPanel ), name, jFrame ) );
	}

	private void install( final Actions actions )
	{
		actions.namedAction( exportToSvgAction, EXPORT_VIEW_TO_SVG_KEYS );
		actions.namedAction( exportToPngAction, EXPORT_VIEW_TO_PNG_KEYS );
	}
}
