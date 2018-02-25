package org.mastodon.revised.ui.coloring.feature;

import javax.swing.JPanel;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.util.Listeners;
import org.mastodon.util.Listeners.SynchronizedList;

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
}
