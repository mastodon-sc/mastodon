/**
 *
 */
package net.trackmate.revised.trackscheme.display.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.yaml.snakeyaml.Yaml;

import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStylePanel.TrackSchemeStyleDialog;

/**
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class TrackSchemeStyleChooser
{

	private static final String STYLE_FILE = "trackschemestyles.yaml";

	private final TrackSchemeStyleChooserModel model;

	public TrackSchemeStyleChooser()
	{
		model = new TrackSchemeStyleChooserModel();
		loadStyles();

		java.awt.EventQueue.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final TrackSchemeStyleChooserFrame frame = new TrackSchemeStyleChooserFrame( model );
				frame.okButton.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						okPressed();
					}
				} );
				frame.buttonDeleteStyle.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						delete();
					}
				} );
				frame.buttonEditStyle.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						edit( frame );
					}
				} );
				frame.buttonNewStyle.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						newStyle();
					}
				} );
				frame.buttonSetStyleName.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						setStyleName( frame );
					}
				} );
				frame.saveButton.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						frame.saveButton.setEnabled( false );
						try
						{
							saveStyles();
						}
						finally
						{
							frame.saveButton.setEnabled( true );
						}
					}
				} );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				frame.setSize( 400, 450 );
				frame.setLocationRelativeTo( frame.getOwner() );
				frame.setVisible( true );
			}
		} );
	}

	private void loadStyles()
	{
		try
		{
			final FileReader input = new FileReader( STYLE_FILE );
			final Yaml yaml = TrackSchemeStyleIOExample.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			for ( final Object obj : objs )
				model.addElement( ( TrackSchemeStyle ) obj );

		}
		catch ( final FileNotFoundException e )
		{
			e.printStackTrace();
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
			final FileWriter output = new FileWriter( STYLE_FILE );
			final Yaml yaml = TrackSchemeStyleIOExample.createYaml();
			yaml.dumpAll( stylesToSave.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private void setStyleName( JFrame frame )
	{
		final TrackSchemeStyle current = ( TrackSchemeStyle ) model.getSelectedItem();
		if ( null == current || TrackSchemeStyle.defaults.contains( current ) )
			return;

		final String newName = ( String ) JOptionPane.showInputDialog(
				frame,
				"Enter the style name:",
				"Style name",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				current.name );
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

	private void edit( TrackSchemeStyleChooserFrame frame )
	{
		final TrackSchemeStyle current = ( TrackSchemeStyle ) model.getSelectedItem();
		if ( null == current || TrackSchemeStyle.defaults.contains( current ) )
			return;

		final TrackSchemeStyle.UpdateListener listener = new TrackSchemeStyle.UpdateListener()
		{
			@Override
			public void trackSchemeStyleChanged()
			{
				frame.panelPreview.setTrackSchemeStyle( current );
				frame.panelPreview.repaint();
			}
		};
		current.addUpdateListener( listener );
		final TrackSchemeStyleDialog dialog = new TrackSchemeStyleDialog( frame, current );
		dialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( java.awt.event.WindowEvent e )
			{
				current.removeUpdateListener( listener );
			};
		} );
		dialog.setModal( true );
		dialog.setVisible( true );
	}

	private void delete()
	{
		if ( TrackSchemeStyle.defaults.contains( model.getSelectedItem() ) )
			return;

		model.removeElement( model.getSelectedItem() );
	}

	private void okPressed()
	{
		// TODO Auto-generated method stub

	}

	public static void main( String[] args )
	{
		new TrackSchemeStyleChooser();
	}

}
