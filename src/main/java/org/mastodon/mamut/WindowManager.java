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
package org.mastodon.mamut;

import static org.mastodon.app.MastodonIcons.FEATURES_ICON;
import static org.mastodon.app.MastodonIcons.TAGS_ICON;

import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.ActionMap;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.mastodon.app.plugin.PluginUtils;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.FeatureColorModeConfigPage;
import org.mastodon.mamut.feature.MamutFeatureProjectionsManager;
import org.mastodon.mamut.managers.StyleManagerFactory;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.mamut.views.MamutViewI;
import org.mastodon.model.tag.ui.TagSetDialog;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeymapSettingsPage;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextProvider;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.context.HasContextProvider;
import org.scijava.Context;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.tools.ToggleDialogAction;
import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;
import bdv.ui.settings.SettingsPage;
import bdv.util.InvokeOnEDT;

/**
 * Main GUI class for the Mastodon Mamut application.
 * <p>
 * It controls the creation of new views, and maintain a list of currently
 * opened views, along with the managers that they may need.
 * 
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 *
 */
public class WindowManager
{

	public static final String PREFERENCES_DIALOG = "Preferences";
	public static final String TAGSETS_DIALOG = "edit tag sets";
	public static final String COMPUTE_FEATURE_DIALOG = "compute features";
	public static final String OPEN_ONLINE_DOCUMENTATION = "open online documentation";

	static final String[] NEW_TABLE_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_SELECTION_TABLE_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] PREFERENCES_DIALOG_KEYS = new String[] { "meta COMMA", "ctrl COMMA" };
	static final String[] TAGSETS_DIALOG_KEYS = new String[] { "not mapped" };
	static final String[] COMPUTE_FEATURE_DIALOG_KEYS = new String[] { "not mapped" };
	static final String[] OPEN_ONLINE_DOCUMENTATION_KEYS = new String[] { "not mapped" };

	static final String[] NEW_BRANCH_TRACKSCHEME_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_HIERARCHY_TRACKSCHEME_VIEW_KEYS = new String[] { "not mapped" };

	public static final String DOCUMENTATION_URL = "https://mastodon.readthedocs.io/en/latest/";

	/**
	 * The {@link ContextProvider}s of all currently open BigDataViewer windows.
	 */
	private final List< ContextProvider< Spot > > contextProviders = new ArrayList<>();

	/**
	 * Stores the various managers used to manage view styles, features, etc.
	 */
	private final Map< Class< ? >, Object > managers = new HashMap<>();

	/** Stores the different lists of mamut views currently opened. */
	private final Map< Class< ? extends MamutViewI >, List< MamutViewI > > openedViews = new HashMap<>();

	private final Map< Class< ? extends MamutViewI >, Listeners.List< ViewCreatedListener< ? extends MamutViewI > > > creationListeners = new HashMap<>();

	private final TagSetDialog tagSetDialog;

	private final JDialog featureComputationDialog;

	private final PreferencesDialog settings;

	/**
	 * The list of windows, that are not Mamut views, registered to this window
	 * manager.
	 */
	private final List< Window > registeredWindows = new ArrayList<>();

	private final ProjectModel appModel;

	private final MamutViews mamutViews;

	/**
	 * Creates new WindowManager.
	 * 
	 * @param appModel
	 *            the parent project model instance. This window manager
	 *            instance will be a component of this project model.
	 * 
	 */
	public WindowManager( final ProjectModel appModel )
	{
		this.appModel = appModel;
		final Context context = appModel.getContext();
		final Model model = appModel.getModel();
		final KeymapManager keymapManager = appModel.getKeymapManager();
		final Keymap keymap = keymapManager.getForwardSelectedKeymap();
		final Actions projectActions = appModel.getProjectActions();

		/*
		 * Preferences dialog.
		 */
		this.settings = new PreferencesDialog( null, keymap, new String[] { KeyConfigContexts.MASTODON } );
		final ToggleDialogAction tooglePreferencesDialogAction = new ToggleDialogAction( PREFERENCES_DIALOG, settings );
		projectActions.namedAction( tooglePreferencesDialogAction, PREFERENCES_DIALOG_KEYS );

		/*
		 * Create, discover and store managers.
		 */

		discoverManagers();
		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();
		managers.put( FeatureColorModeManager.class, featureColorModeManager );
		final MamutFeatureProjectionsManager featureProjectionsManager = new MamutFeatureProjectionsManager( context.getService( FeatureSpecsService.class ), featureColorModeManager );
		featureProjectionsManager.setModel( model, appModel.getSharedBdvData().getSources().size() );
		managers.put( MamutFeatureProjectionsManager.class, featureProjectionsManager );

		/*
		 * Discover and handle view factories
		 */
		this.mamutViews = discoverViewFactories();
		// Build actions to create these views.
		for ( final Class< ? extends MamutViewI > klass : mamutViews.getKeys() )
		{
			final MamutViewFactory< ? extends MamutViewI > factory = mamutViews.getFactory( klass );
			final RunnableAction createViewAction = new RunnableAction( factory.getCommandName(), () -> createView( klass ) );
			projectActions.namedAction( createViewAction, factory.getCommandKeys() );
		}

		/*
		 * Discover and handle command descriptions.
		 */
		final CommandDescriptions descriptions = buildCommandDescriptions();
		final Consumer< Keymap > augmentInputTriggerConfig = k -> descriptions.augmentInputTriggerConfig( k.getConfig() );
		keymapManager.getUserStyles().forEach( augmentInputTriggerConfig );
		keymapManager.getBuiltinStyles().forEach( augmentInputTriggerConfig );

		/*
		 * Actions to create dialogs.
		 */
		final RunnableAction editTagSetsAction = new RunnableAction( TAGSETS_DIALOG, this::editTagSets );
		final RunnableAction featureComputationAction = new RunnableAction( COMPUTE_FEATURE_DIALOG, this::computeFeatures );
		final RunnableAction openOnlineDocumentation = new RunnableAction( OPEN_ONLINE_DOCUMENTATION, this::openOnlineDocumentation );
		projectActions.namedAction( editTagSetsAction, TAGSETS_DIALOG_KEYS );
		projectActions.namedAction( featureComputationAction, COMPUTE_FEATURE_DIALOG_KEYS );
		projectActions.namedAction( openOnlineDocumentation, OPEN_ONLINE_DOCUMENTATION_KEYS );

		/*
		 * Extra settings pages.
		 */
		settings.addPage( new KeymapSettingsPage( "Settings > Keymap", keymapManager, descriptions ) );
		settings.addPage( new FeatureColorModeConfigPage( "Settings > Feature Color Modes", featureColorModeManager,
				featureProjectionsManager, "Spot", "Link" ) );
		settings.pack();

		/*
		 * Tag-set and feature computation dialogs
		 */
		tagSetDialog = new TagSetDialog( null, model.getTagSetModel(), model, keymap, new String[] { KeyConfigContexts.MASTODON } );
		tagSetDialog.setIconImages( TAGS_ICON );
		featureComputationDialog = MamutFeatureComputation.getDialog( appModel, context );
		featureComputationDialog.setIconImages( FEATURES_ICON );

		/*
		 * Register windows.
		 */
		registeredWindows.add( featureComputationDialog );
		registeredWindows.add( tagSetDialog );
		registeredWindows.add( settings );
		forEachWindow( w -> adjustTitle( w, appModel.getProjectName() ) );
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
	public < T extends MamutViewI > T createView( final Class< T > klass )
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
	 * @return a new instance of the view, or <code>null</code> if the view
	 *         class is unknown to the window manager.
	 */
	public synchronized < T extends MamutViewI > T createView( final Class< T > klass, final Map< String, Object > guiState )
	{
		// Get the right factory.
		final MamutViewFactory< T > factory = mamutViews.getFactory( klass );

		// Return null if the view type is unknown to us.
		if ( factory == null )
			return null;

		// Create the view.
		final T view = factory.create( appModel );

		// Adjust the frame name.
		adjustTitle( view.getFrame(), appModel.getProjectName() );

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
			forEachView( v -> {
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
			getViewList( klass ).remove( view );

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
				forEachView( v -> {
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
	 * Adds the create view sub-menu 'Window' to the specified menu, using the specified action-map. 
	 * @param menu the menu to add to.
	 * @param actionMap the action map of the frame where the menu is.
	 */
	public void addWindowMenu( final ViewMenu menu, final ActionMap actionMap )
	{
		mamutViews.addWindowMenuTo( menu, actionMap );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private MamutViews discoverViewFactories()
	{
		final MamutViews mamutViews = new MamutViews();
		final Consumer< MamutViewFactory > registerViewFactory = factory -> mamutViews.register( factory, appModel );
		PluginUtils.forEachDiscoveredPlugin( MamutViewFactory.class, registerViewFactory, appModel.getContext() );
		return mamutViews;
	}

	/**
	 * Discovers the {@link StyleManagerFactory}s present at runtime. Uses them
	 * to instantiate the managers, and registers them into the
	 * {@link #managers} map. If the factory can return a {@link SettingsPage},
	 * create one and add it to the {@link #settings} page.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private void discoverManagers()
	{
		final Consumer< StyleManagerFactory > registerAction = ( factory ) -> {
			final Object manager = factory.create( appModel );
			managers.put( factory.getManagerClass(), manager );
			// Settings page.
			if ( factory.hasSettingsPage() )
				settings.addPage( factory.createSettingsPage( manager ) );
		};
		PluginUtils.forEachDiscoveredPlugin( StyleManagerFactory.class, registerAction, appModel.getContext() );
	}

	/**
	 * Returns the manager object of the specified class used in this window
	 * manager, or <code>null</code> if a manager of the specified class does
	 * not exist.
	 * 
	 * @param <T>
	 *            the manager type.
	 * @param klass
	 *            the manager class.
	 * @return the manager instance or <code>null</code>.
	 */
	public < T > T getManager( final Class< T > klass )
	{
		@SuppressWarnings( "unchecked" )
		final T manager = ( T ) managers.get( klass );
		return manager;
	}

	/**
	 * Returns the list of opened mamut views of the specified type, or
	 * <code>null</code> if a view of this type is not registered.
	 * 
	 * @param <T>
	 *            the view type, must extend {@link MamutViewI}.
	 * @param klass
	 *            the view class, must extend {@link MamutViewI}.
	 * @return the list of view of specified class, or <code>null</code>.
	 */
	public < T extends MamutViewI > List< T > getViewList( final Class< T > klass )
	{
		@SuppressWarnings( "unchecked" )
		final List< T > list = ( List< T > ) openedViews.get( klass );
		return list;
	}

	/**
	 * Executes the specified action for all the currently opened mamut views of
	 * the specified class.
	 * 
	 * @param action
	 *            the action to execute.
	 * @param klass
	 *            the view class.
	 * @param <T>
	 *            the type of the view to operate on.
	 */
	public < T extends MamutViewI > void forEachView( final Class< T > klass, final Consumer< T > action )
	{
		Optional.ofNullable( getViewList( klass ) )
				.orElse( Collections.emptyList() )
				.forEach( action );
	}

	/**
	 * Executes the specified action for all the currently opened mamut views.
	 * 
	 * @param action
	 *            the action to execute.
	 */
	public void forEachView( final Consumer< ? super MamutViewI > action )
	{
		openedViews.forEach( ( k, l ) -> l.forEach( action ) );
	}

	/**
	 * Executes the specified actions for all the windows currently opened and
	 * managed by this window manager. This includes the Mamut views, and the
	 * various dialogs.
	 * 
	 * @param action
	 *            the action to execute.
	 */
	public void forEachWindow( final Consumer< ? super Window > action )
	{
		forEachView( v -> action.accept( v.getFrame() ) );
		registeredWindows.forEach( action );
	}

	/**
	 * Opens the online documentation in a browser window.
	 */
	public void openOnlineDocumentation()
	{
		new Thread( () -> {
			try
			{
				Desktop.getDesktop().browse( new URI( DOCUMENTATION_URL ) );
			}
			catch ( IOException | URISyntaxException e1 )
			{
				e1.printStackTrace();
			}
		} ).start();
	}

	/**
	 * Displays the tag-set editor dialog.
	 */
	public void editTagSets()
	{
		tagSetDialog.setVisible( true );
	}

	/**
	 * Displays the feature computation dialog.
	 */
	public void computeFeatures()
	{
		featureComputationDialog.setVisible( true );
	}

	/**
	 * Close all opened views and dialogs.
	 */
	public void closeAllWindows()
	{
		final ArrayList< Window > windows = new ArrayList<>();
		forEachWindow( w -> windows.add( w ) );
		try
		{
			InvokeOnEDT.invokeAndWait(
					() -> windows.stream()
							.filter( Objects::nonNull )
							.forEach( window -> window
									.dispatchEvent( new WindowEvent( window, WindowEvent.WINDOW_CLOSING ) ) ) );
		}
		catch ( final InvocationTargetException e )
		{
			e.printStackTrace();
		}
		catch ( final InterruptedException e )
		{
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}

	/**
	 * Exposes the preferences dialog, in which configuration options are
	 * listed.
	 * 
	 * @return the preferences dialog.
	 */
	public PreferencesDialog getPreferencesDialog()
	{
		return settings;
	}

	/**
	 * Exposes the collection of view factories used to create views from this
	 * window manager.
	 * 
	 * @return the view factories.
	 */
	public MamutViews getViewFactories()
	{
		return mamutViews;
	}

	/**
	 * Discovers and build command descriptions. Manually add descriptions for
	 * the views managed by {@link MamutViews}.
	 * 
	 * @return the command descriptions object.
	 */
	private CommandDescriptions buildCommandDescriptions()
	{
		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
		appModel.getContext().inject( builder );
		builder.discoverProviders();
		// Manually declare command descriptions.
		builder.addManually( mamutViews.getCommandDescriptions(), KeyConfigContexts.MASTODON );
		return builder.build();
	}

	/**
	 * Interface for listeners that are notified of the creation of views of the
	 * specified class. Registered listeners will be notified when a view of the
	 * specific class is created.
	 * 
	 * @param <T>
	 *            the class of the view to listen for the creation of.
	 */
	public interface ViewCreatedListener< T extends MamutViewI >
	{
		/**
		 * Called when a view of the class is created, just before it is shown.
		 * 
		 * @param view
		 *            the view that was just created.
		 */
		void viewCreated( final T view );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public < T extends MamutViewI > Listeners.List< ViewCreatedListener< T > > viewCreatedListeners( final Class< T > klass )
	{
		final Listeners listeners = creationListeners.computeIfAbsent( klass, v -> new Listeners.SynchronizedList<>() );
		return ( Listeners.List< ViewCreatedListener< T > > ) listeners;
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( PREFERENCES_DIALOG, PREFERENCES_DIALOG_KEYS, "Edit Mastodon preferences." );
			descriptions.add( TAGSETS_DIALOG, TAGSETS_DIALOG_KEYS, "Edit tag definitions." );
			descriptions.add( COMPUTE_FEATURE_DIALOG, COMPUTE_FEATURE_DIALOG_KEYS, "Show the feature computation dialog." );
			descriptions.add( OPEN_ONLINE_DOCUMENTATION, OPEN_ONLINE_DOCUMENTATION_KEYS, "Open a browser with the online documentation for Mastodon." );
		}
	}

	/**
	 * Possibly appends the project name to a given window title, making sure we
	 * do not append to an already appended project name.
	 * 
	 * @param title
	 *            the initial window name.
	 * @param projectName
	 *            the project name.
	 * @return an adjusted window name.
	 */
	private static final String adjustTitle( final String title, final String projectName )
	{
		if ( projectName == null || projectName.isEmpty() )
			return title;

		final String separator = " - ";
		final int index = title.indexOf( separator );
		final String prefix = ( index < 0 ) ? title : title.substring( 0, index );
		return prefix + separator + projectName;
	}

	public static void adjustTitle( final JDialog dialog, final String projectName )
	{
		dialog.setTitle( adjustTitle( dialog.getTitle(), projectName ) );
	}

	public static void adjustTitle( final JFrame frame, final String projectName )
	{
		frame.setTitle( adjustTitle( frame.getTitle(), projectName ) );
	}

	public static void adjustTitle( final Window w, final String projectName )
	{
		if ( w instanceof JDialog )
			adjustTitle( ( JDialog ) w, projectName );
		else if ( w instanceof JFrame )
			adjustTitle( ( JFrame ) w, projectName );
	}
}
