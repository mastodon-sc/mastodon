/**
 *
 */
package org.mastodon.revised.trackscheme.display.style;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.MutableComboBoxModel;

import org.mastodon.revised.trackscheme.TrackSchemeFeatures;
import org.mastodon.revised.trackscheme.display.AbstractTrackSchemeOverlay;
import org.mastodon.revised.trackscheme.display.DefaultTrackSchemeOverlay;
import org.mastodon.revised.trackscheme.display.TrackSchemePanel;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleEditorPanel.TrackSchemeStyleEditorDialog;

/**
 * User interface to chose a style from a list, preview it and possible edit it.
 * 
 * @author Jean-Yves Tinevez
 */
public class TrackSchemeStyleChooser
{

	private final MutableComboBoxModel< TrackSchemeStyle > model;

	private final TrackSchemeStyleChooserDialog dialog;

	private final TrackSchemePanel trackSchemePanel;

	private final TrackSchemeStyleManager styleManager;

	private final TrackSchemeFeatures features;

	public TrackSchemeStyleChooser(
			final JFrame owner,
			final TrackSchemePanel trackSchemePanel,
			final TrackSchemeStyleManager styleManager,
			final TrackSchemeFeatures features )
	{
		this.trackSchemePanel = trackSchemePanel;
		this.styleManager = styleManager;
		this.features = features;

		model = styleManager.createComboBoxModel();
		model.setSelectedItem( ( ( DefaultTrackSchemeOverlay ) trackSchemePanel.getGraphOverlay() ).getStyle() );

		dialog = new TrackSchemeStyleChooserDialog( owner, model );
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
		dialog.buttonEditStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				edit();
			}
		} );
		dialog.buttonNewStyle.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				newStyle();
				setStyleName();
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
					styleManager.saveStyles();
				}
				finally
				{
					dialog.saveButton.setEnabled( true );
				}
			}
		} );

		dialog.setSize( 400, 480 );
		dialog.setLocationRelativeTo( dialog.getOwner() );
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
		final TrackSchemeStyle current = ( TrackSchemeStyle ) model.getSelectedItem();
		final TrackSchemeStyle newStyle = styleManager.copy( current );
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
				final AbstractTrackSchemeOverlay overlay = dialog.panelPreview.getGraphOverlay();
				if ( overlay instanceof DefaultTrackSchemeOverlay )
				{
					final DefaultTrackSchemeOverlay dtso = ( DefaultTrackSchemeOverlay ) overlay;
					dtso.setStyle( current );
				}
				dialog.panelPreview.repaint();
			}
		};
		current.addUpdateListener( listener );
		final TrackSchemeStyleEditorDialog nameDialog = new TrackSchemeStyleEditorDialog( dialog, current, features );
		nameDialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final java.awt.event.WindowEvent e )
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
		final AbstractTrackSchemeOverlay overlay = trackSchemePanel.getGraphOverlay();
		if ( overlay instanceof DefaultTrackSchemeOverlay )
		{
			final DefaultTrackSchemeOverlay dtso = ( DefaultTrackSchemeOverlay ) overlay;
			dtso.setStyle( ( TrackSchemeStyle ) model.getSelectedItem() );
		}
		trackSchemePanel.repaint();
		dialog.setVisible( false );
	}

	public JDialog getDialog()
	{
		return dialog;
	}
}