package org.mastodon.app.factory;

import java.util.Map;

import org.mastodon.app.AbstractMastodonViewFactory;
import org.mastodon.app.AppModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.views.trackscheme.MamutViewTrackScheme2;
import org.mastodon.model.MastodonModel;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.display.TrackSchemePanel;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;

import com.google.common.reflect.TypeToken;

/**
 * Base class for view factories that create TrackScheme views. This abstract
 * class is specific to a view (TrackScheme) and to a model type.
 * <p>
 * The GUI state is specified as a map of strings to objects. The accepted key
 * and value types are:
 * <ul>
 * <li><code>'FramePosition'</code> &rarr; an <code>int[]</code> array of 4
 * elements: x, y, width and height.
 * <li><code>'LockGroupId'</code> &rarr; an integer that specifies the lock
 * group id.
 * <li><code>'SettingsPanelVisible'</code> &rarr; a boolean that specifies
 * whether the settings panel is visible on this view.
 * <li><code>'TrackSchemeTransform'</code> &rarr; a {@link ScreenTransform} that
 * defines the starting view zone in TrackScheme.
 * <li><code>'NoColoring'</code> &rarr; a boolean; if <code>true</code>, the
 * feature or tag coloring will be ignored.
 * <li><code>'TagSet'</code> &rarr; a string specifying the name of the tag-set
 * to use for coloring. If not <code>null</code>, the coloring will be done
 * using the tag-set.
 * <li><code>'FeatureColorMode'</code> &rarr; a @link String specifying the name
 * of the feature color mode to use for coloring. If not <code>null</code>, the
 * coloring will be done using the feature color mode.
 * <li><code>'ColorbarVisible'</code> &rarr; a boolean specifying whether the
 * colorbar is visible for tag-set and feature-based coloring.
 * <li><code>'ColorbarPosition'</code> &rarr; a {@link Position} specifying the
 * position of the colorbar.
 * </ul>
 *
 * @author Jean-Yves Tinevez
 *
 * @param <M>
 *            the type of app model used in the application.
 * @param <G>
 *            the type of graph used in the model.
 * @param <V>
 *            the type of vertex in the graph.
 * @param <E>
 *            the type of edge in the graph.
 */
public abstract class MastodonViewTrackSchemeFactory<
		M extends MastodonModel< G, V, E >,
		G extends ListenableReadOnlyGraph< V, E >,
		V extends Vertex< E >,
		E extends Edge< V > >
		extends AbstractMastodonViewFactory< MamutViewTrackScheme2< M, G, V, E > >
{

	/**
	 * Returns a {@link ModelGraphProperties} for the specific graph type this
	 * TrackScheme view should be created.
	 *
	 * @param graph
	 *            the graph.
	 * @return the model graph properties.
	 */
	protected abstract ModelGraphProperties< V, E > getModelGraphProperties( G graph );

	@Override
	public MamutViewTrackScheme2< M, G, V, E > create( final AppModel< ?, ?, ?, ?, MamutViewTrackScheme2< M, G, V, E >, ? > appModel )
	{
		@SuppressWarnings( "unchecked" )
		final ModelGraphProperties< V, E > modelGraphProperties = getModelGraphProperties( ( G ) appModel.dataModel().getGraph() );
		@SuppressWarnings( "unchecked" )
		final MamutViewTrackScheme2< M, G, V, E > view = new MamutViewTrackScheme2<>( ( AppModel< M, G, V, E, ?, ? > ) appModel, modelGraphProperties );
		return view;
	}

	@Override
	public String getCommandName()
	{
		return NEW_TRACKSCHEME_VIEW;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new TrackScheme view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New TrackScheme";
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Class< MamutViewTrackScheme2< M, G, V, E > > getViewClass()
	{
		// We use Guava type token to capture the generic parameters.
		final TypeToken< MamutViewTrackScheme2< M, G, V, E > > typeToken = new TypeToken< MamutViewTrackScheme2< M, G, V, E > >()
		{};
		return ( Class< MamutViewTrackScheme2< M, G, V, E > > ) typeToken.getRawType();
	}

	@Override
	public Map< String, Object > getGuiState( final MamutViewTrackScheme2< M, G, V, E > view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		storeTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutViewTrackScheme2< M, G, V, E > view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		restoreTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
	}

	static void storeTrackSchemeTransform( final TrackSchemePanel trackschemePanel, final Map< String, Object > guiState )
	{
		// Transform.
		final ScreenTransform t = trackschemePanel.getScreenTransform().get();
		guiState.put( TRACKSCHEME_TRANSFORM_KEY, t );
	}

	static void restoreTrackSchemeTransform( final TrackSchemePanel trackSchemePanel, final Map< String, Object > guiState )
	{
		// Transform.
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( TRACKSCHEME_TRANSFORM_KEY );
		if ( null != tLoaded )
			trackSchemePanel.getScreenTransform().set( tLoaded );
	}

	public static final String NEW_TRACKSCHEME_VIEW = "new trackscheme view";

	/**
	 * Key for the transform in a TrackScheme view. Value is a
	 * {@link ScreenTransform} instance.
	 */
	public static final String TRACKSCHEME_TRANSFORM_KEY = "TrackSchemeTransform";

}
