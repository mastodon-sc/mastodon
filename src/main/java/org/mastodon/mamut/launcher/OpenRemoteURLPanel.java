package org.mastodon.mamut.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.embl.mobie.io.util.S3Utils;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.XmlFileFilter;

import mpicbg.spim.data.SpimData;

public class OpenRemoteURLPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	SpimData spimData;

	final JLabel log;

	final JTextArea taFileSave;

	final JButton btnCreate;

	public OpenRemoteURLPanel()
	{
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 90, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 35, 70, 65, 0, 50, 65, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblTitle = new JLabel( "Open a remote image" );
		lblTitle.setFont( lblTitle.getFont().deriveFont( lblTitle.getFont().getStyle() | Font.BOLD ) );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 2;
		gbcLblTitle.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		add( lblTitle, gbcLblTitle );

		final JLabel lblImgUrl = new JLabel( "Image URL:" );
		final GridBagConstraints gbcLblImgUrl = new GridBagConstraints();
		gbcLblImgUrl.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblImgUrl.gridwidth = 2;
		gbcLblImgUrl.insets = new Insets( 5, 5, 5, 5 );
		gbcLblImgUrl.gridx = 0;
		gbcLblImgUrl.gridy = 1;
		add( lblImgUrl, gbcLblImgUrl );

		final JTextArea taURL = new JTextArea();
		taURL.setLineWrap( true );
		final GridBagConstraints gbcTextArea = new GridBagConstraints();
		gbcTextArea.gridwidth = 2;
		gbcTextArea.insets = new Insets( 5, 5, 5, 5 );
		gbcTextArea.fill = GridBagConstraints.BOTH;
		gbcTextArea.gridx = 0;
		gbcTextArea.gridy = 2;
		add( taURL, gbcTextArea );

		final JButton btnOpen = new JButton( "open" );
		final GridBagConstraints gbcBtnOpen = new GridBagConstraints();
		gbcBtnOpen.anchor = GridBagConstraints.EAST;
		gbcBtnOpen.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnOpen.gridx = 1;
		gbcBtnOpen.gridy = 3;
		add( btnOpen, gbcBtnOpen );

		final JLabel lblResave = new JLabel( "Resave BDV file to:" );
		final GridBagConstraints gbcLblResave = new GridBagConstraints();
		gbcLblResave.insets = new Insets( 5, 5, 5, 5 );
		gbcLblResave.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblResave.gridwidth = 2;
		gbcLblResave.gridx = 0;
		gbcLblResave.gridy = 4;
		add( lblResave, gbcLblResave );

		taFileSave = new JTextArea();
		taFileSave.setLineWrap( true );
		final GridBagConstraints gbcTAFileSave = new GridBagConstraints();
		gbcTAFileSave.gridwidth = 2;
		gbcTAFileSave.insets = new Insets( 5, 5, 5, 5 );
		gbcTAFileSave.fill = GridBagConstraints.BOTH;
		gbcTAFileSave.gridx = 0;
		gbcTAFileSave.gridy = 5;
		add( taFileSave, gbcTAFileSave );

		final JButton btnBrowse = new JButton( "browse" );
		final GridBagConstraints gbcBtnBrowse = new GridBagConstraints();
		gbcBtnBrowse.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnBrowse.anchor = GridBagConstraints.EAST;
		gbcBtnBrowse.gridx = 1;
		gbcBtnBrowse.gridy = 6;
		add( btnBrowse, gbcBtnBrowse );

		log = new JLabel( " " );
		log.setFont( log.getFont().deriveFont( log.getFont().getStyle() | Font.ITALIC ) );
		final GridBagConstraints gbcLbLog = new GridBagConstraints();
		gbcLbLog.fill = GridBagConstraints.BOTH;
		gbcLbLog.insets = new Insets( 5, 5, 5, 5 );
		gbcLbLog.gridwidth = 2;
		gbcLbLog.gridx = 0;
		gbcLbLog.gridy = 7;
		add( log, gbcLbLog );

		btnCreate = new JButton( "create" );
		final GridBagConstraints gbcBtnCreate = new GridBagConstraints();
		gbcBtnCreate.gridwidth = 2;
		gbcBtnCreate.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnCreate.gridx = 0;
		gbcBtnCreate.gridy = 8;
		add( btnCreate, gbcBtnCreate );

		/*
		 * Wire listeners.
		 */

		btnOpen.addActionListener( l -> parseURL( taURL.getText() ) );
		LauncherUtil.decorateJComponent( taURL, () -> parseURL( taURL.getText() ) );
		btnBrowse.addActionListener( l -> browseSaveToBDVFile(
				null,
				taFileSave,
				() -> {},
				this ) );
		LauncherUtil.decorateJComponent( taFileSave, () -> {} );
	}

	private void parseURL( final String urlString )
	{
		spimData = null;
		if ( urlString == null || urlString.isEmpty() )
		{
			log.setForeground( Color.RED.darker() );
			log.setText( "Please enter a URL pointing to an image." );
			return;
		}

		// Check that we have a reachable URL.
		try
		{
			new URL( urlString );
		}
		catch ( final MalformedURLException e2 )
		{
			log.setForeground( Color.RED.darker() );
			log.setText( "Malformed URL." + LauncherUtil.toMessage( e2 ) );
			return;
		}

		final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );
		disabler.disable();
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					log.setForeground( Color.BLUE.darker() );
					log.setText( "Inspecting image URL..." );
					spimData = OMEZarrS3Opener.readURL( urlString );
					final String str = LauncherUtil.buildInfoString( spimData );
					log.setForeground( Color.BLACK );
					log.setText( str );
				}
				catch ( final RuntimeException e )
				{
					log.setForeground( Color.BLUE.darker() );
					log.setText( "Image access requires credentials." );

					final JLabel lblUsername = new JLabel( "Username" );
					final JTextField textFieldUsername = new JTextField();
					final JLabel lblPassword = new JLabel( "Password" );
					final JPasswordField passwordField = new JPasswordField();
					final Object[] ob = { lblUsername, textFieldUsername, lblPassword, passwordField };
					final int result = JOptionPane.showConfirmDialog( null, ob, "Please input credentials", JOptionPane.OK_CANCEL_OPTION );

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
							final String str = LauncherUtil.buildInfoString( spimData );
							log.setForeground( Color.BLACK );
							log.setText( str );
						}
						catch ( final Exception e1 )
						{
							log.setForeground( Color.RED.darker() );
							log.setText( "Error opening remote image with credentials:\n" + e1.getMessage() );
						}
					}
					else
					{
						log.setForeground( Color.RED.darker() );
						log.setText( "Please enter username and password." );
						return;
					}
				}
				catch ( final Exception e )
				{
					log.setForeground( Color.RED.darker() );
					log.setText( "Error opening remote image:\n" + e.getMessage() );
				}
				finally
				{
					disabler.reenable();
				}
			};
		}.start();
	}

	private static final void browseSaveToBDVFile( final String suggestedFile, final JTextArea target, final Runnable onSucess, final JComponent parent )
	{
		final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( parent, new Class[] { JLabel.class } );
		disabler.disable();
		try
		{
			final File file = FileChooser.chooseFile(
					parent,
					suggestedFile,
					new XmlFileFilter(),
					"Save to a BigDataViewer File",
					FileChooser.DialogType.SAVE );
			if ( file == null )
				return;

			target.setText( file.getAbsolutePath() );
			onSucess.run();
		}
		finally
		{
			disabler.reenable();
		}
	}
}
