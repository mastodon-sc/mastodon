/**
 *
 */
package net.trackmate.revised.trackscheme.display.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStylePanel.TrackSchemeStyleDialog;

/**
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class TrackSchemeStyleChooser
{

	private final TrackSchemeStyleChooserModel model;

	public TrackSchemeStyleChooser()
	{
		model = new TrackSchemeStyleChooserModel();
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

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				frame.setSize( 400, 450 );
				frame.setLocationRelativeTo( frame.getOwner() );
				frame.setVisible( true );
			}
		} );
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
