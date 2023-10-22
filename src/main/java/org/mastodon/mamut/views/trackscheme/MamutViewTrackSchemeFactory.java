package org.mastodon.mamut.views.trackscheme;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.display.TrackSchemePanel;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * 
 * Factory to create and display a TrackScheme views.
 * <p>
 * The GUI state is specified as a map of strings to objects. The accepted key
 * and value types are::
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
 */
@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 1 )
public class MamutViewTrackSchemeFactory extends AbstractMamutViewFactory< MamutViewTrackScheme >
{

	public static final String NEW_TRACKSCHEME_VIEW = "new trackscheme view";

	/**
	 * Key for the transform in a TrackScheme view. Value is a
	 * {@link ScreenTransform} instance.
	 */
	public static final String TRACKSCHEME_TRANSFORM_KEY = "TrackSchemeTransform";

	@Override
	public MamutViewTrackScheme create( final ProjectModel projectModel )
	{
		return new MamutViewTrackScheme( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutViewTrackScheme view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		storeTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutViewTrackScheme view, final Map< String, Object > guiState )
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

	@Override
	public Class< MamutViewTrackScheme > getViewClass()
	{
		return MamutViewTrackScheme.class;
	}
}
