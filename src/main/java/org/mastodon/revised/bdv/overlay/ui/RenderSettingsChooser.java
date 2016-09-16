/**
 *
 */
package org.mastodon.revised.bdv.overlay.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class RenderSettingsChooser
{

	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/rendersettings.yaml";

	private final DefaultComboBoxModel< RenderSettings > model;

	private final RenderSettingsDialog dialog;

	private final RenderSettings targetSettings;

	public RenderSettingsChooser( final JFrame owner )
	{
		this.model = new DefaultComboBoxModel<>();
		for ( final RenderSettings defaultStyle : RenderSettings.defaults )
			model.addElement( defaultStyle );
		loadStyles();

		// Give the choose its own render settings instance.
		this.targetSettings = RenderSettings.defaultStyle().copy( RenderSettings.defaultStyle().getName() );
		dialog = new RenderSettingsDialog( owner, model, targetSettings );
		dialog.okButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				okPressed();
			}
		} );
		dialog.buttonDeleteStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				delete();
			}
		} );
		dialog.buttonNewStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				newStyle();
			}
		} );
		dialog.buttonSetStyleName.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				setStyleName();
			}
		} );
		dialog.saveButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
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

		dialog.setSize( 420, 750 );
		dialog.setLocationRelativeTo( dialog.getOwner() );
	}

	private void loadStyles()
	{
		try
		{
			final FileReader input = new FileReader( STYLE_FILE );
			final Yaml yaml = RenderSettingsIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			for ( final Object obj : objs )
				model.addElement( ( RenderSettings ) obj );

		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "BDV render settings file " + STYLE_FILE + " not found. Using builtin styles." );
		}
	}

	private void saveStyles()
	{
		try
		{
			final List< RenderSettings > stylesToSave = new ArrayList<>();
			for ( int i = 0; i < model.getSize(); i++ )
			{
				final RenderSettings style = model.getElementAt( i );
				if ( RenderSettings.defaults.contains( style ) )
					continue;
				stylesToSave.add( style );
			}

			mkdirs( STYLE_FILE );
			final FileWriter output = new FileWriter( STYLE_FILE );
			final Yaml yaml = RenderSettingsIO.createYaml();
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
		final RenderSettings current = ( RenderSettings ) model.getSelectedItem();
		if ( null == current || RenderSettings.defaults.contains( current ) )
			return;

		final String newName = ( String ) JOptionPane.showInputDialog( dialog, "Enter the render settings name:", "Style name", JOptionPane.PLAIN_MESSAGE, null, null, current.getName() );
		current.setName( newName );
	}

	private void newStyle()
	{
		RenderSettings current = ( RenderSettings ) model.getSelectedItem();
		if ( null == current )
			current = RenderSettings.defaultStyle();

		final String name = current.getName();
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
				if ( model.getElementAt( j ).getName().equals( newName ) )
					continue INCREMENT;
			}
			break;
		}

		final RenderSettings newStyle = current.copy( newName );
		model.addElement( newStyle );
		model.setSelectedItem( newStyle );
	}

	private void delete()
	{
		if ( RenderSettings.defaults.contains( model.getSelectedItem() ) )
			return;

		model.removeElement( model.getSelectedItem() );
	}

	private void okPressed()
	{
		dialog.setVisible( false );
	}

	public JDialog getDialog()
	{
		return dialog;
	}

	public RenderSettings getRenderSettings()
	{
		return targetSettings;
	}

	private static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir == null ? false : dir.mkdirs();
	}

	public void setTitle( final String title )
	{
		dialog.setTitle( title );
	}
}
