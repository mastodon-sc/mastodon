package org.mastodon.app.ui.settings;

/**
 * A {@code ModificationListener} to a {@link SettingsPage} is notified, when
 * any setting is changed. This is used to track whether there is anything to
 * "Apply" in the {@link SettingsPage}.
 */
public interface ModificationListener
{
	public void modified();
}
