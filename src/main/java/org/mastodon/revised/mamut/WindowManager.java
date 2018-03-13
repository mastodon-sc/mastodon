package org.mastodon.revised.mamut;

import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFrame;

import org.mastodon.revised.bdv.overlay.ui.RenderSettingsConfigPage;
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsManager;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.ui.TagSetDialog;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleSettingsPage;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.ui.keymap.CommandDescriptionProvider;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.mastodon.revised.ui.keymap.CommandDescriptionsBuilder;
import org.mastodon.revised.ui.keymap.Keymap;
import org.mastodon.revised.ui.keymap.KeymapManager;
import org.mastodon.revised.ui.keymap.KeymapSettingsPage;
import org.mastodon.revised.util.ToggleDialogAction;
import org.mastodon.views.context.ContextProvider;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.util.InvokeOnEDT;

public class WindowManager
{
	public static final String NEW_BDV_VIEW = "new bdv view";
	public static final String NEW_TRACKSCHEME_VIEW = "new trackscheme view";
	public static final String PREFERENCES_DIALOG = "Preferences";
	public static final String TAGSETS_DIALOG = "edit tag sets";

	static final String[] NEW_BDV_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_TRACKSCHEME_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] PREFERENCES_DIALOG_KEYS = new String[] { "meta COMMA", "control COMMA" };
	static final String[] TAGSETS_DIALOG_KEYS = new String[] { "not mapped" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( NEW_BDV_VIEW, NEW_BDV_VIEW_KEYS, "Open new BigDataViewer view." );
			descriptions.add( NEW_TRACKSCHEME_VIEW, NEW_TRACKSCHEME_VIEW_KEYS, "Open new TrackScheme view." );
			descriptions.add( PREFERENCES_DIALOG, PREFERENCES_DIALOG_KEYS, "Edit Mastodon preferences." );
			descriptions.add( TAGSETS_DIALOG, TAGSETS_DIALOG_KEYS, "Edit tag definitions." );
		}
	}

	private final Context context;

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

	private final KeyPressedManager keyPressedManager;

	private final TrackSchemeStyleManager trackSchemeStyleManager;

	private final RenderSettingsManager renderSettingsManager;

	private final KeymapManager keymapManager;

	private final Actions globalAppActions;

	private final AbstractNamedAction newBdvViewAction;

	private final AbstractNamedAction newTrackSchemeViewAction;

	private final AbstractNamedAction editTagSetsAction;

	private MamutAppModel appModel;

	private TagSetDialog tagSetDialog;

	final ProjectManager projectManager;

	public WindowManager( final Context context )
	{
		this.context = context;

		keyPressedManager = new KeyPressedManager();
		trackSchemeStyleManager = new TrackSchemeStyleManager();
		renderSettingsManager = new RenderSettingsManager();
		keymapManager = new KeymapManager();

		final Keymap keymap = keymapManager.getForwardDefaultKeymap();

		// TODO: naming, this should be named appActions and the AppModel.appActions should become modelActions?
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
		editTagSetsAction = new RunnableAction( TAGSETS_DIALOG, this::editTagSets );

		globalAppActions.namedAction( newBdvViewAction, NEW_BDV_VIEW_KEYS );
		globalAppActions.namedAction( newTrackSchemeViewAction, NEW_TRACKSCHEME_VIEW_KEYS );
		globalAppActions.namedAction( editTagSetsAction, TAGSETS_DIALOG_KEYS );

		final PreferencesDialog settings = new PreferencesDialog( null, keymap, new String[] { KeyConfigContexts.MASTODON } );
		settings.addPage( new TrackSchemeStyleSettingsPage( "TrackScheme Styles", trackSchemeStyleManager ) );
		settings.addPage( new RenderSettingsConfigPage( "BDV Render Settings", renderSettingsManager ) );
		settings.addPage( new KeymapSettingsPage( "Keymap", keymapManager, buildCommandDescriptions()  ) );
		final ToggleDialogAction tooglePreferencesDialogAction = new ToggleDialogAction( PREFERENCES_DIALOG, settings );
		globalAppActions.namedAction( tooglePreferencesDialogAction, PREFERENCES_DIALOG_KEYS );

		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		newBdvViewAction.setEnabled( appModel != null );
		newTrackSchemeViewAction.setEnabled( appModel != null );
		editTagSetsAction.setEnabled( appModel != null );
	}

	void setAppModel( final MamutAppModel appModel )
	{
		closeAllWindows();

		this.appModel = appModel;
		if ( appModel == null )
		{
			tagSetDialog.dispose();
			tagSetDialog = null;
			updateEnabledActions();
			return;
		}

		final Model model = appModel.getModel();
		UndoActions.install( appModel.getAppActions(), model );
		SelectionActions.install( appModel.getAppActions(), model.getGraph(), model.getGraph().getLock(), model.getGraph(), appModel.getSelectionModel(), model );

		final Keymap keymap = keymapManager.getForwardDefaultKeymap();
		tagSetDialog = new TagSetDialog( null, model.getTagSetModel(), model, keymap, new String[] { KeyConfigContexts.MASTODON } );
		updateEnabledActions();
	}

	private synchronized void addBdvWindow( final MamutViewBdv w )
	{
		bdvWindows.add( w );
		contextProviders.add( w.getContextProvider() );
		for ( final MamutViewTrackScheme tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
		w.onClose( () -> {
			bdvWindows.remove( w );
			contextProviders.remove( w.getContextProvider() );
			for ( final MamutViewTrackScheme tsw : tsWindows )
				tsw.getContextChooser().updateContextProviders( contextProviders );
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

	public void forEachTrackSchemeView( final Consumer< ? super MamutViewTrackScheme > action )
	{
		tsWindows.forEach( action );
	}

	public void forEachView( final Consumer< ? super MamutView< ?, ?, ? > > action )
	{
		forEachBdvView( action );
		forEachTrackSchemeView( action );
	}

	public MamutViewBdv createBigDataViewer()
	{
		if ( appModel != null )
		{
			final MamutViewBdv view = new MamutViewBdv( appModel );
			addBdvWindow( view );
			return view;
		}
		return null;
	}

	public MamutViewTrackScheme createTrackScheme()
	{
		if ( appModel != null )
		{
			final MamutViewTrackScheme view = new MamutViewTrackScheme( appModel );
			addTsWindow( view );
			return view;
		}
		return null;
	}

	public void editTagSets()
	{
		if ( appModel != null )
		{
			tagSetDialog.setVisible( true );
		}
	}

	public void closeAllWindows()
	{
		final ArrayList< JFrame > frames = new ArrayList<>();
		for ( final MamutViewBdv w : bdvWindows )
			frames.add( w.getFrame() );
		for ( final MamutViewTrackScheme w : tsWindows )
			frames.add( w.getFrame() );
		try
		{
			InvokeOnEDT.invokeAndWait( new Runnable()
			{
				@Override
				public void run()
				{
					for ( final JFrame f : frames )
						f.dispatchEvent( new WindowEvent( f, WindowEvent.WINDOW_CLOSING ) );
				}
			} );
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

	public KeymapManager getKeymapManager()
	{
		return keymapManager;
	}

	public MamutAppModel getAppModel()
	{
		return appModel;
	}

	Actions getGlobalAppActions()
	{
		return globalAppActions;
	}

	public Context getContext()
	{
		return context;
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
}
