/**
 *
 */
package org.mastodon.revised.trackscheme.display.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.mastodon.revised.trackscheme.display.TrackSchemePanel;
import org.mastodon.revised.trackscheme.display.laf.TrackSchemeStyle;
import org.mastodon.revised.trackscheme.display.ui.TrackSchemeStyleEditorPanel.TrackSchemeStyleEditorDialog;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class TrackSchemeStyleChooser
{

	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/trackschemestyles.yaml";

	private final DefaultComboBoxModel< TrackSchemeStyle > model;

	private final TrackSchemeStyleChooserDialog dialog;

	private final TrackSchemePanel trackSchemePanel;

	public TrackSchemeStyleChooser( JFrame owner, TrackSchemePanel trackSchemePanel )
	{
		this.trackSchemePanel = trackSchemePanel;

		model = new DefaultComboBoxModel< >();
		for ( final TrackSchemeStyle defaultStyle : TrackSchemeStyle.defaults )
			model.addElement( defaultStyle );
		loadStyles();

		dialog = new TrackSchemeStyleChooserDialog( owner, model );
		dialog.okButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				okPressed();
			}
		} );
		dialog.buttonDeleteStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				delete();
			}
		} );
		dialog.buttonEditStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				edit();
			}
		} );
		dialog.buttonNewStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				newStyle();
			}
		} );
		dialog.buttonSetStyleName.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				setStyleName();
			}
		} );
		dialog.saveButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				dialog.saveButton.setEnabled( false );
				try
				{
					saveStyles();
				}
				finally
				{
					dialog.saveButton.setEnabled( true );
				}
			}
		} );

		dialog.setSize( 400, 450 );
		dialog.setLocationRelativeTo( dialog.getOwner() );
	}

	private void loadStyles()
	{
		try
		{
			final FileReader input = new FileReader( STYLE_FILE );
			final Yaml yaml = TrackSchemeStyleIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			for ( final Object obj : objs )
				model.addElement( ( TrackSchemeStyle ) obj );

		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "TrackScheme style file " + STYLE_FILE + " not found. Using builtin styles." );
		}
	}

	private void saveStyles()
	{
		try
		{
			final List< TrackSchemeStyle > stylesToSave = new ArrayList<>();
			for ( int i = 0; i < model.getSize(); i++ )
			{
				final TrackSchemeStyle style = model.getElementAt( i );
				if ( TrackSchemeStyle.defaults.contains( style ) )
					continue;
				stylesToSave.add( style );
			}

			mkdirs( STYLE_FILE );
			final FileWriter output = new FileWriter( STYLE_FILE );
			final Yaml yaml = TrackSchemeStyleIO.createYaml();
			yaml.dumpAll( stylesToSave.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private void setStyleName()
	{
		final TrackSchemeStyle current = ( TrackSchemeStyle ) model.getSelectedItem();
		if ( null == current || TrackSchemeStyle.defaults.contains( current ) )
			return;

		final String newName = ( String ) JOptionPane.showInputDialog( dialog, "Enter the style name:", "Style name", JOptionPane.PLAIN_MESSAGE, null, null, current.name );
		current.name = newName;
	}

	private void newStyle()
	{
		TrackSchemeStyle current = ( TrackSchemeStyle ) model.getSelectedItem();
		if ( null == current )
			current = TrackSchemeStyle.defaultStyle();

		final String name = current.name;
		final Pattern pattern = Pattern.compile( "(.+) \\((\\d+)\\)$" );
		final Matcher matcher = pattern.matcher( name );
		int n;
		String prefix;
		if ( matcher.matches() )
		{
			final String nstr = matcher.group( 2 );
			n = Integer.parseInt( nstr );
			prefix = matcher.group( 1 );
		}
		else
		{
			n = 1;
			prefix = name;
		}
		String newName;
		INCREMENT: while ( true )
		{
			newName = prefix + " (" + ( ++n ) + ")";
			for ( int j = 0; j < model.getSize(); j++ )
			{
				if ( model.getElementAt( j ).name.equals( newName ) )
					continue INCREMENT;
			}
			break;
		}

		final TrackSchemeStyle newStyle = current.copy( newName );
		model.addElement( newStyle );
		model.setSelectedItem( newStyle );
	}

	private void edit()
	{
		final TrackSchemeStyle current = ( TrackSchemeStyle ) model.getSelectedItem();
		if ( null == current || TrackSchemeStyle.defaults.contains( current ) )
			return;

		final TrackSchemeStyle.UpdateListener listener = new TrackSchemeStyle.UpdateListener()
		{
			@Override
			public void trackSchemeStyleChanged()
			{
				dialog.panelPreview.setTrackSchemeStyle( current );
				dialog.panelPreview.repaint();
			}
		};
		current.addUpdateListener( listener );
		final TrackSchemeStyleEditorDialog nameDialog = new TrackSchemeStyleEditorDialog( dialog, current );
		nameDialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( java.awt.event.WindowEvent e )
			{
				current.removeUpdateListener( listener );
			};
		} );
		nameDialog.setModal( true );
		nameDialog.setVisible( true );
	}

	private void delete()
	{
		if ( TrackSchemeStyle.defaults.contains( model.getSelectedItem() ) )
			return;

		model.removeElement( model.getSelectedItem() );
	}

	private void okPressed()
	{
		trackSchemePanel.setTrackSchemeStyle( ( TrackSchemeStyle ) model.getSelectedItem() );
		trackSchemePanel.repaint();
		dialog.setVisible( false );
	}

	public JDialog getDialog()
	{
		return dialog;
	}

	private static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir == null ? false : dir.mkdirs();
	}
}
