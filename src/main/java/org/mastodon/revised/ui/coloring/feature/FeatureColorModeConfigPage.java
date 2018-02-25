package org.mastodon.revised.ui.coloring.feature;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.ProgressListener;
import org.mastodon.util.Listeners;
import org.mastodon.util.Listeners.SynchronizedList;
import org.scijava.Context;

public class FeatureColorModeConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< FeatureColorMode > >
{


	public FeatureColorModeConfigPage(
			final String treePath,
			final FeatureColorModeManager featureColorModeManager, final FeatureModel featureModel, final Class< ? extends Vertex< ? > > vertexClass, final Class< ? extends Edge< ? > > edgeClass )
	{
		super( treePath,
				new StyleProfileManager<>( featureColorModeManager, new FeatureColorModeManager( false ) ),
				new FeatureColorModelEditPanel( featureColorModeManager.getDefaultStyle(), featureModel, vertexClass, edgeClass ) );
	}

	static class FeatureColorModelEditPanel implements FeatureColorMode.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< FeatureColorMode > >
	{

		private final FeatureColorMode editedMode;

		private final SynchronizedList< ModificationListener > modificationListeners;

		private final FeatureColorModePanel featureColorModeEditorPanel;

		private boolean trackModifications = true;

		public FeatureColorModelEditPanel( final FeatureColorMode initialMode, final FeatureModel featureModel, final Class< ? extends Vertex< ? > > vertexClass, final Class< ? extends Edge< ? > > edgeClass )
		{
			this.editedMode = initialMode.copy( "Edited" );
			this.featureColorModeEditorPanel = new FeatureColorModePanel( editedMode, featureModel, vertexClass, edgeClass );
			this.modificationListeners = new Listeners.SynchronizedList<>();
			editedMode.updateListeners().add( this );
		}


		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public void loadProfile( final StyleProfile< FeatureColorMode > profile )
		{
			trackModifications = false;
			editedMode.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< FeatureColorMode > profile )
		{
			trackModifications = false;
			editedMode.setName( profile.getStyle().getName() );
			trackModifications = true;
			profile.getStyle().set( editedMode );
		}

		@Override
		public JPanel getJPanel()
		{
			return featureColorModeEditorPanel;
		}

		@Override
		public void featureColorModeChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}
	}

	/*
	 * MAIN METHOD
	 */

	public static void main( final String[] args )
	{
		final Context context = new Context();
		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );
		final Model model = new Model();
		final FeatureModel featureModel = model.getFeatureModel();
		service.compute( model, featureModel, new HashSet<>( service.getFeatureComputers() ), pl );

		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new FeatureColorModeConfigPage( "Feature coloring", featureColorModeManager, featureModel, Spot.class, Link.class ) );

		final JDialog dialog = new JDialog( ( Frame ) null, "Settings" );
		dialog.getContentPane().add( settings, BorderLayout.CENTER );

		settings.onOk( () -> dialog.setVisible( false ) );
		settings.onCancel( () -> dialog.setVisible( false ) );

		dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		dialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				settings.cancel();
			}
		} );

		dialog.pack();
		dialog.setVisible( true );

		featureColorModeManager.saveStyles();
	}

	private static final ProgressListener pl = new ProgressListener()
	{

		@Override
		public void showStatus( final String string )
		{
			System.out.println( " - " + string );
		}

		@Override
		public void showProgress( final int current, final int total )
		{}

		@Override
		public void clearStatus()
		{}
	};
}
