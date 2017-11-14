package org.mastodon.revised.trackscheme.display.style;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JDialog;

import javax.swing.JPanel;
import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage.Profile;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage.ProfileManager;
import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.model.DefaultFocusModel;
import org.mastodon.model.DefaultHighlightModel;
import org.mastodon.model.DefaultNavigationHandler;
import org.mastodon.model.DefaultTimepointModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeEdgeBimap;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.TrackSchemeVertexBimap;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.TrackSchemePanel;
import org.mastodon.revised.trackscheme.display.style.dummygraph.DummyEdge;
import org.mastodon.revised.trackscheme.display.style.dummygraph.DummyGraph;
import org.mastodon.revised.trackscheme.display.style.dummygraph.DummyVertex;
import org.mastodon.revised.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.revised.trackscheme.wrap.ModelGraphProperties;
import org.mastodon.util.Listeners;

public class TrackSchemeStyleSettingsPage
{
	static class TrackSchemeProfileEditPanel extends JPanel implements TrackSchemeStyle.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< TrackSchemeProfile >
	{
		private static final long serialVersionUID = 1L;

		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final TrackSchemeStyle editedStyle;

		public TrackSchemeProfileEditPanel()
		{
			final DummyGraph.Examples ex = DummyGraph.Examples.CELEGANS;
			final DummyGraph example = ex.getGraph();
			final GraphIdBimap< DummyVertex, DummyEdge > idmap = example.getIdBimap();
			final ModelGraphProperties< DummyVertex, DummyEdge > dummyProps = new DefaultModelGraphProperties<>();
			final TrackSchemeGraph< DummyVertex, DummyEdge > graph = new TrackSchemeGraph<>( example, idmap, dummyProps );
			final RefBimap< DummyVertex, TrackSchemeVertex > vertexMap = new TrackSchemeVertexBimap<>( graph );
			final RefBimap< DummyEdge, TrackSchemeEdge > edgeMap = new TrackSchemeEdgeBimap<>( graph );
			final HighlightModel< TrackSchemeVertex, TrackSchemeEdge > highlight = new HighlightModelAdapter<>( new DefaultHighlightModel<>( idmap ), vertexMap, edgeMap );
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus = new FocusModelAdapter<>( new DefaultFocusModel<>( idmap ), vertexMap, edgeMap );
			final TimepointModel timepoint = new DefaultTimepointModel();
			final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection = new SelectionModelAdapter<>( ex.getSelectionModel(), vertexMap, edgeMap );
			final NavigationHandler< TrackSchemeVertex, TrackSchemeEdge > navigation = new DefaultNavigationHandler<>();

			editedStyle = TrackSchemeStyle.defaultStyle().copy( "Edited" );

			final TrackSchemeOptions options = TrackSchemeOptions.options()
					.trackSchemeOverlayFactory(
							( g, h, f, o ) -> new DefaultTrackSchemeOverlay( g, h, f, o, editedStyle )
					);
			final TrackSchemePanel panelPreview = new TrackSchemePanel( graph, highlight, focus, timepoint, selection, navigation, options );
			editedStyle.addUpdateListener( panelPreview::graphChanged );

			panelPreview.setTimepointRange( 0, 7 );
			timepoint.setTimepoint( 2 );
			panelPreview.graphChanged();

			setLayout( new BorderLayout() );
			add( panelPreview, BorderLayout.CENTER );
			add( new TrackSchemeStyleEditorPanel( editedStyle ), BorderLayout.SOUTH );

			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.addUpdateListener( this );
		}

		private boolean trackModifications = true;

		@Override
		public void trackSchemeStyleChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final TrackSchemeProfile profile )
		{
			trackModifications = false;
			editedStyle.set( profile.style );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final TrackSchemeProfile profile )
		{
			trackModifications = false;
			editedStyle.name( profile.style.getName() );
			trackModifications = true;
			profile.style.set( editedStyle );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public JPanel getJPanel()
		{
			return this;
		}
	}

	static class TrackSchemeProfile implements Profile
	{
		TrackSchemeStyle style;

		boolean isBuiltin;

		public TrackSchemeProfile( final TrackSchemeStyle style, final boolean isBuiltin )
		{
			this.style = style;
			this.isBuiltin = isBuiltin;
		}

		@Override
		public boolean isBuiltin()
		{
			return isBuiltin;
		}

		@Override
		public String getName()
		{
			return style.getName();
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;

			final TrackSchemeProfile that = ( TrackSchemeProfile ) o;
			return isBuiltin == that.isBuiltin && style.getName().equals( that.style.getName() );
		}

		@Override
		public int hashCode()
		{
			int result = style.hashCode();
			result = 31 * result + ( isBuiltin ? 1 : 0 );
			return result;
		}
	}

	static class TrackSchemeStyleProfileManager implements ProfileManager< TrackSchemeProfile >
	{
		private final TrackSchemeStyleManager styleManager;

		public TrackSchemeStyleProfileManager( final TrackSchemeStyleManager styleManager )
		{
			this.styleManager = styleManager;
		}

		@Override
		public List< TrackSchemeProfile > getProfiles()
		{
			return Stream.concat(
					styleManager.getBuiltinStyles().stream().map( style -> new TrackSchemeProfile( style, true ) ),
					styleManager.getUserStyles().stream().map( style -> new TrackSchemeProfile( style, false ) )
			).collect( Collectors.toList() );
		}

		@Override
		public TrackSchemeProfile getSelectedProfile()
		{
			final TrackSchemeStyle style = styleManager.getDefaultStyle();
			final boolean isBuiltin = styleManager.getBuiltinStyles().stream().anyMatch( s -> s.getName().equals( style.getName() ) );
			return new TrackSchemeProfile( style, isBuiltin );
		}

		@Override
		public void select( final TrackSchemeProfile profile )
		{
			styleManager.setDefaultStyle( profile.style );
			System.out.println( "TrackSchemeStyleProfileManager.select" );
			System.out.println( styleManager.getDefaultStyle() );
		}

		@Override
		public TrackSchemeProfile duplicate( final TrackSchemeProfile profile )
		{
			final TrackSchemeStyle duplicate = styleManager.duplicate( profile.style );
			return new TrackSchemeProfile( duplicate, false );
		}

		@Override
		public void rename( final TrackSchemeProfile profile, final String newName )
		{
			styleManager.rename( profile.style, newName );
		}

		@Override
		public void delete( final TrackSchemeProfile profile )
		{
			final boolean wasSelected = getSelectedProfile().equals( profile );
			int newSelectedIndex = -1;
			if ( wasSelected )
			{
				final List< TrackSchemeProfile > profiles = getProfiles();
				newSelectedIndex = Math.max( 0, profiles.indexOf( profile ) - 1 );
			}
			styleManager.remove( profile.style );
			if ( wasSelected )
			{
				select( getProfiles().get( newSelectedIndex ) );
			}
		}

		@Override
		public void apply()
		{
			System.out.println( "TrackSchemeStyleProfileManager.apply" );
			System.out.println( "APPLY TODO");
		}

		@Override
		public void cancel()
		{
			System.out.println( "TrackSchemeStyleProfileManager.cancel" );
			System.out.println( "CANCEL TODO");
		}
	}

	public static void main( final String[] args )
	{
		final TrackSchemeStyleProfileManager profileManager = new TrackSchemeStyleProfileManager( new TrackSchemeStyleManager() );
		final TrackSchemeProfileEditPanel contentPanel = new TrackSchemeProfileEditPanel();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new SelectAndEditProfileSettingsPage<>( "Style > TrackScheme", profileManager, contentPanel ) );

		final JDialog dialog = new JDialog( ( Frame ) null, "Settings" );
		dialog.getContentPane().add( settings, BorderLayout.CENTER );
		dialog.pack();
		dialog.setVisible( true );
	}
}
