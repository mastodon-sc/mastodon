package org.mastodon.mamut.views;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.scijava.plugin.SciJavaPlugin;

public interface MamutViewFactory< T extends MamutViewI > extends SciJavaPlugin
{

	/**
	 * Key to the view type name. Value is a string.
	 */
	static final String VIEW_TYPE_KEY = "Type";

	/**
	 * Creates a new view for the specified project model.
	 * <p>
	 * The new view has default GUI state and is not shown.
	 * 
	 * @param projectModel
	 *            the project model.
	 * 
	 * @return a new view.
	 */
	public T create( final ProjectModel projectModel );

	/**
	 * Creates and shows a new view for the specified project model, and restore
	 * the GUI state stored in the specified map.
	 * 
	 * @param projectModel
	 *            the project model.
	 * @param guiState
	 *            the GUI state map.
	 * @return a new view.
	 */
	public T show( final ProjectModel projectModel, final Map< String, Object > guiState );

	/**
	 * Restores the GUI state stored in the specified map for the specified
	 * view.
	 * 
	 * @param view
	 *            the view.
	 * @param guiState
	 *            the GUI state map.
	 */
	public void restoreGuiState( final T view, final Map< String, Object > guiState );

	/**
	 * Serializes the current GUI state of the specified view in a map.
	 * 
	 * @param view
	 *            the view.
	 * @return a new map.
	 */
	public Map< String, Object > getGuiState( final T view );

	/**
	 * Returns the name of the command that will use this factory to create a
	 * new view.
	 * 
	 * @return the command name.
	 */
	public String getCommandName();

	/**
	 * Returns the list of default keystrokes of the command.
	 * 
	 * @return the default keystrokes0
	 */
	public String[] getCommandKeys();

	/**
	 * Returns the description of the command.
	 * 
	 * @return the description.
	 */
	public String getCommandDescription();

	/**
	 * Returns the text of the command to appear in menus.
	 * 
	 * @return the menu text for the command.
	 */
	public String getCommandMenuText();

	/**
	 * Returns the class of the view created by this factory.
	 * <p>
	 * This class is used as key in several maps or to get the right factory
	 * when deserializing GUI state.
	 * 
	 * @return the view class.
	 */
	public Class< T > getViewClass();
}
