package org.mastodon.mamut.views.trackscheme;

import java.util.Map;

import org.mastodon.mamut.MamutViewTrackScheme;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.display.TrackSchemePanel;

public class MamutViewTrackSchemeFactory extends AbstractMamutViewFactory< MamutViewTrackScheme >
{

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
}
