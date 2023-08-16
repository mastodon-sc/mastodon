package org.mastodon.mamut.io;

import static org.mastodon.app.MastodonIcons.MAMUT_IMPORT_ICON_MEDIUM;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.importer.simi.SimiImportDialog;
import org.mastodon.mamut.importer.tgmm.TgmmImportDialog;
import org.mastodon.mamut.importer.trackmate.TrackMateImporter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.XmlFileFilter;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

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
	 * @return the loaded {@link MamutAppModel}, or <code>null</code> if the
	 *         user cancels loading or if there is a problem reading the data.
	 */
	public static synchronized MamutAppModel openMamutWithDialog( final Component parentComponent, final Context context )
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
			final MamutAppModel appModel = ProjectLoader.openWithDialog( importer.createProject(), context, parentComponent );
			final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
			importer.readModel( appModel.getModel(), featureSpecsService );
			return appModel;
		}
		catch ( IOException | SpimDataException e )
		{
			JOptionPane.showMessageDialog(
					parentComponent,
					"Problem reading MaMuT file:\n" + e.getMessage(),
					"Error reading MaMuT file",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Shows an importer window that allows importing data from a Simi Biocell
	 * file. The cell data is <b>added</b> to the existing project.
	 * 
	 * @see http://www.simi.com/en/products/cell-research.html
	 * 
	 * @param appModel
	 *            the project to add the data to.
	 * @param parentComponent
	 *            a frame to use as parent for the dialog.
	 */
	public static synchronized void importSimiDataWithDialog( final MamutAppModel appModel, final Frame parentComponent )
	{
		final SimiImportDialog simiImportDialog = new SimiImportDialog( parentComponent );
		simiImportDialog.showImportDialog( appModel.getSharedBdvData().getSpimData(), appModel.getModel() );
	}

	/**
	 * Shows an importer window that allows importing data from a TGMM file. The
	 * cell data is <b>added</b> to the existing project.
	 * 
	 * @see https://github.com/KellerLabTeam/tgmm-docker
	 * 
	 * @param appModel
	 *            the project to add the data to.
	 * @param parentComponent
	 *            a frame to use as parent for the dialog.
	 */
	public static synchronized void importTgmmDataWithDialog( final MamutAppModel appModel, final Frame parentComponent )
	{
		final TgmmImportDialog tgmmImportDialog = new TgmmImportDialog( parentComponent );
		tgmmImportDialog.showImportDialog( appModel.getSharedBdvData().getSpimData(), appModel.getModel() );
	}
}
