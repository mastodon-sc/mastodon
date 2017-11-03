package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;

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
		getContentPane().add( settingsPanel, BorderLayout.CENTER );
		pack();
	}

	public void addPage( final SettingsPage page )
	{
		settingsPanel.addPage( page );
		pack();
	}
}
