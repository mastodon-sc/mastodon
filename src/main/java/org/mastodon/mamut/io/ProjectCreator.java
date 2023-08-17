package org.mastodon.mamut.io;

import static org.mastodon.app.MastodonIcons.NEW_ICON_MEDIUM;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.embl.mobie.io.util.S3Utils;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.XmlFileFilter;
import org.scijava.Context;

import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

/**
 * Static methods to create new Mastodon Mamut projects from images.
 */
public class ProjectCreator
{

	/**
	 * Interactively creates a new project prompting the user for a path to a
	 * BDV/XML file.
	 * <p>
	 * A dialog is shown to prompt the user for the path to the XML file. If the
	 * image data cannot be loaded a dialog shows up telling the user about the
	 * problem, and offering to start Mastodon on substituted dummy image data.
	 * 
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @return a new {@link ProjectModel} or <code>null</code> if the user
	 *         clicked cancel, or if the BDV file is faulty and the user
	 *         declined to substitute a dummy dataset.
	 */
	public static synchronized ProjectModel createProjectWithDialog( final Context context, final Component parentComponent )
	{
		final File file = FileChooser.chooseFile(
				parentComponent,
				null,
				new XmlFileFilter(),
				"Open BigDataViewer File",
				FileChooser.DialogType.LOAD,
				NEW_ICON_MEDIUM.getImage() );
		if ( file == null )
			return null;

		try
		{
			return createProjectFromBdvFileWithDialog( file, context, parentComponent );
		}
		catch ( final SpimDataException e )
		{
			JOptionPane.showMessageDialog(
					parentComponent,
					"Problem reading image file:\n" + e.getMessage(),
					"Error reading image data",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Interactively creates a new project from a BDV/XML file.
	 * <p>
	 * If the specified BDV file cannot be loaded a dialog shows up telling the
	 * user about the problem, and offering to start Mastodon on substituted
	 * dummy image data.
	 * 
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @return a new {@link ProjectModel}.
	 * @throws SpimDataException
	 *             if the BDV file that cannot be opened, and the user declined
	 *             to substitute a dummy dataset.
	 */
	public static ProjectModel createProjectFromBdvFileWithDialog( final File file, final Context context, final Component parentComponent ) throws SpimDataException
	{
		final MamutProject project = MamutProjectIO.fromBdvFile( file );
		try
		{
			return ProjectLoader.openWithDialog( project, context, parentComponent );
		}
		catch ( final IOException e )
		{
			// Should not happen because the data model and the GUI state are empty.
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a new project from a BDV/XML file.
	 * 
	 * @param file
	 *            the BDV file.
	 * @param context
	 *            the current context.
	 * @return a new {@link ProjectModel}.
	 * @throws SpimDataException
	 *             if the BDV file cannot be opened properly.
	 */
	public static ProjectModel createProjectFromBdvFile( final File file, final Context context ) throws SpimDataException
	{
		final MamutProject project = MamutProjectIO.fromBdvFile( file );
		try
		{
			return ProjectLoader.open( project, context );
		}
		catch ( final IOException e )
		{
			// Should not happen because the data model and the GUI state are empty.
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Interactively creates a new project from a remote file.
	 * <p>
	 * A dialog is shown to prompt the user for the URL of the image, then
	 * another one to resave the resulting BDV/XML file pointing to the remote
	 * URL.
	 * 
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @return a new {@link ProjectModel} or <code>null</code> if the user
	 *         clicked cancel, or if the image data cannot be accessed.
	 */
	public static synchronized ProjectModel createProjectFromUrl( final Context context, final Component parentComponent )
	{
		final String urlString = JOptionPane.showInputDialog( parentComponent, "Please input a url for image data" );
		if ( urlString == null )
			return null;

		SpimData spimData = null;
		try
		{
			spimData = OMEZarrS3Opener.readURL( urlString );
		}
		catch ( final RuntimeException e )
		{
			final JLabel lblUsername = new JLabel( "Username" );
			final JTextField textFieldUsername = new JTextField();
			final JLabel lblPassword = new JLabel( "Password" );
			final JPasswordField passwordField = new JPasswordField();
			final Object[] ob = { lblUsername, textFieldUsername, lblPassword, passwordField };
			final int result =
					JOptionPane.showConfirmDialog( parentComponent, ob, "Please input credentials", JOptionPane.OK_CANCEL_OPTION );

			if ( result == JOptionPane.OK_OPTION )
			{
				final String username = textFieldUsername.getText();
				final char[] password = passwordField.getPassword();
				try
				{
					S3Utils.setS3AccessAndSecretKey( new String[] { username, new String( password ) } );
				}
				finally
				{
					Arrays.fill( password, '0' );
				}
				try
				{
					spimData = OMEZarrS3Opener.readURL( urlString );
				}
				catch ( final Exception e1 )
				{
					e1.printStackTrace();
				}
			}
			else
			{
				return null;
			}
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}

		final File file = FileChooser.chooseFile(
				parentComponent,
				null,
				new XmlFileFilter(),
				"Save BigDataViewer File",
				FileChooser.DialogType.SAVE,
				NEW_ICON_MEDIUM.getImage() );
		if ( file == null )
			return null;

		final XmlIoSpimData xmlIoSpimData = new XmlIoSpimData();
		spimData.setBasePath( file.getParentFile() );
		try
		{
			xmlIoSpimData.save( spimData, file.getAbsolutePath() );
			return ProjectLoader.open( MamutProjectIO.fromBdvFile( file ), context );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a new project from an {@link ImagePlus}.
	 * 
	 * @param imp
	 *            the source image.
	 * @param context
	 *            the current context.
	 * @return a new {@link ProjectModel}.
	 */
	public static ProjectModel createProjectFromImp( final ImagePlus imp, final Context context ) throws SpimDataException
	{
		final MamutProject project = MamutProjectIO.fromImagePlus( imp );
		try
		{
			return ProjectLoader.open( project, context );
		}
		catch ( final IOException e )
		{
			// Should not happen because the data model and the GUI state are empty.
			e.printStackTrace();
		}
		return null;
	}
}
