package org.mastodon.revised.ui.coloring.feature;

import java.util.Collection;
import java.util.Map;

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
			final FeatureColorModeManager featureColorModeManager,
			final FeatureModel featureModel,
			final FeatureRangeCalculator< ? extends Vertex< ? >, ? extends Edge< ? > > rangeCalculator,
			final Class< ? extends Vertex< ? > > vertexClass,
			final Map< String, Collection<String> > vertexFeatureKeys,
			final Class< ? extends Edge< ? > > edgeClass,
			final Map< String, Collection<String> > edgeFeatureKeys )
	{
		super( treePath,
				new StyleProfileManager<>( featureColorModeManager, new FeatureColorModeManager( false ) ),
				new FeatureColorModelEditPanel(
						featureColorModeManager.getDefaultStyle(),
						featureModel,
						rangeCalculator,
						vertexClass,
						vertexFeatureKeys,
						edgeClass,
						edgeFeatureKeys ) );
	}

	static class FeatureColorModelEditPanel implements FeatureColorMode.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< FeatureColorMode > >
	{

		private final FeatureColorMode editedMode;

		private final SynchronizedList< ModificationListener > modificationListeners;

		private final FeatureColorModeEditorPanel featureColorModeEditorPanel;

		private boolean trackModifications = true;

		public FeatureColorModelEditPanel(
				final FeatureColorMode initialMode,
				final FeatureModel featureModel,
				final FeatureRangeCalculator< ? extends Vertex< ? >, ? extends Edge< ? > > rangeCalculator,
				final Class< ? extends Vertex< ? > > vertexClass,
				final Map< String, Collection<String> > vertexFeatureKeys,
				final Class< ? extends Edge< ? > > edgeClass,
				final Map< String, Collection<String> > edgeFeatureKeys )
		{
			this.editedMode = initialMode.copy( "Edited" );
			this.featureColorModeEditorPanel = new FeatureColorModeEditorPanel(
					editedMode,
					featureModel,
					rangeCalculator,
					vertexClass,
					vertexFeatureKeys,
					edgeClass,
					edgeFeatureKeys );
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
