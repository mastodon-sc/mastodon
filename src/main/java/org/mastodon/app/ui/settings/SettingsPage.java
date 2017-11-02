package org.mastodon.app.ui.settings;

import javax.swing.JPanel;

public interface SettingsPage
{
	public String getTreePath();

	public JPanel getJPanel();

	public void cancel();

	public void apply();
}
