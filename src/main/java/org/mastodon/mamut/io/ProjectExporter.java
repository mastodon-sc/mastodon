/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io;

import static org.mastodon.app.MastodonIcons.MAMUT_EXPORT_ICON_MEDIUM;
import static org.mastodon.mamut.io.ProjectSaver.EXT_DOT_MASTODON;
import static org.mastodon.mamut.io.ProjectSaver.stripExtensionIfPresent;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.trackmate.MamutExporter;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.XmlFileFilter;

/**
 * Static methods for exporting projects to other file formats.
 */
public class ProjectExporter
{

	public static synchronized void exportMamut( final ProjectModel appModel, final Component parentComponent )
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
