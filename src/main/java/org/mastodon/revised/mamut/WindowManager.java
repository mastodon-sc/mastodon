package org.mastodon.revised.mamut;

import bdv.util.InvokeOnEDT;
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
import javax.swing.SwingUtilities;

import org.mastodon.app.ui.settings.SettingsPage;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.util.ToggleDialogAction;
import org.mastodon.views.context.ContextProvider;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.VisualEditorPanel;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.spimdata.SpimDataMinimal;
import bdv.viewer.RequestRepaint;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.generic.AbstractSpimData;

public class WindowManager
{
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

	private MamutAppModel appModel;

	@Deprecated
	public WindowManager(
			final String spimDataXmlFilename,
			final SpimDataMinimal spimData,
			final Model model,
			final InputTriggerConfig keyconf )
	{
		this.keyconf = keyconf;
		keyPressedManager = new KeyPressedManager();

		final ViewerOptions options = ViewerOptions.options()
				.inputTriggerConfig( keyconf )
				.shareKeyPressedEvents( keyPressedManager );
		final SharedBigDataViewerData sharedBdvData = new SharedBigDataViewerData( spimDataXmlFilename, spimData, options, () -> forEachBdvView( bdv -> bdv.requestRepaint() ) );

		setAppModel( new MamutAppModel( model, sharedBdvData, keyconf, keyPressedManager ) );
	}

	public WindowManager( final InputTriggerConfig keyconf )
	{
		this.keyconf = keyconf;
		keyPressedManager = new KeyPressedManager();
	}

	void setAppModel( MamutAppModel appModel )
	{
		closeAllWindows();

		this.appModel = appModel;

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
		final DefaultSettingsPage page = new DefaultSettingsPage( "keymap", keyconfEditor );
		page.onCancel( () -> keyconfEditor.configToModel() );
		page.onApply( () -> {
			keyconfEditor.modelToConfig();
			if ( appModel != null )
				appModel.getAppActions().updateKeyConfig( keyconf );
		} );
		settings.addPage( page );
		settings.addPage( page );
		settings.addPage( page );
		final ToggleDialogAction tooglePreferencesDialogAction = new ToggleDialogAction( "Preferences", settings );
		appModel.getAppActions().namedAction( tooglePreferencesDialogAction, "meta COMMA" );
	}

	// TODO FIX HACK
	static class DefaultSettingsPage implements SettingsPage
	{
		private final String treePath;

		private final JPanel panel;

		public DefaultSettingsPage( final String treePath, final JPanel panel )
		{
			this.treePath = treePath;
			this.panel = panel;
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

	public void forEachBdvView( Consumer< ? super MamutViewBdv > action )
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

	public void forEachTrackSchemwView( Consumer< ? super MamutViewTrackScheme > action )
	{
		tsWindows.forEach( action );
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
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
		catch ( InterruptedException e )
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

	public Model getModel()
	{
		return appModel.getModel();
	}

	public AbstractSpimData< ? > getSpimData()
	{
		return appModel.getSharedBdvData().getSpimData();
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
