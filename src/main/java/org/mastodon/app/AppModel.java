/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.app.MastodonIcons.TAGS_ICON;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.app.ui.UIModel;
import org.mastodon.app.ui.UIUtils;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.CloseListener;
import org.mastodon.mamut.MamutFeatureComputation;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.tag.ui.TagSetDialog;
import org.mastodon.ui.coloring.TrackGraphColorGenerator;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;

/**
 * Data class that stores the data model and the application model of a Mastodon
 * application.
 * <p>
 * This aggregates the {@link MastodonModel} and the {@link UIModel}, and
 * creates some of the sub-components that need both components to be
 * initialized s (e.g. ui components that need to access or listen to the data
 * model).
 * <p>
 * Currently, this class does the following:
 * <ul>
 * <li>creates a UIModel,
 * <li>creates and registers a {@link TrackGraphColorGenerator} for the model's
 * graph, if it is a {@link ListenableReadOnlyGraph}, and stores it in the
 * UIModel as singleton.
 * <li>creates and registers a {@link TagSetDialog} if the model is an
 * {@link UndoPointMarker}, and binds it to a menu item and a shortcut. The
 * dialog is registered to the UIModel as a window.
 * <li>creates and registers a {@link MamutFeatureComputation} dialog, and binds
 * it to a menu item and a shortcut. The dialog is registered to the UIModel as
 * a window.
 * </ul>
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
public class AppModel<
		M extends MastodonModel< G, V, E >,
		G extends ReadOnlyGraph< V, E >,
		V extends Vertex< E >,
		E extends Edge< V > >
{

	protected final M model;

	protected final UIModel< ? > uiModel;

	private final int minTimepoint;

	private final int maxTimepoint;

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
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public AppModel(
			final Context context,
			final M model,
			final Class< ? > viewFactoryType,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts,
			final int numGroups,
			final int minTimepoint,
			final int maxTimepoint )
	{
		this.model = model;
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;
		this.uiModel = new UIModel<>( ( Class ) viewFactoryType, context, numGroups, keyPressedManager, keymapManager, plugins, globalActions, keyConfigContexts );

		/*
		 * Singletons and managers.
		 */
		/*
		 * Register a TrackColorGenerator for this model's graph if it is a
		 * listenable graph.
		 */
		final G graph = model.getGraph();
		if ( graph instanceof ListenableReadOnlyGraph )
		{
			final ListenableReadOnlyGraph< V, E > lg = ( ListenableReadOnlyGraph< V, E > ) graph;
			final TrackGraphColorGenerator< V, E > trackGraphColorGenerator = new TrackGraphColorGenerator< V, E >( lg );
			uiModel.closeListeners().add( () -> trackGraphColorGenerator.close() );
			uiModel.registerInstance( trackGraphColorGenerator );
		}

		/*
		 * Tag-set and feature computation dialogs
		 */
		if ( model instanceof UndoPointMarker )
		{
			// Tag-set edit dialog: only if the model is an UndoableModel.
			final UndoPointMarker undo = ( UndoPointMarker ) model;
			final Keymap keymap = keymapManager.getForwardSelectedKeymap();
			final TagSetDialog tagSetDialog = new TagSetDialog( null, model.getTagSetModel(), undo, keymap, keyConfigContexts );
			final RunnableAction editTagSetsAction = new RunnableAction( TAGSETS_DIALOG, () -> tagSetDialog.setVisible( true ) );
			uiModel.getProjectActions().namedAction( editTagSetsAction, TAGSETS_DIALOG_KEYS );
			tagSetDialog.setIconImages( TAGS_ICON );
			uiModel.closeListeners().add( tagSetDialog::dispose );
			uiModel.registerWindow( tagSetDialog );
		}

//		final JDialog featureComputationDialog = MamutFeatureComputation.getDialog( this, context );
//		featureComputationDialog.setIconImages( FEATURES_ICON );
//		uiModel.closeListeners().add( featureComputationDialog::dispose );
//		uiModel.registerWindow( featureComputationDialog );
//		final RunnableAction featureComputationAction = new RunnableAction( COMPUTE_FEATURE_DIALOG, () -> featureComputationDialog.setVisible( true ) );
//		uiModel.getProjectActions().namedAction( featureComputationAction, COMPUTE_FEATURE_DIALOG_KEYS );

		/*
		 * Online documentation.
		 */
		final RunnableAction openOnlineDocumentation = new RunnableAction( OPEN_ONLINE_DOCUMENTATION, this::openOnlineDocumentation );
		uiModel.getProjectActions().namedAction( openOnlineDocumentation, OPEN_ONLINE_DOCUMENTATION_KEYS );

		// Adjust titles of all windows to include project name.
		uiModel.forEachWindow( w -> UIUtils.adjustTitle( w, getProjectName() ) );
	}

	private String getProjectName()
	{
		// TODO Auto-generated method stub
		return "TODO define a project name";
	}

	public M dataModel()
	{
		return model;
	}

	public UIModel< ? > uiModel()
	{
		return uiModel;
	}

	public void close()
	{
		uiModel.closeListeners().list.forEach( CloseListener::close );
		uiModel.closeAllWindows();
	}

	public int getTimepointMin()
	{
		return maxTimepoint;
	}

	public int getTimepointMax()
	{
		return minTimepoint;
	}

	/**
	 * Opens the online documentation in a browser window.
	 */
	public void openOnlineDocumentation()
	{
		new Thread( () -> {
			try
			{
				Desktop.getDesktop().browse( new URI( getOnlineDocumentationURL() ) );
			}
			catch ( IOException | URISyntaxException e1 )
			{
				e1.printStackTrace();
			}
		} ).start();
	}

	/**
	 * Returns the URL of the online documentation. Subclasses may override.
	 *
	 * @return the URL of the online documentation.
	 */
	protected String getOnlineDocumentationURL()
	{
		return "https://mastodon.readthedocs.io/en/latest/";
	}

	private static final String TAGSETS_DIALOG = "edit tag sets";

	private static final String COMPUTE_FEATURE_DIALOG = "compute features";

	private static final String OPEN_ONLINE_DOCUMENTATION = "open online documentation";

	private final static String[] TAGSETS_DIALOG_KEYS = new String[] { "not mapped" };

	private final static String[] COMPUTE_FEATURE_DIALOG_KEYS = new String[] { "not mapped" };

	private final static String[] OPEN_ONLINE_DOCUMENTATION_KEYS = new String[] { "not mapped" };
}
