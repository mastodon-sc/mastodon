package org.mastodon.mamut.io.importer.graphml;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mastodon.app.MastodonIcons;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.DialogType;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.ui.util.SetupIDComboBox;

import bdv.viewer.SourceAndConverter;

public class GraphMLImporterPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JTextField tfFile;

	private final SpinnerNumberModel spinnerModel;

	private final SetupIDComboBox setupIDComboBox;

	private static String previousFile = System.getProperty( "user.home" );

	public GraphMLImporterPanel( final List< SourceAndConverter< ? > > sources, final String units )
	{
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblFile = new JLabel( "GraphML file" );
		final GridBagConstraints gbcLblFile = new GridBagConstraints();
		gbcLblFile.anchor = GridBagConstraints.WEST;
		gbcLblFile.insets = new Insets( 0, 0, 5, 5 );
		gbcLblFile.gridx = 0;
		gbcLblFile.gridy = 0;
		add( lblFile, gbcLblFile );

		final JButton btnBrowse = new JButton( "Browse" );
		final GridBagConstraints gbcBtnBrowse = new GridBagConstraints();
		gbcBtnBrowse.gridwidth = 2;
		gbcBtnBrowse.anchor = GridBagConstraints.EAST;
		gbcBtnBrowse.insets = new Insets( 0, 0, 5, 5 );
		gbcBtnBrowse.gridx = 1;
		gbcBtnBrowse.gridy = 0;
		add( btnBrowse, gbcBtnBrowse );

		tfFile = new JTextField( previousFile );
		final GridBagConstraints gbcTextField = new GridBagConstraints();
		gbcTextField.insets = new Insets( 0, 0, 5, 5 );
		gbcTextField.gridwidth = 3;
		gbcTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTextField.gridx = 0;
		gbcTextField.gridy = 1;
		add( tfFile, gbcTextField );
		tfFile.setColumns( 10 );

		final JLabel lblSetup = new JLabel( "Setup ID" );
		final GridBagConstraints gbcLblSetup = new GridBagConstraints();
		gbcLblSetup.anchor = GridBagConstraints.EAST;
		gbcLblSetup.insets = new Insets( 0, 0, 5, 5 );
		gbcLblSetup.gridx = 0;
		gbcLblSetup.gridy = 2;
		add( lblSetup, gbcLblSetup );

		this.setupIDComboBox = new SetupIDComboBox( sources );
		final GridBagConstraints gbcSetupIDComboBox = new GridBagConstraints();
		gbcSetupIDComboBox.gridwidth = 2;
		gbcSetupIDComboBox.insets = new Insets( 0, 0, 5, 5 );
		gbcSetupIDComboBox.anchor = GridBagConstraints.EAST;
		gbcSetupIDComboBox.gridx = 1;
		gbcSetupIDComboBox.gridy = 2;
		add( setupIDComboBox, gbcSetupIDComboBox );

		final JLabel lblRadius = new JLabel( "Spot radius" );
		final GridBagConstraints gbcLblRadius = new GridBagConstraints();
		gbcLblRadius.insets = new Insets( 0, 0, 0, 5 );
		gbcLblRadius.gridx = 0;
		gbcLblRadius.gridy = 3;
		add( lblRadius, gbcLblRadius );

		this.spinnerModel = new SpinnerNumberModel( 2.5, 0.1, 100, 0.1 );
		final JSpinner spinner = new JSpinner( spinnerModel );
		final GridBagConstraints gbcSpinner = new GridBagConstraints();
		gbcSpinner.anchor = GridBagConstraints.EAST;
		gbcSpinner.insets = new Insets( 0, 0, 0, 5 );
		gbcSpinner.gridx = 1;
		gbcSpinner.gridy = 3;
		add( spinner, gbcSpinner );

		final JLabel lblUnits = new JLabel( units );
		final GridBagConstraints gbcLblUnits = new GridBagConstraints();
		gbcLblUnits.gridx = 2;
		gbcLblUnits.gridy = 3;
		add( lblUnits, gbcLblUnits );
		
		btnBrowse.addActionListener( e -> browse() );
	}

	private void browse()
	{
		final File file = FileChooser.chooseFile(
				false,
				null,
				tfFile.getText(),
				new FileNameExtensionFilter( "GraphML files", "graphml" ),
				"Browse to a GraphML file",
				DialogType.LOAD,
				SelectionMode.FILES_ONLY,
				MastodonIcons.MASTODON_ICON_MEDIUM.getImage() );
		if ( file != null )
		{
			tfFile.setText( file.getAbsolutePath() );
			previousFile = file.getAbsolutePath();
		}
	}

	public String getPath()
	{
		return tfFile.getText();
	}

	public double getRadius()
	{
		return spinnerModel.getNumber().doubleValue();
	}

	public int getSetupID()
	{
		return setupIDComboBox.getSelectedSetupID();
	}
}
