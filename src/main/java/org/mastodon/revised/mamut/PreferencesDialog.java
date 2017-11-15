package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.SettingsPage;
import org.mastodon.app.ui.settings.SettingsPanel;

public class PreferencesDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private final SettingsPanel settingsPanel;

	public PreferencesDialog( final Frame owner )
	{
		super( owner, "Preferences", false );
		settingsPanel = new SettingsPanel();
		settingsPanel.onOk( () -> setVisible( false ) );
		settingsPanel.onCancel( () -> setVisible( false ) );

		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				settingsPanel.cancel();
			}
		} );

		getContentPane().add( settingsPanel, BorderLayout.CENTER );
		pack();
	}

	public void addPage( final SettingsPage page )
	{
		settingsPanel.addPage( page );
		pack();
	}
}
