package org.mastodon.tomancak;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.mastodon.revised.ui.util.ExtensionFileFilter;
import org.mastodon.revised.ui.util.FileChooser;

public class MergingDialog extends JDialog
{
	private final JTextField pathATextField;

	private final JTextField pathBTextField;

	private final JTextField distCutoffTextField;

	private final JTextField mahalanobisDistCutoffTextField;

	private final JTextField ratioThresholdTextField;

	private Runnable onMerge;

	public MergingDialog( final Frame owner )
	{
		super( owner, "Merge Projects...", true );

		final JPanel content = new JPanel();
		getContentPane().add( content, BorderLayout.CENTER );
		final GridBagLayout l = new GridBagLayout();
		l.columnWeights = new double[] { 0, 1, 0 };
		content.setLayout( l );

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 5, 5, 5, 5 );
		c.ipadx = 0;
		c.ipady = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridy = 0;
		c.gridx = 0;
		content.add( new JLabel( "Project A" ), c );
		pathATextField = new JTextField( "" );
		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		content.add( pathATextField, c );
		pathATextField.setColumns( 20 );
		final JButton browseAButton = new JButton( "Browse" );
		c.gridx = 2;
		content.add( browseAButton, c );

		++c.gridy;
		c.gridx = 0;
		content.add( new JLabel( "Project B" ), c );
		pathBTextField = new JTextField( "" );
		c.gridx = 1;
		content.add( pathBTextField, c );
		pathBTextField.setColumns( 20 );
		final JButton browseBButton = new JButton( "Browse" );
		c.gridx = 2;
		content.add( browseBButton, c );

		++c.gridy;
		c.gridx = 0;
		content.add( new JLabel( "absolute distance cutoff" ), c );
		distCutoffTextField = new JTextField( "1000" );
		c.gridx = 1;
		content.add( distCutoffTextField, c );

		++c.gridy;
		c.gridx = 0;
		content.add( new JLabel( "mahalanobis distance cutoff" ), c );
		mahalanobisDistCutoffTextField = new JTextField( "1" );
		c.gridx = 1;
		content.add( mahalanobisDistCutoffTextField, c );

		++c.gridy;
		c.gridx = 0;
		content.add( new JLabel( "ratio threshold" ), c );
		ratioThresholdTextField = new JTextField( "2" );
		c.gridx = 1;
		content.add( ratioThresholdTextField, c );

		class Browse implements ActionListener
		{
			private final JTextField path;

			private final String dialogTitle;

			public Browse( final JTextField path, final String dialogTitle )
			{
				this.path = path;
				this.dialogTitle = dialogTitle;
			}

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final File file = FileChooser.chooseFile(
						true,
						MergingDialog.this,
						path.getText(),
						new ExtensionFileFilter( "mastodon" ),
						dialogTitle,
						FileChooser.DialogType.LOAD,
						FileChooser.SelectionMode.FILES_AND_DIRECTORIES );
				if ( file != null )
					path.setText( file.getAbsolutePath() );
			}
		}
		browseAButton.addActionListener( new Browse( pathATextField, "Select Mastodon Project A" ) );
		browseBButton.addActionListener( new Browse( pathBTextField, "Select Mastodon Project B" ) );

		final JPanel buttons = new JPanel();
		final JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( e -> cancel() );
		final JButton okButton = new JButton( "Merge" );
		okButton.addActionListener( e -> merge() );
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
		buttons.add( Box.createHorizontalGlue() );
		buttons.add( cancelButton );
		buttons.add( okButton );
		getContentPane().add( buttons, BorderLayout.SOUTH );

		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				cancel();
			}
		} );

		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Object hideKey = new Object();
		final Action hideAction = new AbstractAction()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				cancel();
			}

			private static final long serialVersionUID = 1L;
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		pack();
		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
	}

	private void cancel()
	{
		setVisible( false );
	}

	public void onMerge( final Runnable onMerge )
	{
		this.onMerge = onMerge;
	}

	public String getPathA()
	{
		return pathATextField.getText();
	}

	public String getPathB()
	{
		return pathBTextField.getText();
	}

	public double getDistCutoff() throws NumberFormatException
	{
		return Double.parseDouble( distCutoffTextField.getText() );
	}

	public double getMahalanobisDistCutoff() throws NumberFormatException
	{
		return Double.parseDouble( mahalanobisDistCutoffTextField.getText() );
	}

	public double getRatioThreshold() throws NumberFormatException
	{
		return Double.parseDouble( ratioThresholdTextField.getText() );
	}

	private void merge()
	{
		if ( onMerge != null )
			onMerge.run();
		setVisible( false );
	}

	public static void main( String[] args )
	{
		new MergingDialog( null ).setVisible( true );
	}
}
