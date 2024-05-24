/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;

public class DatasetPathDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	private final Path projectRootWoMastodonFile;

	public DatasetPathDialog( final Frame owner, final ProjectModel appModel )
	{
		super( owner, "Edit Dataset Path...", true );

		final MamutProject project = appModel.getProject();
		final boolean projectInContainerFile = project.getProjectRoot().isFile();
		projectRootWoMastodonFile = projectInContainerFile ? project.getProjectRoot().toPath().getParent() : project.getProjectRoot().toPath();

		final JPanel content = new JPanel();
		content.setLayout( new GridBagLayout() );
		content.setBorder( BorderFactory.createEmptyBorder( 30, 20, 20, 20 ) );

		final GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		content.add( new JLabel( "Current project path: " ), c );

		c.gridx = 1;
		final JLabel rootPathTextField = new JLabel( tellProjectPath( !project.isDatasetXmlPathRelative() ) );
		content.add( rootPathTextField, c );

		++c.gridy;
		c.gridx = 0;
		content.add( new JLabel( "Current BDV dataset path: " ), c );

		String initialPathValue = tellXmlFilePath( convertFromWinOrLeaveAsIs( project.getDatasetXmlFile().toPath() ),
				!project.isDatasetXmlPathRelative() );
		if ( projectInContainerFile && project.isDatasetXmlPathRelative() && initialPathValue.startsWith( ".." ) )
		{
			// this is hacky, it removes the leading "../" or "..\" from the
			// (for sure!) relative path, which
			// was here to "get out of" the .mastodon container file, and which
			// also confuses the Java Path functions...
			initialPathValue = initialPathValue.substring( 3 );
		}
		final JTextField xmlPathTextField = new JTextField( initialPathValue );
		c.gridx = 1;
		c.weightx = 1.0;
		content.add( xmlPathTextField, c );
		xmlPathTextField.setColumns( 50 );

		final JButton browseButton = new JButton( "Browse" );
		c.gridx = 2;
		c.weightx = 0.0;
		content.add( browseButton, c );

		++c.gridy;
		c.gridx = 0;
		content.add( new JLabel( "Store as absolute path: " ), c );
		final JCheckBox storeAbsoluteCheckBox = new JCheckBox();
		storeAbsoluteCheckBox.setSelected( !project.isDatasetXmlPathRelative() );

		c.gridx = 1;
		content.add( storeAbsoluteCheckBox, c );

		c.gridx = 2;
		final JButton testButton = new JButton( "Test Path" );
		content.add( testButton, c );
		//
		final Color normalBgColor = xmlPathTextField.getBackground();
		testButton.addChangeListener( l -> {
			if ( testButton.getModel().isPressed() )
			{
				final File f = new File( tellXmlFilePath( Paths.get( xmlPathTextField.getText() ), true ) );
				xmlPathTextField.setBackground( f.isFile() ? Color.GREEN : Color.RED );
			}
			else
			{
				xmlPathTextField.setBackground( normalBgColor );
			}
		} );

		final JPanel infoLine = new JPanel();
		infoLine.setLayout( new BoxLayout( infoLine, BoxLayout.LINE_AXIS ) );
		infoLine.add( Box.createHorizontalGlue() );
		infoLine.add( new JLabel( "Save the project eventually to make the changes permanent." ) );

		final JPanel buttons = new JPanel();
		final JButton dummy = new JButton( "I want dummy image data instead" );
		final JButton cancel = new JButton( "Cancel" );
		final JButton ok = new JButton( "OK" );
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
		buttons.add( dummy );
		buttons.add( Box.createHorizontalGlue() );
		buttons.add( cancel );
		buttons.add( ok );

		getContentPane().add( content, BorderLayout.NORTH );
		getContentPane().add( infoLine, BorderLayout.CENTER );
		getContentPane().add( buttons, BorderLayout.SOUTH );

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
						DatasetPathDialog.this,
						tellXmlFilePath( Paths.get( path.getText() ), true ),
						new ExtensionFileFilter( "xml" ),
						dialogTitle,
						FileChooser.DialogType.LOAD,
						FileChooser.SelectionMode.FILES_ONLY );
				if ( file != null )
					path.setText( tellXmlFilePath( file.toPath(), storeAbsoluteCheckBox.isSelected() ) );
			}
		}
		browseButton.addActionListener( new Browse( xmlPathTextField, "Select BDV XML file" ) );

		storeAbsoluteCheckBox.addActionListener( e -> {
			rootPathTextField.setText( tellProjectPath( storeAbsoluteCheckBox.isSelected() ) );
			xmlPathTextField.setText( tellXmlFilePath( Paths.get( xmlPathTextField.getText() ), storeAbsoluteCheckBox.isSelected() ) );
		} );

		ok.addActionListener( e -> {
			final String path = xmlPathTextField.getText();
			final boolean relative = !storeAbsoluteCheckBox.isSelected();

			// always give the absolute path! -- the underlying spim_data
			// library "relativyfies"
			// the path on its own (provided the set..Xml..Relative() is set to
			// true)
			final File xmlFilePath = new File( tellXmlFilePath( Paths.get( path ), true ) );
			project.setDatasetXmlFile( xmlFilePath );
			project.setDatasetXmlPathRelative( relative );
			System.out.println( "Storing BDV xml path as " + xmlFilePath + " (should be relative: " + relative + ")" );
			close();
		} );

		cancel.addActionListener( e -> close() );

		dummy.addActionListener( e -> {
			final DummyImageDataParams params = new DummyImageDataParams( owner, appModel );
			if ( !params.wasOkClosed )
				return;

			rootPathTextField.setText( tellProjectPath( true ) );
			xmlPathTextField.setText( "x=" + params.xSize
					+ " y=" + params.ySize + " z=" + params.zSize
					+ " sx=1 sy=1 sz=1 t=" + params.timePoints + ".dummy" );
			storeAbsoluteCheckBox.setSelected( false );
			storeAbsoluteCheckBox.setEnabled( false );
			browseButton.setEnabled( false );
			testButton.setEnabled( false );
		} );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
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
				close();
			}

			private static final long serialVersionUID = 1L;
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
	}

	private void close()
	{
		setVisible( false );
		dispose();
	}

	private static Path convertFromWinOrLeaveAsIs( final Path relativePath )
	{
		return Paths.get( relativePath.toString().replace( "\\", "/" ) );
	}

	private String tellXmlFilePath( final Path xmlFilePath, final boolean tellAsAbsolutePath )
	{
		if ( tellAsAbsolutePath )
		{
			if ( xmlFilePath.isAbsolute() )
				return xmlFilePath.toString();
			else
				return projectRootWoMastodonFile.resolve( xmlFilePath ).toString();
		}
		else
		{
			// should create relative paths
			if ( xmlFilePath.isAbsolute() )
				return projectRootWoMastodonFile.relativize( xmlFilePath ).toString();
			else
				return xmlFilePath.toString();
		}
	}

	private String tellProjectPath( final boolean tellAsAbsolutePath )
	{
		return tellAsAbsolutePath ? "(this path is not considered now)" : projectRootWoMastodonFile.toString();
	}

	private static class DummyImageDataParams extends JDialog
	{

		private static final long serialVersionUID = 1L;

		public DummyImageDataParams( final Frame owner, final ProjectModel appModel )
		{
			super( owner, "Adjust Dummy Dataset Parameters", true );

			final JPanel content = new JPanel();
			content.setLayout( new GridBagLayout() );
			content.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			final GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridy = 0;

			final JSpinner xSpinner = new JSpinner( new SpinnerNumberModel( xSize, 10, 10000, 100 ) );
			final JSpinner ySpinner = new JSpinner( new SpinnerNumberModel( ySize, 10, 10000, 100 ) );
			final JSpinner zSpinner = new JSpinner( new SpinnerNumberModel( zSize, 10, 10000, 100 ) );
			final JSpinner tpSpinner = new JSpinner( new SpinnerNumberModel( timePoints, 10, 10000, 100 ) );

			c.gridx = 0;
			content.add( new JLabel( "Size in pixels in X: " ), c );
			c.gridx = 1;
			content.add( xSpinner, c );

			c.gridy++;
			c.gridx = 0;
			content.add( new JLabel( "Size in pixels in Y: " ), c );
			c.gridx = 1;
			content.add( ySpinner, c );

			c.gridy++;
			c.gridx = 0;
			content.add( new JLabel( "Size in pixels in Z: " ), c );
			c.gridx = 1;
			content.add( zSpinner, c );

			c.gridy++;
			c.gridx = 0;
			content.add( new JLabel( "Number of time points: " ), c );
			c.gridx = 1;
			content.add( tpSpinner, c );

			c.gridy++;
			c.gridx = 0;
			final JButton fromSpots = new JButton( "From spots" );
			if ( appModel == null )
			{
				fromSpots.setEnabled( false );
			}
			else
			{
				fromSpots.addActionListener( l -> {
					final double[] max = new double[ 3 ];
					final double[] pos = new double[ 3 ];
					appModel.getModel()
							.getSpatioTemporalIndex()
							.forEach( s -> {
								s.localize( pos );
								if ( pos[ 0 ] > max[ 0 ] )
									max[ 0 ] = pos[ 0 ];
								if ( pos[ 1 ] > max[ 1 ] )
									max[ 1 ] = pos[ 1 ];
								if ( pos[ 2 ] > max[ 2 ] )
									max[ 2 ] = pos[ 2 ];
							} );
					xSpinner.setValue( ( int ) Math.floor( 1.1 * max[ 0 ] ) );
					ySpinner.setValue( ( int ) Math.floor( 1.1 * max[ 1 ] ) );
					zSpinner.setValue( ( int ) Math.floor( 1.1 * max[ 2 ] ) );
					tpSpinner.setValue( appModel.getMaxTimepoint() + 1 );
				} );
			}
			content.add( fromSpots, c );
			//
			c.gridx = 1;
			final JButton ok = new JButton( "OK" );
			ok.addActionListener( l -> {
				xSize = ( int ) ( xSpinner.getValue() );
				ySize = ( int ) ( ySpinner.getValue() );
				zSize = ( int ) ( zSpinner.getValue() );
				timePoints = ( int ) ( tpSpinner.getValue() );
				wasOkClosed = true;
				close();
			} );
			content.add( ok, c );

			getContentPane().add( content );
			pack();
			setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
			setVisible( true );
		}

		private void close()
		{
			setVisible( false );
			dispose();
		}

		int xSize = 1000;

		int ySize = 1000;

		int zSize = 1000;

		int timePoints = 1000;

		boolean wasOkClosed = false;
	}
}
