/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.app;

import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.mastodon.model.AbstractModel;
import org.mastodon.model.AbstractSpot;
import org.mastodon.model.DefaultFocusModel;
import org.mastodon.model.DefaultHighlightModel;
import org.mastodon.model.DefaultSelectionModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.ForwardingNavigationHandler;
import org.mastodon.model.ForwardingTimepointModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.ui.keymap.KeymapManager;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

/**
 * Data class that stores the data model and the application model of a Mastodon
 * application.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 * @param <M>
 *            the type of the model used in the application.
 * @param <V>
 *            the type of vertices in the model graph.
 * @param <E>
 *            the type of edges in the model graph.
 */
public class MastodonAppModel<
		M extends AbstractModel< ?, V, E >,
		V extends AbstractSpot< V, E, ?, ?, ? >,
		E extends AbstractListenableEdge< E, V, ?, ? > >
{
	public final GroupableModelFactory< NavigationHandler< V, E > > NAVIGATION =
			new ForwardingNavigationHandler.Factory<>();

	public final GroupableModelFactory< TimepointModel > TIMEPOINT = ForwardingTimepointModel.factory;

	private final M model;

	private final SelectionModel< V, E > selectionModel;

	private final HighlightModel< V, E > highlightModel;

	private final FocusModel< V, E > focusModel;

	private final GroupManager groupManager;

	private final KeyPressedManager keyPressedManager;

	private final KeymapManager keymapManager;

	private final MastodonPlugins< ?, ? > plugins;

	private final String[] keyConfigContexts;

	/**
	 * Actions that should be available in all views.
	 */
	private final Actions appActions;

	/**
	 * Actions that are always available, even if no {@link MastodonAppModel}
	 * currently exists.
	 */
	private final Actions globalActions;

	/**
	 * Instantiate a new Mastodon-app model.
	 *
	 * @param numGroups
	 *            the number of groups to create in the group manager,
	 * @param model
	 *            the data model.
	 * @param keyPressedManager
	 *            the key-pressed manager.
	 * @param keymapManager
	 *            the keymap manager.
	 * @param plugins
	 *            the plugins.
	 * @param globalActions
	 *            the global actions.
	 * @param keyConfigContexts
	 *            keyconf contexts for appActions (actions that should be
	 *            available in all views)
	 */
	public MastodonAppModel(
			final int numGroups,
			final M model,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts )
	{
		this.model = model;
		this.plugins = plugins;
		this.globalActions = globalActions;

		final ListenableReadOnlyGraph< V, E > graph = model.getGraph();
		final GraphIdBimap< V, E > idmap = model.getGraphIdBimap();

		final DefaultSelectionModel< V, E > selectionModel = new DefaultSelectionModel<>( graph, idmap );
		graph.addGraphListener( selectionModel );
		this.selectionModel = selectionModel;

		final DefaultHighlightModel< V, E > highlightModel = new DefaultHighlightModel<>( idmap );
		graph.addGraphListener( highlightModel );
		this.highlightModel = highlightModel;

		final DefaultFocusModel< V, E > focusModel = new DefaultFocusModel<>( idmap );
		graph.addGraphListener( focusModel );
		this.focusModel = focusModel;

		groupManager = new GroupManager( numGroups );
		groupManager.registerModel( TIMEPOINT );
		groupManager.registerModel( NAVIGATION );

		this.keyPressedManager = keyPressedManager;
		this.keymapManager = keymapManager;
		this.keyConfigContexts = keyConfigContexts;

		final InputTriggerConfig keyconf = keymapManager.getForwardDefaultKeymap().getConfig();
		this.appActions = new Actions( keyconf, keyConfigContexts );
	}

	public M getModel()
	{
		return model;
	}

	public SelectionModel< V, E > getSelectionModel()
	{
		return selectionModel;
	}

	public HighlightModel< V, E > getHighlightModel()
	{
		return highlightModel;
	}

	public FocusModel< V, E > getFocusModel()
	{
		return focusModel;
	}

	public GroupManager getGroupManager()
	{
		return groupManager;
	}

	public Keymap getKeymap()
	{
		return keymapManager.getForwardDefaultKeymap();
	}

	public String[] getKeyConfigContexts()
	{
		return keyConfigContexts;
	}

	public KeyPressedManager getKeyPressedManager()
	{
		return keyPressedManager;
	}

	public KeymapManager getKeymapManager()
	{
		return keymapManager;
	}

	public MastodonPlugins< ?, ? > getPlugins()
	{
		return plugins;
	}

	/**
	 * Actions that should be available in all views.
	 *
	 * 
	 * @return the application actions.
	 */
	public Actions getAppActions()
	{
		return appActions;
	}

	/**
	 * Actions that are always available, even if no {@link MastodonAppModel}
	 * currently exists.
	 * 
	 * TODO: naming, this should be named appActions and the AppModel.appActions
	 * should become modelActions?
	 * 
	 * TODO: or rename AppModel --> ProjectModel, then projectActions?
	 *
	 * @return the global actions.
	 */
	public Actions getGlobalActions()
	{
		return globalActions;
	}
}
