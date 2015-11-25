package net.trackmate.revised.trackscheme;

import gnu.trove.set.TIntSet;

public interface ModelNavigationProperties
{
	public void notifyListeners( final TIntSet fromGroups, final int modelVertexId );

	public void forwardNavigationEventsTo( final ModelNavigationListener listener );
}
