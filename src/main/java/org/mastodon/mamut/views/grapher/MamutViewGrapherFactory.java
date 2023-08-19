package org.mastodon.mamut.views.grapher;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class )
public class MamutViewGrapherFactory extends AbstractMamutViewFactory< MamutViewGrapher >
{

	/**
	 * Key for the transform in a Grapher view. Value is a Grapher
	 * ScreenTransform instance.
	 */
	public static final String GRAPHER_TRANSFORM_KEY = "GrapherTransform";

	@Override
	public MamutViewGrapher create( final ProjectModel projectModel )
	{
		return new MamutViewGrapher( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutViewGrapher view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		return guiState;
	}


	@Override
	public void restoreGuiState( final MamutViewGrapher view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );

		// Transform.
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( GRAPHER_TRANSFORM_KEY );
		if ( null != tLoaded )
			view.getDataDisplayPanel().getScreenTransform().set( tLoaded );
	}
}
