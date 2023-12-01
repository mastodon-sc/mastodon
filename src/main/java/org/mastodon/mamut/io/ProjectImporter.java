/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.app.MastodonIcons.MAMUT_IMPORT_ICON_MEDIUM;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.simi.SimiImportDialog;
import org.mastodon.mamut.io.importer.tgmm.TgmmImportDialog;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImporter;
import org.mastodon.mamut.launcher.LauncherUtil;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.XmlFileFilter;
import org.scijava.Context;

/**
 * Static methods for creating projects from other file formats.
 */
public class ProjectImporter
{

	/**
	 * Creates and opens a new Mastodon project from a Fiji MaMuT file. The user
	 * is prompted for the MaMuT file.
	 * 
	 * @param parentComponent
	 *            a component used as parent for dialogs.
	 * @param context
	 *            the current context.
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return the loaded {@link ProjectModel}, or <code>null</code> if the user
	 *         cancels loading or if there is a problem reading the data.
	 */
	public static synchronized ProjectModel openMamutWithDialog( final Component parentComponent, final Context context, final Consumer< String > errorConsumer )
	{
		final File file = FileChooser.chooseFile(
				parentComponent,
				null,
				new XmlFileFilter(),
				"Import MaMuT Project",
				FileChooser.DialogType.LOAD,
				MAMUT_IMPORT_ICON_MEDIUM.getImage() );
		if ( file == null )
			return null;

		try
		{
			final TrackMateImporter importer = new TrackMateImporter( file );
			final ProjectModel appModel = LauncherUtil.openWithDialog( importer.createProject(), context, parentComponent, errorConsumer );
			final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
			importer.readModel( appModel.getModel(), featureSpecsService );
			return appModel;
		}
		catch ( final IOException e )
		{
			errorConsumer.accept( "Problem reading MaMuT file:\n" + e.getMessage() );
		}
		return null;
	}

	/**
	 * Shows an importer window that allows importing data from a Simi Biocell
	 * file. The cell data is <b>added</b> to the existing project.
	 * 
	 * @see <a href=
	 *      "http://www.simi.com/en/products/cell-research.html">http://www.simi.com/en/products/cell-research.html</a>
	 * 
	 * @param appModel
	 *            the project to add the data to.
	 * @param parentComponent
	 *            a frame to use as parent for the dialog.
	 */
	public static synchronized void importSimiDataWithDialog( final ProjectModel appModel, final Frame parentComponent )
	{
		final SimiImportDialog simiImportDialog = new SimiImportDialog( parentComponent );
		simiImportDialog.showImportDialog( appModel.getSharedBdvData().getSpimData(), appModel.getModel() );
	}

	/**
	 * Shows an importer window that allows importing data from a TGMM file. The
	 * cell data is <b>added</b> to the existing project.
	 * 
	 * @see <a href=
	 *      "https://github.com/KellerLabTeam/tgmm-docker">https://github.com/KellerLabTeam/tgmm-docker</a>
	 * 
	 * @param appModel
	 *            the project to add the data to.
	 * @param parentComponent
	 *            a frame to use as parent for the dialog.
	 */
	public static synchronized void importTgmmDataWithDialog( final ProjectModel appModel, final Frame parentComponent )
	{
		final TgmmImportDialog tgmmImportDialog = new TgmmImportDialog( parentComponent );
		tgmmImportDialog.showImportDialog( appModel.getSharedBdvData().getSpimData(), appModel.getModel() );
	}
}
