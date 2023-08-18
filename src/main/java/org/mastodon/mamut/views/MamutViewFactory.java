package org.mastodon.mamut.views;

import java.util.Map;

import org.mastodon.mamut.MamutView;
import org.mastodon.mamut.ProjectModel;
import org.scijava.plugin.SciJavaPlugin;

public interface MamutViewFactory< T extends MamutView< ?, ?, ? > > extends SciJavaPlugin
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
}
