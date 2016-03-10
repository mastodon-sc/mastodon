package net.trackmate.revised;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	public MainWindow( final WindowManager wm )
	{
		super( "test" );

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout( new GridLayout( 2, 1 ) );
		final JButton bdvButton = new JButton( "bdv" );
		bdvButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				wm.createBigDataViewer();
			}
		} );
		final JButton trackschemeButton = new JButton( "trackscheme" );
		trackschemeButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				wm.createTrackScheme();
			}
		} );
		buttonsPanel.add( bdvButton );
		buttonsPanel.add( trackschemeButton );

		final Container content = getContentPane();
		content.add( buttonsPanel, BorderLayout.NORTH );
	}
}
