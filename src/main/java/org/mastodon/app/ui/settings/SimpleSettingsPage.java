package org.mastodon.app.ui.settings;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.mastodon.util.Listeners;

/**
 * Helper class to easily put a {@link JPanel} as a page into a {@link SettingsPanel}.
 * <p>
 * Call {@link #notifyModified()} when edits were made in your panel.
 * (This will trigger correct "Cancel", "Apply", and "OK" behaviour by the {@link SettingsPanel}.)
 * </p>
 * <p>
 * To specify what should happen on "Apply" (propagate panel edits to your data model) add a {@link #runOnApply} callback.
 * To specify what should happen on "Cancel" (reset panel contents to your data model) add a {@link #runOnCancel} callback.
 * </p>
 *
 * @author Tobias Pietzsch
 */
public class SimpleSettingsPage implements SettingsPage
{
	private final String treePath;

	private final JPanel panel;

	private final Listeners.List< ModificationListener > modificationListeners;

	public SimpleSettingsPage( final String treePath, final JPanel panel )
	{
		this.treePath = treePath;
		this.panel = panel;
		modificationListeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public String getTreePath()
	{
		return treePath;
	}

	@Override
	public JPanel getJPanel()
	{
		return panel;
	}

	public void notifyModified()
	{
		modificationListeners.list.forEach( ModificationListener::modified );
	}

	@Override
	public Listeners< ModificationListener > modificationListeners()
	{
		return modificationListeners;
	}

	protected final ArrayList< Runnable > runOnApply = new ArrayList<>();

	public synchronized void onApply( final Runnable runnable )
	{
		runOnApply.add( runnable );
	}

	protected final ArrayList< Runnable > runOnCancel = new ArrayList<>();

	public synchronized void onCancel( final Runnable runnable )
	{
		runOnCancel.add( runnable );
	}

	@Override
	public void cancel()
	{
		runOnCancel.forEach( Runnable::run );
	}

	@Override
	public void apply()
	{
		runOnApply.forEach( Runnable::run );
	}
}