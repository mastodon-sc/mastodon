/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.app.MastodonIcons.BDV_VIEW_ICON;
import static org.mastodon.app.MastodonIcons.FEATURES_ICON_LARGE;
import static org.mastodon.app.MastodonIcons.TABLE_VIEW_ICON;
import static org.mastodon.app.MastodonIcons.TAGS_ICON_LARGE;
import static org.mastodon.app.MastodonIcons.TRACKSCHEME_VIEW_ICON;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JDialog;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.FeatureColorModeConfigPage;
import org.mastodon.mamut.feature.MamutFeatureProjectionsManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.plugin.MamutPlugins;
import org.mastodon.model.tag.ui.TagSetDialog;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.CommandDescriptionsBuilder;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.ui.keymap.KeymapManager;
import org.mastodon.ui.keymap.KeymapSettingsPage;
import org.mastodon.util.ToggleDialogAction;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsConfigPage;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsManager;
import org.mastodon.views.context.ContextProvider;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleSettingsPage;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.util.InvokeOnEDT;

public class WindowManager
{
	public static final String NEW_BDV_VIEW = "new bdv view";
	public static final String NEW_TRACKSCHEME_VIEW = "new trackscheme view";
	public static final String NEW_TABLE_VIEW = "new full table view";
	public static final String NEW_SELECTION_TABLE_VIEW = "new selection table view";
	public static final String PREFERENCES_DIALOG = "Preferences";
	public static final String TAGSETS_DIALOG = "edit tag sets";
	public static final String COMPUTE_FEATURE_DIALOG = "compute features";

	static final String[] NEW_BDV_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_TRACKSCHEME_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_TABLE_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_SELECTION_TABLE_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] PREFERENCES_DIALOG_KEYS = new String[] { "meta COMMA", "ctrl COMMA" };
	static final String[] TAGSETS_DIALOG_KEYS = new String[] { "not mapped" };
	static final String[] COMPUTE_FEATURE_DIALOG_KEYS = new String[] { "not mapped" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( NEW_BDV_VIEW, NEW_BDV_VIEW_KEYS, "Open a new BigDataViewer view." );
			descriptions.add( NEW_TRACKSCHEME_VIEW, NEW_TRACKSCHEME_VIEW_KEYS, "Open a new TrackScheme view." );
			descriptions.add( NEW_TABLE_VIEW, NEW_TABLE_VIEW_KEYS, "Open a new table view. "
					+ "The table displays the full data." );
			descriptions.add( NEW_SELECTION_TABLE_VIEW, NEW_SELECTION_TABLE_VIEW_KEYS,
					"Open a new selection table view. "
							+ "The table only displays the current selection and "
							+ "is updated as the selection changes." );
			descriptions.add( PREFERENCES_DIALOG, PREFERENCES_DIALOG_KEYS, "Edit Mastodon preferences." );
			descriptions.add( TAGSETS_DIALOG, TAGSETS_DIALOG_KEYS, "Edit tag definitions." );
			descriptions.add( COMPUTE_FEATURE_DIALOG, COMPUTE_FEATURE_DIALOG_KEYS, "Show the feature computation dialog." );
		}
	}

	private final Context context;

	private final MamutPlugins plugins;

	/**
	 * All currently open BigDataViewer windows.
	 */
	private final List< MamutViewBdv > bdvWindows = new ArrayList<>();

	/**
	 * The {@link ContextProvider}s of all currently open BigDataViewer windows.
	 */
	private final List< ContextProvider< Spot > > contextProviders = new ArrayList<>();

	/**
	 * All currently open TrackScheme windows.
	 */
	private final List< MamutViewTrackScheme > tsWindows = new ArrayList<>();

	/**
	 * All currently open Table windows.
	 */
	private final List< MamutViewTable > tableWindows = new ArrayList<>();

	private final KeyPressedManager keyPressedManager;

	private final TrackSchemeStyleManager trackSchemeStyleManager;

	private final RenderSettingsManager renderSettingsManager;

	private final FeatureColorModeManager featureColorModeManager;

	private final MamutFeatureProjectionsManager featureProjectionsManager;

	private final KeymapManager keymapManager;

	private final Actions globalAppActions;

	private final AbstractNamedAction newBdvViewAction;

	private final AbstractNamedAction newTrackSchemeViewAction;

	private final AbstractNamedAction newTableViewAction;

	private final AbstractNamedAction newSelectionTableViewAction;

	private final AbstractNamedAction editTagSetsAction;

	private final AbstractNamedAction featureComputationAction;

	private MamutAppModel appModel;

	private TagSetDialog tagSetDialog;

	private JDialog featureComputationDialog;

	final ProjectManager projectManager;

	private final List< BdvViewCreatedListener > listeners;

	public WindowManager( final Context context )
	{
		this.context = context;

		keyPressedManager = new KeyPressedManager();
		trackSchemeStyleManager = new TrackSchemeStyleManager();
		renderSettingsManager = new RenderSettingsManager();
		featureColorModeManager = new FeatureColorModeManager();
		featureProjectionsManager = new MamutFeatureProjectionsManager(
				context.getService( FeatureSpecsService.class ),
				featureColorModeManager );
		keymapManager = new KeymapManager();

		final Keymap keymap = keymapManager.getForwardDefaultKeymap();

		plugins = new MamutPlugins( keymap );
		discoverPlugins();

		final CommandDescriptions descriptions = buildCommandDescriptions();
		final Consumer< Keymap > augmentInputTriggerConfig = k -> descriptions.augmentInputTriggerConfig( k.getConfig() );
		keymapManager.getUserStyles().forEach( augmentInputTriggerConfig );
		keymapManager.getBuiltinStyles().forEach( augmentInputTriggerConfig );

		// TODO: naming, this should be named appActions and the AppModel.appActions should become modelActions?
		// TODO: or rename AppModel --> ProjectModel, then projectActions?
		globalAppActions = new Actions( keymap.getConfig(), KeyConfigContexts.MASTODON );
		keymap.updateListeners().add( () -> {
			globalAppActions.updateKeyConfig( keymap.getConfig() );
			if ( appModel != null )
				appModel.getAppActions().updateKeyConfig( keymap.getConfig() );
		} );

		projectManager = new ProjectManager( this );
		projectManager.install( globalAppActions );

		newBdvViewAction = new RunnableAction( NEW_BDV_VIEW, this::createBigDataViewer );
		newTrackSchemeViewAction = new RunnableAction( NEW_TRACKSCHEME_VIEW, this::createTrackScheme );
		newTableViewAction = new RunnableAction( NEW_TABLE_VIEW, () -> createTable( false ) );
		newSelectionTableViewAction = new RunnableAction( NEW_SELECTION_TABLE_VIEW, () -> createTable( true ) );
		editTagSetsAction = new RunnableAction( TAGSETS_DIALOG, this::editTagSets );
		featureComputationAction = new RunnableAction( COMPUTE_FEATURE_DIALOG, this::computeFeatures );

		globalAppActions.namedAction( newBdvViewAction, NEW_BDV_VIEW_KEYS );
		globalAppActions.namedAction( newTrackSchemeViewAction, NEW_TRACKSCHEME_VIEW_KEYS );
		globalAppActions.namedAction( newTableViewAction, NEW_SELECTION_TABLE_VIEW_KEYS );
		globalAppActions.namedAction( newSelectionTableViewAction, NEW_SELECTION_TABLE_VIEW_KEYS );
		globalAppActions.namedAction( editTagSetsAction, TAGSETS_DIALOG_KEYS );
		globalAppActions.namedAction( featureComputationAction, COMPUTE_FEATURE_DIALOG_KEYS );

		final PreferencesDialog settings = new PreferencesDialog( null, keymap, new String[] { KeyConfigContexts.MASTODON } );
		settings.addPage( new TrackSchemeStyleSettingsPage( "TrackScheme Styles", trackSchemeStyleManager ) );
		settings.addPage( new RenderSettingsConfigPage( "BDV Render Settings", renderSettingsManager ) );
		settings.addPage( new KeymapSettingsPage( "Keymap", keymapManager, descriptions ) );
		settings.addPage( new FeatureColorModeConfigPage( "Feature Color Modes", featureColorModeManager, featureProjectionsManager ) );

		final ToggleDialogAction tooglePreferencesDialogAction = new ToggleDialogAction( PREFERENCES_DIALOG, settings );
		globalAppActions.namedAction( tooglePreferencesDialogAction, PREFERENCES_DIALOG_KEYS );

		updateEnabledActions();

		listeners = new ArrayList<>();
	}

	private void discoverPlugins()
	{
		if ( context == null )
			return;

		final PluginService pluginService = context.getService( PluginService.class );
		final List< PluginInfo< MamutPlugin > > infos = pluginService.getPluginsOfType( MamutPlugin.class );
		for ( final PluginInfo< MamutPlugin > info : infos )
		{
			try
			{
				final MamutPlugin plugin = info.createInstance();
				context.inject( plugin );
				plugins.register( plugin );
			}
			catch ( final InstantiableException e )
			{
				e.printStackTrace();
			}
		}
	}

	private void updateEnabledActions()
	{
		newBdvViewAction.setEnabled( appModel != null );
		newTrackSchemeViewAction.setEnabled( appModel != null );
		newTableViewAction.setEnabled( appModel != null );
		newSelectionTableViewAction.setEnabled( appModel != null );
		editTagSetsAction.setEnabled( appModel != null );
		featureComputationAction.setEnabled( appModel != null );
	}

	void setAppModel( final MamutAppModel appModel )
	{
		closeAllWindows();

		this.appModel = appModel;
		if ( appModel == null )
		{
			tagSetDialog.dispose();
			tagSetDialog = null;
			featureComputationDialog.dispose();
			featureComputationDialog = null;
			featureProjectionsManager.setModel( null, 1 );
			updateEnabledActions();
			return;
		}

		final Model model = appModel.getModel();
		UndoActions.install( appModel.getAppActions(), model );
		SelectionActions.install( appModel.getAppActions(), model.getGraph(), model.getGraph().getLock(), model.getGraph(), appModel.getSelectionModel(), model );
		MamutActions.install( appModel.getAppActions(), appModel );

		final Keymap keymap = keymapManager.getForwardDefaultKeymap();
		tagSetDialog = new TagSetDialog( null, model.getTagSetModel(), model, keymap, new String[] { KeyConfigContexts.MASTODON } );
		tagSetDialog.setIconImage( TAGS_ICON_LARGE.getImage() );
		featureComputationDialog = MamutFeatureComputation.getDialog( appModel, context );
		featureComputationDialog.setIconImage( FEATURES_ICON_LARGE.getImage() );
		featureProjectionsManager.setModel( model, appModel.getSharedBdvData().getSources().size() );

		updateEnabledActions();

		plugins.setAppPluginModel( new MamutPluginAppModel( appModel, this ) );
	}

	private synchronized void addBdvWindow( final MamutViewBdv w )
	{
		bdvWindows.add( w );
		contextProviders.add( w.getContextProvider() );
		for ( final MamutViewTrackScheme tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
		for ( final MamutViewTable tw : tableWindows )
			tw.getContextChooser().updateContextProviders( contextProviders );
		w.onClose( () -> {
			bdvWindows.remove( w );
			contextProviders.remove( w.getContextProvider() );
			for ( final MamutViewTrackScheme tsw : tsWindows )
				tsw.getContextChooser().updateContextProviders( contextProviders );
			for ( final MamutViewTable tw : tableWindows )
				tw.getContextChooser().updateContextProviders( contextProviders );
		} );
	}

	public void forEachBdvView( final Consumer< ? super MamutViewBdv > action )
	{
		bdvWindows.forEach( action );
	}

	private synchronized void addTsWindow( final MamutViewTrackScheme w )
	{
		tsWindows.add( w );
		w.getContextChooser().updateContextProviders( contextProviders );
		w.onClose( () -> {
			tsWindows.remove( w );
			w.getContextChooser().updateContextProviders( new ArrayList<>() );
		} );
	}

	private synchronized void addTableWindow( final MamutViewTable table )
	{
		tableWindows.add( table );
		table.getContextChooser().updateContextProviders( contextProviders );
		table.onClose( () -> {
			tableWindows.remove( table );
			table.getContextChooser().updateContextProviders( new ArrayList<>() );
		} );
	}

	public void forEachTableView( final Consumer< ? super MamutViewTable > action )
	{
		tableWindows.forEach( action );
	}

	public void forEachTrackSchemeView( final Consumer< ? super MamutViewTrackScheme > action )
	{
		tsWindows.forEach( action );
	}

	public void forEachView( final Consumer< ? super MamutView< ?, ?, ? > > action )
	{
		forEachBdvView( action );
		forEachTrackSchemeView( action );
		forEachTableView( action );
	}

	public MamutViewBdv createBigDataViewer()
	{
		return createBigDataViewer( new HashMap<>() );
	}

	public MamutViewBdv createBigDataViewer( final Map< String, Object > guiState )
	{
		if ( appModel != null )
		{
			final MamutViewBdv view = new MamutViewBdv( appModel, guiState );
			view.getFrame().setIconImage( BDV_VIEW_ICON );
			addBdvWindow( view );
			notifyListeners( view );
			return view;
		}
		return null;
	}

	public MamutViewTrackScheme createTrackScheme()
	{
		return createTrackScheme( new HashMap<>() );
	}

	public MamutViewTrackScheme createTrackScheme( final Map< String, Object > guiState )
	{
		if ( appModel != null )
		{
			final MamutViewTrackScheme view = new MamutViewTrackScheme( appModel, guiState );
			view.getFrame().setIconImage( TRACKSCHEME_VIEW_ICON );
			addTsWindow( view );
			return view;
		}
		return null;
	}

	public MamutViewTable createTable( final Map< String, Object > guiState )
	{
		if ( appModel != null )
		{
			final MamutViewTable view = new MamutViewTable( appModel, guiState );
			view.getFrame().setIconImage( TABLE_VIEW_ICON );
			addTableWindow( view );
			return view;
		}
		return null;

	}

	/**
	 * Creates, shows and registers a new table view.
	 *
	 * @param selectionOnly
	 *            if <code>true</code>, the table will only display the current
	 *            content of the selection, and will listen to its changes. If
	 *            <code>false</code>, the table will display the full graph content,
	 *            listen to its changes, and will be able to edit the selection.
	 * @return a new table view.
	 */
	public MamutViewTable createTable( final boolean selectionOnly )
	{
		final Map< String, Object > guiState = Collections.singletonMap(
				MamutViewStateSerialization.TABLE_SELECTION_ONLY, Boolean.valueOf( selectionOnly ) );
		return createTable( guiState );
	}

	public void editTagSets()
	{
		if ( appModel != null )
		{
			tagSetDialog.setVisible( true );
		}
	}

	public void computeFeatures()
	{
		if ( appModel != null )
		{
			featureComputationDialog.setVisible( true );
		}
	}

	public void closeAllWindows()
	{
		final ArrayList< Window > windows = new ArrayList<>();
		for ( final MamutViewBdv w : bdvWindows )
			windows.add( w.getFrame() );
		for ( final MamutViewTrackScheme w : tsWindows )
			windows.add( w.getFrame() );
		for ( final MamutViewTable w : tableWindows )
			windows.add( w.getFrame() );
		windows.add( tagSetDialog );
		windows.add( featureComputationDialog );

		try
		{
			InvokeOnEDT.invokeAndWait(
					() -> windows.stream()
							.filter( Objects::nonNull )
							.forEach( window -> window.dispatchEvent( new WindowEvent( window, WindowEvent.WINDOW_CLOSING ) ) ) );
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

	KeyPressedManager getKeyPressedManager()
	{
		return keyPressedManager;
	}

	TrackSchemeStyleManager getTrackSchemeStyleManager()
	{
		return trackSchemeStyleManager;
	}

	RenderSettingsManager getRenderSettingsManager()
	{
		return renderSettingsManager;
	}

	FeatureColorModeManager getFeatureColorModeManager()
	{
		return featureColorModeManager;
	}

	KeymapManager getKeymapManager()
	{
		return keymapManager;
	}

	// TODO: make package private
	public MamutAppModel getAppModel()
	{
		return appModel;
	}

	Actions getGlobalAppActions()
	{
		return globalAppActions;
	}

	MamutPlugins getPlugins()
	{
		return plugins;
	}

	public Context getContext()
	{
		return context;
	}

	public FeatureSpecsService getFeatureSpecsService()
	{
		return context.getService( FeatureSpecsService.class );
	}

	/**
	 * Exposes currently open BigDataViewer windows.
	 *
	 * @return a {@link List} of {@link MamutViewBdv}.
	 */
	public List< MamutViewBdv > getBdvWindows()
	{
		return bdvWindows;
	}

	/**
	 * Exposes the {@link ProjectManager} of this window manager, that handles
	 * project files.
	 *
	 * @return the project manager.
	 */
	public ProjectManager getProjectManager()
	{
		return projectManager;
	}

	private CommandDescriptions buildCommandDescriptions()
	{
		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
		context.inject( builder );
		builder.discoverProviders();
		return builder.build();
	}

	/**
	 * Classes that implement {@link BdvViewCreatedListener} get a notification
	 * when a new {@link MamutViewBdv} instance is created.
	 */
	public interface BdvViewCreatedListener
	{
		void bdvViewCreated( final MamutViewBdv view );
	}

	public synchronized boolean addBdvViewCreatedListner( final BdvViewCreatedListener listener )
	{
		if ( !listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	public synchronized boolean removeBdvViewCreatedListner( final BdvViewCreatedListener listener )
	{
		return listeners.remove( listener );
	}

	private void notifyListeners( final MamutViewBdv view )
	{
		for ( final BdvViewCreatedListener l : listeners )
			l.bdvViewCreated( view );
	}
}
