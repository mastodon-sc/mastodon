package org.mastodon.app.ui;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.mastodon.app.AppModel;
import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.app.plugin.PluginUtils;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.DefaultFeatureProjectionsManager;
import org.mastodon.feature.ui.FeatureColorModeConfigPage;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.mastodon.mamut.CloseListener;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.PreferencesDialog;
import org.mastodon.mamut.managers.StyleManagerFactory2;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.mamut.views.MamutViewI;
import org.mastodon.model.ForwardingNavigationHandler;
import org.mastodon.model.ForwardingTimepointModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.TimepointModel;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeymapSettingsPage;
import org.scijava.Context;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.ToggleDialogAction;
import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;
import bdv.util.InvokeOnEDT;

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

	private final Context context;

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

	/**
	 * Stores the various singleton instances used in an app instance, to manage
	 * view styles, features, coloring models and feature models. It makes sense
	 * to store them here when there is a single instance needed for the app,
	 * and when their use cannot be predicted now.
	 */
	private final Map< Class< ? >, Object > singletons = new HashMap<>();

	/**
	 * The list of windows, that are not data views, registered to this model.
	 */
	private final List< Window > registeredWindows = new ArrayList<>();

	/** Stores the different lists of data views currently opened. */
	private final Map< Class< ? extends MastodonView2< ?, ?, ?, ?, ?, ? > >, List< MastodonView2< ?, ?, ?, ?, ?, ? > > > openedViews = new HashMap<>();

	/** Collection of listeners that are notified when the project is closed. */
	private final Listeners.List< CloseListener > closeListeners = new Listeners.List<>();

	private final PreferencesDialog settings;

	public UIModel(
			final Context context,
			final int numGroups,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts )
	{
		this.context = context;
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

		/*
		 * Singleton instances.
		 */
		registerInstance( new FeatureModel() );

		/*
		 * Preferences dialog.
		 */
		final Keymap keymap = keymapManager.getForwardSelectedKeymap();
		this.settings = new PreferencesDialog( null, keymap, keyConfigContexts );
		final ToggleDialogAction tooglePreferencesDialogAction = new ToggleDialogAction( PREFERENCES_DIALOG, settings );
		getProjectActions().namedAction( tooglePreferencesDialogAction, PREFERENCES_DIALOG_KEYS );
		registerWindow( settings );
		closeListeners.add( settings::dispose );

		/*
		 * Command descriptions.
		 */
		final CommandDescriptions descriptions = buildCommandDescriptions();
		final Consumer< Keymap > augmentInputTriggerConfig = k -> descriptions.augmentInputTriggerConfig( k.getConfig() );
		keymapManager.getUserStyles().forEach( augmentInputTriggerConfig );
		keymapManager.getBuiltinStyles().forEach( augmentInputTriggerConfig );

		/*
		 * Extra settings pages.
		 */
		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();
		registerInstance( featureColorModeManager );
		final DefaultFeatureProjectionsManager featureProjectionsManager = new DefaultFeatureProjectionsManager( context.getService( FeatureSpecsService.class ), featureColorModeManager );
		settings.addPage( new KeymapSettingsPage( "Settings > Keymap", keymapManager, descriptions ) );
		settings.addPage( new FeatureColorModeConfigPage( "Settings > Feature Color Modes", featureColorModeManager, featureProjectionsManager, "Vertex", "Edge" ) );
		settings.pack();

		/*
		 * FIXME: This issue is that we will discover and register all factories
		 * that implements StyleManagerFactory2, and some might not be adequate
		 * to an app. Solution is to have an app-specific interface that extends
		 * StyleManagerFactory2, and ask callers to provide that interface.
		 */
		@SuppressWarnings( { "rawtypes", "unchecked" } )
		final Consumer< StyleManagerFactory2 > registerAction = ( factory ) -> {
			final Object manager = factory.create( this );
			registerInstance( manager );
			// Settings page.
			if ( factory.hasSettingsPage() )
				settings.addPage( factory.createSettingsPage( manager ) );
		};
		PluginUtils.forEachDiscoveredPlugin( StyleManagerFactory2.class, registerAction, context );

	}

	public Context getContext()
	{
		return context;
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

	public Listeners.List< CloseListener > closeListeners()
	{
		return closeListeners;
	}

	/*
	 * Singletons.
	 */

	/**
	 * Returns the instance of the specified class used in this app, or
	 * <code>null</code> if an instance of the specified class has not been
	 * registered.
	 *
	 * @param <T>
	 *            the instance type.
	 * @param klass
	 *            the instance class.
	 * @return the instance or <code>null</code>.
	 */
	public < T > T getInstance( final Class< T > klass )
	{
		@SuppressWarnings( "unchecked" )
		final T manager = ( T ) singletons.get( klass );
		return manager;
	}

	/**
	 * Registers the specified instance to be retrievable by its class using
	 * {@link #getInstance(Class)}.
	 *
	 * @param <T>
	 *            the instance type.
	 * @param instance
	 *            the instance to register.
	 */
	public < T > void registerInstance( final T instance )
    {
        singletons.put( instance.getClass(), instance );
    }

	/*
	 * Windows and views.
	 */

	/**
	 * Registers the specified window to be managed by this model. This method
	 * is suitable to windows that are not data views, such as dialogs.
	 *
	 * @param window
	 *            the window to register.
	 */
	public void registerWindow( final Window window )
	{
		registeredWindows.add( window );
	}

	/**
	 * Closes all opened views and dialogs.
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
	 * Returns the list of opened views of the specified type. The list can be
	 * empty if a view of this type is not registered, but is never
	 * <code>null</code>.
	 *
	 * @param <T>
	 *            the view type, must extend {@link MastodonView2}.
	 * @param klass
	 *            the view class, must extend {@link MastodonView2}.
	 * @return a new, unmodified list of view of specified class.
	 */
	public < T extends MastodonView2< ?, ?, ?, ?, ?, ? > > List< T > getViewList( final Class< T > klass )
	{
		@SuppressWarnings( "unchecked" )
		final List< T > list = ( List< T > ) openedViews.get( klass );
		if ( list == null )
			return Collections.emptyList();
		return Collections.unmodifiableList( list );
	}

	/**
	 * Executes the specified action for all the currently opened views of the
	 * specified class.
	 *
	 * @param action
	 *            the action to execute.
	 * @param klass
	 *            the view class.
	 * @param <T>
	 *            the type of the view to operate on.
	 */
	@SuppressWarnings( "unchecked" )
	public < T extends MastodonView2< ?, ?, ?, ?, ?, ? > > void forEachView( final Class< T > klass, final Consumer< T > action )
	{
		Optional.ofNullable( ( List< T > ) openedViews.get( klass ) )
				.orElse( Collections.emptyList() )
				.forEach( action );
	}

	/**
	 * Executes the specified action for all the currently opened views.
	 *
	 * @param action
	 *            the action to execute.
	 */
	public void forEachView( final Consumer< ? super MastodonView2< ?, ?, ?, ?, ?, ? > > action )
	{
		openedViews.forEach( ( k, l ) -> l.forEach( action ) );
	}

	/**
	 * Executes the specified actions for all the windows currently opened and
	 * managed by this window manager. This includes the
	 * {@link MastodonFrameView2} views, and the various dialogs.
	 *
	 * @param action
	 *            the action to execute.
	 */
	public void forEachWindow( final Consumer< ? super Window > action )
	{
		forEachView( v -> {
			if ( v instanceof MastodonFrameView2 )
				action.accept( ( ( MastodonFrameView2< ?, ?, ?, ?, ?, ? > ) v ).getFrame() );
		} );
		registeredWindows.forEach( action );
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
		context.inject( builder );
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
				// FIXME: Restrict the discovery to the views of a specific app.
				for ( final MamutViewFactory< ? extends MamutViewI > factory : factories.values() )
					descriptions.add( factory.getCommandName(), factory.getCommandKeys(), factory.getCommandDescription() );
			}
		};
	}

	private static final String PREFERENCES_DIALOG = "Preferences";
	private final static String[] PREFERENCES_DIALOG_KEYS = new String[] { "meta COMMA", "ctrl COMMA" };

}
