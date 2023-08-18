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

import static org.mastodon.app.MastodonIcons.BDV_VIEW_ICON;
import static org.mastodon.app.MastodonIcons.FEATURES_ICON;
import static org.mastodon.app.MastodonIcons.TABLE_VIEW_ICON;
import static org.mastodon.app.MastodonIcons.TAGS_ICON;
import static org.mastodon.app.MastodonIcons.TRACKSCHEME_VIEW_ICON;

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

import javax.swing.JDialog;

import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.FeatureColorModeConfigPage;
import org.mastodon.mamut.feature.MamutFeatureProjectionsManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ui.TagSetDialog;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.CommandDescriptionsBuilder;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.ui.keymap.KeymapManager;
import org.mastodon.ui.keymap.KeymapSettingsPage;
import org.mastodon.util.RunnableActionPair;
import org.mastodon.util.ToggleDialogAction;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsConfigPage;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsManager;
import org.mastodon.views.context.ContextProvider;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.grapher.display.style.DataDisplayStyleManager;
import org.mastodon.views.grapher.display.style.DataDisplayStyleSettingsPage;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleSettingsPage;
import org.scijava.Context;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.util.InvokeOnEDT;
import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Main GUI class for the Mastodon Mamut application.
 * <p>
 * It controls the creation of new views, and maintain a list of currently
 * opened views. It has a {@link #getProjectManager()} instance that can be used
 * to open or create Mastodon projects. It has also the main app-model for the
 * session.
 * 
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 *
 */
public class WindowManager
{

	public static final String NEW_BDV_VIEW = "new bdv view";
	public static final String NEW_TRACKSCHEME_VIEW = "new trackscheme view";
	public static final String NEW_TABLE_VIEW = "new full table view";
	public static final String NEW_SELECTION_TABLE_VIEW = "new selection table view";
	public static final String NEW_GRAPHER_VIEW = "new grapher view";
	public static final String PREFERENCES_DIALOG = "Preferences";
	public static final String TAGSETS_DIALOG = "edit tag sets";
	public static final String COMPUTE_FEATURE_DIALOG = "compute features";
	public static final String OPEN_ONLINE_DOCUMENTATION = "open online documentation";

	static final String[] NEW_BDV_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_TRACKSCHEME_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_TABLE_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_SELECTION_TABLE_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_GRAPHER_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] PREFERENCES_DIALOG_KEYS = new String[] { "meta COMMA", "ctrl COMMA" };
	static final String[] TAGSETS_DIALOG_KEYS = new String[] { "not mapped" };
	static final String[] COMPUTE_FEATURE_DIALOG_KEYS = new String[] { "not mapped" };
	static final String[] OPEN_ONLINE_DOCUMENTATION_KEYS = new String[] { "not mapped" };

	static final String NEW_BRANCH_BDV_VIEW = "new branch bdv view";
	static final String NEW_BRANCH_TRACKSCHEME_VIEW = "new branch trackscheme view";
	static final String NEW_HIERARCHY_TRACKSCHEME_VIEW = "new hierarchy trackscheme view";

	static final String[] NEW_BRANCH_BDV_VIEW_KEYS = new String[] { "not mapped" };
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
	private final Map< Class< ? extends MamutView< ?, ?, ? > >, List< MamutView< ?, ?, ? > > > mamutViews = new HashMap<>();

	/** Stores the different lists of mamut branch views currently opened. */
	private final Map< Class< ? extends MamutBranchView< ?, ?, ? > >, List< MamutBranchView< ?, ?, ? > > > mamutBranchViews = new HashMap<>();

	private final TagSetDialog tagSetDialog;

	private final JDialog featureComputationDialog;

	private final Listeners.List< BdvViewCreatedListener > bdvViewCreatedListeners;

	private final PreferencesDialog settings;

	private final ProjectModel appModel;

	/**
	 * Creates a new, empty WindowManager instance using the specified context.
	 * 
	 * @param context
	 *            the context to use. Cannot be <code>null</code>.
	 * @param globalActions
	 */
	public WindowManager( final ProjectModel appModel )
	{
		this.appModel = appModel;
		final Context context = appModel.getContext();
		final Model model = appModel.getModel();

		/*
		 * Create and store window lists.
		 */

		@SuppressWarnings( "unchecked" )
		final Class< MamutView< ?, ?, ? > >[] knownMamutViewTypes = new Class[] {
				MamutViewBdv.class,
				MamutViewTrackScheme.class,
				MamutViewTable.class,
				MamutViewGrapher.class
		};
		for ( final Class< MamutView< ?, ?, ? > > klass : knownMamutViewTypes )
			mamutViews.put( klass, new ArrayList<>() );

		@SuppressWarnings( "unchecked" )
		final Class< MamutBranchView< ?, ?, ? > >[] knownMamutBranchViewTypes = new Class[] {
				MamutBranchViewBdv.class,
				MamutBranchViewTrackScheme.class,
		};
		for ( final Class< MamutBranchView< ?, ?, ? > > klass : knownMamutBranchViewTypes )
			mamutBranchViews.put( klass, new ArrayList<>() );

		/*
		 * Create and store managers.
		 */

		managers.put( TrackSchemeStyleManager.class, new TrackSchemeStyleManager() );
		managers.put( DataDisplayStyleManager.class, new DataDisplayStyleManager() );
		managers.put( RenderSettingsManager.class, new RenderSettingsManager() );
		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();
		managers.put( FeatureColorModeManager.class, featureColorModeManager );
		final MamutFeatureProjectionsManager featureProjectionsManager = new MamutFeatureProjectionsManager( context.getService( FeatureSpecsService.class ), featureColorModeManager );
		featureProjectionsManager.setModel( model, appModel.getSharedBdvData().getSources().size() );
		managers.put( MamutFeatureProjectionsManager.class, featureProjectionsManager );
		final KeymapManager keymapManager = appModel.getKeymapManager();
		final Keymap keymap = keymapManager.getForwardDefaultKeymap();
		// TODO: still needed?
		this.bdvViewCreatedListeners = new Listeners.SynchronizedList<>();

		final CommandDescriptions descriptions = buildCommandDescriptions();
		final Consumer< Keymap > augmentInputTriggerConfig =
				k -> descriptions.augmentInputTriggerConfig( k.getConfig() );
		keymapManager.getUserStyles().forEach( augmentInputTriggerConfig );
		keymapManager.getBuiltinStyles().forEach( augmentInputTriggerConfig );

		/*
		 * Actions to create views.
		 */

		final RunnableActionPair newBdvViewAction = new RunnableActionPair( NEW_BDV_VIEW, this::createBigDataViewer, this::createBranchBigDataViewer );
		final RunnableActionPair newTrackSchemeViewAction = new RunnableActionPair( NEW_TRACKSCHEME_VIEW, this::createTrackScheme, this::createBranchTrackScheme );
		final RunnableAction newTableViewAction = new RunnableAction( NEW_TABLE_VIEW, () -> createTable( false ) );
		final RunnableAction newSelectionTableViewAction = new RunnableAction( NEW_SELECTION_TABLE_VIEW, () -> createTable( true ) );
		final RunnableAction newGrapherViewAction = new RunnableAction( NEW_GRAPHER_VIEW, this::createGrapher );
		final RunnableAction editTagSetsAction = new RunnableAction( TAGSETS_DIALOG, this::editTagSets );
		final RunnableAction featureComputationAction = new RunnableAction( COMPUTE_FEATURE_DIALOG, this::computeFeatures );
		final RunnableAction newBranchBdvViewAction = new RunnableAction( NEW_BRANCH_BDV_VIEW, this::createBranchBigDataViewer );
		final RunnableAction newBranchTrackSchemeViewAction = new RunnableAction( NEW_BRANCH_TRACKSCHEME_VIEW, this::createBranchTrackScheme );
		final RunnableAction newHierarchyTrackSchemeViewAction = new RunnableAction( NEW_HIERARCHY_TRACKSCHEME_VIEW, this::createHierarchyTrackScheme );
		final RunnableAction openOnlineDocumentation = new RunnableAction( OPEN_ONLINE_DOCUMENTATION, this::openOnlineDocumentation );

		final Actions projectActions = appModel.getProjectActions();
		projectActions.namedAction( newBdvViewAction, NEW_BDV_VIEW_KEYS );
		projectActions.namedAction( newTrackSchemeViewAction, NEW_TRACKSCHEME_VIEW_KEYS );
		projectActions.namedAction( newTableViewAction, NEW_SELECTION_TABLE_VIEW_KEYS );
		projectActions.namedAction( newSelectionTableViewAction, NEW_SELECTION_TABLE_VIEW_KEYS );
		projectActions.namedAction( newGrapherViewAction, NEW_GRAPHER_VIEW_KEYS );
		projectActions.namedAction( editTagSetsAction, TAGSETS_DIALOG_KEYS );
		projectActions.namedAction( featureComputationAction, COMPUTE_FEATURE_DIALOG_KEYS );
		projectActions.namedAction( newBranchBdvViewAction, NEW_BRANCH_BDV_VIEW_KEYS );
		projectActions.namedAction( newBranchTrackSchemeViewAction, NEW_BRANCH_TRACKSCHEME_VIEW_KEYS );
		projectActions.namedAction( newHierarchyTrackSchemeViewAction, NEW_HIERARCHY_TRACKSCHEME_VIEW_KEYS );
		projectActions.namedAction( openOnlineDocumentation, OPEN_ONLINE_DOCUMENTATION_KEYS );

		this.settings = new PreferencesDialog( null, keymap, new String[] { KeyConfigContexts.MASTODON } );
		settings.addPage( new TrackSchemeStyleSettingsPage( "TrackScheme Styles", getManager( TrackSchemeStyleManager.class ) ) );
		settings.addPage( new RenderSettingsConfigPage( "BDV Render Settings", getManager( RenderSettingsManager.class ) ) );
		settings.addPage( new DataDisplayStyleSettingsPage( "Grapher styles", getManager( DataDisplayStyleManager.class ) ) );
		settings.addPage( new KeymapSettingsPage( "Keymap", keymapManager, descriptions ) );
		settings.addPage( new FeatureColorModeConfigPage( "Feature Color Modes", featureColorModeManager,
				featureProjectionsManager, "Spot", "Link" ) );
		settings.pack();

		final ToggleDialogAction tooglePreferencesDialogAction = new ToggleDialogAction( PREFERENCES_DIALOG, settings );
		projectActions.namedAction( tooglePreferencesDialogAction, PREFERENCES_DIALOG_KEYS );

		tagSetDialog = new TagSetDialog( null, model.getTagSetModel(), model, keymap,
				new String[] { KeyConfigContexts.MASTODON } );
		tagSetDialog.setIconImages( TAGS_ICON );
		featureComputationDialog = MamutFeatureComputation.getDialog( appModel, context );
		featureComputationDialog.setIconImages( FEATURES_ICON );
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
	 *            the view type, must extend {@link MamutView}.
	 * @param klass
	 *            the view class, must extend {@link MamutView}.
	 * @return the list of view of specified class, or <code>null</code>.
	 */
	private < T extends MamutView< ?, ?, ? > > List< T > getViewList( final Class< T > klass )
	{
		@SuppressWarnings( "unchecked" )
		final List< T > list = ( List< T > ) mamutViews.get( klass );
		return list;
	}

	/**
	 * Returns the list of opened mamut branch views of the specified type, or
	 * <code>null</code> if a view of this type is not registered.
	 * 
	 * @param <T>
	 *            the view type, must extend {@link MamutBranchView}.
	 * @param klass
	 *            the view class, must extend {@link MamutBranchView}.
	 * @return the list of view of specified class, or <code>null</code>.
	 */
	private < T extends MamutBranchView< ?, ?, ? > > List< T > getBranchViewList( final Class< T > klass )
	{
		@SuppressWarnings( "unchecked" )
		final List< T > list = ( List< T > ) mamutBranchViews.get( klass );
		return list;
	}

	private synchronized void addBdvWindow( final MamutViewBdv w )
	{
		getViewList( MamutViewBdv.class ).add( w );
		contextProviders.add( w.getContextProvider() );
		forEachView( v -> {
			if ( v instanceof HasContextChooser )
			{
				@SuppressWarnings( "unchecked" )
				final HasContextChooser< Spot > cc = ( HasContextChooser< Spot > ) v;
				cc.getContextChooser().updateContextProviders( contextProviders );
			}
		} );

		w.onClose( () -> {
			getViewList( MamutViewBdv.class ).remove( w );
			contextProviders.remove( w.getContextProvider() );
			forEachView( v -> {
				if ( v instanceof HasContextChooser )
				{
					@SuppressWarnings( "unchecked" )
					final HasContextChooser< Spot > cc = ( HasContextChooser< Spot > ) v;
					cc.getContextChooser().updateContextProviders( contextProviders );
				}
			} );
		} );
	}

	private synchronized void addBBdvWindow( final MamutBranchViewBdv w )
	{
		getBranchViewList( MamutBranchViewBdv.class ).add( w );
		w.onClose( () -> getBranchViewList( MamutBranchViewBdv.class ).remove( w ) );
	}

	private synchronized void addTsWindow( final MamutViewTrackScheme w )
	{
		getViewList( MamutViewTrackScheme.class ).add( w );
		w.getContextChooser().updateContextProviders( contextProviders );
		w.onClose( () -> {
			getViewList( MamutViewTrackScheme.class ).remove( w );
			w.getContextChooser().updateContextProviders( new ArrayList<>() );
		} );
	}

	private synchronized void addBTsWindow( final MamutBranchViewTrackScheme w )
	{
		getBranchViewList( MamutBranchViewTrackScheme.class ).add( w );
		w.onClose( () -> getBranchViewList( MamutBranchViewTrackScheme.class ).remove( w ) );
	}

	private synchronized void addTableWindow( final MamutViewTable table )
	{
		getViewList( MamutViewTable.class ).add( table );
		table.getContextChooser().updateContextProviders( contextProviders );
		table.onClose( () -> {
			getViewList( MamutViewTable.class ).remove( table );
			table.getContextChooser().updateContextProviders( new ArrayList<>() );
		} );
	}

	private synchronized void addGrapherWindow( final MamutViewGrapher grapher )
	{
		getViewList( MamutViewGrapher.class ).add( grapher );
		grapher.getContextChooser().updateContextProviders( contextProviders );
		grapher.onClose( () -> {
			getViewList( MamutViewGrapher.class ).remove( grapher );
			grapher.getContextChooser().updateContextProviders( new ArrayList<>() );
		} );
	}

	/**
	 * Executes the specified action for all the currently opened mamut views of
	 * the specified class.
	 * 
	 * @param action
	 *            the action to execute.
	 * @param klass
	 *            the view class.
	 */
	public < T extends MamutView< ?, ?, ? > > void forEachView( final Class< T > klass, final Consumer< T > action )
	{
		Optional.of( getViewList( klass ) )
				.orElse( Collections.emptyList() )
				.forEach( action );
	}

	/**
	 * Executes the specified action for all the currently opened branch views
	 * of the specified class.
	 * 
	 * @param action
	 *            the action to execute.
	 * @param klass
	 *            the view class.
	 */
	public < T extends MamutBranchView< ?, ?, ? > > void forEachBranchView( final Class< T > klass, final Consumer< T > action )
	{
		Optional.of( getBranchViewList( klass ) )
				.orElse( Collections.emptyList() )
				.forEach( action );
	}

	/**
	 * Executes the specified action for all the currently opened mamut views.
	 * 
	 * @param action
	 *            the action to execute.
	 */
	public void forEachView( final Consumer< ? super MamutView< ?, ?, ? > > action )
	{
		mamutViews.forEach( ( k, l ) -> l.forEach( action ) );
	}

	/**
	 * Executes the specified action for all the currently opened mamut branch
	 * views.
	 * 
	 * @param action
	 *            the action to execute.
	 */
	public void forEachBranchView( final Consumer< ? super MamutBranchView< ?, ?, ? > > action )
	{
		mamutBranchViews.forEach( ( k, l ) -> l.forEach( action ) );
	}

	/**
	 * Creates and displays a new BDV view, with default display settings.
	 */
	public MamutViewBdv createBigDataViewer()
	{
		return createBigDataViewer( new HashMap<>() );
	}

	/**
	 * Creates and displays a new BDV view, using a map to specify the display
	 * settings.
	 * <p>
	 * The display settings are specified as a map of strings to objects. The
	 * accepted key and value types are:
	 * <ul>
	 * <li><code>'FramePosition'</code> &rarr; an <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 * <li><code>'LockGroupId'</code> &rarr; an integer that specifies the lock
	 * group id.
	 * <li><code>'SettingsPanelVisible'</code> &rarr; a boolean that specifies
	 * whether the settings panel is visible on this view.
	 * <li><code>'BdvState'</code> &rarr; a XML Element that specifies the BDV
	 * window state. See {@link ViewerPanel#stateToXml()} and
	 * {@link ViewerPanel#stateFromXml(org.jdom2.Element)} for more information.
	 * <li><code>'BdvTransform'</code> &rarr; an {@link AffineTransform3D} that
	 * specifies the view point.
	 * <li><code>'NoColoring'</code> &rarr; a boolean; if <code>true</code>, the
	 * feature or tag coloring will be ignored.
	 * <li><code>'TagSet'</code> &rarr; a string specifying the name of the
	 * tag-set to use for coloring. If not <code>null</code>, the coloring will
	 * be done using the tag-set.
	 * <li><code>'FeatureColorMode'</code> &rarr; a String specifying the name
	 * of the feature color mode to use for coloring. If not <code>null</code>,
	 * the coloring will be done using the feature color mode.
	 * <li><code>'ColorbarVisible'</code> &rarr; a boolean specifying whether
	 * the colorbar is visible for tag-set and feature-based coloring.
	 * <li><code>'ColorbarPosition'</code> &rarr; a {@link Position} specifying
	 * the position of the colorbar.
	 * </ul>
	 * 
	 * @param guiState
	 *            the map of settings.
	 */
	public MamutViewBdv createBigDataViewer( final Map< String, Object > guiState )
	{
		final MamutViewBdv view = new MamutViewBdv( appModel, guiState );
		view.getFrame().setIconImages( BDV_VIEW_ICON );
		addBdvWindow( view );
		bdvViewCreatedListeners.list.forEach( l -> l.bdvViewCreated( view ) );
		return view;
	}

	/**
	 * Creates and displays a new TrackScheme view, with default display
	 * settings.
	 */
	public MamutViewTrackScheme createTrackScheme()
	{
		return createTrackScheme( new HashMap<>() );
	}

	/**
	 * Creates and displays a new BDV view, using a map to specify the display
	 * settings.
	 * <p>
	 * The display settings are specified as a map of strings to objects. The
	 * accepted key and value types are:
	 * <ul>
	 * <li><code>'FramePosition'</code> &rarr; an <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 * <li><code>'LockGroupId'</code> &rarr; an integer that specifies the lock
	 * group id.
	 * <li><code>'SettingsPanelVisible'</code> &rarr; a boolean that specifies
	 * whether the settings panel is visible on this view.
	 * <li><code>'TrackSchemeTransform'</code> &rarr; a {@link ScreenTransform}
	 * that defines the starting view zone in TrackScheme.
	 * <li><code>'NoColoring'</code> &rarr; a boolean; if <code>true</code>, the
	 * feature or tag coloring will be ignored.
	 * <li><code>'TagSet'</code> &rarr; a string specifying the name of the
	 * tag-set to use for coloring. If not <code>null</code>, the coloring will
	 * be done using the tag-set.
	 * <li><code>'FeatureColorMode'</code> &rarr; a @link String specifying the
	 * name of the feature color mode to use for coloring. If not
	 * <code>null</code>, the coloring will be done using the feature color
	 * mode.
	 * <li><code>'ColorbarVisible'</code> &rarr; a boolean specifying whether
	 * the colorbar is visible for tag-set and feature-based coloring.
	 * <li><code>'ColorbarPosition'</code> &rarr; a {@link Position} specifying
	 * the position of the colorbar.
	 * </ul>
	 * 
	 * @param guiState
	 *            the map of settings.
	 */
	public MamutViewTrackScheme createTrackScheme( final Map< String, Object > guiState )
	{
		final MamutViewTrackScheme view = new MamutViewTrackScheme( appModel, guiState );
		view.getFrame().setIconImages( TRACKSCHEME_VIEW_ICON );
		addTsWindow( view );
		return view;
	}

	/**
	 * Creates and displays a new Table or a Selection Table view, using a map
	 * to specify the display settings.
	 * <p>
	 * The display settings are specified as a map of strings to objects. The
	 * accepted key and value types are:
	 * <ul>
	 * <li><code>'TableSelectionOnly'</code> &rarr; a boolean specifying whether
	 * the table to create will be a selection table of a full table. If
	 * <code>true</code>, the table will only display the current content of the
	 * selection, and will listen to its changes. If <code>false</code>, the
	 * table will display the full graph content, listen to its changes, and
	 * will be able to edit the selection.
	 * <li><code>'FramePosition'</code> &rarr; an <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 * <li><code>'LockGroupId'</code> &rarr; an integer that specifies the lock
	 * group id.
	 * <li><code>'SettingsPanelVisible'</code> &rarr; a boolean that specifies
	 * whether the settings panel is visible on this view.
	 * <li><code>'NoColoring'</code> &rarr; a boolean; if <code>true</code>, the
	 * feature or tag coloring will be ignored.
	 * <li><code>'TagSet'</code> &rarr; a string specifying the name of the
	 * tag-set to use for coloring. If not <code>null</code>, the coloring will
	 * be done using the tag-set.
	 * <li><code>'FeatureColorMode'</code> &rarr; a @link String specifying the
	 * name of the feature color mode to use for coloring. If not
	 * <code>null</code>, the coloring will be done using the feature color
	 * mode.
	 * <li><code>'ColorbarVisible'</code> &rarr; a boolean specifying whether
	 * the colorbar is visible for tag-set and feature-based coloring.
	 * <li><code>'ColorbarPosition'</code> &rarr; a {@link Position} specifying
	 * the position of the colorbar.
	 * </ul>
	 * 
	 * @param guiState
	 *            the map of settings.
	 */
	public MamutViewTable createTable( final Map< String, Object > guiState )
	{
		final MamutViewTable view = new MamutViewTable( appModel, guiState );
		view.getFrame().setIconImages( TABLE_VIEW_ICON );
		addTableWindow( view );
		return view;
	}

	/**
	 * Creates and display a new Table or Selection Table view with default
	 * settings.
	 *
	 * @param selectionOnly
	 *            if <code>true</code>, the table will only display the current
	 *            content of the selection, and will listen to its changes. If
	 *            <code>false</code>, the table will display the full graph
	 *            content, listen to its changes, and will be able to edit the
	 *            selection.
	 * @return a new table view.
	 */
	public MamutViewTable createTable( final boolean selectionOnly )
	{
		final Map< String, Object > guiState = Collections.singletonMap(
				MamutViewTable.TABLE_SELECTION_ONLY, Boolean.valueOf( selectionOnly ) );
		return createTable( guiState );
	}

	/**
	 * Creates and displays a new Grapher view, with default display settings.
	 */
	public MamutViewGrapher createGrapher()
	{
		return createGrapher( new HashMap<>() );
	}

	/**
	 * Creates and displays a new Grapher view, using a map to specify the
	 * display settings.
	 * <p>
	 * The display settings are specified as a map of strings to objects. The
	 * accepted key and value types are:
	 * <ul>
	 * <li><code>'FramePosition'</code> &rarr; an <code>int[]</code> array of 4
	 * elements: x, y, width and height.
	 * <li><code>'LockGroupId'</code> &rarr; an integer that specifies the lock
	 * group id.
	 * <li><code>'SettingsPanelVisible'</code> &rarr; a boolean that specifies
	 * whether the settings panel is visible on this view.
	 * <li><code>'NoColoring'</code> &rarr; a boolean; if <code>true</code>, the
	 * feature or tag coloring will be ignored.
	 * <li><code>'TagSet'</code> &rarr; a string specifying the name of the
	 * tag-set to use for coloring. If not <code>null</code>, the coloring will
	 * be done using the tag-set.
	 * <li><code>'FeatureColorMode'</code> &rarr; a @link String specifying the
	 * name of the feature color mode to use for coloring. If not
	 * <code>null</code>, the coloring will be done using the feature color
	 * mode.
	 * <li><code>'ColorbarVisible'</code> &rarr; a boolean specifying whether
	 * the colorbar is visible for tag-set and feature-based coloring.
	 * <li><code>'ColorbarPosition'</code> &rarr; a {@link Position} specifying
	 * the position of the colorbar.
	 * <li><code>'GrapherTransform'</code> &rarr; a
	 * {@link org.mastodon.views.grapher.datagraph.ScreenTransform} specifying
	 * the region to initially zoom on the XY plot.
	 * 
	 * </ul>
	 * 
	 * @param guiState
	 *            the map of settings.
	 */
	public MamutViewGrapher createGrapher( final Map< String, Object > guiState )
	{
		final MamutViewGrapher view = new MamutViewGrapher( appModel, guiState );
		view.getFrame().setIconImages( FEATURES_ICON );
		addGrapherWindow( view );
		return view;
	}

	/**
	 * Creates and displays a new Branch-BDV view, with default display
	 * settings. The branch version of this view displays the branch graph.
	 */
	public MamutBranchViewBdv createBranchBigDataViewer()
	{
		return createBranchBigDataViewer( new HashMap<>() );
	}

	/**
	 * Creates and displays a new Branch-BDV view, using a map to specify the
	 * display settings.
	 * 
	 * @see #createBigDataViewer(Map)
	 * @param guiState
	 *            the settings map.
	 */
	public MamutBranchViewBdv createBranchBigDataViewer( final Map< String, Object > guiState )
	{
		final MamutBranchViewBdv view = new MamutBranchViewBdv( appModel, guiState );
		view.getFrame().setIconImages( BDV_VIEW_ICON );
		addBBdvWindow( view );
		return view;
	}

	/**
	 * Creates and displays a new Branch-TrackScheme view, with default display
	 * settings. The branch version of this view displays the branch graph.
	 */
	public MamutBranchViewTrackScheme createBranchTrackScheme()
	{
		return createBranchTrackScheme( new HashMap<>() );
	}

	/**
	 * Creates and displays a new Branch-TrackScheme view, using a map to
	 * specify the display settings.
	 * 
	 * @see #createTrackScheme(Map)
	 * @param guiState
	 *            the settings map.
	 */
	public MamutBranchViewTrackScheme createBranchTrackScheme( final Map< String, Object > guiState )
	{
		final MamutBranchViewTrackScheme view = new MamutBranchViewTrackScheme( appModel, guiState );
		view.getFrame().setIconImages( TRACKSCHEME_VIEW_ICON );
		addBTsWindow( view );
		return view;
	}

	/**
	 * Creates and displays a new Hierarchy-TrackScheme view, with default
	 * display settings.
	 */
	public MamutBranchViewTrackScheme createHierarchyTrackScheme()
	{
		return createHierarchyTrackScheme( new HashMap<>() );
	}

	/**
	 * Creates and displays a new Hierarchy-TrackScheme view, using a map to
	 * specify the display settings.
	 * 
	 * @see #createTrackScheme(Map)
	 * @param guiState
	 *            the settings map.
	 */
	public MamutBranchViewTrackScheme createHierarchyTrackScheme( final Map< String, Object > guiState )
	{
		final MamutBranchViewTrackSchemeHierarchy view =
				new MamutBranchViewTrackSchemeHierarchy( appModel, guiState );
		view.getFrame().setIconImages( TRACKSCHEME_VIEW_ICON );
		addBTsWindow( view );
		return view;
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
		forEachView( v -> windows.add( v.getFrame() ) );
		forEachBranchView( v -> windows.add( v.getFrame() ) );
		windows.add( tagSetDialog );
		windows.add( featureComputationDialog );

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
	 * This method is called when the MainWindow is closed.
	 */
	public void dispose()
	{
		settings.dispose();
	}

	public PreferencesDialog getPreferencesDialog()
	{
		return settings;
	}

	private CommandDescriptions buildCommandDescriptions()
	{
		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
		appModel.getContext().inject( builder );
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

	public Listeners< BdvViewCreatedListener > bdvViewCreatedListeners()
	{
		return bdvViewCreatedListeners;
	}

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
			descriptions.add( COMPUTE_FEATURE_DIALOG, COMPUTE_FEATURE_DIALOG_KEYS,
					"Show the feature computation dialog." );
			descriptions.add( NEW_BRANCH_BDV_VIEW, NEW_BRANCH_BDV_VIEW_KEYS, "Open a new branch BigDataViewer view." );
			descriptions.add( NEW_BRANCH_TRACKSCHEME_VIEW, NEW_BRANCH_TRACKSCHEME_VIEW_KEYS,
					"Open a new branch TrackScheme view." );
			descriptions.add( NEW_HIERARCHY_TRACKSCHEME_VIEW, NEW_HIERARCHY_TRACKSCHEME_VIEW_KEYS,
					"Open a new hierarchy TrackScheme view." );
			descriptions.add( OPEN_ONLINE_DOCUMENTATION, OPEN_ONLINE_DOCUMENTATION_KEYS,
					"Open the online documentation in a web browser." );
			descriptions.add( NEW_GRAPHER_VIEW, NEW_GRAPHER_VIEW_KEYS, "Open a new Grapher view." );
		}
	}
}
