package org.mastodon.app;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
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
import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.AbstractSpot;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

/**
 * Data class that stores the data model and the application model of the MaMuT
 * application.
 *
 * @author Jean-Yves Tinevez
 */
public class MastodonAppModel<
		M extends AbstractModel< ?, V, E >,
		V extends AbstractSpot< V, E, ?, ?, ? >,
		E extends AbstractListenableEdge< E, V, ?, ? > >
{
	public final GroupableModelFactory< NavigationHandler< V, E > > NAVIGATION = new ForwardingNavigationHandler.Factory<>();

	public final GroupableModelFactory< TimepointModel > TIMEPOINT = ForwardingTimepointModel.factory;

	private final M model;

	private final SelectionModel< V, E > selectionModel;

	private final HighlightModel< V, E > highlightModel;

	private final FocusModel< V, E > focusModel;

	private final GroupManager groupManager;

	private InputTriggerConfig keyconf;

	private final KeyPressedManager keyPressedManager;

	private final String[] keyConfigContexts;

	/**
	 * Actions that should be available in all views.
	 */
	private final Actions appActions;

	/**
	 *
	 * @param numGroups
	 * @param model
	 * @param keyconf
	 * @param keyPressedManager
	 * @param keyConfigContexts
	 *            keyconf contexts for appActions (actions that should be
	 *            available in all views)
	 */
	public MastodonAppModel(
			final int numGroups,
			final M model,
			final InputTriggerConfig keyconf,
			final KeyPressedManager keyPressedManager,
			final String[] keyConfigContexts )
	{
		this.model = model;

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

		this.keyconf = keyconf;
		this.keyPressedManager = keyPressedManager;
		this.keyConfigContexts = keyConfigContexts;

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

	public InputTriggerConfig getKeyConfig()
	{
		return keyconf;
	}

	public void setKeyConfig( final InputTriggerConfig keyconf )
	{
		this.keyconf = keyconf;
	}

	public String[] getKeyConfigContexts()
	{
		return keyConfigContexts;
	}

	public KeyPressedManager getKeyPressedManager()
	{
		return keyPressedManager;
	}

	public Actions getAppActions()
	{
		return appActions;
	}
}
