package org.mastodon.revised.bdv;

import bdv.cache.CacheControl;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import java.util.List;

public class ViewerPanelMamut extends ViewerPanel
{
	private final BehaviourTransformEventHandler3DMaMuT transformEventHandler;

	public ViewerPanelMamut( final List< SourceAndConverter< ? > > sources, final int numTimepoints, final CacheControl cacheControl, final ViewerOptions optional )
	{
		super( sources, numTimepoints, cacheControl, optional.transformEventHandlerFactory( BehaviourTransformEventHandler3DMaMuT::new ) );
		transformEventHandler = ( BehaviourTransformEventHandler3DMaMuT ) display.getTransformEventHandler();
	}

	public BehaviourTransformEventHandler3DMaMuT getTransformEventHandler()
	{
		return transformEventHandler;
	}
}
