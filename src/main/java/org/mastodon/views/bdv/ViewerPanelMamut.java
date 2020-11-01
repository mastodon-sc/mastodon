package org.mastodon.views.bdv;

import java.util.List;

import bdv.cache.CacheControl;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;

public class ViewerPanelMamut extends ViewerPanel
{
	private static final long serialVersionUID = 1L;

	public ViewerPanelMamut( final List< SourceAndConverter< ? > > sources, final int numTimepoints, final CacheControl cacheControl, final ViewerOptions optional )
	{
		super( sources, numTimepoints, cacheControl, optional );
	}

	@Override
	public synchronized void align( final AlignPlane plane )
	{
		super.align( plane );
	}
}
