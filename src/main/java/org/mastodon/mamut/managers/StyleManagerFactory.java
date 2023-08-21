package org.mastodon.mamut.managers;

import org.mastodon.mamut.ProjectModel;
import org.scijava.plugin.SciJavaPlugin;

import bdv.ui.settings.SettingsPage;

/**
 * Interface for discoverable style manager factories.
 * <p>
 * Such factories are meant to be automatically discovered by the window
 * manager, and used to create style managers.
 * 
 * @param <T>
 *            TODO
 */
public interface StyleManagerFactory< T > extends SciJavaPlugin
{

	/**
	 * Creates a new manager instance for the specified project model.
	 * 
	 * @param projectModel
	 *            the project model.
	 * 
	 * @return a new manager instance.
	 */
	public T create( final ProjectModel projectModel );

	/**
	 * Returns <code>true</code> if the manager handled by this factory has a
	 * {@link SettingsPage} that can configure it.
	 * 
	 * @return whether there is a settings page for the manager.
	 */
	public boolean hasSettingsPage();
	
	/**
	 * Creates a new settings page for the specified manager.
	 * 
	 * @param manager
	 *            the manager.
	 * @return a new settings page.
	 */
	public SettingsPage createSettingsPage( T manager );

	/**
	 * Returns the class of the manager created by this factory.
	 * 
	 * @return the manager class.
	 */
	public Class< T > getManagerClass();

}
