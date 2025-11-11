package org.mastodon.views.grapher;

import java.util.Map;

import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.model.app.AppModel;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.views.AbstractMastodonViewFactory;
import org.mastodon.views.MastodonViewFactory;
import org.mastodon.views.grapher.display.DataDisplayFrame;
import org.mastodon.views.trackscheme.graph.ScreenTransform;

/**
 * Base class for view factories that create Grapher views. This abstract class
 * is specific to a view (Grapher) and to a model type.
 * <p>
 * The factory has still a generic type for the view it creates, that must
 * extends {@link MastodonViewGrapher}. This is required to have app-specific
 * factories, discoverable separately.
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
 */
public abstract class AbstractMastodonViewGrapherFactory<
		T extends MastodonViewGrapher< ?, ?, ?, ? >,
		G extends ListenableReadOnlyGraph< ?, ? >,
		AM extends AppModel< AM, ?, G, ?, ? > >
		extends AbstractMastodonViewFactory< T, AM >
		implements MastodonViewFactory< T, AM >
{

	@Override
	public String getCommandDescription()
	{
		return "Open a new Grapher view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New Grapher";
	}

	@Override
	public Map< String, Object > getGuiState( final T view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		final DataDisplayFrame< ?, ? > frame = ( DataDisplayFrame< ?, ? > ) view.getFrame();
		GrapherGuiState.writeGuiState(
				frame.getDataDisplayPanel().getScreenTransform(),
				frame.getVertexSidePanel().getGraphConfig(),
				guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final T view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		final DataDisplayFrame< ?, ? > frame = ( DataDisplayFrame< ?, ? > ) view.getFrame();
		GrapherGuiState.loadGuiState(
				guiState,
				frame.getDataDisplayPanel().getScreenTransform(),
				frame.getVertexSidePanel(),
				frame );
	}
}
