package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.simi.SimiImporter;
import org.mastodon.revised.model.mamut.simi.SimiImporter.LabelFunction;
import org.mastodon.revised.model.mamut.tgmm.TgmmImporter;
import org.mastodon.revised.ui.util.ExtensionFileFilter;
import org.mastodon.revised.ui.util.FileChooser;

import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.sequence.TimePoint;

public class SimiImportDialog extends JDialog
{
	private final JTextField pathTextField;

	private final JSpinner spinnerSetup;

	private final JCheckBox interpolateCheckbox;

	private final JTextField radiusTextField;

	private AbstractSpimData< ? > spimData;

	private Model model;

	public SimiImportDialog( final Frame owner )
	{
		super( owner, true );

		final JPanel content = new JPanel();
		getContentPane().add( content, BorderLayout.CENTER );
		content.setLayout( new GridBagLayout() );

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.ipadx = 0;
		c.ipady = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridy = 0;
		c.gridx = 0;
		content.add( new JLabel( "import from" ), c );
		pathTextField = new JTextField( "/Users/pietzsch/Desktop/SIMI/040612_300312_A190_TL71-418_AP_DV_boundary_curated.sbd" );
		c.gridx = 1;
		content.add( pathTextField, c );
		pathTextField.setColumns( 20 );
		final JButton browseButton = new JButton( "Browse" );
		c.gridx = 2;
		content.add( browseButton, c );

		c.gridy++;
		c.gridx = 0;
		content.add( new JLabel( "transform of setup" ), c );
		spinnerSetup = new JSpinner();
		spinnerSetup.setModel( new SpinnerNumberModel( 0, 0, 0, 1 ) );
		c.gridx = 1;
		content.add( spinnerSetup, c );

		c.gridy++;
		c.gridx = 1;
		interpolateCheckbox = new JCheckBox( "interpolate missing frames", true );
		content.add( interpolateCheckbox, c );

		c.gridy++;
		c.gridx = 0;
		content.add( new JLabel( "spot radius" ), c );
		radiusTextField = new JTextField( "10" );
		c.gridx = 1;
		content.add( radiusTextField, c );

		c.gridy++;
		c.gridx = 0;
		content.add( Box.createVerticalStrut( 15 ), c );

		c.gridy++;
		c.gridx = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		final JButton importButton = new JButton( "Import" );
		content.add( importButton, c );

		c.gridx = 2;
		c.anchor = GridBagConstraints.WEST;
		final JButton cancelButton = new JButton( "Cancel" );
		content.add( cancelButton, c );

		browseButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final File file = FileChooser.chooseFile(
						SimiImportDialog.this,
						null,
						new ExtensionFileFilter( "sbd" ),
						"Open .sbd File",
						FileChooser.DialogType.LOAD );
				if ( file != null )
					pathTextField.setText( file.getAbsolutePath() );
			}
		} );

		importButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				importButton.setEnabled( false );
				cancelButton.setEnabled( false );
				doImport();
				setVisible( false );
				importButton.setEnabled( true );
				cancelButton.setEnabled( true );
			}
		} );

		cancelButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				setVisible( false );
			}
		} );

		pack();
		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
	}

	private void doImport()
	{
		try
		{
			final String sbdFilename = pathTextField.getText();

			final int setupIndex = ( Integer ) spinnerSetup.getValue();
			final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
			final int setupID = seq.getViewSetupsOrdered().get( setupIndex ).getId();

			final Map< TimePoint, Integer > timepointToIndex = TgmmImporter.getTimepointToIndex( spimData );

			final IntUnaryOperator timepointIdFunction = frame -> {
				final Integer t = timepointToIndex.get( new TimePoint( frame ) );
				return t != null ? t : frame;
			};
			final LabelFunction labelFunction = ( generic_name, generation_name, name ) -> {
				if ( name != null )
					return name;
				if ( generic_name != null )
					return generic_name;
				if ( generation_name != null )
					return generation_name;
				return null;
			};
			final BiFunction< Integer, double[], double[] > positionFunction = ( frame, lPos ) -> {
				final double[] gPos = new double[ 3 ];
				final int timepointId = timepointIdFunction.applyAsInt( frame );
				spimData.getViewRegistrations().getViewRegistration( timepointId, setupID ).getModel().apply( lPos, gPos );
				return gPos;
			};
			final int radius = Integer.parseInt( radiusTextField.getText() );
			final boolean interpolateMissingSpots = interpolateCheckbox.isSelected();
			SimiImporter.read( sbdFilename, timepointIdFunction, labelFunction, positionFunction, radius, interpolateMissingSpots, model );
		}
		catch ( final ParseException e )
		{
			e.printStackTrace();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void showImportDialog( final AbstractSpimData< ? > spimData, final Model model )
	{
		this.spimData = spimData;
		this.model = model;

		final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		final int numSetups = seq.getViewSetupsOrdered().size();
		spinnerSetup.setModel( new SpinnerNumberModel( 0, 0, numSetups - 1, 1 ) );

		setVisible( true );
	}

}
