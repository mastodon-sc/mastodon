package org.mastodon.app.ui.settings;

import javax.swing.JPanel;

import org.scijava.listeners.Listeners;

/**
 * A content page in a {@link SettingsPanel}.
 *
 * @author Tobias Pietzsch
 */
public interface SettingsPage
{
	/**
	 * Get the path of this page in the settings tree.
	 *
	 * @return the path.
	 */
	public String getTreePath();

	public JPanel getJPanel();

	Listeners< ModificationListener > modificationListeners();

	/**
	 * Called by the {@link SettingsPanel} if user presses "Cancel". Should
	 * restore any modified state to the original state.
	 */
	public void cancel();

	/**
	 * Called by the {@link SettingsPanel} if user presses "OK" or "Cancel".
	 * Should commit any modified state.
	 */
	public void apply();
}
