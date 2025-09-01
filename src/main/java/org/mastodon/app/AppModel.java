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
import static org.mastodon.mamut.MamutMenuBuilder.windowMenu;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ActionMap;

import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.app.plugin.PluginUtils;
import org.mastodon.app.ui.MastodonFrameView2;
import org.mastodon.app.ui.UIModel;
import org.mastodon.app.ui.UIUtils;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.CloseListener;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.MamutFeatureComputation;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.WindowManager.ViewCreatedListener;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.tag.ui.TagSetDialog;
import org.mastodon.ui.coloring.TrackGraphColorGenerator;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextProvider;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.context.HasContextProvider;
import org.scijava.Context;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;
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
		E extends Edge< V >,
		VF extends MastodonViewFactory< ?, M, G, V, E > & SciJavaPlugin >
{

	protected final M model;

	protected final UIModel uiModel;

	/** Manages the collections of view factories. */
	protected final ViewFactories viewFactories;

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
			final Class< VF > viewFactoryType,
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
		this.uiModel = new UIModel( context, numGroups, keyPressedManager, keymapManager, plugins, globalActions, keyConfigContexts );

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
		

		/*
		 * Discover view factories.
		 */
		this.viewFactories = new ViewFactories();
		final Consumer< VF > registerViewFactory = factory -> viewFactories.register( factory );
		PluginUtils.forEachDiscoveredPlugin( viewFactoryType, registerViewFactory, context );
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

	public UIModel uiModel()
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

	/*
	 * Views and view factories.
	 */

	public ViewFactories getViewFactories()
	{
		return viewFactories;
	}

	/**
	 * Creates, shows, registers and returns a view of the specified class with
	 * default GUI state.
	 *
	 * @param <T>
	 *            the view type.
	 * @param klass
	 *            the view class.
	 * @return a new instance of the view, that was shown.
	 */
	public < T extends MastodonFrameView2< M, ?, V, E, ?, ? > > T createView( final Class< T > klass )
	{
		return createView( klass, Collections.emptyMap() );
	}

	/**
	 * Creates, shows, registers and returns a view of the specified class, with
	 * GUI state read from the specified map.
	 * <p>
	 * Return <code>null</code> if the type of view is unknown to the window
	 * manager.
	 *
	 * @param <T>
	 *            the view type.
	 * @param klass
	 *            the view class.
	 * @param guiState
	 *            the GUI state map.
	 * @param appModel
	 * @return a new instance of the view, or <code>null</code> if the view
	 *         class is unknown to the window manager.
	 */
	public synchronized < T extends MastodonFrameView2< M, ?, V, E, ?, ? > > T createView(
			final Class< T > klass,
			final Map< String, Object > guiState )
	{
		// Get the right factory.
		@SuppressWarnings( "unchecked" )
		final MastodonViewFactory< T, M, G, V, E > factory = ( MastodonViewFactory< T, M, G, V, E > ) viewFactories.getFactory( klass );

		// Return null if the view type is unknown to us.
		if ( factory == null )
			return null;

		// Create the view.
		final T view = factory.create( this );

		// Adjust the frame name.
		UIUtils.adjustTitle( view.getFrame(), getProjectName() );

		// Restore the view GUI state.
		factory.restoreGuiState( view, guiState );

		// Store the view for window manager.
		openedViews.computeIfAbsent( klass, ( v ) -> new ArrayList<>() ).add( view );

		// Does it has a context chooser?
		if ( view instanceof HasContextChooser )
		{
			@SuppressWarnings( "unchecked" )
			final ContextChooser< Spot > cc = ( ( HasContextChooser< Spot > ) view ).getContextChooser();
			cc.updateContextProviders( contextProviders );
		}

		// Does it has a context provider?
		if ( view instanceof HasContextProvider )
		{
			final ContextProvider< Spot > cp = ( ( HasContextProvider ) view ).getContextProvider();
			contextProviders.add( cp );
			// Notify context choosers.
			uiModel.forEachView( v -> {
				if ( v instanceof HasContextChooser )
				{
					@SuppressWarnings( "unchecked" )
					final HasContextChooser< Spot > cc = ( HasContextChooser< Spot > ) v;
					cc.getContextChooser().updateContextProviders( contextProviders );
				}
			} );
		}

		// Register close listener.
		view.onClose( () -> {
			// Remove view from list of opened views.
			openedViews.get( klass ).remove( view );

			if ( view instanceof HasContextChooser )
			{
				// Remove context providers from it.
				@SuppressWarnings( "unchecked" )
				final ContextChooser< Spot > cc = ( ( HasContextChooser< Spot > ) view ).getContextChooser();
				cc.updateContextProviders( new ArrayList<>() );
			}

			if ( view instanceof HasContextProvider )
			{
				// Remove it from the list of context providers.
				final ContextProvider< Spot > cp = ( ( HasContextProvider ) view ).getContextProvider();
				contextProviders.remove( cp );
				// Notify context choosers.
				uiModel.forEachView( v -> {
					if ( v instanceof HasContextChooser )
					{
						@SuppressWarnings( "unchecked" )
						final HasContextChooser< Spot > cc = ( HasContextChooser< Spot > ) v;
						cc.getContextChooser().updateContextProviders( contextProviders );
					}
				} );
			}
		} );

		// Notify listeners that it has been created.
		@SuppressWarnings( "rawtypes" )
		final Listeners.List l1 = creationListeners.get( klass );
		@SuppressWarnings( "unchecked" )
		final Listeners.List< ViewCreatedListener< T > > list = l1;
		if ( list != null )
			list.list.forEach( l -> l.viewCreated( view ) );

		// Finally, show it.
		view.getFrame().setVisible( true );
		return view;
	}

	/**
	 * Discovers and build command descriptions. Manually add descriptions for
	 * the views managed here.
	 *
	 * @return the command descriptions object.
	 */
	private CommandDescriptions buildCommandDescriptions()
	{
		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
		uiModel.getContext().inject( builder );
		builder.discoverProviders();
		// Manually declare command descriptions.
		builder.addManually( getCommandDescriptions(), KeyConfigContexts.MASTODON );
		return builder.build();
	}

	public CommandDescriptionProvider getCommandDescriptions()
	{
		return new CommandDescriptionProvider( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON )
		{

			@Override
			public void getCommandDescriptions( final CommandDescriptions descriptions )
			{
				for ( final VF viewFactory : viewFactories.factories.values() )
					descriptions.add( viewFactory.getCommandName(), viewFactory.getCommandKeys(), viewFactory.getCommandDescription() );
			}
		};
	}

	/**
	 * Manages a collection of view factories.
	 * <p>
	 * Collect and install actions, menu items, menu texts.
	 */
	public class ViewFactories
	{

		private final Map< Class< ? extends MastodonFrameView2< M, ?, V, E, ?, ? > >, VF > factories = new HashMap<>();

		private final ArrayList< MenuItem > menuItems;

		private final HashMap< String, String > menuTexts;

		ViewFactories()
		{
			menuItems = new ArrayList<>();
			menuTexts = new HashMap<>();
		}

		@SuppressWarnings( "unchecked" )
		synchronized void register( final VF factory )
		{
			if ( !factories.containsValue( factory ) )
			{
				factories.put( factory.getViewClass(), factory );
				menuItems.add( ViewMenuBuilder.item( factory.getCommandName() ) );
				menuTexts.put( factory.getCommandName(), factory.getCommandMenuText() );
			}
		}

		/**
		 * Returns the collection of view classes for which we have a factory.
		 *
		 * @return the collection of view classes.
		 */
		public Collection< Class< ? extends MastodonFrameView2< M, ?, V, E, ?, ? > > > getKeys()
		{
			return Collections.unmodifiableCollection( factories.keySet() );
		}

		/**
		 * Returns a factory for the specified view class.
		 *
		 * @param <T>
		 *            the type of view.
		 * @param klass
		 *            the class of the view.
		 * @return a view factory, or <code>null</code> if the specified class
		 *         is unknown.
		 */
		public < T > VF getFactory( final Class< T > klass )
		{
			return factories.get( klass );
		}

		public CommandDescriptionProvider getCommandDescriptions()
		{
			return new CommandDescriptionProvider( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON )
			{

				@Override
				public void getCommandDescriptions( final CommandDescriptions descriptions )
				{
					for ( final VF factory : factories.values() )
						descriptions.add( factory.getCommandName(), factory.getCommandKeys(), factory.getCommandDescription() );
				}
			};
		}

		void addWindowMenuTo( final ViewMenu menu, final ActionMap actionMap )
		{
			MamutMenuBuilder.build( menu, actionMap, menuTexts, windowMenu( menuItems.toArray( new MenuItem[ 0 ] ) ) );
		}
	}

	private static final String TAGSETS_DIALOG = "edit tag sets";

	private static final String COMPUTE_FEATURE_DIALOG = "compute features";

	private static final String OPEN_ONLINE_DOCUMENTATION = "open online documentation";

	private final static String[] TAGSETS_DIALOG_KEYS = new String[] { "not mapped" };

	private final static String[] COMPUTE_FEATURE_DIALOG_KEYS = new String[] { "not mapped" };

	private final static String[] OPEN_ONLINE_DOCUMENTATION_KEYS = new String[] { "not mapped" };
}
