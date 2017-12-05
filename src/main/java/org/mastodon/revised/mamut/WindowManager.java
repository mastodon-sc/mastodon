package org.mastodon.revised.mamut;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SettingsPage;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleSettingsPage;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.util.ToggleDialogAction;
import org.mastodon.util.Listeners;
import org.mastodon.views.context.ContextProvider;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.VisualEditorPanel;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.util.InvokeOnEDT;

public class WindowManager
{
	public static final String NEW_BDV_VIEW = "new bdv view";
	public static final String NEW_TRACKSCHEME_VIEW = "new trackscheme view";

	static final String[] NEW_BDV_VIEW_KEYS = new String[] { "not mapped" };
	static final String[] NEW_TRACKSCHEME_VIEW_KEYS = new String[] { "not mapped" };

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

	private final InputTriggerConfig keyconf;

	private final KeyPressedManager keyPressedManager;

	private final TrackSchemeStyleManager trackSchemeStyleManager;

	private final Actions globalAppActions;

	private final AbstractNamedAction newBdvViewAction;

	private final AbstractNamedAction newTrackSchemeViewAction;

	private MamutAppModel appModel;

	final ProjectManager projectManager;

	public WindowManager( final InputTriggerConfig keyconf )
	{
		this.keyconf = keyconf;
		keyPressedManager = new KeyPressedManager();
		trackSchemeStyleManager = new TrackSchemeStyleManager();

		// TODO: naming, this should be named appActions and the AppModel.appActions should become modelActions?
		globalAppActions = new Actions( keyconf, "mastodon" );

		projectManager = new ProjectManager( this );
		projectManager.install( globalAppActions );

		newBdvViewAction = new RunnableAction( NEW_BDV_VIEW, this::createBigDataViewer );
		newTrackSchemeViewAction = new RunnableAction( NEW_TRACKSCHEME_VIEW, this::createTrackScheme );

		globalAppActions.namedAction( newBdvViewAction, NEW_BDV_VIEW_KEYS );
		globalAppActions.namedAction( newTrackSchemeViewAction, NEW_TRACKSCHEME_VIEW_KEYS );

		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		newBdvViewAction.setEnabled( appModel != null );
		newTrackSchemeViewAction.setEnabled( appModel != null );
	}

	void setAppModel( final MamutAppModel appModel )
	{
		closeAllWindows();

		this.appModel = appModel;
		if ( appModel == null )
		{
			updateEnabledActions();
			return;
		}

		final Model model = appModel.getModel();
		UndoActions.install( appModel.getAppActions(), model );
		SelectionActions.install( appModel.getAppActions(), model.getGraph(), model.getGraph().getLock(), model.getGraph(), appModel.getSelectionModel(), model );

		// TODO FIX HACK: We are creating a new dialog everytime so that the keyconf (which is filled from programmatically set defaults is)
		final PreferencesDialog settings = new PreferencesDialog( null );
		final InputTriggerDescriptionsBuilder builder = new InputTriggerDescriptionsBuilder( keyconf );
		final Map< String, String > commandDescriptions = new HashMap<>();
		builder.getBehaviourNames().forEach( name -> commandDescriptions.put( name, null ) );
		final Set< String > contexts = builder.getContexts();

		final VisualEditorPanel keyconfEditor = new VisualEditorPanel( keyconf, commandDescriptions, contexts );
		keyconfEditor.setButtonPanelVisible( false );
		final DefaultSettingsPage page = new DefaultSettingsPage( "Keymap", keyconfEditor );
		page.onCancel( () -> keyconfEditor.configToModel() );
		page.onApply( () -> {
			keyconfEditor.modelToConfig();
			if ( appModel != null )
				appModel.getAppActions().updateKeyConfig( keyconf );
				forEachView( v -> v.updateKeyConfig() );
		} );
		settings.addPage( page );
		settings.addPage( new TrackSchemeStyleSettingsPage( "TrackScheme Styles", trackSchemeStyleManager ) );
		final ToggleDialogAction tooglePreferencesDialogAction = new ToggleDialogAction( "Preferences", settings );
		appModel.getAppActions().namedAction( tooglePreferencesDialogAction, "meta COMMA" );

		updateEnabledActions();
	}

	// TODO FIX HACK
	public static class DefaultSettingsPage implements SettingsPage
	{
		private final String treePath;

		private final JPanel panel;

		private final Listeners.List< ModificationListener > modificationListeners;

		public DefaultSettingsPage( final String treePath, final JPanel panel )
		{
			this.treePath = treePath;
			this.panel = panel;
			modificationListeners = new Listeners.SynchronizedList<>();
		}

		@Override
		public String getTreePath()
		{
			return treePath;
		}

		@Override
		public JPanel getJPanel()
		{
			return panel;
		}

		public void notifyModified()
		{
			modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		protected final ArrayList< Runnable > runOnApply = new ArrayList<>();

		public synchronized void onApply( final Runnable runnable )
		{
			runOnApply.add( runnable );
		}

		protected final ArrayList< Runnable > runOnCancel = new ArrayList<>();

		public synchronized void onCancel( final Runnable runnable )
		{
			runOnCancel.add( runnable );
		}

		@Override
		public void cancel()
		{
			runOnCancel.forEach( Runnable::run );
		}

		@Override
		public void apply()
		{
			runOnApply.forEach( Runnable::run );
		}
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

	public void forEachView(  final Consumer< ? super MamutView > action  )
	{
		forEachBdvView( action );
		forEachTrackSchemeView( action );
	}

	public void createBigDataViewer()
	{
		if ( appModel != null )
		{
			final MamutViewBdv view = new MamutViewBdv( appModel );
			addBdvWindow( view );
		}
	}

	public void createTrackScheme()
	{
		if ( appModel != null )
		{
			final MamutViewTrackScheme view = new MamutViewTrackScheme( appModel );
			addTsWindow( view );
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

	InputTriggerConfig getKeyConfig()
	{
		return keyconf;
	}

	KeyPressedManager getKeyPressedManager()
	{
		return keyPressedManager;
	}

	TrackSchemeStyleManager getTrackSchemeStyleManager()
	{
		return trackSchemeStyleManager;
	}

	MamutAppModel getAppModel()
	{
		return appModel;
	}

	Actions getGlobalAppActions()
	{
		return globalAppActions;
	}

	// TODO: move somewhere else. make bdvWindows, tsWindows accessible.
	public static class DumpInputConfig
	{
		public static boolean mkdirs( final String fileName )
		{
			final File dir = new File( fileName ).getParentFile();
			return dir == null ? false : dir.mkdirs();
		}

		public static void writeToYaml( final String fileName, final WindowManager wm ) throws IOException
		{
			mkdirs( fileName );
			final List< InputTriggerDescription > descriptions = new InputTriggerDescriptionsBuilder( wm.appModel.getKeyConfig() ).getDescriptions();
			YamlConfigIO.write( descriptions, fileName );
		}
	}
}
