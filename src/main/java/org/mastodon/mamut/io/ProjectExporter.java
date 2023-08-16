package org.mastodon.mamut.io;

import static org.mastodon.app.MastodonIcons.MAMUT_EXPORT_ICON_MEDIUM;
import static org.mastodon.mamut.io.ProjectSaver.EXT_DOT_MASTODON;
import static org.mastodon.mamut.io.ProjectSaver.stripExtensionIfPresent;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.importer.trackmate.MamutExporter;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.XmlFileFilter;

/**
 * Static methods for exporting projects to other file formats.
 */
public class ProjectExporter
{

	public static synchronized void exportMamut( final MamutAppModel appModel, final Component parentComponent )
	{
		final MamutProject project = appModel.getProject();
		final String filename = getProprosedMamutExportFileName( project );

		final File file = FileChooser.chooseFile(
				parentComponent,
				filename,
				new XmlFileFilter(),
				"Export to MaMuT file",
				FileChooser.DialogType.SAVE,
				MAMUT_EXPORT_ICON_MEDIUM.getImage() );
		if ( file == null )
			return;

		try
		{
			MamutExporter.export( file, appModel.getModel(), project );
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog(
					parentComponent,
					"Error exporting to MaMuT file:\n" + e.getMessage(),
					"MaMuT export error",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
	}

	private static String getProprosedMamutExportFileName( final MamutProject project )
	{
		final File pf = project.getProjectRoot();
		if ( pf != null )
		{
			final String fn = stripExtensionIfPresent( pf.getName(), EXT_DOT_MASTODON );
			return new File( pf.getParentFile(), fn + "_mamut.xml" ).getAbsolutePath();
		}
		else
		{
			final File f = project.getDatasetXmlFile();
			final String fn = stripExtensionIfPresent( f.getName(), ".xml" );
			return new File( f.getParentFile(), fn + "_mamut.xml" ).getAbsolutePath();
		}
	}
}
