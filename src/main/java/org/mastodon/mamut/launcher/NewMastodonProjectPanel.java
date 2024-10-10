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
package org.mastodon.mamut.launcher;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;

class NewMastodonProjectPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	final JButton btnCreate;

	final JLabel labelInfo;

	final JTextArea textAreaFile;

	final JComboBox< String > comboBox;

	final JRadioButton rdbtBrowseToBDV;

	public NewMastodonProjectPanel( final String panelTitle, final String buttonTitle )
	{
		final GridBagLayout gblNewMastodonProjectPanel = new GridBagLayout();
		gblNewMastodonProjectPanel.columnWidths = new int[] { 0, 0 };
		gblNewMastodonProjectPanel.rowHeights = new int[] { 35, 70, 65, 0, 25, 45, 0, 0, 25, 0, 0, 0 };
		gblNewMastodonProjectPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblNewMastodonProjectPanel.rowWeights =
				new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout( gblNewMastodonProjectPanel );

		final JLabel lblNewMastodonProject = new JLabel( panelTitle );
		lblNewMastodonProject.setFont(
				lblNewMastodonProject.getFont().deriveFont( lblNewMastodonProject.getFont().getStyle() | Font.BOLD ) );
		final GridBagConstraints gbcLblNewMastodonProject = new GridBagConstraints();
		gbcLblNewMastodonProject.insets = new Insets( 5, 5, 5, 5 );
		gbcLblNewMastodonProject.gridx = 0;
		gbcLblNewMastodonProject.gridy = 0;
		add( lblNewMastodonProject, gbcLblNewMastodonProject );

		rdbtBrowseToBDV = new JRadioButton( "Browse to a BDV file (xml + N5/OME-Zarr/HDF5 pair):" );
		final GridBagConstraints gbc_rdbtBrowseToBDV = new GridBagConstraints();
		gbc_rdbtBrowseToBDV.anchor = GridBagConstraints.SOUTHWEST;
		gbc_rdbtBrowseToBDV.insets = new Insets( 5, 5, 5, 5 );
		gbc_rdbtBrowseToBDV.gridx = 0;
		gbc_rdbtBrowseToBDV.gridy = 1;
		add( rdbtBrowseToBDV, gbc_rdbtBrowseToBDV );

		textAreaFile = new JTextArea();
		textAreaFile.setLineWrap( true );
		final GridBagConstraints gbcTextAreaFile = new GridBagConstraints();
		gbcTextAreaFile.insets = new Insets( 5, 5, 5, 5 );
		gbcTextAreaFile.fill = GridBagConstraints.BOTH;
		gbcTextAreaFile.gridx = 0;
		gbcTextAreaFile.gridy = 2;
		add( textAreaFile, gbcTextAreaFile );

		final JButton btnBrowse = new JButton( "browse" );
		final GridBagConstraints gbcBtnBrowse = new GridBagConstraints();
		gbcBtnBrowse.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnBrowse.anchor = GridBagConstraints.EAST;
		gbcBtnBrowse.gridx = 0;
		gbcBtnBrowse.gridy = 3;
		add( btnBrowse, gbcBtnBrowse );

		final GridBagConstraints gbcSeparator1 = new GridBagConstraints();
		gbcSeparator1.fill = GridBagConstraints.BOTH;
		gbcSeparator1.insets = new Insets( 5, 5, 5, 5 );
		gbcSeparator1.gridx = 0;
		gbcSeparator1.gridy = 4;
		add( new JSeparator(), gbcSeparator1 );

		final JRadioButton rdbtnOpenedImage = new JRadioButton( "Use an image opened in ImageJ: " );
		final GridBagConstraints gbc_rdbtnOpenedImage = new GridBagConstraints();
		gbc_rdbtnOpenedImage.anchor = GridBagConstraints.SOUTHWEST;
		gbc_rdbtnOpenedImage.insets = new Insets( 5, 5, 5, 5 );
		gbc_rdbtnOpenedImage.gridx = 0;
		gbc_rdbtnOpenedImage.gridy = 5;
		add( rdbtnOpenedImage, gbc_rdbtnOpenedImage );

		comboBox = new JComboBox<>();
		final GridBagConstraints gbcComboBox = new GridBagConstraints();
		gbcComboBox.insets = new Insets( 5, 5, 5, 5 );
		gbcComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBox.gridx = 0;
		gbcComboBox.gridy = 6;
		add( comboBox, gbcComboBox );

		final JButton btnRefresh = new JButton( "refresh" );
		final GridBagConstraints gbcBtnRefresh = new GridBagConstraints();
		gbcBtnRefresh.anchor = GridBagConstraints.EAST;
		gbcBtnRefresh.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnRefresh.gridx = 0;
		gbcBtnRefresh.gridy = 7;
		add( btnRefresh, gbcBtnRefresh );

		final GridBagConstraints gbcSeparator2 = new GridBagConstraints();
		gbcSeparator2.fill = GridBagConstraints.BOTH;
		gbcSeparator2.insets = new Insets( 5, 5, 5, 5 );
		gbcSeparator2.gridx = 0;
		gbcSeparator2.gridy = 8;
		add( new JSeparator(), gbcSeparator2 );

		labelInfo = new JLabel( "" );
		final GridBagConstraints gbcLabelInfo = new GridBagConstraints();
		gbcLabelInfo.insets = new Insets( 5, 5, 5, 5 );
		gbcLabelInfo.fill = GridBagConstraints.BOTH;
		gbcLabelInfo.gridx = 0;
		gbcLabelInfo.gridy = 9;
		add( labelInfo, gbcLabelInfo );

		btnCreate = new JButton( buttonTitle );
		final GridBagConstraints gbcBtnCreate = new GridBagConstraints();
		gbcBtnCreate.anchor = GridBagConstraints.EAST;
		gbcBtnCreate.gridx = 0;
		gbcBtnCreate.gridy = 10;
		add( btnCreate, gbcBtnCreate );

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtBrowseToBDV );
		buttonGroup.add( rdbtnOpenedImage );

		/*
		 * Wire listeners.
		 */

		btnBrowse.addActionListener( l -> LauncherUtil.browseToBDVFile(
				null,
				textAreaFile,
				() -> checkBDVFile(),
				this ) );
		LauncherUtil.decorateJComponent( textAreaFile, () -> checkBDVFile() );

		rdbtBrowseToBDV.addItemListener( e -> {
			final boolean selected = rdbtBrowseToBDV.isSelected();
			btnBrowse.setEnabled( selected );
			textAreaFile.setEnabled( selected );
			comboBox.setEnabled( !selected );
			btnRefresh.setEnabled( !selected );
		} );

		btnRefresh.addActionListener( e -> refreshImpList() );
		refreshImpList();
		rdbtBrowseToBDV.setSelected( true );
	}

	private void refreshImpList()
	{
		final String[] imageNames = ij.WindowManager.getImageTitles();
		final DefaultComboBoxModel< String > model = new DefaultComboBoxModel<>( imageNames );
		comboBox.setModel( model );
	}

	boolean checkBDVFile()
	{
		final File file = new File( textAreaFile.getText() );
		try
		{
			final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( file.getAbsolutePath() );
			final String str = LauncherUtil.buildInfoString( spimData );
			labelInfo.setText( str );
			return true;
		}
		catch ( final SpimDataException | RuntimeException e )
		{
			labelInfo.setText( "<html>Invalid BDV xml/h5 file.<p>" + e.getMessage() + "</html>" );
			return false;
		}
	}
}
