package org.mastodon.app.ui;

import org.mastodon.app.AppModel;
import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.mastodon.model.ForwardingNavigationHandler;
import org.mastodon.model.ForwardingTimepointModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.TimepointModel;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;

/**
 * Components of a {@link AppModel} that stores elements related to user
 * interface (creating views, making them interactive, and in sync).
 *
 * @author Jean-Yves Tinevez
 */
public class UIModel
{

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public final GroupableModelFactory< NavigationHandler< ?, ? > > NAVIGATION = ( GroupableModelFactory ) new ForwardingNavigationHandler.Factory<>();

	public final GroupableModelFactory< TimepointModel > TIMEPOINT = ForwardingTimepointModel.factory;

	private final GroupManager groupManager;

	private final KeyPressedManager keyPressedManager;

	private final KeymapManager keymapManager;

	private final MastodonPlugins< ?, ? > plugins;

	private final String[] keyConfigContexts;

	/**
	 * Actions that should be available in all views.
	 */
	private final Actions modelActions;

	/**
	 * Actions that are always available, even if no {@link AppModel} currently
	 * exists.
	 */
	private final Actions projectActions;

	public UIModel(
			final int numGroups,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts )
	{
		this.plugins = plugins;
		this.projectActions = globalActions;

		this.groupManager = new GroupManager( numGroups );
		groupManager.registerModel( TIMEPOINT );
		groupManager.registerModel( NAVIGATION );

		this.keyPressedManager = keyPressedManager;
		this.keymapManager = keymapManager;
		this.keyConfigContexts = keyConfigContexts;

		final InputTriggerConfig keyconf = keymapManager.getForwardSelectedKeymap().getConfig();
		this.modelActions = new Actions( keyconf, keyConfigContexts );
	}

	/**
	 * Exposes the group manager that manages the view groups of this
	 * application.
	 *
	 * @return the group manager.
	 */
	public GroupManager getGroupManager()
	{
		return groupManager;
	}

	/**
	 * Exposes the keymap that is currently selected in the keymap manager.
	 *
	 * @return the selected keymap.
	 */
	public Keymap getKeymap()
	{
		return keymapManager.getForwardSelectedKeymap();
	}

	/**
	 * Exposes the key configuration contexts for actions that should be
	 * available in all views.
	 *
	 * @return the key configuration contexts.
	 */
	public String[] getKeyConfigContexts()
	{
		return keyConfigContexts;
	}

	/**
	 * Exposes the key-pressed manager that manages the key events and
	 * dispatches them to the appropriate view.
	 *
	 * @return the key-pressed manager.
	 */
	public KeyPressedManager getKeyPressedManager()
	{
		return keyPressedManager;
	}

	/**
	 * Exposes the keymap manager that manages the keymaps and the currently
	 * selected keymap.
	 *
	 * @return the keymap manager.
	 */
	public KeymapManager getKeymapManager()
	{
		return keymapManager;
	}

	/**
	 * Exposes the plugins that are available in this application.
	 *
	 * @return the plugins.
	 */
	public MastodonPlugins< ?, ? > getPlugins()
	{
		return plugins;
	}

	/**
	 * Actions that operates on the whole data model and are available in all
	 * views of the data.
	 * <p>
	 * For instance undo/redo, select all, etc.
	 *
	 * @return the model actions.
	 */
	public Actions getModelActions()
	{
		return modelActions;
	}

	/**
	 * Actions that operates on the app or whole project.
	 * <p>
	 * For instance, saving, importing, creating a new view, showing the
	 * preference window, etc.
	 *
	 * @return the project actions.
	 */
	public Actions getProjectActions()
	{
		return projectActions;
	}
}
