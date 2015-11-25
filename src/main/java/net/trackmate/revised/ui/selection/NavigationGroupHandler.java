package net.trackmate.revised.ui.selection;

import gnu.trove.set.TIntSet;

public interface NavigationGroupHandler
{
	public void setGroupActive( int lockId, boolean activate );

	public TIntSet getGroups();

	public boolean isInGroup( int group );
}
