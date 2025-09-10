package org.mastodon.app;

import static org.mastodon.mamut.MamutMenuBuilder2.windowMenu;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.ActionMap;

import org.mastodon.app.plugin.MastodonPlugins2;
import org.mastodon.app.plugin.PluginUtils;
import org.mastodon.app.ui.UIUtils;
import org.mastodon.app.ui.ViewMenu2;
import org.mastodon.app.ui.ViewMenuBuilder2;
import org.mastodon.app.ui.ViewMenuBuilder2.MenuItem;
import org.mastodon.app.views.AbstractMastodonFrameView2;
import org.mastodon.app.views.AbstractMastodonView2;
import org.mastodon.app.views.MastodonFrameView2;
import org.mastodon.app.views.MastodonViewFactory;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.mastodon.mamut.CloseListener;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.MamutMenuBuilder2;
import org.mastodon.mamut.PreferencesDialog;
import org.mastodon.mamut.managers.StyleManagerFactory2;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.ForwardingNavigationHandler;
import org.mastodon.model.ForwardingTimepointModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.TimepointModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeymapSettingsPage;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextProvider;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.context.HasContextProvider;
import org.scijava.Context;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider.Scope;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.ToggleDialogAction;
import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;
import bdv.util.InvokeOnEDT;

/**
 * Components of a {@link AppModel} that stores elements related to user
 * interfaces (creating views, making them interactive, and in sync). This class
 * is meant to create only components that do not depend on the data model.
 *
 * @author Jean-Yves Tinevez
 */
public class UIModel< AM extends AppModel< AM, ?, ?, ?, ? > >
{

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public final GroupableModelFactory< NavigationHandler< ?, ? > > NAVIGATION = ( GroupableModelFactory ) new ForwardingNavigationHandler.Factory<>();

	public final GroupableModelFactory< TimepointModel > TIMEPOINT = ForwardingTimepointModel.factory;

	private final Context context;

	private final GroupManager groupManager;

	private final KeyPressedManager keyPressedManager;

	private final KeymapManager keymapManager;

	private final MastodonPlugins2< ?, ? > plugins;

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
	private final Map< Class< ? >, List< MastodonFrameView2 > > openedViews = new HashMap<>();

	/** Listeners that are notified when a view is created. */
	private final Map< Class< ? extends MastodonFrameView2 >, Listeners.List< ViewCreatedListener< ? extends MastodonFrameView2 > > > creationListeners = new HashMap<>();

	/** Manages the collections of view factories. */
	protected final ViewFactories viewFactories;

	/**
	 * The {@link ContextProvider}s of all currently opened
	 * {@link HasContextProvider} views.
	 */
	private final List< ContextProvider< Spot > > contextProviders = new ArrayList<>();

	/** Collection of listeners that are notified when the project is closed. */
	private final Listeners.List< CloseListener > closeListeners = new Listeners.List<>();

	protected final PreferencesDialog settings;

	private final Scope scope;

	/**
	 * Instantiates a UI model.
	 *
	 * @param viewFactoryType
	 *            the class of view factories managed by this UI model. It is
	 *            specific to an app.
	 * @param context
	 *            the SciJava context.
	 * @param numGroups
	 *            the number of view groups to manage.
	 * @param keyPressedManager
	 *            the key-pressed manager that will manage key events and
	 *            dispatch them to the appropriate view.
	 * @param keymapManager
	 *            the keymap manager that manages the keymaps and the currently
	 *            selected keymap. Again, this is app-specific to avoid mixing
	 *            keymaps and actions from different apps.
	 * @param plugins
	 *            the plugins that are available in this application.
	 * @param globalActions
	 *            the global actions that are available in this application.
	 * @param keyConfigContexts
	 *            the key config contexts of the views managed by this app.
	 * @param vft
	 */
	@SuppressWarnings( "unchecked" )
	public UIModel(
			final Context context,
			@SuppressWarnings( "rawtypes" ) final Class viewFactoryType,
			final int numGroups,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins2< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts,
			final Scope scope )
	{
		this.context = context;
		this.plugins = plugins;
		this.projectActions = globalActions;
		this.scope = scope;

		this.groupManager = new GroupManager( numGroups );
		groupManager.registerModel( TIMEPOINT );
		groupManager.registerModel( NAVIGATION );

		this.keyPressedManager = keyPressedManager;
		this.keymapManager = keymapManager;
		this.keyConfigContexts = keyConfigContexts;

		final InputTriggerConfig keyconf = keymapManager.getForwardSelectedKeymap().getConfig();
		this.modelActions = new Actions( keyconf, keyConfigContexts );

		/*
		 * Discover view factories.
		 */
		this.viewFactories = new ViewFactories();
		final Consumer< ? extends MastodonViewFactory< ?, AM > > registerViewFactory = factory -> viewFactories.register( factory );
		PluginUtils.forEachDiscoveredPlugin( viewFactoryType, registerViewFactory, context );

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
		settings.addPage( new KeymapSettingsPage( "Settings > Keymap", keymapManager, descriptions ) );
		settings.pack();

		/*
		 * FIXME: This issue is that we will discover and register all factories
		 * that implements StyleManagerFactory2, and some might not be adequate
		 * to an app. Solution is to have an app-specific interface that extends
		 * StyleManagerFactory2, and ask callers to provide that interface.
		 */
		@SuppressWarnings( "rawtypes" )
		final Consumer< StyleManagerFactory2 > registerAction = ( factory ) -> {
			final Object manager = factory.create( this );
			registerInstance( manager );
			// Settings page.
			if ( factory.hasSettingsPage() )
				settings.addPage( factory.createSettingsPage( manager ) );
		};
		PluginUtils.forEachDiscoveredPlugin( StyleManagerFactory2.class, registerAction, context );
	}

	public ViewFactories getViewFactories()
	{
		return viewFactories;
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
	public MastodonPlugins2< ?, ? > getPlugins()
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

	public PreferencesDialog getPreferencesDialog()
	{
		return settings;
	}

	/*
	 * Singletons.
	 */

	/**
	 * Returns the instance of the specified class used in this app, or
	 * <code>null</code> if an instance of the specified class has not been
	 * registered.
	 *
	 * @param <I>
	 *            the instance type.
	 * @param klass
	 *            the instance class.
	 * @return the instance or <code>null</code>.
	 */
	public < I > I getInstance( final Class< I > klass )
	{
		@SuppressWarnings( "unchecked" )
		final I manager = ( I ) singletons.get( klass );
		return manager;
	}

	/**
	 * Registers the specified instance to be retrievable by its class using
	 * {@link #getInstance(Class)}.
	 *
	 * @param <I>
	 *            the instance type.
	 * @param instance
	 *            the instance to register.
	 */
	public < I > void registerInstance( final I instance )
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

	< V extends MastodonFrameView2 > void registerView( final V view )
	{
		openedViews.computeIfAbsent( view.getClass(), k -> new ArrayList<>() ).add( view );
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
	 * @param <V>
	 *            the view type, must extend {@link AbstractMastodonView2}.
	 * @param klass
	 *            the view class, must extend {@link AbstractMastodonView2}.
	 * @return a new, unmodified list of view of specified class.
	 */
	public < V extends MastodonFrameView2 > List< V > getViewList( final Class< V > klass )
	{
		@SuppressWarnings( "unchecked" )
		final List< V > list = ( List< V > ) openedViews.get( klass );
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
	 * @param <V>
	 *            the type of the view to operate on.
	 */
	@SuppressWarnings( "unchecked" )
	public < V extends MastodonFrameView2 > void forEachView( final Class< V > klass, final Consumer< V > action )
	{
		Optional.ofNullable( openedViews.get( klass ) )
				.orElse( Collections.emptyList() )
				.forEach( ( Consumer< ? super MastodonFrameView2 > ) action );
	}

	/**
	 * Executes the specified action for all the currently opened views.
	 *
	 * @param action
	 *            the action to execute.
	 */
	@SuppressWarnings( "unchecked" )
	public < V extends MastodonFrameView2 > void forEachView( final Consumer< V > action )
	{
		openedViews.forEach( ( k, l ) -> l.forEach( ( Consumer< ? super MastodonFrameView2 > ) action ) );
	}

	/**
	 * Executes the specified actions for all the windows currently opened and
	 * managed by this window manager. This includes the
	 * {@link AbstractMastodonFrameView2} views, and the various dialogs.
	 *
	 * @param action
	 *            the action to execute.
	 */
	public void forEachWindow( final Consumer< ? super Window > action )
	{
		forEachView( v -> {
			if ( v instanceof AbstractMastodonFrameView2 )
				action.accept( ( ( AbstractMastodonFrameView2< ?, ?, ?, ?, ?, ? > ) v ).getFrame() );
		} );
		registeredWindows.forEach( action );
	}

	/*
	 * Views and view factories.
	 */

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
	public < T extends MastodonFrameView2 > T createView( final AM appModel, final Class< T > klass )
	{
		return createView( appModel, klass, Collections.emptyMap() );
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
	 * @param appModel
	 * @return a new instance of the view, or <code>null</code> if the view
	 *         class is unknown to the window manager.
	 */
	public synchronized < T extends MastodonFrameView2 > T createView(
			final AM appModel,
			final Class< T > klass,
			final Map< String, Object > guiState )
	{
		// Get the right factory.
		final MastodonViewFactory< ?, AM > factory = viewFactories.getFactory( klass );

		// Return null if the view type is unknown to us.
		if ( factory == null )
			throw new UnsupportedOperationException( "No view factory that can create views of type "
					+ klass.getName() + " have been registered to this UI model." );

		// Create the view.
		@SuppressWarnings( "unchecked" )
		final T view = ( T ) factory.create( appModel );

		// Adjust the frame name.
		UIUtils.adjustTitle( view.getFrame(), appModel.getProjectName() );

		// Restore the view GUI state.
		@SuppressWarnings( "unchecked" )
		final MastodonViewFactory< T, AM > f = ( ( MastodonViewFactory< T, AM > ) factory );
		f.restoreGuiState( view, guiState );

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
		return new CommandDescriptionProvider( scope, KeyConfigContexts.MASTODON )
		{

			@Override
			public void getCommandDescriptions( final CommandDescriptions descriptions )
			{
				for ( final MastodonViewFactory< ?, AM > viewFactory : viewFactories.factories.values() )
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

		private final Map< Class< ? extends MastodonFrameView2 >, MastodonViewFactory< ?, AM > > factories = new HashMap<>();

		private final ArrayList< MenuItem > menuItems;

		private final HashMap< String, String > menuTexts;

		ViewFactories()
		{
			menuItems = new ArrayList<>();
			menuTexts = new HashMap<>();
		}

		synchronized void register( final MastodonViewFactory< ?, AM > factory )
		{
			if ( !factories.containsValue( factory ) )
			{
				factories.put( factory.getViewClass(), factory );
				menuItems.add( ViewMenuBuilder2.item( factory.getCommandName() ) );
				menuTexts.put( factory.getCommandName(), factory.getCommandMenuText() );
			}
		}

		/**
		 * Returns the collection of view classes for which we have a factory.
		 *
		 * @return the collection of view classes.
		 */
		public Collection< Class< ? extends MastodonFrameView2 > > getKeys()
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
		public < T > MastodonViewFactory< ?, AM > getFactory( final Class< T > klass )
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
					for ( final MastodonViewFactory< ?, AM > factory : factories.values() )
						descriptions.add( factory.getCommandName(), factory.getCommandKeys(), factory.getCommandDescription() );
				}
			};
		}

		public void addWindowMenuTo( final ViewMenu2 menu, final ActionMap actionMap )
		{
			MamutMenuBuilder2.build( menu, actionMap, menuTexts, windowMenu( menuItems.toArray( new MenuItem[ 0 ] ) ) );
		}
	}

	/**
	 * Interface for listeners that are notified of the creation of views of the
	 * specified class. Registered listeners will be notified when a view of the
	 * specific class is created.
	 *
	 * @param <T>
	 *            the class of the view to listen for the creation of.
	 */
	public interface ViewCreatedListener< T extends MastodonFrameView2 >
	{
		/**
		 * Called when a view of the class is created, just before it is shown.
		 *
		 * @param view
		 *            the view that was just created.
		 */
		void viewCreated( final T view );
	}

	private static final String PREFERENCES_DIALOG = "Preferences";

	private final static String[] PREFERENCES_DIALOG_KEYS = new String[] { "meta COMMA", "ctrl COMMA" };
}
