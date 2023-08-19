package org.mastodon.mamut.views.bdv;

import java.util.Map;

import org.jdom2.Element;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.plugin.Plugin;

import bdv.tools.InitializeViewerState;
import bdv.viewer.ViewerPanel;
import bdv.viewer.ViewerState;
import net.imglib2.realtransform.AffineTransform3D;

@Plugin( type = MamutViewFactory.class )
public class MamutViewBdvFactory extends AbstractMamutViewFactory< MamutViewBdv >
{

	/**
	 * Key for the {@link ViewerState} in a BDV view. Value is a XML
	 * {@link Element} serialized from the state.
	 *
	 * @see ViewerPanelMamut#stateToXml()
	 * @see ViewerPanelMamut#stateFromXml(Element)
	 */
	public static final String BDV_STATE_KEY = "BdvState";

	/**
	 * Key for the transform in a BDV view. Value is an
	 * {@link AffineTransform3D} instance.
	 */
	public static final String BDV_TRANSFORM_KEY = "BdvTransform";

	@Override
	public MamutViewBdv create( final ProjectModel projectModel )
	{
		return new MamutViewBdv( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutViewBdv view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		getBdvGuiState( view.getViewerPanelMamut(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutViewBdv view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		restoreBdvGuiState( view.getViewerPanelMamut(), guiState );
	}

	static void getBdvGuiState( final ViewerPanel viewerPanel, final Map< String, Object > guiState )
	{
		// Viewer state.
		final Element stateEl = viewerPanel.stateToXml();
		guiState.put( BDV_STATE_KEY, stateEl );
		// Transform.
		final AffineTransform3D t = new AffineTransform3D();
		viewerPanel.state().getViewerTransform( t );
		guiState.put( BDV_TRANSFORM_KEY, t );
	}

	static void restoreBdvGuiState( final ViewerPanel viewerPanel, final Map< String, Object > guiState )
	{
		// Restore transform.
		final AffineTransform3D tLoaded = ( AffineTransform3D ) guiState.get( BDV_TRANSFORM_KEY );
		if ( null == tLoaded )
			InitializeViewerState.initTransform( viewerPanel );
		else
			viewerPanel.state().setViewerTransform( tLoaded );
		// Restore BDV state.
		final Element stateEl = ( Element ) guiState.get( BDV_STATE_KEY );
		if ( null != stateEl )
			viewerPanel.stateFromXml( stateEl );
	}
}
